package hu.xannosz.flyingships.networking;

import hu.xannosz.flyingships.block.Marker;
import hu.xannosz.flyingships.block.Rudder;
import hu.xannosz.flyingships.blockentity.RudderBlockEntity;
import hu.xannosz.flyingships.warp.SavedCoordinate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class SendNewCoordinatePacket {

	private final int mode;
	private final BlockPos position;
	private final String name;
	private final String marker;

	public SendNewCoordinatePacket(int mode, BlockPos position, SavedCoordinate coordinate) {
		this.mode = mode;
		this.position = position;
		if (coordinate != null) {
			this.name = coordinate.getName();
			this.marker = coordinate.getMarker();
		} else {
			this.name = "";
			this.marker = "";
		}
	}

	public SendNewCoordinatePacket(FriendlyByteBuf buf) {
		mode = buf.readInt();
		position = buf.readBlockPos();
		name = buf.readUtf();
		marker = buf.readUtf();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(mode);
		buf.writeBlockPos(position);
		buf.writeUtf(name);
		buf.writeUtf(marker);
	}

	public void handler(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			// SERVER SITE
			if (context.getSender() == null) {
				return;
			}
			BlockEntity entity = context.getSender().getLevel().getBlockEntity(position);
			if (entity instanceof RudderBlockEntity) {
				if (name.equals("")) {
					((RudderBlockEntity) entity).removeCoordinate(mode);
				} else {
					SavedCoordinate savedCoordinate = new SavedCoordinate();
					savedCoordinate.setName(name);
					savedCoordinate.setMarker(marker);
					if (marker.isEmpty()) {
						savedCoordinate.setCoordinate(position);
						savedCoordinate.setMarkerDirection(Direction.NORTH);
					} else {
						Map<String, BlockPos> markers = ((RudderBlockEntity) entity).getMarkersInRange();
						if (!markers.containsKey(marker)) {
							return;
						}
						savedCoordinate.setCoordinate(position.subtract(markers.get(marker)));
						savedCoordinate.setMarkerDirection(context.getSender().getLevel()
								.getBlockState(markers.get(marker)).getValue(Marker.FACING));
					}
					savedCoordinate.setRudderDirection(entity.getBlockState().getValue(Rudder.FACING));
					((RudderBlockEntity) entity).addCoordinate(mode, savedCoordinate);
				}
			}
		});
	}
}
