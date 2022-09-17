package hu.xannosz.flyingships.networking;

import hu.xannosz.flyingships.blockentity.MarkerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class SendNewMarkerNamePacket {
	private final BlockPos position;
	private final String markerName;

	public SendNewMarkerNamePacket(BlockPos position, String markerName) {
		this.position = position;
		this.markerName = markerName;
	}

	public SendNewMarkerNamePacket(FriendlyByteBuf buf) {
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
			// SERVER SITE
			BlockEntity entity = Objects.requireNonNull(context.getSender()).getLevel().getBlockEntity(position);
			if (entity instanceof MarkerBlockEntity) {
				((MarkerBlockEntity) entity).setMarkerName(markerName);
			}
		});
	}
}
