package hu.xannosz.flyingships;

import lombok.experimental.UtilityClass;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class Util {

	public static final List<Block> PRE_PROCESS = Arrays.asList(Blocks.REDSTONE_WIRE);
	public static final List<Block> POST_PROCESS = Arrays.asList(Blocks.PISTON_HEAD);
	private static final List<Block> HOLLOW_BLOCKS = Arrays.asList(Blocks.LADDER, Blocks.TORCH, Blocks.WALL_TORCH,
			Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH, Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH,
			Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
			Blocks.REDSTONE_WIRE, Blocks.TRIPWIRE
	);

	private static final Map<Material, Integer> DENSITY_MAP = new HashMap<>();

	public static final int CLOUD_LEVEL = 196;
	public static final int RUDDER_TYPES = 3; //started from zero
	public static final int RUNE_TYPES = 3; //started from zero

	public static final List<String> INNER_ROUNDS = Arrays.asList("gui.innerRound.base", "gui.innerRound.triangle",
			"gui.innerRound.submarine", "gui.innerRound.modern", "gui.innerRound.jumper", "gui.innerRound.pyramid");

	static {
		DENSITY_MAP.put(Material.AIR, 0);
		DENSITY_MAP.put(Material.STRUCTURAL_AIR, 0);
		DENSITY_MAP.put(Material.PORTAL, 0);
		DENSITY_MAP.put(Material.CLOTH_DECORATION, 2);
		DENSITY_MAP.put(Material.PLANT, 2);
		DENSITY_MAP.put(Material.WATER_PLANT, 4);
		DENSITY_MAP.put(Material.REPLACEABLE_PLANT, 2);
		DENSITY_MAP.put(Material.REPLACEABLE_FIREPROOF_PLANT, 2);
		DENSITY_MAP.put(Material.REPLACEABLE_WATER_PLANT, 2);
		DENSITY_MAP.put(Material.WATER, 4);
		DENSITY_MAP.put(Material.BUBBLE_COLUMN, 4);
		DENSITY_MAP.put(Material.LAVA, 6);
		DENSITY_MAP.put(Material.TOP_SNOW, 1);
		DENSITY_MAP.put(Material.FIRE, 1);
		DENSITY_MAP.put(Material.DECORATION, 2);
		DENSITY_MAP.put(Material.WEB, 2);
		DENSITY_MAP.put(Material.SCULK, 4);
		DENSITY_MAP.put(Material.BUILDABLE_GLASS, 4);
		DENSITY_MAP.put(Material.CLAY, 4);
		DENSITY_MAP.put(Material.DIRT, 4);
		DENSITY_MAP.put(Material.GRASS, 4);
		DENSITY_MAP.put(Material.ICE_SOLID, 5);
		DENSITY_MAP.put(Material.SAND, 3);
		DENSITY_MAP.put(Material.SPONGE, 4);
		DENSITY_MAP.put(Material.SHULKER_SHELL, 5);
		DENSITY_MAP.put(Material.WOOD, 4);
		DENSITY_MAP.put(Material.NETHER_WOOD, 4);
		DENSITY_MAP.put(Material.BAMBOO_SAPLING, 2);
		DENSITY_MAP.put(Material.BAMBOO, 2);
		DENSITY_MAP.put(Material.WOOL, 2);
		DENSITY_MAP.put(Material.EXPLOSIVE, 5);
		DENSITY_MAP.put(Material.LEAVES, 1);
		DENSITY_MAP.put(Material.GLASS, 4);
		DENSITY_MAP.put(Material.ICE, 4);
		DENSITY_MAP.put(Material.CACTUS, 4);
		DENSITY_MAP.put(Material.STONE, 6);
		DENSITY_MAP.put(Material.METAL, 7);
		DENSITY_MAP.put(Material.SNOW, 4);
		DENSITY_MAP.put(Material.HEAVY_METAL, 8);
		DENSITY_MAP.put(Material.BARRIER, 10);
		DENSITY_MAP.put(Material.PISTON, 6);
		DENSITY_MAP.put(Material.MOSS, 3);
		DENSITY_MAP.put(Material.VEGETABLE, 2);
		DENSITY_MAP.put(Material.EGG, 2);
		DENSITY_MAP.put(Material.CAKE, 3);
		DENSITY_MAP.put(Material.AMETHYST, 6);
		DENSITY_MAP.put(Material.POWDER_SNOW, 3);
		DENSITY_MAP.put(Material.FROGSPAWN, 0);
		DENSITY_MAP.put(Material.FROGLIGHT, 0);
	}

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

	public static boolean isNotField(Block block) {
		return block.equals(Blocks.AIR) || block.equals(Blocks.VOID_AIR) || isFluid(block);
	}

	public static boolean isHollow(Block block) {
		return block.defaultBlockState().hasProperty(BlockStateProperties.WATERLOGGED) || HOLLOW_BLOCKS.contains(block) ||
				block instanceof PressurePlateBlock || block instanceof ButtonBlock || block instanceof FenceGateBlock ||
				block instanceof BonemealableBlock;
	}

	public static float getDensity(BlockState blockState) {
		if (DENSITY_MAP.containsKey(blockState.getMaterial())) {
			return DENSITY_MAP.get(blockState.getMaterial());
		}
		return DENSITY_MAP.get(Material.STONE);
	}
}
