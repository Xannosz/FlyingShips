package hu.xannosz.flyingships.networking;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@Getter
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
		context.enqueueWork(() ->
				// CLIENT SITE
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
						ClientPacketHandlerClass.handleSendSavedMarkerNamePacket(this, supplier))
		);
	}
}
