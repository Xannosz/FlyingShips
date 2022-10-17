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
	public static final RegistryObject<BlockEntityType<ItemGateBlockEntity>> ITEM_GATE_BLOCK_ENTITY =
			BLOCK_ENTITIES.register("item_gate_block_entity", () ->
					BlockEntityType.Builder.of(
							ItemGateBlockEntity::new,
							ModBlocks.ITEM_GATE.get()).build(null));
	public static final RegistryObject<BlockEntityType<SubRudderBlockEntity>> SUB_RUDDER_BLOCK_ENTITY =
			BLOCK_ENTITIES.register("sub_rudder_block_entity", () ->
					BlockEntityType.Builder.of(
							SubRudderBlockEntity::new,
							ModBlocks.SUB_RUDDER.get()).build(null));
	public static final RegistryObject<BlockEntityType<MarkerBlockEntity>> MARKER_BLOCK_ENTITY =
			BLOCK_ENTITIES.register("marker_block_entity", () ->
					BlockEntityType.Builder.of(
							MarkerBlockEntity::new,
							ModBlocks.MARKER.get()).build(null));

	public static final RegistryObject<BlockEntityType<RuneBlockEntity>> RUNE_BLOCK_ENTITY =
			BLOCK_ENTITIES.register("rune_block_entity", () ->
					BlockEntityType.Builder.of(
							RuneBlockEntity::new,
							ModBlocks.RUNE.get()).build(null));


	public static void register(IEventBus eventBus) {
		BLOCK_ENTITIES.register(eventBus);
	}
}
