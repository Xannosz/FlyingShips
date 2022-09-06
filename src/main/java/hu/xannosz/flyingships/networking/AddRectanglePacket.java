package hu.xannosz.flyingships.networking;

import hu.xannosz.flyingships.blockentity.RudderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class AddRectanglePacket {

	private final BlockPos rudderPos;
	private final int mode;
	private final BlockPos blockPos1;
	private final BlockPos blockPos2;

	public AddRectanglePacket(BlockPos rudderPos, int mode, BlockPos blockPos1, BlockPos blockPos2) {
		this.rudderPos = rudderPos;
		this.mode = mode;
		this.blockPos1 = blockPos1;
		this.blockPos2 = blockPos2;
	}

	public AddRectanglePacket(FriendlyByteBuf buf) {
		this.rudderPos = buf.readBlockPos();
		this.mode = buf.readInt();
		this.blockPos1 = buf.readBlockPos();
		this.blockPos2 = buf.readBlockPos();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBlockPos(rudderPos);
		buf.writeInt(mode);
		buf.writeBlockPos(blockPos1);
		buf.writeBlockPos(blockPos2);
	}

	public void handler(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			// SERVER SITE
			BlockEntity entity = Objects.requireNonNull(context.getSender()).getLevel().getBlockEntity(rudderPos);
			if (entity instanceof RudderBlockEntity) {
				((RudderBlockEntity) entity).addRectangle(mode, blockPos1, blockPos2);
			}
		});
	}
}
