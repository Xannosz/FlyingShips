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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static hu.xannosz.flyingships.Util.*;

@Slf4j
@UtilityClass
public class JumpUtil {

	public static boolean jump(ServerLevel level, BlockPos pivotPointPosition, List<BlockPosStruct> blockPositions,
							   Vec3 additional, boolean copyMode) {
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
				return false;
			}

			//get removable fluids
			final Set<BlockPos> removableFluids = new HashSet<>();
			final Set<BlockPos> waterTagged = new HashSet<>();
			getRemovableFluids(level, rectangles, additional, sourceShell, removableFluids, waterTagged);

			if (!copyMode) {
				//delete structure
				deleteStructure(rectangles, level);
				//delete inner shell with update
				sourceInnerShell.forEach(blockPos -> {
					level.setBlock(blockPos, Blocks.WATER.defaultBlockState().setValue(BlockStateProperties.LEVEL, 1), 5);
					level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 5);
				});
			}

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
			try {
				blockUpdates(level, sourceShell);
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
				//TODO not a good solution
			}
			try {
				blockUpdates(level, targetShell);
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
				//TODO not a good solution
			}

			//reset chunks force load
			resetChunkForceLoad(chunks, level);

			//save chunks
			saveChunks(chunks);
			return true;
		} catch (Exception ex) {
			log.error("Exception during jump", ex);
		}
		return false;
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
		for (AbsoluteRectangleData rectangleData : rectangles) {
			rectangleData.setStructureBlocks(new HashMap<>());
			rectangleData.setPostStructureBlocks(new HashMap<>());
			rectangleData.setStructureBlockEntities(new HashMap<>());
			for (int x = rectangleData.getNorthWestCorner().getX(); x <= rectangleData.getSouthEastCorner().getX(); x++) {
				for (int y = rectangleData.getNorthWestCorner().getY(); y <= rectangleData.getSouthEastCorner().getY(); y++) {
					for (int z = rectangleData.getNorthWestCorner().getZ(); z <= rectangleData.getSouthEastCorner().getZ(); z++) {
						final BlockPos position = new BlockPos(x, y, z);
						final BlockEntity blockEntity = level.getBlockEntity(position);
						final BlockState state = level.getBlockState(position);

						if (PRE_PROCESS.contains(state.getBlock())) {
							rectangleData.getPostStructureBlocks().put(position, state);
						} else {
							rectangleData.getStructureBlocks().put(position, state);
						}

						if (blockEntity != null) {
							rectangleData.getStructureBlockEntities().put(position, blockEntity);
						}
					}
				}
			}
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
			for (Map.Entry<BlockPos, BlockState> blocks : rectangleData.getStructureBlocks().entrySet()) {
				level.setBlock(blocks.getKey().offset(additional.x(), additional.y(), additional.z()),
						blocks.getValue(), 2, 0);
			}
			for (Map.Entry<BlockPos, BlockState> blocks : rectangleData.getPostStructureBlocks().entrySet()) {
				level.setBlock(blocks.getKey().offset(additional.x(), additional.y(), additional.z()),
						blocks.getValue(), 2, 0);
			}
			for (Map.Entry<BlockPos, BlockEntity> blockEntity : rectangleData.getStructureBlockEntities().entrySet()) {
				final BlockPos pos = blockEntity.getKey().offset(additional.x(), additional.y(), additional.z());

				try {
					Field fx = Vec3i.class.getDeclaredField(BLOCK_POS_X_FIELD_NAME);
					fx.setAccessible(true);
					fx.set(blockEntity.getValue().getBlockPos(), pos.getX());

					Field fy = Vec3i.class.getDeclaredField(BLOCK_POS_Y_FIELD_NAME);
					fy.setAccessible(true);
					fy.set(blockEntity.getValue().getBlockPos(), pos.getY());

					Field fz = Vec3i.class.getDeclaredField(BLOCK_POS_Z_FIELD_NAME);
					fz.setAccessible(true);
					fz.set(blockEntity.getValue().getBlockPos(), pos.getZ());
				} catch (Exception e) {
					log.error("BlockEntity loading failed", e);
					log.error("Bugging BlockEntity on {}", pos);
				}

				level.setBlockEntity(blockEntity.getValue());
				blockEntity.getValue().setChanged();
			}
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
		chunks.forEach((chunk, isForced) -> level.setChunkForced(chunk.getPos().x, chunk.getPos().z, isForced));
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

	public static void useRune(ServerLevel level, BlockPos source, BlockPos target) {
		try {
			final AbsoluteRectangleData sourceData = new AbsoluteRectangleData();
			sourceData.setNorthWestCorner(new BlockPos(source.getX() - 1, source.getY() - 1, source.getZ() - 1));
			sourceData.setSouthEastCorner(new BlockPos(source.getX() + 2, source.getY() + 2, source.getZ() + 2));
			sourceData.setStructureSize(new Vec3i(5, 5, 5));

			final AbsoluteRectangleData targetData = new AbsoluteRectangleData();
			targetData.setNorthWestCorner(new BlockPos(target.getX() - 1, target.getY() - 1, target.getZ() - 1));
			targetData.setSouthEastCorner(new BlockPos(target.getX() + 2, target.getY() + 2, target.getZ() + 2));
			targetData.setStructureSize(new Vec3i(5, 5, 5));

			final Vec3 sourceAdditional = new Vec3(target.getX() - source.getX(), target.getY() - source.getY(), target.getZ() - source.getZ());
			final Vec3 targetAdditional = new Vec3(source.getX() - target.getX(), source.getY() - target.getY(), source.getZ() - target.getZ());

			//save entities
			final Map<Entity, Vec3> entities = getEntities(Collections.singletonList(sourceData), sourceAdditional, level);
			entities.putAll(getEntities(Collections.singletonList(targetData), targetAdditional, level));
			//save players
			final Map<ServerPlayer, Vec3> players = getPlayers(Collections.singletonList(sourceData), sourceAdditional, level);
			players.putAll(getPlayers(Collections.singletonList(targetData), targetAdditional, level));
			//get chunks
			final Map<LevelChunk, Boolean> chunks = getChunks(Collections.singletonList(sourceData), sourceAdditional, level); // target chunks added automatically

			//sound effect
			playSoundEffect(source, players, level.getPlayers((serverPlayer -> true)));
			//sound effect
			playSoundEffect(target, players, level.getPlayers((serverPlayer -> true)));
			//effects on player
			addEffectsToPlayers(players);

			//force load chunks
			forceLoadChunks(chunks, level);

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

			//save chunks
			saveChunks(chunks);
		} catch (Exception ex) {
			log.error("Exception during rune usage", ex);
		}
	}
}
