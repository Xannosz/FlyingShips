package hu.xannosz.flyingships.warp.scan;

import lombok.Data;

@Data
public class ScanResult {
	private CellingPosition cellingPosition = CellingPosition.VOID;
	private FloatingPosition floatingPosition = FloatingPosition.VOID;
	private BottomPosition bottomPosition = BottomPosition.VOID;
	private FluidType fluidType;
	private int toFluidLine;
	private int maxCelling;
	private int maxBottom;
	private int absoluteFluidLine;
}
