package hu.xannosz.flyingships.warp.jump;

import hu.xannosz.flyingships.networking.ModMessages;
import hu.xannosz.flyingships.networking.PlaySoundPacket;
import hu.xannosz.flyingships.warp.AbsoluteRectangleData;
import hu.xannosz.flyingships.warp.BlockPosStruct;
import hu.xannosz.flyingships.warp.vehiclescan.VehicleScanUtil;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Collectors;

import static hu.xannosz.flyingships.Util.*;

@Slf4j
@UtilityClass
public class JumpUtil {

	public static void jump(ServerLevel level, BlockPos pivotPointPosition, List<BlockPosStruct> blockPositions, Vec3 additional) {
		try {
			//create absolute coordinates
			final List<AbsoluteRectangleData> rectangles = createRectangles(pivotPointPosition, blockPositions);

			//save structure
			saveStructure(rectangles, level);
			//save entities
			final Map<Entity, Vec3> entities = getEntities(rectangles, additional, level);
			//save players
			final Map<ServerPlayer, Vec3> players = getPlayers(rectangles, additional, level);
			//get chunks
			final Map<LevelChunk, Boolean> chunks = getChunks(rectangles, additional, level);

			//get shell
			final Set<BlockPos> sourceShell = getShell(rectangles, true);
			//target shell
			final Set<BlockPos> targetShell = sourceShell.stream().map(
					blockPos -> blockPos.offset(additional.x, additional.y, additional.z)).collect(Collectors.toSet());
			//source inner shell
			final Set<BlockPos> sourceInnerShell = getShell(rectangles, false);

			//sound effect
			playSoundEffect(pivotPointPosition, players, level.getPlayers((serverPlayer -> true)));
			//effects on player
			addEffectsToPlayers(players);

			//force load chunks
			forceLoadChunks(chunks, level);
			//check collision -> send error message and return
			if (hasCollisionOnTarget(rectangles, additional, level)) {
				sendErrorMessage(players);
				return;
			}

			//get removable fluids
			final Set<BlockPos> removableFluids = new HashSet<>();
			final Set<BlockPos> waterTagged = new HashSet<>();
			getRemovableFluids(level, rectangles, additional, sourceShell, removableFluids, waterTagged);

			//delete structure
			deleteStructure(rectangles, level);
			//delete inner shell with update
			sourceInnerShell.forEach(blockPos -> {
				level.setBlock(blockPos, Blocks.WATER.defaultBlockState().setValue(BlockStateProperties.LEVEL, 1), 5);
				level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 5);
			});

			//clean place
			cleanPlace(rectangles, additional, level);
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

			//remove fluids
			removableFluids.forEach(fluid -> deleteBlock(level, fluid));
			waterTagged.forEach(waterTag -> level.setBlock(waterTag, level.getBlockState(waterTag).setValue(BlockStateProperties.WATERLOGGED, false), 2, 0));

			//start block updates
			blockUpdates(level, sourceShell);
			blockUpdates(level, targetShell);

			//reset chunks force load
			resetChunkForceLoad(chunks, level);

			//save chunks
			saveChunks(chunks);
		} catch (Exception ex) {
			log.error("Exception during jump, whit ship", ex);
		}
	}

	public static List<AbsoluteRectangleData> createRectangles(BlockPos pivotPointPosition, List<BlockPosStruct> blockPositions) {
		List<AbsoluteRectangleData> absoluteRectangleDataList = new ArrayList<>();
		for (BlockPosStruct struct : blockPositions) {
			final BlockPos northWestCorner = new BlockPos(pivotPointPosition.getX() + struct.getPosition1().getX(), pivotPointPosition.getY() + struct.getPosition1().getY(), pivotPointPosition.getZ() + struct.getPosition1().getZ());
			final BlockPos southEastCorner = new BlockPos(pivotPointPosition.getX() + struct.getPosition2().getX(), pivotPointPosition.getY() + struct.getPosition2().getY(), pivotPointPosition.getZ() + struct.getPosition2().getZ());
			final Vec3i structureSize = new Vec3i(struct.getPosition2().getX() - struct.getPosition1().getX() + 1, struct.getPosition2().getY() - struct.getPosition1().getY() + 1, struct.getPosition2().getZ() - struct.getPosition1().getZ() + 1);

			AbsoluteRectangleData absoluteRectangleData = new AbsoluteRectangleData();
			absoluteRectangleData.setNorthWestCorner(northWestCorner);
			absoluteRectangleData.setSouthEastCorner(southEastCorner);
			absoluteRectangleData.setStructureSize(structureSize);

			absoluteRectangleDataList.add(absoluteRectangleData);
		}


		return absoluteRectangleDataList;
	}

	private static void saveStructure(List<AbsoluteRectangleData> rectangles, ServerLevel level) {
		int i = 0;
		final StructureTemplateManager structuretemplatemanager = level.getStructureManager();

		for (AbsoluteRectangleData rectangleData : rectangles) {
			final StructureTemplate structuretemplate = structuretemplatemanager.getOrCreate(new ResourceLocation(UUID.randomUUID().toString().toLowerCase(Locale.ROOT).replace(" ", "_") + (i++)));
			structuretemplate.fillFromWorld(level, rectangleData.getNorthWestCorner(), rectangleData.getStructureSize(), false, Blocks.STRUCTURE_VOID);
			rectangleData.setStructuretemplate(structuretemplate);
		}
	}

	public static Map<Entity, Vec3> getEntities(List<AbsoluteRectangleData> rectangles, Vec3 additional, ServerLevel level) {

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

	public static Map<ServerPlayer, Vec3> getPlayers(List<AbsoluteRectangleData> rectangles, Vec3 additional, ServerLevel level) {
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

	private static void playSoundEffect(BlockPos pivotPointPosition, Map<ServerPlayer, Vec3> players, List<ServerPlayer> serverPlayers) {
		for (ServerPlayer player : serverPlayers) {
			ModMessages.sendToPlayer(new PlaySoundPacket(pivotPointPosition, players.containsKey(player)), player);
		}
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
						if (!level.getBlockState(state).getBlock().equals(Blocks.AIR) &&
								!isFluid(level.getBlockState(state).getBlock())) {
							target.add(state);
						}
					}
				}
			}
		}

		target.removeAll(source);

		return !target.isEmpty();
	}

	private static void sendErrorMessage(Map<ServerPlayer, Vec3> players) {
		players.keySet().forEach(player ->
				player.sendSystemMessage(Component.translatable("message.collisionWarning")));
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

	private static void cleanPlace(List<AbsoluteRectangleData> rectangles, Vec3 additional, ServerLevel level) {
		for (AbsoluteRectangleData rectangleData : rectangles) {
			for (int x = 0; x < rectangleData.getStructureSize().getX(); x++) {
				for (int y = 0; y < rectangleData.getStructureSize().getY(); y++) {
					for (int z = 0; z < rectangleData.getStructureSize().getZ(); z++) {
						BlockPos blockPos = new BlockPos(
								rectangleData.getNorthWestCorner().getX() + x + additional.x,
								rectangleData.getNorthWestCorner().getY() + y + additional.y,
								rectangleData.getNorthWestCorner().getZ() + z + additional.z);
						deleteBlock(level, blockPos);
					}
				}
			}
		}
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

	private static void getRemovableFluids(ServerLevel level, List<AbsoluteRectangleData> rectangles, Vec3 additional,
										   Set<BlockPos> sourceShell, Set<BlockPos> removableFluids, Set<BlockPos> waterTagged) {
		for (BlockPos blockPos : VehicleScanUtil.getFluidsRecursive(level, rectangles, sourceShell)) {
			if (isFluid(level.getBlockState(blockPos).getBlock()) &&
					!isFluid(level.getBlockState(blockPos.offset(new Vec3i(additional.x, additional.y, additional.z))).getBlock())) {
				removableFluids.add(blockPos.offset(new Vec3i(additional.x, additional.y, additional.z)));
			}
			if (isFluidTagged(level.getBlockState(blockPos))) {
				waterTagged.add(blockPos.offset(new Vec3i(additional.x, additional.y, additional.z)));
			}
		}
	}

	private static void blockUpdates(ServerLevel level, Set<BlockPos> shell) {
		for (BlockPos pos : shell) {
			level.updateNeighborsAt(pos, null);
		}
	}

	private static void resetChunkForceLoad(Map<LevelChunk, Boolean> chunks, ServerLevel level) {
		chunks.forEach((chunk, isForced) ->
				level.setChunkForced(chunk.getPos().x, chunk.getPos().z, isForced));
	}

	private static void saveChunks(Map<LevelChunk, Boolean> chunks) {
		chunks.forEach((chunk, isForced) -> chunk.setUnsaved(true));
	}

	private static Set<BlockPos> getShell(List<AbsoluteRectangleData> rectangleDataList, boolean outer) {
		Set<BlockPos> result = new HashSet<>();
		for (AbsoluteRectangleData rectangleData : rectangleDataList) {
			int minX = rectangleData.getNorthWestCorner().getX() - (outer ? 1 : 0);
			int maxX = rectangleData.getSouthEastCorner().getX() + (outer ? 1 : 0);
			int minY = rectangleData.getNorthWestCorner().getY() - (outer ? 1 : 0);
			int maxY = rectangleData.getSouthEastCorner().getY() + (outer ? 1 : 0);
			int minZ = rectangleData.getNorthWestCorner().getZ() - (outer ? 1 : 0);
			int maxZ = rectangleData.getSouthEastCorner().getZ() + (outer ? 1 : 0);

			for (int y = rectangleData.getNorthWestCorner().getY(); y <= rectangleData.getSouthEastCorner().getY(); y++) {
				for (int z = rectangleData.getNorthWestCorner().getZ(); z <= rectangleData.getSouthEastCorner().getZ(); z++) {
					result.add(new BlockPos(minX, y, z));
					result.add(new BlockPos(maxX, y, z));
				}
			}

			for (int x = rectangleData.getNorthWestCorner().getX(); x <= rectangleData.getSouthEastCorner().getX(); x++) {
				for (int y = rectangleData.getNorthWestCorner().getY(); y <= rectangleData.getSouthEastCorner().getY(); y++) {
					result.add(new BlockPos(x, y, minZ));
					result.add(new BlockPos(x, y, maxZ));
				}
			}

			for (int x = rectangleData.getNorthWestCorner().getX(); x <= rectangleData.getSouthEastCorner().getX(); x++) {
				for (int z = rectangleData.getNorthWestCorner().getZ(); z <= rectangleData.getSouthEastCorner().getZ(); z++) {
					result.add(new BlockPos(x, minY, z));
					result.add(new BlockPos(x, maxY, z));
				}
			}
		}

		return result;
	}
}
