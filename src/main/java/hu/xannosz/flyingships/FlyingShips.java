package hu.xannosz.flyingships;

import hu.xannosz.flyingships.block.ModBlocks;
import hu.xannosz.flyingships.blockentity.ModBlockEntities;
import hu.xannosz.flyingships.config.FlyingShipsConfiguration;
import hu.xannosz.flyingships.item.ModItems;
import hu.xannosz.flyingships.loot.ModLootTables;
import hu.xannosz.flyingships.networking.ModMessages;
import hu.xannosz.flyingships.screen.MarkerScreen;
import hu.xannosz.flyingships.screen.ModMenuTypes;
import hu.xannosz.flyingships.screen.RudderScreen;
import hu.xannosz.flyingships.screen.RuneScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(FlyingShips.MOD_ID)
public class FlyingShips {
	public static final String MOD_ID = "flyingships";

	public FlyingShips() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		ModItems.register(modEventBus);
		ModBlocks.register(modEventBus);
		ModBlockEntities.register(modEventBus);
		ModMenuTypes.register(modEventBus);
		ModLootTables.register(modEventBus);

		modEventBus.addListener(this::commonSetup);

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FlyingShipsConfiguration.SPEC, MOD_ID + ".toml");

		MinecraftForge.EVENT_BUS.register(this);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		ModMessages.register();
	}

	@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ClientModEvents {
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			MenuScreens.register(ModMenuTypes.RUDDER_MENU.get(), RudderScreen::new);
			MenuScreens.register(ModMenuTypes.MARKER_MENU.get(), MarkerScreen::new);
			MenuScreens.register(ModMenuTypes.RUNE_MENU.get(), RuneScreen::new);
		}
	}
}
