package hu.xannosz.flyingships.warp;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
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

	public SavedCoordinate(@NotNull CompoundTag nbt, int id) {
		name = nbt.getString("rudder.coordinate." + id + ".name");
		marker = nbt.getString("rudder.coordinate." + id + ".marker");
		coordinate = NbtUtils.readBlockPos((CompoundTag) Objects.requireNonNull(nbt.get("rudder.coordinate." + id + ".coordinate")));
	}

	public void saveAdditional(@NotNull CompoundTag tag, int id) {
		tag.putString("rudder.coordinate." + id + ".name", name);
		tag.putString("rudder.coordinate." + id + ".marker", marker);
		tag.put("rudder.coordinate." + id + ".coordinate", NbtUtils.writeBlockPos(coordinate));
	}
}
