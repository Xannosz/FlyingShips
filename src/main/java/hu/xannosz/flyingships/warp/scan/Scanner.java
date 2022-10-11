package hu.xannosz.flyingships.warp.scan;

import hu.xannosz.flyingships.Util;
import hu.xannosz.flyingships.warp.AbsoluteRectangleData;
import lombok.experimental.UtilityClass;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class Scanner {
	public static final int TOP_OF_MAP = 319;
	public static final int BOTTOM_OF_MAP = -64;

	public static ScanResult scan(ServerLevel level, List<AbsoluteRectangleData> rectangleDataList, int waterLine) {
		ScanResult result = new ScanResult();
		LiveDataPackage dataPackage = generateMasks(rectangleDataList);
		scanTerrain(level, dataPackage, result);
		result.setToFluidLine(result.getAbsoluteFluidLine() - dataPackage.getBottomOfBottom() + waterLine + 1);
		calculatePosition(result, TOP_OF_MAP - dataPackage.getTopOfTop(),
				-(BOTTOM_OF_MAP - dataPackage.getBottomOfBottom()));
		return result;
	}

	private static void calculatePosition(ScanResult scanResult, int toTopOfMap, int toBottomOfMap) {
		if (scanResult.getMaxCelling() > scanResult.getToFluidLine() && scanResult.getToFluidLine() > 0) {
			switch (scanResult.getFluidType()) {
				case COMMON -> scanResult.setCellingPosition(CellingPosition.UNDER_WATER);
				case LAVA -> scanResult.setCellingPosition(CellingPosition.UNDER_LAVA);
			}
		} else if (scanResult.getMaxCelling() == toTopOfMap) {
			scanResult.setCellingPosition(CellingPosition.VOID);
		} else if (scanResult.getMaxCelling() == 0) {
			scanResult.setCellingPosition(CellingPosition.TOUCH_CELLING);
		} else {
			scanResult.setCellingPosition(CellingPosition.UNDER_FIELD);
		}

		if (scanResult.getToFluidLine() == 0) {
			switch (scanResult.getFluidType()) {
				case COMMON -> scanResult.setFloatingPosition(FloatingPosition.SWIM_WATER);
				case LAVA -> scanResult.setFloatingPosition(FloatingPosition.SWIM_LAVA);
			}
		} else {
			scanResult.setFloatingPosition(FloatingPosition.VOID);
		}

		if (scanResult.getMaxBottom() == toBottomOfMap) {
			scanResult.setBottomPosition(BottomPosition.VOID);
		} else if (scanResult.getMaxBottom() == 0) {
			scanResult.setBottomPosition(BottomPosition.LANDED);
		} else if (scanResult.getMaxBottom() >= -scanResult.getToFluidLine() && scanResult.getToFluidLine() != 0) {
			switch (scanResult.getFluidType()) {
				case COMMON -> scanResult.setBottomPosition(BottomPosition.FLY_OVER_WATER);
				case LAVA -> scanResult.setBottomPosition(BottomPosition.FLY_OVER_LAVA);
			}
		} else {
			scanResult.setBottomPosition(BottomPosition.FLY_OVER_FIELD);
		}
	}

	private static LiveDataPackage generateMasks(List<AbsoluteRectangleData> rectangleDataList) {
		LiveDataPackage dataPackage = new LiveDataPackage();
		VoxelData voxelData = new VoxelData();

		for (AbsoluteRectangleData rectangleData : rectangleDataList) {
			int minY = rectangleData.getNorthWestCorner().getY();
			int maxY = rectangleData.getSouthEastCorner().getY();
			for (int x = rectangleData.getNorthWestCorner().getX(); x <= rectangleData.getSouthEastCorner().getX(); x++) {
				for (int z = rectangleData.getNorthWestCorner().getZ(); z <= rectangleData.getSouthEastCorner().getZ(); z++) {
					voxelData.addBlockPos(new BlockPos(x, maxY, z));
					voxelData.addBlockPos(new BlockPos(x, minY, z));
				}
			}
			if (minY > dataPackage.getTopOfBottomMask()) {
				dataPackage.setTopOfBottomMask(minY);
			}
			if (maxY < dataPackage.getBottomOfTopMask()) {
				dataPackage.setBottomOfTopMask(maxY);
			}
			if (minY < dataPackage.getBottomOfBottom()) {
				dataPackage.setBottomOfBottom(minY);
			}
			if (maxY > dataPackage.getTopOfTop()) {
				dataPackage.setTopOfTop(maxY);
			}
		}
		dataPackage.setTopMask(voxelData.getTopMask());
		dataPackage.setBottomMask(voxelData.getBottomMask());

		return dataPackage;
	}

	private static void scanTerrain(ServerLevel level, LiveDataPackage dataPackage, ScanResult scanResult) {
		//calculate touching
		nextTopMask(dataPackage);
		nextBottomMask(dataPackage);
		Block topMaskTouch;
		Block bottomMaskTouch;
		int heightOfCelling = 0;
		int heightOfBottom = 0;

		CageStruct cageStruct = getCage(dataPackage.getBottomMask());

		//calculate celling touch
		while (dataPackage.getBottomOfTopMask() <= TOP_OF_MAP) {
			topMaskTouch = topMaskHasCollision(dataPackage.getTopMask(), level);
			if (Util.isNotField(topMaskTouch)) {
				nextTopMask(dataPackage);
				heightOfCelling++;
			} else {
				break;
			}
		}

		//calculate bottom touch
		while (dataPackage.getTopOfBottomMask() >= BOTTOM_OF_MAP) {
			bottomMaskTouch = bottomMaskHasCollision(dataPackage.getBottomMask(), level);
			if (Util.isNotField(bottomMaskTouch)) {
				nextBottomMask(dataPackage);
				heightOfBottom++;
			} else {
				break;
			}
		}

		//calculate fluid line
		scanResult.setAbsoluteFluidLine(getAbsoluteFluidLine(level, heightOfBottom, cageStruct));
		scanResult.setFluidType(getFluidType(level, cageStruct, scanResult.getAbsoluteFluidLine()));

		scanResult.setMaxCelling(heightOfCelling);
		scanResult.setMaxBottom(heightOfBottom - 1);
	}

	private static void nextTopMask(LiveDataPackage dataPackage) {
		Set<BlockPos> newTopMask = new HashSet<>();
		dataPackage.getTopMask().forEach(blockPos -> newTopMask.add(blockPos.above()));
		dataPackage.setTopMask(newTopMask);
		dataPackage.setBottomOfTopMask(dataPackage.getBottomOfTopMask() + 1);
	}

	private static void nextBottomMask(LiveDataPackage dataPackage) {
		Set<BlockPos> newBottomMask = new HashSet<>();
		dataPackage.getBottomMask().forEach(blockPos -> newBottomMask.add(blockPos.below()));
		dataPackage.setBottomMask(newBottomMask);
		dataPackage.setTopOfBottomMask(dataPackage.getTopOfBottomMask() - 1);
	}

	private static Block topMaskHasCollision(Set<BlockPos> topMask, ServerLevel level) {
		for (BlockPos blockPos : topMask) {
			Block block = level.getBlockState(blockPos).getBlock();
			if (!block.equals(Blocks.AIR)) {
				return block;
			}
		}
		return Blocks.AIR;
	}

	private static Block bottomMaskHasCollision(Set<BlockPos> bottomMask, ServerLevel level) {
		for (BlockPos blockPos : bottomMask) {
			Block block = level.getBlockState(blockPos).getBlock();
			if (!block.equals(Blocks.AIR)) {
				return block;
			}
		}
		return Blocks.AIR;
	}

	private static CageStruct getCage(Set<BlockPos> bottomMask) {
		boolean first = true;
		final CageStruct cageStruct = new CageStruct();
		for (BlockPos mask : bottomMask) {
			if (first) {
				first = false;
				cageStruct.setYMax(mask.getY());
				cageStruct.setYMin(mask.getY());
				cageStruct.setXMax(mask.getX());
				cageStruct.setXMin(mask.getX());
				cageStruct.setZMax(mask.getZ());
				cageStruct.setZMin(mask.getZ());
			} else {
				if (cageStruct.getYMin() > mask.getY()) {
					cageStruct.setYMin(mask.getY());
				}
				if (cageStruct.getYMax() < mask.getY()) {
					cageStruct.setYMax(mask.getY());
				}
				if (cageStruct.getXMin() > mask.getX()) {
					cageStruct.setXMin(mask.getX());
				}
				if (cageStruct.getXMax() < mask.getX()) {
					cageStruct.setXMax(mask.getX());
				}
				if (cageStruct.getZMin() > mask.getZ()) {
					cageStruct.setZMin(mask.getZ());
				}
				if (cageStruct.getZMax() < mask.getZ()) {
					cageStruct.setZMax(mask.getZ());
				}
			}
		}
		return cageStruct;
	}

	private static int getAbsoluteFluidLine(ServerLevel level, int maxBottom, CageStruct cage) {
		int fluidLine = cage.getYMin() - maxBottom + 1;
		Set<BlockPos> externalCage = new HashSet<>(Arrays.asList(
				new BlockPos(cage.getXMin() - 1, fluidLine, cage.getZMin() - 1),
				new BlockPos(cage.getXMin() - 1, fluidLine, cage.getZMax() + 1),
				new BlockPos(cage.getXMax() + 1, fluidLine, cage.getZMin() - 1),
				new BlockPos(cage.getXMax() + 1, fluidLine, cage.getZMax() + 1))
		);

		while (cageNotColluded(level, externalCage)) {
			externalCage = moveCage(externalCage);
			fluidLine++;
			if (fluidLine >= TOP_OF_MAP) {
				break;
			}
		}
		return fluidLine - 1;
	}

	private static FluidType getFluidType(ServerLevel level, CageStruct cage, int fluidLine) {
		Set<BlockPos> externalCage = new HashSet<>(Arrays.asList(
				new BlockPos(cage.getXMin() - 1, fluidLine, cage.getZMin() - 1),
				new BlockPos(cage.getXMin() - 1, fluidLine, cage.getZMax() + 1),
				new BlockPos(cage.getXMax() + 1, fluidLine, cage.getZMin() - 1),
				new BlockPos(cage.getXMax() + 1, fluidLine, cage.getZMax() + 1))
		);
		for (BlockPos blockPos : externalCage) {
			if (Util.isLava(level.getBlockState(blockPos).getBlock())) {
				return FluidType.LAVA;
			}
			if (Util.isCommonFluid(level.getBlockState(blockPos).getBlock())) {
				return FluidType.COMMON;
			}
		}

		return FluidType.NONE;
	}

	private static boolean cageNotColluded(ServerLevel level, Set<BlockPos> externalCage) {
		for (BlockPos blockPos : externalCage) {
			if (Util.isFluid(level.getBlockState(blockPos).getBlock())) {
				return true;
			}
		}
		return false;
	}

	private static Set<BlockPos> moveCage(Set<BlockPos> externalCage) {
		return externalCage.stream().map(BlockPos::above).collect(Collectors.toSet());
	}
}
