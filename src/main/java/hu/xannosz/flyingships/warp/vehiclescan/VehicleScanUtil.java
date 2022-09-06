package hu.xannosz.flyingships.warp.vehiclescan;

import hu.xannosz.flyingships.Configuration;
import hu.xannosz.flyingships.warp.AbsoluteRectangleData;
import lombok.experimental.UtilityClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@UtilityClass
public class VehicleScanUtil {
	public static VehicleScanResponseStruct scanVehicle(ServerLevel level, List<AbsoluteRectangleData> rectangleDataList, Direction blockDirection) {
		VehicleScanResponseStruct responseStruct = new VehicleScanResponseStruct();

		// get block position set
		Set<BlockPos> blocks = getBlockPosSet(rectangleDataList);
		VoxelMatrix matrix = new VoxelMatrix(level, blocks);

		// calculate lift surface
		responseStruct.setLiftSurface(calculateSurface(matrix.getYColumns()));

		// count blocks
		countBlocks(responseStruct, matrix.getYColumns());

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

	private static void countBlocks(VehicleScanResponseStruct responseStruct, Set<VoxelColumn> columns) {
		int wool = 0;
		int heater = 0;
		int boiler = 0;
		int density = 0;
		for (VoxelColumn voxelColumn : columns) {
			wool += voxelColumn.getWool();
			heater += voxelColumn.getHeater();
			boiler += voxelColumn.getBoiler();
			density += voxelColumn.getDensity();
		}
		responseStruct.setWool(wool);
		responseStruct.setHeater(heater);
		responseStruct.setBoiler(boiler);
		responseStruct.setDensity(density);
	}

	private static void calculateQuotients(VehicleScanResponseStruct responseStruct) {  //TODO remove after steam engine
		Configuration configuration = Configuration.getConfiguration();

		responseStruct.setFloatingQuotient(responseStruct.getLiftSurface() * configuration.getLiftMultiplier() +
				responseStruct.getWool() * configuration.getBalloonMultiplier());
	}
}
