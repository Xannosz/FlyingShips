package hu.xannosz.flyingships.warp.vehiclescan;

import lombok.Data;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

@Data
public class VehicleScanResponseStruct {
	private int wool;
	private int heater;
	private int tank;
	private int enderOscillator;
	private int liftSurface;
	private int windSurface;
	private int density;
	private int artificialFloater;
	private int bottomY;
	private boolean hyperDriveEngineFound = false;
	private Set<ServerPlayer> players;
	private int blockNumUnderFluid;
}
