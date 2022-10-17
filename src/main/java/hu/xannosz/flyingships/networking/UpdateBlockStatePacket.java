package hu.xannosz.flyingships.networking;

import hu.xannosz.flyingships.Util;
import hu.xannosz.flyingships.block.ModBlocks;
import hu.xannosz.flyingships.block.Rudder;
import hu.xannosz.flyingships.block.Rune;
import hu.xannosz.flyingships.block.SubRudder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateBlockStatePacket {
	private final BlockPos position;

	public UpdateBlockStatePacket(BlockPos position) {
		this.position = position;
	}

	public UpdateBlockStatePacket(FriendlyByteBuf buf) {
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
			BlockState blockState = context.getSender().level.getBlockState(position);
			if (blockState.getBlock().equals(ModBlocks.RUDDER.get())) {
				int currentState = blockState.getValue(Rudder.TYPE);
				currentState++;
				currentState %= Util.RUDDER_TYPES + 1;
				context.getSender().level.setBlock(position, blockState.setValue(Rudder.TYPE, currentState), 3);
			} else if (blockState.getBlock().equals(ModBlocks.SUB_RUDDER.get())) {
				int currentState = blockState.getValue(SubRudder.TYPE);
				currentState++;
				currentState %= Util.RUDDER_TYPES + 1;
				context.getSender().level.setBlock(position, blockState.setValue(SubRudder.TYPE, currentState), 3);
			} else if (blockState.getBlock().equals(ModBlocks.RUNE.get())) {
				int currentState = blockState.getValue(Rune.TYPE);
				currentState++;
				currentState %= Util.RUNE_TYPES + 1;
				context.getSender().level.setBlock(position, blockState.setValue(Rune.TYPE, currentState), 3);
			}
		});
	}
}
