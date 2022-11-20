package hu.xannosz.flyingships.networking;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@Getter
public class PlaySoundPacket {

	private final BlockPos position;
	private final boolean inTheShip;

	public PlaySoundPacket(BlockPos position, boolean inTheShip) {
		this.position = position;
		this.inTheShip = inTheShip;
	}

	public PlaySoundPacket(FriendlyByteBuf buf) {
		position = buf.readBlockPos();
		inTheShip = buf.readBoolean();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBlockPos(position);
		buf.writeBoolean(inTheShip);
	}

	public void handler(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() ->
				// CLIENT SITE
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
						ClientPacketHandlerClass.handlePlaySoundPacket(this, supplier))
		);
	}
}
