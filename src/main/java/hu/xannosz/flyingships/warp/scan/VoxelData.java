package hu.xannosz.flyingships.warp.scan;

import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class VoxelData {
	private final Set<VoxelDataColumn> columns = new HashSet<>();

	public void addBlockPos(BlockPos blockPos) {
		boolean xAccept = false;
		for (VoxelDataColumn column : columns) {
			if (column.add(blockPos)) {
				xAccept = true;
				break;
			}
		}
		if (!xAccept) {
			columns.add(new VoxelDataColumn(blockPos));
		}
	}

	public Set<BlockPos> getTopMask() {
		Set<BlockPos> positions = new HashSet<>();
		columns.forEach(column -> positions.add(column.getTop()));
		return positions;
	}

	public Set<BlockPos> getBottomMask() {
		Set<BlockPos> positions = new HashSet<>();
		columns.forEach(column -> positions.add(column.getBottom()));
		return positions;
	}
}
