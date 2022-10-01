package hu.xannosz.flyingships.warp.terrainscan;

import lombok.Data;

@Data
public class TerrainScanResponseStruct {
	private CellingPosition cellingPosition = CellingPosition.VOID;
	private FloatingPosition floatingPosition = FloatingPosition.VOID;
	private BottomPosition bottomPosition = BottomPosition.VOID;
	private int heightOfCelling = -1;
	private int heightOfBottom = -1;
	private int heightOfWaterLine = -1;
	private int absoluteWaterLine = -1;
}
