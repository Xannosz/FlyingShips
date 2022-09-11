package hu.xannosz.flyingships.networking;

import hu.xannosz.flyingships.blockentity.RudderBlockEntity;
import hu.xannosz.flyingships.warp.SavedCoordinate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class SendSavedCoordinatesPacket {
	private final BlockPos position;
	private final int count;
	private final List<SavedCoordinate> savedCoordinates;

	public SendSavedCoordinatesPacket(BlockPos position, List<SavedCoordinate> savedCoordinates) {
		this.position = position;
		count = savedCoordinates.size();
		this.savedCoordinates = savedCoordinates;
	}

	public SendSavedCoordinatesPacket(FriendlyByteBuf buf) {
		position = buf.readBlockPos();
		count = buf.readInt();
		savedCoordinates = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			SavedCoordinate savedCoordinate = new SavedCoordinate();
			savedCoordinate.setName(buf.readUtf());
			savedCoordinate.setMarker(buf.readUtf());
			savedCoordinate.setCoordinate(buf.readBlockPos());
			savedCoordinates.add(savedCoordinate);
		}
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBlockPos(position);
		buf.writeInt(count);
		savedCoordinates.forEach(savedCoordinate -> {
			buf.writeUtf(savedCoordinate.getName());
			buf.writeUtf(savedCoordinate.getMarker());
			buf.writeBlockPos(savedCoordinate.getCoordinate());
		});
	}

	public void handler(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			// CLIENT SITE
			BlockEntity entity = Objects.requireNonNull(Minecraft.getInstance().level).getBlockEntity(position);
			if (entity instanceof RudderBlockEntity) {
				((RudderBlockEntity) entity).setCoordinates(savedCoordinates);
			}
		});
	}
}
