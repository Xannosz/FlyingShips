package hu.xannosz.flyingships.warp.vehiclescan;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

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
			final BlockState blockState = level.getBlockState(blockPos);
			final Block block = blockState.getBlock();

			if (block.equals(Blocks.AIR)) {
				if (blockPos.getY() <= absoluteFluidLine) {
					blockNumUnderWater++;
				}
				continue;
			}

			boolean xAccept = false;
			for (VoxelColumn xColumn : xColumns) {
				if (xColumn.add(blockPos, blockState)) {
					xAccept = true;
					break;
				}
			}
			if (!xAccept) {
				xColumns.add(new VoxelColumn(blockPos, blockState, ColumnType.X, absoluteFluidLine, isCommonFluid));
			}

			boolean yAccept = false;
			for (VoxelColumn yColumn : yColumns) {
				if (yColumn.add(blockPos, blockState)) {
					yAccept = true;
					break;
				}
			}
			if (!yAccept) {
				yColumns.add(new VoxelColumn(blockPos, blockState, ColumnType.Y, absoluteFluidLine, isCommonFluid));
			}

			boolean zAccept = false;
			for (VoxelColumn zColumn : zColumns) {
				if (zColumn.add(blockPos, blockState)) {
					zAccept = true;
					break;
				}
			}
			if (!zAccept) {
				zColumns.add(new VoxelColumn(blockPos, blockState, ColumnType.Z, absoluteFluidLine, isCommonFluid));
			}
		}
	}
}
