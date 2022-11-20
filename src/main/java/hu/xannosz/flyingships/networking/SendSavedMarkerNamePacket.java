package hu.xannosz.flyingships.networking;

import hu.xannosz.flyingships.blockentity.MarkerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class SendSavedMarkerNamePacket {
	private final BlockPos position;
	private final String markerName;
	private final boolean isEnabled;

	public SendSavedMarkerNamePacket(BlockPos position, String markerName, boolean isEnabled) {
		this.position = position;
		this.markerName = markerName;
		this.isEnabled = isEnabled;
	}

	public SendSavedMarkerNamePacket(FriendlyByteBuf buf) {
		position = buf.readBlockPos();
		markerName = buf.readUtf();
		isEnabled = buf.readBoolean();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBlockPos(position);
		buf.writeUtf(markerName);
		buf.writeBoolean(isEnabled);
	}

	public void handler(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			// CLIENT SITE
			BlockEntity entity = Objects.requireNonNull(Minecraft.getInstance().level).getBlockEntity(position);
			if (entity instanceof MarkerBlockEntity markerBlockEntity) {
				markerBlockEntity.setMarkerName(markerName);
				markerBlockEntity.setEnabled(isEnabled);
			}
		});
	}
}
