package hu.xannosz.flyingships.warp;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Data
@NoArgsConstructor
public class SavedCoordinate {
	private String name = "";
	private String marker = "";
	private BlockPos coordinate;
	private Direction rudderDirection;
	private Direction markerDirection = Direction.NORTH;

	public SavedCoordinate(@NotNull CompoundTag nbt, int id) {
		name = nbt.getString("rudder.coordinate." + id + ".name");
		marker = nbt.getString("rudder.coordinate." + id + ".marker");
		coordinate = NbtUtils.readBlockPos((CompoundTag) Objects.requireNonNull(nbt.get("rudder.coordinate." + id + ".coordinate")));
		rudderDirection = Direction.byName(nbt.getString("rudder.coordinate." + id + ".rudderDirection"));
		markerDirection = Direction.byName(nbt.getString("rudder.coordinate." + id + ".markerDirection"));
	}

	public void saveAdditional(@NotNull CompoundTag tag, int id) {
		tag.putString("rudder.coordinate." + id + ".name", name);
		tag.putString("rudder.coordinate." + id + ".marker", marker);
		tag.put("rudder.coordinate." + id + ".coordinate", NbtUtils.writeBlockPos(coordinate));
		tag.putString("rudder.coordinate." + id + ".rudderDirection", rudderDirection.getSerializedName());
		tag.putString("rudder.coordinate." + id + ".markerDirection", markerDirection.getSerializedName());
	}
}
