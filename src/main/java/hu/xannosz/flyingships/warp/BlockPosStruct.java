package hu.xannosz.flyingships.warp;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
public class BlockPosStruct {
	private final BlockPos position1;
	private final BlockPos position2;

	public BlockPosStruct(BlockPos blockPos1, BlockPos blockPos2, BlockPos rudder) {
		int x1 = blockPos1.getX() - rudder.getX();
		int x2 = blockPos2.getX() - rudder.getX();
		int y1 = blockPos1.getY() - rudder.getY();
		int y2 = blockPos2.getY() - rudder.getY();
		int z1 = blockPos1.getZ() - rudder.getZ();
		int z2 = blockPos2.getZ() - rudder.getZ();

		position1 = new BlockPos(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2));
		position2 = new BlockPos(Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
	}

	public BlockPosStruct(BlockPos blockPos1, BlockPos blockPos2) {
		position1 = new BlockPos(Math.min(blockPos1.getX(), blockPos2.getX()), Math.min(blockPos1.getY(), blockPos2.getY()), Math.min(blockPos1.getZ(), blockPos2.getZ()));
		position2 = new BlockPos(Math.max(blockPos1.getX(), blockPos2.getX()), Math.max(blockPos1.getY(), blockPos2.getY()), Math.max(blockPos1.getZ(), blockPos2.getZ()));
	}

	public BlockPosStruct(@NotNull CompoundTag nbt, int id) {
		position1 = NbtUtils.readBlockPos((CompoundTag) Objects.requireNonNull(nbt.get("rudder.position." + id + ".1")));
		position2 = NbtUtils.readBlockPos((CompoundTag) Objects.requireNonNull(nbt.get("rudder.position." + id + ".2")));
	}

	public void saveAdditional(@NotNull CompoundTag tag, int id) {
		tag.put("rudder.position." + id + ".1", NbtUtils.writeBlockPos(position1));
		tag.put("rudder.position." + id + ".2", NbtUtils.writeBlockPos(position2));
	}
}
