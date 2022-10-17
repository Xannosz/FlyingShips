package hu.xannosz.flyingships.networking;

import hu.xannosz.flyingships.blockentity.RuneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class RuneActivationPacket {
	private final BlockPos position;

	public RuneActivationPacket(BlockPos position) {
		this.position = position;
	}

	public RuneActivationPacket(FriendlyByteBuf buf) {
		position = buf.readBlockPos();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBlockPos(position);
	}

	public void handler(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			// SERVER SITE
			if (context.getSender() == null) {
				return;
			}

			int distance = 400;
			RuneBlockEntity runeBlockEntity = null;

			final Map<BlockPos, BlockEntity> entities = context.getSender().getLevel().getChunkAt(position).getBlockEntities();
			for (Map.Entry<BlockPos, BlockEntity> entity : entities.entrySet()) {
				if (entity.getKey().getX() == position.getX() && entity.getKey().getZ() == position.getZ()
						&& entity.getValue() instanceof RuneBlockEntity) {
					if (Math.abs(entity.getKey().getY() - position.getY()) < Math.abs(distance)) {
						distance = entity.getKey().getY() - position.getY();
						runeBlockEntity = (RuneBlockEntity) entity.getValue();
					}
				}
			}

			if (runeBlockEntity == null) {
				return;
			}

			if (distance >= 0) {
				runeBlockEntity.wandSignalDown();
			} else {
				runeBlockEntity.wandSignalUp();
			}
		});
	}
}
