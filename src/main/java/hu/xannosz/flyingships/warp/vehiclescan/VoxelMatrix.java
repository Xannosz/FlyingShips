package hu.xannosz.flyingships.warp.vehiclescan;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashSet;
import java.util.Set;

@Getter
public class VoxelMatrix {
	private final Set<VoxelColumn> xColumns = new HashSet<>();
	private final Set<VoxelColumn> yColumns = new HashSet<>();
	private final Set<VoxelColumn> zColumns = new HashSet<>();

	public VoxelMatrix(ServerLevel level, Set<BlockPos> blockPosSet) {
		for (BlockPos blockPos : blockPosSet) {
			Block block = level.getBlockState(blockPos).getBlock();

			if (block.equals(Blocks.AIR)) {
				continue;
			}

			boolean xAccept = false;
			for (VoxelColumn xColumn : xColumns) {
				if (xColumn.add(blockPos, block)) {
					xAccept = true;
					break;
				}
			}
			if (!xAccept) {
				xColumns.add(new VoxelColumn(blockPos, block, ColumnType.X));
			}

			boolean yAccept = false;
			for (VoxelColumn yColumn : yColumns) {
				if (yColumn.add(blockPos, block)) {
					yAccept = true;
					break;
				}
			}
			if (!yAccept) {
				yColumns.add(new VoxelColumn(blockPos, block, ColumnType.Y));
			}

			boolean zAccept = false;
			for (VoxelColumn zColumn : zColumns) {
				if (zColumn.add(blockPos, block)) {
					zAccept = true;
					break;
				}
			}
			if (!zAccept) {
				zColumns.add(new VoxelColumn(blockPos, block, ColumnType.Z));
			}
		}
	}
}
