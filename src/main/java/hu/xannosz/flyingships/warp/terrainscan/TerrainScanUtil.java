package hu.xannosz.flyingships.warp.terrainscan;

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
public class TerrainScanUtil {
	public static final int TOP_OF_MAP = 319;
	public static final int BOTTOM_OF_MAP = -64;

	public static TerrainScanResponseStruct scanTerrain(ServerLevel level, LiveDataPackage dataPackage, int waterLine) {

		TerrainScanResponseStruct responseStruct = new TerrainScanResponseStruct();

		final int vehicleSize = getVehicleSize(dataPackage);

		//calculate touching
		nextTopMask(dataPackage);
		nextBottomMask(dataPackage);
		Block topMaskTouch = topMaskHasCollision(dataPackage.getTopMask(), level);
		Block bottomMaskTouch = bottomMaskHasCollision(dataPackage.getBottomMask(), level);
		int heightOfCelling = 0;
		int heightOfBottom = 0;

		//calculate floating
		final CageStruct cage = getCage(dataPackage.getBottomMask());
		responseStruct.setFloatingPosition(getFloatingPosition(level, cage, waterLine));

		//calculate celling warp / set indicator
		if (topMaskTouch.equals(Blocks.WATER)) {
			responseStruct.setCellingPosition(CellingPosition.UNDER_WATER);
			responseStruct.setFloatingPosition(FloatingPosition.VOID);
			while (dataPackage.getBottomOfTopMask() < TOP_OF_MAP) {
				heightOfCelling++;
				nextTopMask(dataPackage);
				topMaskTouch = topMaskHasCollision(dataPackage.getTopMask(), level);
				if (topMaskTouch.equals(Blocks.AIR) || topMaskTouch.equals(Blocks.VOID_AIR)) {
					responseStruct.setHeightOfWaterLine(heightOfCelling + vehicleSize + 1);
					break;
				}
			}
		} else if (topMaskTouch.equals(Blocks.LAVA)) {
			responseStruct.setCellingPosition(CellingPosition.UNDER_LAVA);
			responseStruct.setFloatingPosition(FloatingPosition.VOID);
			while (dataPackage.getBottomOfTopMask() < TOP_OF_MAP) {
				heightOfCelling++;
				nextTopMask(dataPackage);
				topMaskTouch = topMaskHasCollision(dataPackage.getTopMask(), level);
				if (topMaskTouch.equals(Blocks.AIR) || topMaskTouch.equals(Blocks.VOID_AIR)) {
					responseStruct.setHeightOfWaterLine(heightOfCelling + vehicleSize + 1);
					break;
				}
			}
		} else if (topMaskTouch.equals(Blocks.AIR)) {
			while (dataPackage.getBottomOfTopMask() < TOP_OF_MAP) {
				heightOfCelling++;
				nextTopMask(dataPackage);
				topMaskTouch = topMaskHasCollision(dataPackage.getTopMask(), level);
				if (!topMaskTouch.equals(Blocks.AIR) && !topMaskTouch.equals(Blocks.VOID_AIR)) {
					responseStruct.setCellingPosition(CellingPosition.UNDER_FIELD);
					responseStruct.setHeightOfCelling(heightOfCelling);
					break;
				}
			}
			if (topMaskTouch.equals(Blocks.AIR)) {
				responseStruct.setCellingPosition(CellingPosition.VOID);
				responseStruct.setHeightOfCelling(TOP_OF_MAP);
			}
		} else {
			responseStruct.setCellingPosition(CellingPosition.TOUCH_CELLING);
		}

		//calculate landing warp / set indicator
		if (bottomMaskTouch.equals(Blocks.AIR)) {
			while (dataPackage.getTopOfBottomMask() > BOTTOM_OF_MAP) {
				heightOfBottom++;
				nextBottomMask(dataPackage);
				bottomMaskTouch = bottomMaskHasCollision(dataPackage.getBottomMask(), level);
				if (bottomMaskTouch.equals(Blocks.WATER)) {
					responseStruct.setBottomPosition(BottomPosition.FLY_OVER_WATER);
					responseStruct.setHeightOfBottom(heightOfBottom);
					responseStruct.setHeightOfWaterLine(-heightOfBottom);
					break;
				} else if (bottomMaskTouch.equals(Blocks.LAVA)) {
					responseStruct.setBottomPosition(BottomPosition.FLY_OVER_LAVA);
					responseStruct.setHeightOfBottom(heightOfBottom);
					responseStruct.setHeightOfWaterLine(-heightOfBottom);
					break;
				} else if (!bottomMaskTouch.equals(Blocks.AIR)) {
					responseStruct.setBottomPosition(BottomPosition.FLY_OVER_FIELD);
					responseStruct.setHeightOfBottom(heightOfBottom);
					break;
				}
			}
			if (bottomMaskTouch.equals(Blocks.AIR)) {
				responseStruct.setBottomPosition(BottomPosition.VOID);
			}
		} else if (bottomMaskTouch.equals(Blocks.WATER) || bottomMaskTouch.equals(Blocks.LAVA)) {
			while (dataPackage.getTopOfBottomMask() > BOTTOM_OF_MAP) {
				heightOfBottom++;
				nextBottomMask(dataPackage);
				bottomMaskTouch = bottomMaskHasCollisionWaterMode(dataPackage.getBottomMask(), level);
				if (!bottomMaskTouch.equals(Blocks.WATER) && !bottomMaskTouch.equals(Blocks.LAVA)) {
					responseStruct.setBottomPosition(BottomPosition.FLY_OVER_FIELD);
					responseStruct.setHeightOfBottom(heightOfBottom);
					break;
				}
			}
			if (bottomMaskTouch.equals(Blocks.AIR) || bottomMaskTouch.equals(Blocks.VOID_AIR) ||
					bottomMaskTouch.equals(Blocks.WATER) || bottomMaskTouch.equals(Blocks.LAVA)) {
				responseStruct.setBottomPosition(BottomPosition.VOID);
			}
		} else {
			responseStruct.setBottomPosition(BottomPosition.LANDED);
		}

		responseStruct.setAbsoluteWaterLine(getAbsoluteFluidLine(level, responseStruct, cage));

		return responseStruct;
	}

	public static LiveDataPackage generateMasks(List<AbsoluteRectangleData> rectangleDataList) {
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
		}
		dataPackage.setTopMask(voxelData.getTopMask());
		dataPackage.setBottomMask(voxelData.getBottomMask());

		return dataPackage;
	}

	public static Set<BlockPos> getShell(List<AbsoluteRectangleData> rectangleDataList, boolean outer) {
		Set<BlockPos> result = new HashSet<>();
		for (AbsoluteRectangleData rectangleData : rectangleDataList) {
			int minX = rectangleData.getNorthWestCorner().getX() - (outer ? 1 : 0);
			int maxX = rectangleData.getSouthEastCorner().getX() + (outer ? 1 : 0);
			int minY = rectangleData.getNorthWestCorner().getY() - (outer ? 1 : 0);
			int maxY = rectangleData.getSouthEastCorner().getY() + (outer ? 1 : 0);
			int minZ = rectangleData.getNorthWestCorner().getZ() - (outer ? 1 : 0);
			int maxZ = rectangleData.getSouthEastCorner().getZ() + (outer ? 1 : 0);

			for (int y = rectangleData.getNorthWestCorner().getY(); y <= rectangleData.getSouthEastCorner().getY(); y++) {
				for (int z = rectangleData.getNorthWestCorner().getZ(); z <= rectangleData.getSouthEastCorner().getZ(); z++) {
					result.add(new BlockPos(minX, y, z));
					result.add(new BlockPos(maxX, y, z));
				}
			}

			for (int x = rectangleData.getNorthWestCorner().getX(); x <= rectangleData.getSouthEastCorner().getX(); x++) {
				for (int y = rectangleData.getNorthWestCorner().getY(); y <= rectangleData.getSouthEastCorner().getY(); y++) {
					result.add(new BlockPos(x, y, minZ));
					result.add(new BlockPos(x, y, maxZ));
				}
			}

			for (int x = rectangleData.getNorthWestCorner().getX(); x <= rectangleData.getSouthEastCorner().getX(); x++) {
				for (int z = rectangleData.getNorthWestCorner().getZ(); z <= rectangleData.getSouthEastCorner().getZ(); z++) {
					result.add(new BlockPos(x, minY, z));
					result.add(new BlockPos(x, maxY, z));
				}
			}
		}

		return result;
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

	private static Block bottomMaskHasCollisionWaterMode(Set<BlockPos> bottomMask, ServerLevel level) {
		for (BlockPos blockPos : bottomMask) {
			Block block = level.getBlockState(blockPos).getBlock();
			if (!block.equals(Blocks.WATER) && !block.equals(Blocks.LAVA)) {
				return block;
			}
		}
		return Blocks.WATER;
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

	private static FloatingPosition getFloatingPosition(ServerLevel level, CageStruct cage, int waterLine) {
		if (level.getBlockState(new BlockPos(cage.getXMin() - 1,
				cage.getYMin() - waterLine, cage.getZMin() - 1)).getBlock().equals(Blocks.WATER)) {
			return FloatingPosition.SWIM_WATER;
		}
		if (level.getBlockState(new BlockPos(cage.getXMin() - 1,
				cage.getYMin() - waterLine, cage.getZMax() + 1)).getBlock().equals(Blocks.WATER)) {
			return FloatingPosition.SWIM_WATER;
		}
		if (level.getBlockState(new BlockPos(cage.getXMax() + 1,
				cage.getYMin() - waterLine, cage.getZMin() - 1)).getBlock().equals(Blocks.WATER)) {
			return FloatingPosition.SWIM_WATER;
		}
		if (level.getBlockState(new BlockPos(cage.getXMax() + 1,
				cage.getYMin() - waterLine, cage.getZMax() + 1)).getBlock().equals(Blocks.WATER)) {
			return FloatingPosition.SWIM_WATER;
		}

		if (level.getBlockState(new BlockPos(cage.getXMin() - 1,
				cage.getYMin() - waterLine, cage.getZMin() - 1)).getBlock().equals(Blocks.LAVA)) {
			return FloatingPosition.SWIM_LAVA;
		}
		if (level.getBlockState(new BlockPos(cage.getXMin() - 1,
				cage.getYMin() - waterLine, cage.getZMax() + 1)).getBlock().equals(Blocks.LAVA)) {
			return FloatingPosition.SWIM_LAVA;
		}
		if (level.getBlockState(new BlockPos(cage.getXMax() + 1,
				cage.getYMin() - waterLine, cage.getZMin() - 1)).getBlock().equals(Blocks.LAVA)) {
			return FloatingPosition.SWIM_LAVA;
		}
		if (level.getBlockState(new BlockPos(cage.getXMax() + 1,
				cage.getYMin() - waterLine, cage.getZMax() + 1)).getBlock().equals(Blocks.LAVA)) {
			return FloatingPosition.SWIM_LAVA;
		}

		return FloatingPosition.VOID;
	}

	private static int getVehicleSize(LiveDataPackage dataPackage) {
		int maxY = 0;
		int minY = 0;
		boolean first = true;

		for (BlockPos mask : dataPackage.getTopMask()) {
			if (first) {
				first = false;
				maxY = mask.getY();
				minY = mask.getY();
			} else {
				if (maxY < mask.getY()) {
					maxY = mask.getY();
				}
			}
		}

		for (BlockPos mask : dataPackage.getBottomMask()) {
			if (minY > mask.getY()) {
				minY = mask.getY();
			}
		}

		return maxY - minY;
	}

	private static int getAbsoluteFluidLine(ServerLevel level, TerrainScanResponseStruct terrainScanResponseStruct, CageStruct cage) {
		int waterline = cage.getYMin() + 1;
		Set<BlockPos> externalCage = new HashSet<>(Arrays.asList(
				new BlockPos(cage.getXMin() - 1, waterline, cage.getZMin() - 1),
				new BlockPos(cage.getXMin() - 1, waterline, cage.getZMax() + 1),
				new BlockPos(cage.getXMax() + 1, waterline, cage.getZMin() - 1),
				new BlockPos(cage.getXMax() + 1, waterline, cage.getZMax() + 1))
		);

		if (terrainScanResponseStruct.getCellingPosition().equals(CellingPosition.UNDER_LAVA) ||
				terrainScanResponseStruct.getCellingPosition().equals(CellingPosition.UNDER_WATER) ||
				terrainScanResponseStruct.getFloatingPosition().equals(FloatingPosition.SWIM_LAVA) ||
				terrainScanResponseStruct.getFloatingPosition().equals(FloatingPosition.SWIM_WATER)) {
			while (cageNotColluded(level, externalCage)) {
				externalCage = moveCage(externalCage);
				waterline++;
			}
			return waterline;
		}

		return TerrainScanUtil.BOTTOM_OF_MAP;
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
