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

	public PlaySoundPacket(BlockPos position) {
		this.position = position;
	}

	public PlaySoundPacket(FriendlyByteBuf buf) {
		position = buf.readBlockPos();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBlockPos(position);
	}

	public void handler(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			// CLIENT SITE
			Objects.requireNonNull(Minecraft.getInstance().level).playLocalSound(
					(double) position.getX() + 0.5D, (double) position.getY() + 0.5D, (double) position.getZ() + 0.5D,
					SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 3F, 2F, false);
			Objects.requireNonNull(Minecraft.getInstance().level).playSound(Minecraft.getInstance().player
					, position, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 2f, 2f);
		});
	}
}
