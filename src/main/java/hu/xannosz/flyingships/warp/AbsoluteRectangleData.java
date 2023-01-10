package hu.xannosz.flyingships.warp;

import lombok.Data;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

@Data
public class AbsoluteRectangleData {
	private BlockPos northWestCorner;
	private BlockPos southEastCorner;
	private Vec3i structureSize;
	private Map<BlockPos, BlockState> structureBlocks;
	private Map<BlockPos, BlockState> postStructureBlocks;
	private Map<BlockPos, BlockEntity> structureBlockEntities;
}
