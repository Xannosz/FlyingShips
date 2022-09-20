package hu.xannosz.flyingships.warp.vehiclescan;

import lombok.Data;

@Data
public class VehicleScanResponseStruct {
	private int wool;
	private int heater;
	private int tank;
	private int enderOscillator;
	private int liftSurface;
	private int windSurface;
	private int density;
	private int bottomY;
	private boolean hyperDriveEngineFound = false; //TODO detect
}
