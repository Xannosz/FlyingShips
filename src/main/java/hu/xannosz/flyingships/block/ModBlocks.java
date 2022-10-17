package hu.xannosz.flyingships.block;

import hu.xannosz.flyingships.FlyingShips;
import hu.xannosz.flyingships.ModCreativeModeTab;
import hu.xannosz.flyingships.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FlyingShips.MOD_ID);

	public static final RegistryObject<Block> RUDDER = registerBlock("rudder",
			() -> new Rudder(BlockBehaviour.Properties.of(Material.WOOD).strength(1.5F, 6.0F).sound(SoundType.WOOD).noOcclusion())
	);

	public static final RegistryObject<Block> SUB_RUDDER = registerBlock("sub_rudder",
			() -> new SubRudder(BlockBehaviour.Properties.of(Material.WOOD).strength(1.5F, 6.0F).sound(SoundType.WOOD).noOcclusion())
	);

	public static final RegistryObject<Block> TANK = registerBlock("tank",
			() -> new Block(BlockBehaviour.Properties.of(Material.METAL).strength(1.5F, 6.0F).sound(SoundType.METAL))
	);

	public static final RegistryObject<Block> HEATER = registerBlock("heater",
			() -> new Heater(BlockBehaviour.Properties.of(Material.HEAVY_METAL).lightLevel(
					state -> state.getValue(Heater.LIT) ? 10 : 0
			).strength(1.5F, 6.0F).sound(SoundType.METAL))
	);

	public static final RegistryObject<Block> HYPER_DRIVE_CORE = registerBlock("hyper_drive_core",
			() -> new Block(BlockBehaviour.Properties.of(Material.METAL).lightLevel(state -> 12).noOcclusion().strength(1.5F, 6.0F).sound(SoundType.STONE))
	);

	public static final RegistryObject<Block> ENDER_OSCILLATOR = registerBlock("ender_oscillator",
			() -> new ShapedBlock(BlockBehaviour.Properties.of(Material.GLASS).lightLevel(state -> 12).noOcclusion().strength(1.5F, 6.0F).sound(SoundType.GLASS),
					Shapes.or(
							Block.box(2, 0, 2, 14, 1, 14),
							Block.box(6, 1, 6, 10, 3, 10),

							Block.box(7, 1, 7, 9, 14, 9),
							Block.box(5, 9, 5, 11, 15, 11),

							Block.box(6, 8, 6, 10, 9, 10),
							Block.box(6, 15, 6, 10, 16, 10),
							Block.box(6, 10, 4, 10, 14, 5),
							Block.box(6, 10, 11, 10, 14, 12),
							Block.box(4, 10, 6, 5, 14, 10),
							Block.box(11, 10, 6, 12, 14, 10)
					))
	);

	public static final RegistryObject<Block> MARKER = registerBlock("marker",
			() -> new Marker(BlockBehaviour.Properties.of(Material.WOOD).lightLevel(state -> 12).noOcclusion().strength(1.5F, 6.0F).sound(SoundType.WOOD))
	);

	public static final RegistryObject<Block> ITEM_GATE = registerBlock("item_gate",
			() -> new ItemGate(BlockBehaviour.Properties.of(Material.METAL).strength(1.5F, 6.0F).sound(SoundType.WOOD))
	);

	public static final RegistryObject<Block> ARTIFICIAL_FLOATER = registerBlock("artificial_floater",
			() -> new ShapedBlock(BlockBehaviour.Properties.of(Material.GLASS).noOcclusion().strength(1.5F, 6.0F).sound(SoundType.GLASS),
					Shapes.or(
							Block.box(2, 0, 2, 14, 1, 14),
							Block.box(3, 1, 3, 13, 16, 13)
					))
	);

	@SuppressWarnings("unused")
	public static final RegistryObject<Block> COOPER_TRAPDOOR = registerBlock("cooper_trapdoor",
			() -> new ModdedTrapDoorBlock(BlockBehaviour.Properties.of(Material.WOOD)
					.strength(5f).noOcclusion()));

	public static final RegistryObject<Block> RUNE = registerBlock("rune",
			() -> new Rune(BlockBehaviour.Properties.of(Material.WOOD).lightLevel(state -> 12).noOcclusion().strength(1.5F, 6.0F).sound(SoundType.WOOD))
	);

	private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> blockCreator) {
		RegistryObject<T> block = BLOCKS.register(name, blockCreator);
		registerBlockItem(name, block);
		return block;
	}

	private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
		ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(ModCreativeModeTab.FLYING_SHIPS_TAB)));
	}

	public static void register(IEventBus eventBus) {
		BLOCKS.register(eventBus);
	}
}
