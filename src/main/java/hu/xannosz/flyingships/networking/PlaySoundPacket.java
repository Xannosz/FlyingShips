package hu.xannosz.flyingships.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

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
		context.enqueueWork(() -> {
			// CLIENT SITE
			if (inTheShip) {
				Objects.requireNonNull(Minecraft.getInstance().level).playSound(Minecraft.getInstance().player
						, position, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 0.5f, 2f);
			} else {
				Objects.requireNonNull(Minecraft.getInstance().level).playLocalSound(
						(double) position.getX() + 0.5D, (double) position.getY() + 0.5D, (double) position.getZ() + 0.5D,
						SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 2F, 2F, false);
			}
		});
	}
}
