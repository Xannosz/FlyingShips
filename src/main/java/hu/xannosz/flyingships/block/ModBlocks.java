package hu.xannosz.flyingships.block;

import hu.xannosz.flyingships.FlyingShips;
import hu.xannosz.flyingships.ModCreativeModeTab;
import hu.xannosz.flyingships.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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
			() -> new Rudder(BlockBehaviour.Properties.of(Material.WOOD))
	);
	public static final RegistryObject<Block> BOILER = registerBlock("boiler",
			() -> new Block(BlockBehaviour.Properties.of(Material.METAL))
	);
	public static final RegistryObject<Block> HEATER = registerBlock("heater",
			() -> new Heater(BlockBehaviour.Properties.of(Material.HEAVY_METAL).lightLevel(
					state -> state.getValue(Heater.LIT) ? 10 : 0
			))
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
