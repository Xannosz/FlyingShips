package hu.xannosz.flyingships.warp.terrainscan;

import net.minecraft.core.BlockPos;

public class VoxelDataColumn {
	private final int x;
	private final int z;
	private int minY;
	private int maxY;

	public VoxelDataColumn(BlockPos blockPos) {
		x = blockPos.getX();
		z = blockPos.getZ();
		minY = blockPos.getY();
		maxY = blockPos.getY();
	}

	public boolean add(BlockPos blockPos) {
		if (blockPos.getX() == x && blockPos.getZ() == z) {
			if (minY > blockPos.getY()) {
				minY = blockPos.getY();
			}
			if (maxY < blockPos.getY()) {
				maxY = blockPos.getY();
			}
			return true;
		}
		return false;
	}

	public BlockPos getTop() {
		return new BlockPos(x, maxY, z);
	}

	public BlockPos getBottom() {
		return new BlockPos(x, minY, z);
	}
}
