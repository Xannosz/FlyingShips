package hu.xannosz.flyingships.warp.vehiclescan;

import lombok.Data;

@Data
public class VehicleScanResponseStruct {
	private int wool;
	private int heater;
	private int boiler;
	private int liftSurface;
	private int windSurface;
	private int density;
	private int floatingQuotient;
}
