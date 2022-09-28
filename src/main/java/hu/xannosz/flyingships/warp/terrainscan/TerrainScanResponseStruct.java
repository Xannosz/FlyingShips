package hu.xannosz.flyingships.warp.terrainscan;

import lombok.Data;

@Data
public class TerrainScanResponseStruct {
	private CellingPosition cellingPosition;
	private FloatingPosition floatingPosition;
	private BottomPosition bottomPosition;
	private int heightOfCelling = -1;
	private int heightOfBottom = -1;
	private int heightOfWaterLine = -1;
	private int absoluteWaterLine = -1;
}
