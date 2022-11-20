package hu.xannosz.flyingships.networking;

import hu.xannosz.flyingships.blockentity.MarkerBlockEntity;
import hu.xannosz.flyingships.blockentity.RudderBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ClientPacketHandlerClass {
	public static void handleSendSavedMarkerNamePacket(SendSavedMarkerNamePacket msg, Supplier<NetworkEvent.Context> ctx) {
		BlockEntity entity = Objects.requireNonNull(Minecraft.getInstance().level).getBlockEntity(msg.getPosition());
		if (entity instanceof MarkerBlockEntity markerBlockEntity) {
			markerBlockEntity.setMarkerName(msg.getMarkerName());
			markerBlockEntity.setEnabled(msg.isEnabled());
		}
	}

	public static void handleSendSavedCoordinatesPacket(SendSavedCoordinatesPacket msg, Supplier<NetworkEvent.Context> ctx) {
		BlockEntity entity = Objects.requireNonNull(Minecraft.getInstance().level).getBlockEntity(msg.getPosition());
		if (entity instanceof RudderBlockEntity) {
			((RudderBlockEntity) entity).setCoordinates(msg.getSavedCoordinates());
		}
	}

	public static void handlePlaySoundPacket(PlaySoundPacket msg, Supplier<NetworkEvent.Context> ctx) {
		if (msg.isInTheShip()) {
			Objects.requireNonNull(Minecraft.getInstance().level).playSound(Minecraft.getInstance().player,
					msg.getPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.5f, 2f);
		}
		Objects.requireNonNull(Minecraft.getInstance().level).playLocalSound(
				(double) msg.getPosition().getX() + 0.5D,
				(double) msg.getPosition().getY() + 0.5D,
				(double) msg.getPosition().getZ() + 0.5D,
				SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 2F, 2F, false);
	}
}
