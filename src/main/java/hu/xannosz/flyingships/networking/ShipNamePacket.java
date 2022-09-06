package hu.xannosz.flyingships.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class ShipNamePacket {
	protected final String shipName;
	protected final BlockPos position;

	public ShipNamePacket(String shipName, BlockPos position) {
		this.shipName = shipName;
		this.position = position;
	}

	public ShipNamePacket(FriendlyByteBuf buf) {
		shipName = buf.readUtf();
		position = buf.readBlockPos();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeUtf(shipName);
		buf.writeBlockPos(position);
	}

	public abstract void handler(Supplier<NetworkEvent.Context> supplier);
}
