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

	private int blockNumUnderWater = 0;

	public VoxelMatrix(ServerLevel level, Set<BlockPos> blockPosSet, int absoluteFluidLine, boolean isCommonFluid) {
		for (BlockPos blockPos : blockPosSet) {
			Block block = level.getBlockState(blockPos).getBlock();

			if (block.equals(Blocks.AIR)) {
				if (blockPos.getY() < absoluteFluidLine) {
					blockNumUnderWater++;
				}
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
				xColumns.add(new VoxelColumn(blockPos, block, ColumnType.X, absoluteFluidLine, isCommonFluid));
			}

			boolean yAccept = false;
			for (VoxelColumn yColumn : yColumns) {
				if (yColumn.add(blockPos, block)) {
					yAccept = true;
					break;
				}
			}
			if (!yAccept) {
				yColumns.add(new VoxelColumn(blockPos, block, ColumnType.Y, absoluteFluidLine, isCommonFluid));
			}

			boolean zAccept = false;
			for (VoxelColumn zColumn : zColumns) {
				if (zColumn.add(blockPos, block)) {
					zAccept = true;
					break;
				}
			}
			if (!zAccept) {
				zColumns.add(new VoxelColumn(blockPos, block, ColumnType.Z, absoluteFluidLine, isCommonFluid));
			}
		}
	}
}
