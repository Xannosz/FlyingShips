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
		INSTANCE.messageBuilder(PlaySoundPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(PlaySoundPacket::new)
				.encoder(PlaySoundPacket::toBytes)
				.consumerMainThread(PlaySoundPacket::handler)
				.add();
		INSTANCE.messageBuilder(SendNewCoordinatePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(SendNewCoordinatePacket::new)
				.encoder(SendNewCoordinatePacket::toBytes)
				.consumerMainThread(SendNewCoordinatePacket::handler)
				.add();
		INSTANCE.messageBuilder(SendSavedCoordinatesPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(SendSavedCoordinatesPacket::new)
				.encoder(SendSavedCoordinatesPacket::toBytes)
				.consumerMainThread(SendSavedCoordinatesPacket::handler)
				.add();
		INSTANCE.messageBuilder(GetSavedCoordinatesPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(GetSavedCoordinatesPacket::new)
				.encoder(GetSavedCoordinatesPacket::toBytes)
				.consumerMainThread(GetSavedCoordinatesPacket::handler)
				.add();
		INSTANCE.messageBuilder(ConnectToRudder.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(ConnectToRudder::new)
				.encoder(ConnectToRudder::toBytes)
				.consumerMainThread(ConnectToRudder::handler)
				.add();
	}

	public static <MSG> void sendToServer(MSG message) {
		INSTANCE.sendToServer(message);
	}

	public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
		INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
	}
}
