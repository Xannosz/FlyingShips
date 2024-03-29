package hu.xannosz.flyingships.warp.vehiclescan;

import hu.xannosz.flyingships.Util;
import hu.xannosz.flyingships.block.ModBlocks;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Getter
public class VoxelColumn {
	private final ColumnType columnType;
	private int pos1;
	private int pos2;
	private int min;
	private int max;

	private int wool = 0;
	private int heater = 0;
	private int enderOscillator = 0;
	private int tank = 0;
	private int artificialFloater = 0;
	private int blockNumUnderWater = 0;
	private boolean copyMode = false;

	private int density = 0;
	private boolean blocked = false;

	private final int absoluteFluidLine;
	private final boolean isCommonFluid;

	public VoxelColumn(BlockPos blockPos, BlockState blockState, ColumnType columnType, int absoluteFluidLine, boolean isCommonFluid) {
		this.columnType = columnType;
		this.absoluteFluidLine = absoluteFluidLine;
		this.isCommonFluid = isCommonFluid;

		calculateInterestBlock(blockState, blockPos.getY());
		switch (columnType) {
			case X -> {
				min = blockPos.getX();
				max = blockPos.getX();
				pos1 = blockPos.getY();
				pos2 = blockPos.getZ();
			}
			case Y -> {
				min = blockPos.getY();
				max = blockPos.getY();
				pos1 = blockPos.getX();
				pos2 = blockPos.getZ();
			}
			case Z -> {
				min = blockPos.getZ();
				max = blockPos.getZ();
				pos1 = blockPos.getX();
				pos2 = blockPos.getY();
			}
		}
	}

	public boolean add(BlockPos blockPos, BlockState blockState) {
		boolean canAccept = canAcceptInColumn(blockPos);
		if (canAccept) {
			calculateInterestBlock(blockState, blockPos.getY());
		}
		return canAccept;
	}

	private boolean canAcceptInColumn(BlockPos blockPos) {
		switch (columnType) {
			case X -> {
				if (blockPos.getY() == pos1 && blockPos.getZ() == pos2) {
					if (min > blockPos.getX()) {
						min = blockPos.getX();
					}
					if (max < blockPos.getX()) {
						max = blockPos.getX();
					}
					return true;
				} else {
					return false;
				}
			}
			case Y -> {
				if (blockPos.getX() == pos1 && blockPos.getZ() == pos2) {
					if (min > blockPos.getY()) {
						min = blockPos.getY();
					}
					if (max < blockPos.getY()) {
						max = blockPos.getY();
					}
					return true;
				} else {
					return false;
				}
			}
			case Z -> {
				if (blockPos.getX() == pos1 && blockPos.getY() == pos2) {
					if (min > blockPos.getZ()) {
						min = blockPos.getZ();
					}
					if (max < blockPos.getZ()) {
						max = blockPos.getZ();
					}
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	private void calculateInterestBlock(BlockState blockState, int y) {
		final Block block = blockState.getBlock();
		if (y <= absoluteFluidLine) {
			if (!(isCommonFluid && Util.isCommonFluid(block) || !isCommonFluid && Util.isLava(block))) {
				density += Util.getDensity(blockState);
				blockNumUnderWater++;
			}
		} else {
			density += Util.getDensity(blockState);
			if (isAWool(block)) {
				wool++;
			}
		}
		if (!Util.isHollow(block)) {
			blocked = true;
		}
		if (block.equals(ModBlocks.HEATER.get())) {
			heater++;
		}
		if (block.equals(ModBlocks.TANK.get())) {
			tank++;
		}
		if (block.equals(ModBlocks.ENDER_OSCILLATOR.get())) {
			enderOscillator++;
		}
		if (block.equals(ModBlocks.ARTIFICIAL_FLOATER.get())) {
			artificialFloater++;
		}
		if (block.equals(ModBlocks.ADMIN_ENGINE.get())) {
			artificialFloater += 1000;
		}
		if (block.equals(ModBlocks.COPY_BLOCK.get())) {
			copyMode = true;
		}
	}

	private static boolean isAWool(Block block) {
		return block.equals(Blocks.WHITE_WOOL) && block.equals(Blocks.ORANGE_WOOL) &&
				block.equals(Blocks.MAGENTA_WOOL) && block.equals(Blocks.LIGHT_BLUE_WOOL) &&
				block.equals(Blocks.YELLOW_WOOL) && block.equals(Blocks.LIME_WOOL) &&
				block.equals(Blocks.PINK_WOOL) && block.equals(Blocks.GRAY_WOOL) &&
				block.equals(Blocks.LIGHT_GRAY_WOOL) && block.equals(Blocks.CYAN_WOOL) &&
				block.equals(Blocks.PURPLE_WOOL) && block.equals(Blocks.BLUE_WOOL) &&
				block.equals(Blocks.BROWN_WOOL) && block.equals(Blocks.GREEN_WOOL) &&
				block.equals(Blocks.RED_WOOL) && block.equals(Blocks.BLACK_WOOL);
	}
}
