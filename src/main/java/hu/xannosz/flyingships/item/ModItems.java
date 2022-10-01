package hu.xannosz.flyingships.item;

import hu.xannosz.flyingships.FlyingShips;
import hu.xannosz.flyingships.ModCreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FlyingShips.MOD_ID);

	public static final RegistryObject<Item> WAND = ITEMS.register("wand",
			() -> new Wand(new Item.Properties().tab(ModCreativeModeTab.FLYING_SHIPS_TAB).stacksTo(1)));

	public static final RegistryObject<Item> CRYSTAL_OF_LEVITATION = ITEMS.register("crystal_of_levitation",
			() -> new CrystalOfLevitation(new Item.Properties().tab(ModCreativeModeTab.FLYING_SHIPS_TAB).stacksTo(16)));

	public static void register(IEventBus eventBus) {
		ITEMS.register(eventBus);
	}
}
