package hu.xannosz.flyingships.networking;

import hu.xannosz.flyingships.blockentity.ItemGateBlockEntity;
import hu.xannosz.flyingships.blockentity.SubRudderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ConnectToRudder {
	private final BlockPos connectorPosition;
	private final BlockPos rudderPosition;

	public ConnectToRudder(BlockPos connectorPosition, BlockPos rudderPosition) {
		this.connectorPosition = connectorPosition;
		this.rudderPosition = rudderPosition;
	}

	public ConnectToRudder(FriendlyByteBuf buf) {
		connectorPosition = buf.readBlockPos();
		rudderPosition = buf.readBlockPos();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBlockPos(connectorPosition);
		buf.writeBlockPos(rudderPosition);
	}

	public void handler(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			// SERVER SITE
			BlockEntity entity = Objects.requireNonNull(context.getSender()).getLevel().getBlockEntity(connectorPosition);
			if (entity instanceof SubRudderBlockEntity) {
				((SubRudderBlockEntity) entity).setRudderPosition(rudderPosition.subtract(connectorPosition));
			}
			if (entity instanceof ItemGateBlockEntity) {
				((ItemGateBlockEntity) entity).setRudderPosition(rudderPosition.subtract(connectorPosition));
			}
		});
	}
}
