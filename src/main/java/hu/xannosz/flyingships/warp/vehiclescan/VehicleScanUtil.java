package hu.xannosz.flyingships.warp.vehiclescan;

import hu.xannosz.flyingships.Util;
import hu.xannosz.flyingships.block.ModBlocks;
import hu.xannosz.flyingships.warp.AbsoluteRectangleData;
import hu.xannosz.flyingships.warp.jump.JumpUtil;
import hu.xannosz.flyingships.warp.scan.BottomPosition;
import hu.xannosz.flyingships.warp.scan.CellingPosition;
import hu.xannosz.flyingships.warp.scan.FloatingPosition;
import hu.xannosz.flyingships.warp.scan.ScanResult;
import lombok.experimental.UtilityClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Collectors;

import static hu.xannosz.flyingships.Util.isFluid;
import static hu.xannosz.flyingships.Util.isFluidTagged;

@UtilityClass
public class VehicleScanUtil {
	public static VehicleScanResponseStruct scanVehicle(ServerLevel level, List<AbsoluteRectangleData> rectangleDataList,
														Direction blockDirection, ScanResult terrainScanResponseStruct) {
		VehicleScanResponseStruct responseStruct = new VehicleScanResponseStruct();
		final boolean isCommonFluid = isCommonFluid(terrainScanResponseStruct);

		// get block position set
		Set<BlockPos> blocks = getBlockPosSet(rectangleDataList);
		VoxelMatrix matrix = new VoxelMatrix(level, blocks, terrainScanResponseStruct.getAbsoluteFluidLine(), isCommonFluid);

		// get entities
		Set<Entity> entities = JumpUtil.getEntities(rectangleDataList, new Vec3(0, 0, 0), level).keySet();

		// get players
		Set<ServerPlayer> players = JumpUtil.getPlayers(rectangleDataList, new Vec3(0, 0, 0), level).keySet();
		responseStruct.setPlayers(players);

		// detect hyper drive
		Set<BlockPos> hyperDriveCores = getHyperDriveCores(level, blocks);
		responseStruct.setHyperDriveEngineFound(isHyperDriveEngineFound(level, hyperDriveCores));

		// calculate lift surface
		responseStruct.setLiftSurface(calculateSurface(matrix.getYColumns()));

		// calculate bottom Y
		responseStruct.setBottomY(calculateBottomY(matrix.getYColumns()));

		// count blocks
		countBlocks(responseStruct, matrix.getYColumns(), matrix.getBlockNumUnderWater());

		// calculate mob density
		calculateMobDensity(responseStruct, entities, players);

		// list heaters
		responseStruct.setHeaterBlocks(getHeaterBlocks(level, blocks));

		// list coils
		Set<BlockPos> coilCores = getCoilCores(level, blocks);
		responseStruct.setCoils(coilCores.stream().map(core -> measureCoil(level, blocks, core))
				.collect(Collectors.toSet()));

		// calculate wind surface
		if (blockDirection.equals(Direction.NORTH) || blockDirection.equals(Direction.SOUTH)) {
			responseStruct.setWindSurface(calculateSurface(matrix.getZColumns()));
		} else {
			responseStruct.setWindSurface(calculateSurface(matrix.getXColumns()));
		}

		return responseStruct;
	}

	public static Set<BlockPos> getFluidsRecursive(ServerLevel level, List<AbsoluteRectangleData> rectangleDataList, Set<BlockPos> shell) {
		Set<BlockPos> fluid = new HashSet<>();
		Deque<BlockPos> neighbours = new LinkedList<>();

		for (BlockPos blockPos : shell) {
			BlockPos[] localNeighbours = new BlockPos[]{blockPos.above(), blockPos.below(), blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west()};
			for (BlockPos localNeighbour : localNeighbours) {
				if (inRectangles(rectangleDataList, localNeighbour) && isFluid(level.getBlockState(localNeighbour).getBlock()) && !neighbours.contains(localNeighbour)) {
					neighbours.add(localNeighbour);
				}
			}
		}

		while (neighbours.size() > 0) {
			BlockPos blockPos = neighbours.pop();
			fluid.add(blockPos);
			BlockPos[] localNeighbours = new BlockPos[]{blockPos.above(), blockPos.below(), blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west()};
			for (BlockPos localNeighbour : localNeighbours) {
				if (inRectangles(rectangleDataList, localNeighbour) &&
						(isFluid(level.getBlockState(localNeighbour).getBlock()) || isFluidTagged(level.getBlockState(localNeighbour))) &&
						!neighbours.contains(localNeighbour) && !fluid.contains(localNeighbour)) {
					neighbours.add(localNeighbour);
				}
			}
		}

		return fluid;
	}

	private static boolean inRectangles(List<AbsoluteRectangleData> rectangleDataList, BlockPos blockPos) {
		for (AbsoluteRectangleData rectangleData : rectangleDataList) {
			if (rectangleData.getNorthWestCorner().getX() <= blockPos.getX() && blockPos.getX() <= rectangleData.getSouthEastCorner().getX() &&
					rectangleData.getNorthWestCorner().getY() <= blockPos.getY() && blockPos.getY() <= rectangleData.getSouthEastCorner().getY() &&
					rectangleData.getNorthWestCorner().getZ() <= blockPos.getZ() && blockPos.getZ() <= rectangleData.getSouthEastCorner().getZ()
			) {
				return true;
			}
		}
		return false;
	}

	private static Set<BlockPos> getBlockPosSet(List<AbsoluteRectangleData> rectangleDataList) {
		Set<BlockPos> blocks = new HashSet<>();
		for (AbsoluteRectangleData rectangleData : rectangleDataList) {
			for (int x = rectangleData.getNorthWestCorner().getX(); x <= rectangleData.getSouthEastCorner().getX(); x++) {
				for (int y = rectangleData.getNorthWestCorner().getY(); y <= rectangleData.getSouthEastCorner().getY(); y++) {
					for (int z = rectangleData.getNorthWestCorner().getZ(); z <= rectangleData.getSouthEastCorner().getZ(); z++) {
						blocks.add(new BlockPos(x, y, z));
					}
				}
			}
		}
		return blocks;
	}

	private static Set<BlockPos> getHyperDriveCores(ServerLevel level, Set<BlockPos> blocks) {
		Set<BlockPos> result = new HashSet<>();
		for (BlockPos block : blocks) {
			if (level.getBlockState(block).getBlock().equals(ModBlocks.HYPER_DRIVE_CORE.get())) {
				result.add(block);
			}
		}
		return result;
	}

	private static boolean isHyperDriveEngineFound(ServerLevel level, Set<BlockPos> hyperDriveCores) {
		for (BlockPos hyperDriveCore : hyperDriveCores) {
			if (isEnderOscillator(level, hyperDriveCore, 2, 0, 2) &&
					isEnderOscillator(level, hyperDriveCore, -2, 0, -2) &&
					isEnderOscillator(level, hyperDriveCore, -2, 0, 2) &&
					isEnderOscillator(level, hyperDriveCore, 2, 0, -2)) {
				return true;
			}
			if (isEnderOscillator(level, hyperDriveCore, 2, 2, 0) &&
					isEnderOscillator(level, hyperDriveCore, -2, -2, 0) &&
					isEnderOscillator(level, hyperDriveCore, -2, 2, 0) &&
					isEnderOscillator(level, hyperDriveCore, 2, -2, 0)) {
				return true;
			}
			if (isEnderOscillator(level, hyperDriveCore, 0, 2, 2) &&
					isEnderOscillator(level, hyperDriveCore, 0, -2, -2) &&
					isEnderOscillator(level, hyperDriveCore, 0, -2, 2) &&
					isEnderOscillator(level, hyperDriveCore, 0, 2, -2)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isEnderOscillator(ServerLevel level, BlockPos hyperDriveCore, int x, int y, int z) {
		return level.getBlockState(hyperDriveCore.offset(x, y, z)).getBlock().equals(ModBlocks.ENDER_OSCILLATOR.get());
	}

	private static int calculateSurface(Set<VoxelColumn> columns) {
		int surface = 0;
		for (VoxelColumn voxelColumn : columns) {
			if (voxelColumn.isBlocked()) {
				surface++;
			} else {
				surface += voxelColumn.getWool();
			}
		}
		return surface;
	}

	private static int calculateBottomY(Set<VoxelColumn> yColumns) {
		int bottom = 0;
		boolean first = true;
		for (VoxelColumn column : yColumns) {
			if (first) {
				bottom = column.getMin();
				first = false;
			} else {
				if (bottom > column.getMin()) {
					bottom = column.getMin();
				}
			}
		}
		return bottom;
	}

	private static void countBlocks(VehicleScanResponseStruct responseStruct, Set<VoxelColumn> columns, int blockUnderWater) {
		int wool = 0;
		int heater = 0;
		int enderOscillator = 0;
		int tank = 0;
		int density = 0;
		int artificialFloater = 0;
		int blockNumUnderWater = blockUnderWater;

		for (VoxelColumn voxelColumn : columns) {
			wool += voxelColumn.getWool();
			heater += voxelColumn.getHeater();
			tank += voxelColumn.getTank();
			density += voxelColumn.getDensity();
			enderOscillator += voxelColumn.getEnderOscillator();
			artificialFloater += voxelColumn.getArtificialFloater();
			blockNumUnderWater += voxelColumn.getBlockNumUnderWater();
		}

		responseStruct.setWool(wool);
		responseStruct.setHeater(heater);
		responseStruct.setEnderOscillator(enderOscillator);
		responseStruct.setTank(tank);
		responseStruct.setDensity(density);
		responseStruct.setArtificialFloater(artificialFloater);
		responseStruct.setBlockNumUnderFluid(blockNumUnderWater);
	}

	private static void calculateMobDensity(VehicleScanResponseStruct responseStruct, Set<Entity> entities, Set<ServerPlayer> players) {
		int density = responseStruct.getDensity();
		for (Entity entity : entities) {
			density += entity.getType().getDimensions().height * entity.getType().getDimensions().width * 10;
		}
		for (ServerPlayer serverPlayer : players) {
			density += serverPlayer.getType().getDimensions().height * serverPlayer.getType().getDimensions().width * 10;
		}
		responseStruct.setDensity(density);
	}

	private static Set<BlockPos> getHeaterBlocks(ServerLevel level, Set<BlockPos> blocks) {
		Set<BlockPos> result = new HashSet<>();
		for (BlockPos block : blocks) {
			if (level.getBlockState(block).getBlock().equals(ModBlocks.HEATER.get())) {
				result.add(block);
			}
		}
		return result;
	}

	private static Set<BlockPos> getCoilCores(ServerLevel level, Set<BlockPos> blocks) {
		Set<BlockPos> result = new HashSet<>();
		for (BlockPos block : blocks) {
			if (level.getBlockState(block).getBlock().equals(ModBlocks.COIL_CORE.get())) {
				result.add(block);
			}
		}
		return result;
	}

	private static int measureCoil(ServerLevel level, Set<BlockPos> blocks, BlockPos core) {
		if (isCoilCoreBlock(level, core, 0, 1, 0) &&
				isCoilCoreBlock(level, core, 0, -1, 0) &&
				!isCoilCoreBlock(level, core, 1, 0, 0) &&
				!isCoilCoreBlock(level, core, -1, 0, 0) &&
				!isCoilCoreBlock(level, core, 0, 0, 1) &&
				!isCoilCoreBlock(level, core, 0, 0, -1)
		) {
			return checkCoilLayers(level, core, blocks, 0, 1, 0);
		}
		if (!isCoilCoreBlock(level, core, 0, 1, 0) &&
				!isCoilCoreBlock(level, core, 0, -1, 0) &&
				isCoilCoreBlock(level, core, 1, 0, 0) &&
				isCoilCoreBlock(level, core, -1, 0, 0) &&
				!isCoilCoreBlock(level, core, 0, 0, 1) &&
				!isCoilCoreBlock(level, core, 0, 0, -1)
		) {
			return checkCoilLayers(level, core, blocks, 1, 0, 0);
		}
		if (!isCoilCoreBlock(level, core, 0, 1, 0) &&
				!isCoilCoreBlock(level, core, 0, -1, 0) &&
				!isCoilCoreBlock(level, core, 1, 0, 0) &&
				!isCoilCoreBlock(level, core, -1, 0, 0) &&
				isCoilCoreBlock(level, core, 0, 0, 1) &&
				isCoilCoreBlock(level, core, 0, 0, -1)
		) {
			return checkCoilLayers(level, core, blocks, 0, 0, 1);
		}
		return 0;
	}

	private static boolean isCoilCoreBlock(ServerLevel level, BlockPos coilCore, int x, int y, int z) {
		return level.getBlockState(coilCore.offset(x, y, z)).getBlock().equals(Blocks.IRON_BLOCK);
	}

	private static boolean isCoilWireBlock(ServerLevel level, BlockPos coilCore, Set<BlockPos> blocks,
										   int x, int y, int z) {
		if (!blocks.contains(coilCore.offset(x, y, z))) {
			return false;
		}
		return Util.COPPER_BLOCKS.contains(level.getBlockState(coilCore.offset(x, y, z)).getBlock());
	}

	private static int checkCoilLayers(ServerLevel level, BlockPos coilCore, Set<BlockPos> blocks,
									   int x, int y, int z) {
		int up = 0;
		int down = 0;

		for (int m = 1; ; m += 2) {
			if (isCoilLayer(level, coilCore, blocks, x * m, y * m, z * m)) {
				up++;
			} else {
				break;
			}
		}

		for (int m = 1; ; m += 2) {
			if (isCoilLayer(level, coilCore, blocks, -x * m, -y * m, -z * m)) {
				down++;
			} else {
				break;
			}
		}

		if (up >= 2 && down >= 2) {
			return up + down;
		}

		return 0;
	}

	private static boolean isCoilLayer(ServerLevel level, BlockPos coilCore, Set<BlockPos> blocks,
									   int x, int y, int z) {
		if (x == 0 && z == 0) {
			return level.getBlockState(coilCore.offset(x, y, z)).getBlock().equals(Blocks.IRON_BLOCK) &&
					level.getBlockState(coilCore.offset(x, y > 0 ? y + 1 : y - 1, z))
							.getBlock().equals(Blocks.IRON_BLOCK) &&

					isCoilWireBlock(level, coilCore, blocks, x + 2, y, z + 1) &&
					isCoilWireBlock(level, coilCore, blocks, x + 2, y, z) &&
					isCoilWireBlock(level, coilCore, blocks, x + 2, y, z - 1) &&
					isCoilWireBlock(level, coilCore, blocks, x + 2, y, z - 2) &&

					isCoilWireBlock(level, coilCore, blocks, x + 1, y, z - 2) &&
					isCoilWireBlock(level, coilCore, blocks, x, y, z - 2) &&
					isCoilWireBlock(level, coilCore, blocks, x - 1, y, z - 2) &&
					isCoilWireBlock(level, coilCore, blocks, x - 2, y, z - 2) &&

					isCoilWireBlock(level, coilCore, blocks, x - 2, y, z - 1) &&
					isCoilWireBlock(level, coilCore, blocks, x - 2, y, z) &&
					isCoilWireBlock(level, coilCore, blocks, x - 2, y, z + 1) &&
					isCoilWireBlock(level, coilCore, blocks, x - 2, y, z + 2) &&

					isCoilWireBlock(level, coilCore, blocks, x - 1, y, z + 2) &&
					isCoilWireBlock(level, coilCore, blocks, x, y, z + 2) &&
					isCoilWireBlock(level, coilCore, blocks, x + 1, y, z + 2) &&
					isCoilWireBlock(level, coilCore, blocks, x + 2, y, z + 2);
		}
		if (x == 0 && y == 0) {
			return level.getBlockState(coilCore.offset(x, y, z)).getBlock().equals(Blocks.IRON_BLOCK) &&
					level.getBlockState(coilCore.offset(x, y, z > 0 ? z + 1 : z - 1))
							.getBlock().equals(Blocks.IRON_BLOCK) &&

					isCoilWireBlock(level, coilCore, blocks, x + 2, y + 1, z) &&
					isCoilWireBlock(level, coilCore, blocks, x + 2, y, z) &&
					isCoilWireBlock(level, coilCore, blocks, x + 2, y - 1, z) &&
					isCoilWireBlock(level, coilCore, blocks, x + 2, y - 2, z) &&

					isCoilWireBlock(level, coilCore, blocks, x + 1, y - 2, z) &&
					isCoilWireBlock(level, coilCore, blocks, x, y - 2, z) &&
					isCoilWireBlock(level, coilCore, blocks, x - 1, y - 2, z) &&
					isCoilWireBlock(level, coilCore, blocks, x - 2, y - 2, z) &&

					isCoilWireBlock(level, coilCore, blocks, x - 2, y - 1, z) &&
					isCoilWireBlock(level, coilCore, blocks, x - 2, y, z) &&
					isCoilWireBlock(level, coilCore, blocks, x - 2, y + 1, z) &&
					isCoilWireBlock(level, coilCore, blocks, x - 2, y + 2, z) &&

					isCoilWireBlock(level, coilCore, blocks, x - 1, y + 2, z) &&
					isCoilWireBlock(level, coilCore, blocks, x, y + 2, z) &&
					isCoilWireBlock(level, coilCore, blocks, x + 1, y + 2, z) &&
					isCoilWireBlock(level, coilCore, blocks, x + 2, y + 2, z);
		}
		if (y == 0 && z == 0) {
			return level.getBlockState(coilCore.offset(x, y, z)).getBlock().equals(Blocks.IRON_BLOCK) &&
					level.getBlockState(coilCore.offset(x > 0 ? x + 1 : x - 1, y, z))
							.getBlock().equals(Blocks.IRON_BLOCK) &&

					isCoilWireBlock(level, coilCore, blocks, x, y + 2, z + 1) &&
					isCoilWireBlock(level, coilCore, blocks, x, y + 2, z) &&
					isCoilWireBlock(level, coilCore, blocks, x, y + 2, z - 1) &&
					isCoilWireBlock(level, coilCore, blocks, x, y + 2, z - 2) &&

					isCoilWireBlock(level, coilCore, blocks, x, y + 1, z - 2) &&
					isCoilWireBlock(level, coilCore, blocks, x, y, z - 2) &&
					isCoilWireBlock(level, coilCore, blocks, x, y - 1, z - 2) &&
					isCoilWireBlock(level, coilCore, blocks, x, y - 2, z - 2) &&

					isCoilWireBlock(level, coilCore, blocks, x, y - 2, z - 1) &&
					isCoilWireBlock(level, coilCore, blocks, x, y - 2, z) &&
					isCoilWireBlock(level, coilCore, blocks, x, y - 2, z + 1) &&
					isCoilWireBlock(level, coilCore, blocks, x, y - 2, z + 2) &&

					isCoilWireBlock(level, coilCore, blocks, x, y - 1, z + 2) &&
					isCoilWireBlock(level, coilCore, blocks, x, y, z + 2) &&
					isCoilWireBlock(level, coilCore, blocks, x, y + 1, z + 2) &&
					isCoilWireBlock(level, coilCore, blocks, x, y + 2, z + 2);
		}
		return false;
	}

	public static boolean isCommonFluid(ScanResult terrainScanResponseStruct) {
		return terrainScanResponseStruct.getCellingPosition().equals(CellingPosition.UNDER_WATER) ||
				terrainScanResponseStruct.getFloatingPosition().equals(FloatingPosition.SWIM_WATER) ||
				terrainScanResponseStruct.getBottomPosition().equals(BottomPosition.FLY_OVER_WATER);
	}
}
