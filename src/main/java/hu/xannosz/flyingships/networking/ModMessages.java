package hu.xannosz.flyingships.networking;

import hu.xannosz.flyingships.FlyingShips;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
	private static SimpleChannel INSTANCE;

	private static int pocketId = 0;

	private static int id() {
		return pocketId++;
	}

	public static void register() {
		INSTANCE = NetworkRegistry.ChannelBuilder
				.named(new ResourceLocation(FlyingShips.MOD_ID, "messages"))
				.networkProtocolVersion(() -> "1.0")
				.clientAcceptedVersions(s -> true)
				.serverAcceptedVersions(s -> true)
				.simpleChannel();

		INSTANCE.messageBuilder(ButtonClickedPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(ButtonClickedPacket::new)
				.encoder(ButtonClickedPacket::toBytes)
				.consumerMainThread(ButtonClickedPacket::handler)
				.add();
		INSTANCE.messageBuilder(AddRectanglePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(AddRectanglePacket::new)
				.encoder(AddRectanglePacket::toBytes)
				.consumerMainThread(AddRectanglePacket::handler)
				.add();
		INSTANCE.messageBuilder(ShipNamePacket2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(ShipNamePacket2S::new)
				.encoder(ShipNamePacket2S::toBytes)
				.consumerMainThread(ShipNamePacket2S::handler)
				.add();
		INSTANCE.messageBuilder(ShipNamePacket2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(ShipNamePacket2C::new)
				.encoder(ShipNamePacket2C::toBytes)
				.consumerMainThread(ShipNamePacket2C::handler)
				.add();
		INSTANCE.messageBuilder(GetShipNamePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(GetShipNamePacket::new)
				.encoder(GetShipNamePacket::toBytes)
				.consumerMainThread(GetShipNamePacket::handler)
				.add();
	}

	public static <MSG> void sendToServer(MSG message) {
		INSTANCE.sendToServer(message);
	}

	public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
		INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
	}
}
