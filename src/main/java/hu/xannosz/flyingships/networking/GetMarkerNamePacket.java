package hu.xannosz.flyingships.networking;

import hu.xannosz.flyingships.blockentity.MarkerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class GetMarkerNamePacket {
	private final BlockPos position;

	public GetMarkerNamePacket(BlockPos position) {
		this.position = position;
	}

	public GetMarkerNamePacket(FriendlyByteBuf buf) {
		position = buf.readBlockPos();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBlockPos(position);
	}

	public void handler(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			// SERVER SITE
			BlockEntity entity = Objects.requireNonNull(context.getSender()).getLevel().getBlockEntity(position);
			if (entity instanceof MarkerBlockEntity markerBlockEntity) {
				ModMessages.sendToPlayer(
						new SendSavedMarkerNamePacket(position,
								markerBlockEntity.getMarkerName(), markerBlockEntity.isEnabled()),
						context.getSender());
			}
		});
	}
}
