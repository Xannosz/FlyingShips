package hu.xannosz.flyingships.warp.terrainscan;

import lombok.Data;
import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;

import static hu.xannosz.flyingships.warp.terrainscan.TerrainScanUtil.BOTTOM_OF_MAP;
import static hu.xannosz.flyingships.warp.terrainscan.TerrainScanUtil.TOP_OF_MAP;

@Data
public class LiveDataPackage {
	private Set<BlockPos> topMask = new HashSet<>();
	private Set<BlockPos> bottomMask = new HashSet<>();
	private int bottomOfTopMask = TOP_OF_MAP;
	private int topOfBottomMask = BOTTOM_OF_MAP;
}
