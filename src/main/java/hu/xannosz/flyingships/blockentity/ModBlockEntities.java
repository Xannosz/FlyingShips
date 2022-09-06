package hu.xannosz.flyingships.blockentity;

import hu.xannosz.flyingships.FlyingShips;
import hu.xannosz.flyingships.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
			DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FlyingShips.MOD_ID);

	public static final RegistryObject<BlockEntityType<RudderBlockEntity>> RUDDER_BLOCK_ENTITY =
			BLOCK_ENTITIES.register("rudder_block_entity", () ->
					BlockEntityType.Builder.of(
							RudderBlockEntity::new,
							ModBlocks.RUDDER.get()).build(null));


	public static void register(IEventBus eventBus) {
		BLOCK_ENTITIES.register(eventBus);
	}
}
