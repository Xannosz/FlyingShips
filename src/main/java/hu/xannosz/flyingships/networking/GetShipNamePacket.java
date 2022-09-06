package hu.xannosz.flyingships.networking;

import hu.xannosz.flyingships.blockentity.RudderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class GetShipNamePacket {
	private final BlockPos position;

	public GetShipNamePacket(BlockPos position) {
		this.position = position;
	}

	public GetShipNamePacket(FriendlyByteBuf buf) {
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
			if (entity instanceof RudderBlockEntity) {
				String shipName = ((RudderBlockEntity) entity).getShipName();
				ModMessages.sendToPlayer(new ShipNamePacket2C(shipName, position), context.getSender());
			}
		});
	}
}
