package hu.xannosz.flyingships.loot;

import com.mojang.serialization.Codec;
import hu.xannosz.flyingships.FlyingShips;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootTables {
	public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_TABLES =
			DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, FlyingShips.MOD_ID);

	public static final RegistryObject<Codec<? extends IGlobalLootModifier>> CRYSTAL_OF_LEVITATION =
			LOOT_TABLES.register("crystal_of_levitation_modifier", () -> CrystalOfLevitationModifier.CODEC);

	public static void register(IEventBus eventBus) {
		LOOT_TABLES.register(eventBus);
	}
}
