package hu.xannosz.flyingships;

import lombok.experimental.UtilityClass;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Arrays;
import java.util.List;

@UtilityClass
public class Util {

	public static final List<Block> PRE_PROCESS = Arrays.asList(Blocks.REDSTONE_WIRE);
	public static final List<Block> POST_PROCESS = Arrays.asList(Blocks.PISTON_HEAD);
	private static final List<Block> HOLLOW_BLOCKS = Arrays.asList(Blocks.LADDER, Blocks.TORCH, Blocks.WALL_TORCH,
			Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH, Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH,
			Blocks.ACACIA_FENCE, Blocks.BIRCH_FENCE,
			Blocks.SPRUCE_TRAPDOOR,
			Blocks.STONE_SLAB);

	public static final int CLOUD_LEVEL = 196;
	public static final int RUDDER_TYPES = 5;

	public static int convertBitArrayToInt(boolean[] booleans) {
		int value = 0;
		for (int i = 0; i < booleans.length; ++i) {
			if (booleans[i]) value |= (1 << i);
		}
		return value;
	}

	public static boolean[] convertIntToBitArray(int value, int size) {
		boolean[] booleans = new boolean[size];
		for (int i = 0; i < size; ++i) {
			booleans[i] = (value & (1 << i)) != 0;
		}
		return booleans;
	}

	public static boolean isFluid(Block block) {
		return block instanceof LiquidBlock;
	}

	public static boolean isCommonFluid(Block block) {
		return isFluid(block) && !isLava(block);
	}

	public static boolean isLava(Block block) {
		return block.equals(Blocks.LAVA);
	}

	public static boolean isFluidTagged(BlockState block) {
		return block.hasProperty(BlockStateProperties.WATERLOGGED) && block.getValue(BlockStateProperties.WATERLOGGED);
	}

	public static boolean isHollow(Block block) {
		return block.defaultBlockState().hasProperty(BlockStateProperties.WATERLOGGED) || HOLLOW_BLOCKS.contains(block);
	}

	@SuppressWarnings("deprecation")
	public static float getDensity(Block block) {
		return block.getExplosionResistance();
	}
}
