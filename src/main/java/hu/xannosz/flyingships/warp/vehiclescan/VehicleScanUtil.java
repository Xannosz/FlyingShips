package hu.xannosz.flyingships.warp.vehiclescan;

import hu.xannosz.flyingships.Configuration;
import hu.xannosz.flyingships.warp.AbsoluteRectangleData;
import hu.xannosz.flyingships.warp.jump.JumpUtil;
import lombok.experimental.UtilityClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static hu.xannosz.flyingships.Util.isFluid;
import static hu.xannosz.flyingships.Util.isFluidTagged;

@UtilityClass
public class VehicleScanUtil {
	public static VehicleScanResponseStruct scanVehicle(ServerLevel level, List<AbsoluteRectangleData> rectangleDataList, Direction blockDirection) {
		VehicleScanResponseStruct responseStruct = new VehicleScanResponseStruct();

		// get block position set
		Set<BlockPos> blocks = getBlockPosSet(rectangleDataList);
		VoxelMatrix matrix = new VoxelMatrix(level, blocks);

		// get entities
		Set<Entity> entities = JumpUtil.getEntities(rectangleDataList, new Vec3(0, 0, 0), level).keySet();

		// get players
		Set<ServerPlayer> players = JumpUtil.getPlayers(rectangleDataList, new Vec3(0, 0, 0), level).keySet();

		// calculate lift surface
		responseStruct.setLiftSurface(calculateSurface(matrix.getYColumns()));

		// calculate bottom Y
		responseStruct.setBottomY(calculateBottomY(matrix.getYColumns()));

		// count blocks
		countBlocks(responseStruct, matrix.getYColumns());

		// calculate mob density
		calculateMobDensity(responseStruct, entities, players);

		// calculate wind surface
		if (blockDirection.equals(Direction.NORTH) || blockDirection.equals(Direction.SOUTH)) {
			responseStruct.setWindSurface(calculateSurface(matrix.getZColumns()));
		} else {
			responseStruct.setWindSurface(calculateSurface(matrix.getXColumns()));
		}

		// calculate quotients
		calculateQuotients(responseStruct);

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

	private static void countBlocks(VehicleScanResponseStruct responseStruct, Set<VoxelColumn> columns) {
		int wool = 0;
		int heater = 0;
		int tank = 0;
		int density = 0;
		for (VoxelColumn voxelColumn : columns) {
			wool += voxelColumn.getWool();
			heater += voxelColumn.getHeater();
			tank += voxelColumn.getTank();
			density += voxelColumn.getDensity();
		}
		responseStruct.setWool(wool);
		responseStruct.setHeater(heater);
		responseStruct.setTank(tank);
		responseStruct.setDensity(density);
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

	private static void calculateQuotients(VehicleScanResponseStruct responseStruct) {  //TODO remove after steam engine
		Configuration configuration = Configuration.getConfiguration();

		responseStruct.setFloatingQuotient(responseStruct.getLiftSurface() * configuration.getLiftMultiplier() +
				responseStruct.getWool() * configuration.getBalloonMultiplier());
	}
}
