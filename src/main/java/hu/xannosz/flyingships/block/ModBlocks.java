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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FlyingShips.MOD_ID);

	public static final RegistryObject<Block> RUDDER = registerBlock("rudder",
			() -> new Rudder(BlockBehaviour.Properties.of(Material.WOOD).strength(1.5F, 6.0F).sound(SoundType.WOOD))
	);

	public static final RegistryObject<Block> SUB_RUDDER = registerBlock("sub_rudder",
			() -> new SubRudder(BlockBehaviour.Properties.of(Material.WOOD).strength(1.5F, 6.0F).sound(SoundType.WOOD))
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
			() -> new EnderOscillator(BlockBehaviour.Properties.of(Material.GLASS).lightLevel(state -> 12).noOcclusion().strength(1.5F, 6.0F).sound(SoundType.GLASS))
	);

	public static final RegistryObject<Block> MARKER = registerBlock("marker",
			() -> new Marker(BlockBehaviour.Properties.of(Material.WOOD).lightLevel(state -> 12).noOcclusion().strength(1.5F, 6.0F).sound(SoundType.WOOD))
	);

	public static final RegistryObject<Block> ITEM_GATE = registerBlock("item_gate",
			() -> new ItemGate(BlockBehaviour.Properties.of(Material.METAL).strength(1.5F, 6.0F).sound(SoundType.WOOD))
	);

	public static final RegistryObject<Block> ARTIFICIAL_FLOATER = registerBlock("artificial_floater",
			() -> new Block(BlockBehaviour.Properties.of(Material.GLASS).noOcclusion().strength(1.5F, 6.0F).sound(SoundType.GLASS))
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
