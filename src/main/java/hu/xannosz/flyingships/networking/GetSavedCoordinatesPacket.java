package hu.xannosz.flyingships.networking;

import hu.xannosz.flyingships.blockentity.RudderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class GetSavedCoordinatesPacket {
	private final BlockPos position;

	public GetSavedCoordinatesPacket(BlockPos position) {
		this.position = position;
	}

	public GetSavedCoordinatesPacket(FriendlyByteBuf buf) {
		position = buf.readBlockPos();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBlockPos(position);
	}

	public void handler(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			// SERVER SITE
			System.out.println("###"+"handle get message"); //TODO
			BlockEntity entity = Objects.requireNonNull(context.getSender()).getLevel().getBlockEntity(position);
			if (entity instanceof RudderBlockEntity) {
				ModMessages.sendToPlayer(
						new SendSavedCoordinatesPacket(position, ((RudderBlockEntity) entity).getCoordinates()),
						context.getSender());
			}
		});
	}
}
