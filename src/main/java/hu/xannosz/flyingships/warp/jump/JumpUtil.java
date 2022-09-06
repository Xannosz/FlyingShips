package hu.xannosz.flyingships.warp.jump;

import hu.xannosz.flyingships.warp.AbsoluteRectangleData;
import hu.xannosz.flyingships.warp.BlockPosStruct;
import hu.xannosz.flyingships.warp.WarpDirection;
import hu.xannosz.flyingships.warp.terrainscan.LandButtonSettings;
import hu.xannosz.flyingships.warp.terrainscan.TerrainScanResponseStruct;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.Vec3;

import java.util.*;

@Slf4j
@UtilityClass
public class JumpUtil {

	private static final List<Block> PRE_PROCESS = Arrays.asList(Blocks.REDSTONE_WIRE);
	private static final List<Block> POST_PROCESS = Arrays.asList(Blocks.PISTON_HEAD);

	public static void jump(String shipName, WarpDirection selectedWarpDirection, int speed,
							LandButtonSettings landButtonSettings, TerrainScanResponseStruct terrainScanResponse,
							ServerLevel level, BlockPos rudderBlockPosition, List<BlockPosStruct> blockPositions) {
		try {
			//create absolute coordinates
			final Vec3 additional = getAdditional(selectedWarpDirection, speed, landButtonSettings, terrainScanResponse);
			final List<AbsoluteRectangleData> rectangles = createRectangles(rudderBlockPosition, blockPositions);

			//save structure
			saveStructure(rectangles, level, shipName);
			//save entities
			final Map<Entity, Vec3> entities = getEntities(rectangles, additional, level);
			//save players
			final Map<ServerPlayer, Vec3> players = getPlayers(rectangles, additional, level);
			//get chunks
			final Map<LevelChunk, Boolean> chunks = getChunks(rectangles, additional, level);

			//sound effect
			playSoundEffect(level, rudderBlockPosition, players);
			//effects on player
			addEffectsToPlayers(players);

			//force load chunks
			forceLoadChunks(chunks, level);
			//check collision -> send error message and return
			if (hasCollisionOnTarget(rectangles, additional, level)) {
				sendErrorMessage(players, shipName);
				return;
			}

			//delete structure
			deleteStructure(rectangles, level);

			//create structure
			createStructure(rectangles, additional, level);
			//update chunks (reload structure)
			updateChunks(chunks);
			//teleport entities
			teleportEntities(entities);
			//teleport players
			teleportPlayers(players);
			//update chunks (reload entities)
			updateChunks(chunks);

			//reset chunks force load
			resetChunkForceLoad(chunks, level);
		} catch (Exception ex) {
			log.error("Exception during jump, whit ship: " + shipName, ex);
		}
	}

	private static Vec3 getAdditional(WarpDirection selectedWarpDirection, int speed, LandButtonSettings landButtonSettings, TerrainScanResponseStruct terrainScanResponse) {
		switch (selectedWarpDirection) {
			case UP -> {
				return new Vec3(0, speed, 0);
			}
			case DOWN -> {
				return new Vec3(0, -speed, 0);
			}
			case NORTH -> {
				return new Vec3(0, 0, -speed);
			}
			case SOUTH -> {
				return new Vec3(0, 0, speed);
			}
			case EAST -> {
				return new Vec3(speed, 0, 0);
			}
			case WEST -> {
				return new Vec3(-speed, 0, 0);
			}
			case LAND -> {
				switch (landButtonSettings) {
					case VOID -> {
						return new Vec3(0, 100, 0);
					}
					case LAND -> {
						return new Vec3(0, -terrainScanResponse.getHeightOfBottom(), 0);
					}
					case TOUCH_CELLING -> {
						return new Vec3(0, terrainScanResponse.getHeightOfCelling(), 0);
					}
				}

			}
		}
		return new Vec3(0, 0, 0);
	}

	public static List<AbsoluteRectangleData> createRectangles(BlockPos rudderBlockPosition, List<BlockPosStruct> blockPositions) {
		List<AbsoluteRectangleData> absoluteRectangleDataList = new ArrayList<>();
		for (BlockPosStruct struct : blockPositions) {
			final BlockPos northWestCorner = new BlockPos(rudderBlockPosition.getX() + struct.getPosition1().getX(), rudderBlockPosition.getY() + struct.getPosition1().getY(), rudderBlockPosition.getZ() + struct.getPosition1().getZ());
			final BlockPos southEastCorner = new BlockPos(rudderBlockPosition.getX() + struct.getPosition2().getX(), rudderBlockPosition.getY() + struct.getPosition2().getY(), rudderBlockPosition.getZ() + struct.getPosition2().getZ());
			final Vec3i structureSize = new Vec3i(struct.getPosition2().getX() - struct.getPosition1().getX() + 1, struct.getPosition2().getY() - struct.getPosition1().getY() + 1, struct.getPosition2().getZ() - struct.getPosition1().getZ() + 1);

			AbsoluteRectangleData absoluteRectangleData = new AbsoluteRectangleData();
			absoluteRectangleData.setNorthWestCorner(northWestCorner);
			absoluteRectangleData.setSouthEastCorner(southEastCorner);
			absoluteRectangleData.setStructureSize(structureSize);

			absoluteRectangleDataList.add(absoluteRectangleData);
		}


		return absoluteRectangleDataList;
	}

	private static void saveStructure(List<AbsoluteRectangleData> rectangles, ServerLevel level, String shipName) {
		int i = 0;
		final StructureTemplateManager structuretemplatemanager = level.getStructureManager();

		for (AbsoluteRectangleData rectangleData : rectangles) {
			final StructureTemplate structuretemplate = structuretemplatemanager.getOrCreate(new ResourceLocation(shipName.toLowerCase(Locale.ROOT).replace(" ", "_") + (i++)));
			structuretemplate.fillFromWorld(level, rectangleData.getNorthWestCorner(), rectangleData.getStructureSize(), false, Blocks.STRUCTURE_VOID);
			rectangleData.setStructuretemplate(structuretemplate);
		}
	}

	private static Map<Entity, Vec3> getEntities(List<AbsoluteRectangleData> rectangles, Vec3 additional, ServerLevel level) {

		final Map<Entity, Vec3> result = new HashMap<>();

		level.getEntities().getAll().forEach(
				entity -> {
					Vec3 position = entity.getPosition(0.1f);
					if (isOnTheShip(rectangles, position)) {
						result.put(entity, position.add(additional));
					}
				}
		);

		return result;
	}

	private static Map<ServerPlayer, Vec3> getPlayers(List<AbsoluteRectangleData> rectangles, Vec3 additional, ServerLevel level) {
		final Map<ServerPlayer, Vec3> result = new HashMap<>();

		level.getPlayers(
				player -> {
					Vec3 position = player.getPosition(0.1f);
					return isOnTheShip(rectangles, position);
				}
		).forEach(player -> {
			Vec3 position = player.getPosition(0.1f);
			result.put(player, position.add(additional));
		});

		return result;
	}

	private static boolean isOnTheShip(List<AbsoluteRectangleData> rectangles, Vec3 position) {
		for (AbsoluteRectangleData rectangleData : rectangles) {
			boolean inBox;
			inBox = rectangleData.getNorthWestCorner().getX() <= position.x + 0.5f;
			inBox &= rectangleData.getSouthEastCorner().getX() >= position.x - 0.5f;
			inBox &= rectangleData.getNorthWestCorner().getY() <= position.y + 0.5f;
			inBox &= rectangleData.getSouthEastCorner().getY() >= position.y - 0.5f;
			inBox &= rectangleData.getNorthWestCorner().getZ() <= position.z + 0.5f;
			inBox &= rectangleData.getSouthEastCorner().getZ() >= position.z - 0.5f;

			if (inBox) {
				return true;
			}
		}
		return false;
	}

	private static Map<LevelChunk, Boolean> getChunks(List<AbsoluteRectangleData> rectangles, Vec3 additional, ServerLevel level) {
		final Map<LevelChunk, Boolean> chunks = new HashMap<>();

		final LongSet forcedChunks = level.getForcedChunks();

		for (AbsoluteRectangleData rectangleData : rectangles) {
			for (int x = 0; x < rectangleData.getStructureSize().getX(); x++) {
				for (int z = 0; z < rectangleData.getStructureSize().getZ(); z++) {
					LevelChunk sourceChunk = level.getChunkAt(
							new BlockPos(rectangleData.getNorthWestCorner().getX() + x,
									rectangleData.getNorthWestCorner().getY(),
									rectangleData.getNorthWestCorner().getZ() + z));
					chunks.put(sourceChunk, forcedChunks.contains(sourceChunk.getPos().toLong()));

					LevelChunk targetChunk = level.getChunkAt(
							new BlockPos(rectangleData.getNorthWestCorner().getX() + x + additional.x,
									rectangleData.getNorthWestCorner().getY() + additional.y,
									rectangleData.getNorthWestCorner().getZ() + z + additional.z));
					chunks.put(targetChunk, forcedChunks.contains(targetChunk.getPos().toLong()));
				}
			}
		}

		return chunks;
	}

	private static void playSoundEffect(ServerLevel level, BlockPos rudderBlockPosition, Map<ServerPlayer, Vec3> players) {
		level.playLocalSound((double) rudderBlockPosition.getX() + 0.5D, (double) rudderBlockPosition.getY() + 0.5D, (double) rudderBlockPosition.getZ() + 0.5D,
				SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.5F, RandomSource.create().nextFloat() * 0.4F + 0.8F, false);
		players.keySet().forEach(player -> {
			player.playSound(SoundEvents.ENDERMAN_TELEPORT);
		});
	}

	private static void addEffectsToPlayers(Map<ServerPlayer, Vec3> players) {
		players.keySet().forEach(player -> { // 20 -> 1 second
			player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 16));
			player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 8));
		});
	}

	private static void forceLoadChunks(Map<LevelChunk, Boolean> chunks, ServerLevel level) {
		for (LevelChunk chunk : chunks.keySet()) {
			level.setChunkForced(chunk.getPos().x, chunk.getPos().z, true);
		}
	}

	private static boolean hasCollisionOnTarget(List<AbsoluteRectangleData> rectangles, Vec3 additional, ServerLevel level) {
		Set<BlockPos> source = new HashSet<>();
		Set<BlockPos> target = new HashSet<>();
		for (AbsoluteRectangleData rectangleData : rectangles) {
			for (int x = 0; x < rectangleData.getStructureSize().getX(); x++) {
				for (int y = 0; y < rectangleData.getStructureSize().getY(); y++) {
					for (int z = 0; z < rectangleData.getStructureSize().getZ(); z++) {
						source.add(new BlockPos(
								rectangleData.getNorthWestCorner().getX() + x,
								rectangleData.getNorthWestCorner().getY() + y,
								rectangleData.getNorthWestCorner().getZ() + z));
						BlockPos state = new BlockPos(
								rectangleData.getNorthWestCorner().getX() + x + additional.x,
								rectangleData.getNorthWestCorner().getY() + y + additional.y,
								rectangleData.getNorthWestCorner().getZ() + z + additional.z);
						if (!level.getBlockState(state)
								.getBlock().equals(Blocks.AIR)) {
							target.add(state);
						}
					}
				}
			}
		}

		target.removeAll(source);

		return !target.isEmpty();
	}

	private static void sendErrorMessage(Map<ServerPlayer, Vec3> players, String shipName) {
		players.keySet().forEach(player ->
				player.sendSystemMessage(Component.translatable("message.collisionWarning", shipName)));
	}

	private static void deleteStructure(List<AbsoluteRectangleData> rectangles, ServerLevel level) {
		final Set<BlockPos> pre = new HashSet<>();
		final Set<BlockPos> main = new HashSet<>();
		final Set<BlockPos> post = new HashSet<>();

		for (AbsoluteRectangleData rectangleData : rectangles) {
			for (int x = 0; x < rectangleData.getStructureSize().getX(); x++) {
				for (int y = 0; y < rectangleData.getStructureSize().getY(); y++) {
					for (int z = 0; z < rectangleData.getStructureSize().getZ(); z++) {
						BlockPos blockPos = new BlockPos(
								rectangleData.getNorthWestCorner().getX() + x,
								rectangleData.getNorthWestCorner().getY() + y,
								rectangleData.getNorthWestCorner().getZ() + z);

						Block block = level.getBlockState(blockPos).getBlock();
						if (PRE_PROCESS.contains(block)) {
							pre.add(blockPos);
						} else if (POST_PROCESS.contains(block)) {
							post.add(blockPos);
						} else {
							main.add(blockPos);
						}
					}
				}
			}
		}

		pre.forEach(blockPos -> deleteBlock(level, blockPos));
		main.forEach(blockPos -> deleteBlock(level, blockPos));
		post.forEach(blockPos -> deleteBlock(level, blockPos));
	}

	private static void deleteBlock(ServerLevel level, BlockPos blockPos) {
		level.removeBlockEntity(blockPos);
		level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2, 0);
	}

	private static void createStructure(List<AbsoluteRectangleData> rectangles, Vec3 additional, ServerLevel level) {
		for (AbsoluteRectangleData rectangleData : rectangles) {
			final StructurePlaceSettings structureplacesettings = (new StructurePlaceSettings()).setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);
			structureplacesettings.clearProcessors().addProcessor(new BlockRotProcessor(Mth.clamp(1.0F, 0.0F, 1.0F))).setRandom(RandomSource.create());

			BlockPos blockPos1 = rectangleData.getNorthWestCorner().offset(new BlockPos(additional));
			rectangleData.getStructuretemplate().placeInWorld(level, blockPos1, blockPos1, structureplacesettings, RandomSource.create(), 2);
		}
	}

	private static void updateChunks(Map<LevelChunk, Boolean> chunks) {
		for (LevelChunk chunk : chunks.keySet()) {
			chunk.runPostLoad();
		}
	}

	private static void teleportEntities(Map<Entity, Vec3> entities) {
		entities.forEach(
				(entity, target) -> entity.teleportTo(target.x, target.y, target.z)
		);
	}

	private static void teleportPlayers(Map<ServerPlayer, Vec3> players) {
		players.forEach(
				(player, target) -> player.teleportTo(target.x, target.y, target.z)
		);
	}

	private static void resetChunkForceLoad(Map<LevelChunk, Boolean> chunks, ServerLevel level) {
		chunks.forEach((chunk, isForced) ->
				level.setChunkForced(chunk.getPos().x, chunk.getPos().z, isForced));
	}
}
