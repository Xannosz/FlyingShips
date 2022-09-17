package hu.xannosz.flyingships.networking;

import hu.xannosz.flyingships.blockentity.MarkerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class SendSavedMarkerNamePacket {
	private final BlockPos position;
	private final String markerName;

	public SendSavedMarkerNamePacket(BlockPos position, String markerName) {
		this.position = position;
		this.markerName = markerName;
	}

	public SendSavedMarkerNamePacket(FriendlyByteBuf buf) {
		position = buf.readBlockPos();
		markerName = buf.readUtf();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBlockPos(position);
		buf.writeUtf(markerName);
	}

	public void handler(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			// CLIENT SITE
			BlockEntity entity = Objects.requireNonNull(Minecraft.getInstance().level).getBlockEntity(position);
			if (entity instanceof MarkerBlockEntity) {
				((MarkerBlockEntity) entity).setMarkerName(markerName);
			}
		});
	}
}
