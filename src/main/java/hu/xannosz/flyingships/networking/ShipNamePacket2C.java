package hu.xannosz.flyingships.networking;

import hu.xannosz.flyingships.blockentity.RudderBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ShipNamePacket2C extends ShipNamePacket {

	public ShipNamePacket2C(String shipName, BlockPos position) {
		super(shipName, position);
	}

	public ShipNamePacket2C(FriendlyByteBuf buf) {
		super(buf);
	}

	public void handler(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			BlockEntity entity = Objects.requireNonNull(Minecraft.getInstance().level).getBlockEntity(position);
			if (entity instanceof RudderBlockEntity) {
				((RudderBlockEntity) entity).setShipName(shipName);
				entity.setChanged();
			}
		});
	}
}
