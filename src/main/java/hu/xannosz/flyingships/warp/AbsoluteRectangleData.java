package hu.xannosz.flyingships.warp;

import lombok.Data;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

@Data
public class AbsoluteRectangleData {
	private BlockPos northWestCorner;
	private BlockPos southEastCorner;
	private Vec3i structureSize;
	private StructureTemplate structuretemplate;
}
