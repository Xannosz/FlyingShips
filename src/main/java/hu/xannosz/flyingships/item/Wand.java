package hu.xannosz.flyingships.item;

import hu.xannosz.flyingships.block.ModBlocks;
import hu.xannosz.flyingships.blockentity.RudderBlockEntity;
import hu.xannosz.flyingships.blockentity.SubRudderBlockEntity;
import hu.xannosz.flyingships.networking.AddRectanglePacket;
import hu.xannosz.flyingships.networking.ConnectToRudder;
import hu.xannosz.flyingships.networking.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Wand extends Item {

	private static final String SHIP_UUID_TAG = "shipUUID";
	private static final String SHIP_RUDDER_POSITION_TAG = "shipRudderPosition";
	private static final String FIRST_POSITION_TAG = "firstPosition";

	public Wand(Properties properties) {
		super(properties);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext useOnContext) {

		if (!useOnContext.getLevel().isClientSide()) {
			return InteractionResult.PASS;
		}
		if (useOnContext.getPlayer() == null) {
			return InteractionResult.FAIL;
		}

		final ItemStack itemStack = useOnContext.getItemInHand();
		final Tag rudderPositionTag = itemStack.getOrCreateTag().get(SHIP_RUDDER_POSITION_TAG);
		final Tag firstPositionTag = itemStack.getOrCreateTag().get(FIRST_POSITION_TAG);

		final Player player = useOnContext.getPlayer();
		final BlockPos clickedPosition = useOnContext.getClickedPos();
		final Block block = player.level.getBlockState(clickedPosition).getBlock();
		final String shipUUID = itemStack.getOrCreateTag().getString(SHIP_UUID_TAG);
		final BlockPos rudderPosition = rudderPositionTag == null ? null : NbtUtils.readBlockPos((CompoundTag) rudderPositionTag);
		final BlockPos firstPosition = firstPositionTag == null ? null : NbtUtils.readBlockPos((CompoundTag) firstPositionTag);

		final WandMode mode = calculateWandMode(block, rudderPosition, firstPosition, useOnContext.getPlayer().isShiftKeyDown(), clickedPosition, player);

		if (!isValidContext(player, clickedPosition, block, shipUUID, rudderPosition, firstPosition, mode)) {
			return InteractionResult.FAIL;
		}

		switch (mode) {
			case SELECT_SHIP -> {
				BlockEntity entity = player.level.getBlockEntity(clickedPosition);

				if (!(entity instanceof RudderBlockEntity)) {
					return InteractionResult.FAIL;
				}

				itemStack.getOrCreateTag().putString(SHIP_UUID_TAG, ((RudderBlockEntity) entity).getUuid());
				itemStack.getOrCreateTag().put(SHIP_RUDDER_POSITION_TAG, NbtUtils.writeBlockPos(clickedPosition));
				player.sendSystemMessage(Component.translatable("message.shipNameSet"));
			}
			case SELECT_FIRST_POS -> {
				itemStack.getOrCreateTag().put(FIRST_POSITION_TAG, NbtUtils.writeBlockPos(clickedPosition));
				player.sendSystemMessage(Component.translatable("message.pos1Set", clickedPosition.toShortString()));

			}
			case ADD_REC -> {
				if (firstPosition == null) {
					return InteractionResult.FAIL;
				}

				ModMessages.sendToServer(new AddRectanglePacket(rudderPosition, 0, firstPosition, clickedPosition));
				player.sendSystemMessage(Component.translatable("message.rectangleSet", firstPosition.toShortString(), clickedPosition.toShortString()));
				itemStack.getOrCreateTag().remove(FIRST_POSITION_TAG);
			}
			case CONNECT -> {
				if (rudderPosition == null) {
					return InteractionResult.FAIL;
				}

				ModMessages.sendToServer(new ConnectToRudder(clickedPosition, rudderPosition));
				player.sendSystemMessage(Component.translatable("message.connectedToRudder", rudderPosition.toShortString()));
			}
			case STRUCTURE_CHANGE -> player.sendSystemMessage(Component.literal("STRUCTURE_CHANGE not implemented"));
			case DELETION -> {
				if (itemStack.getOrCreateTag().contains(FIRST_POSITION_TAG)) {
					itemStack.getOrCreateTag().remove(FIRST_POSITION_TAG);
					player.sendSystemMessage(Component.translatable("message.pos1Deleted"));
					break;
				}

				itemStack.getOrCreateTag().remove(SHIP_RUDDER_POSITION_TAG);
				itemStack.getOrCreateTag().remove(SHIP_UUID_TAG);
				player.sendSystemMessage(Component.translatable("message.shipNameDeleted"));
			}
		}

		return InteractionResult.SUCCESS;
	}

	private WandMode calculateWandMode(Block block, BlockPos rudderPosition, BlockPos firstPosition, boolean isShiftDown, BlockPos clickedPosition, Player player) {
		if (rudderPosition == null) {
			return WandMode.SELECT_SHIP;
		}
		if (block.equals(ModBlocks.RUDDER.get()) ||
				(block.equals(ModBlocks.SUB_RUDDER.get()) && subRudderConnectedToRudderInternal(clickedPosition, rudderPosition, player))) {
			return WandMode.STRUCTURE_CHANGE;
		}
		if (block.equals(ModBlocks.SUB_RUDDER.get()) || block.equals(ModBlocks.ITEM_GATE.get())) {
			return WandMode.CONNECT;
		}
		if (isShiftDown) {
			return WandMode.DELETION;
		}
		if (firstPosition == null) {
			return WandMode.SELECT_FIRST_POS;
		}
		return WandMode.ADD_REC;
	}

	private boolean isValidContext(Player player, BlockPos clickedPosition, Block block, String shipUUID, BlockPos rudderPosition, BlockPos firstPosition, WandMode mode) {
		boolean result = true;
		switch (mode) {
			case SELECT_SHIP -> result = isRudderBlock(block, player);
			case SELECT_FIRST_POS, CONNECT -> {
				result = nonNullUUID(shipUUID, player);
				result &= nonNullRudderPosition(rudderPosition, player);
				result &= UUIDMatch(shipUUID, rudderPosition, player);
			}
			case ADD_REC -> {
				result = nonNullUUID(shipUUID, player);
				result &= nonNullRudderPosition(rudderPosition, player);
				result &= UUIDMatch(shipUUID, rudderPosition, player);
				result &= nonNullFirstPosition(firstPosition, player);
			}
			case STRUCTURE_CHANGE -> {
				result = nonNullUUID(shipUUID, player);
				result &= nonNullRudderPosition(rudderPosition, player);
				result &= UUIDMatch(shipUUID, rudderPosition, player);
				if (block.equals(ModBlocks.SUB_RUDDER.get())) {
					result &= subRudderConnectedToRudder(clickedPosition, rudderPosition, player);
				}
			}
			case DELETION -> {
				result = nonNullUUID(shipUUID, player);
				result &= nonNullRudderPosition(rudderPosition, player);
			}
		}
		return result;
	}

	private boolean isRudderBlock(Block block, Player player) {
		if (block.equals(ModBlocks.RUDDER.get())) {
			return true;
		}
		player.sendSystemMessage(Component.translatable("message.selectRudderBlock"));
		return false;
	}

	private boolean nonNullUUID(String shipUUID, Player player) {
		if (shipUUID != null) {
			return true;
		}
		player.sendSystemMessage(Component.translatable("message.rudderNotSelected"));
		return false;
	}

	private boolean nonNullRudderPosition(BlockPos rudderPosition, Player player) {
		if (rudderPosition != null) {
			return true;
		}
		player.sendSystemMessage(Component.translatable("message.rudderNotSelected"));
		return false;
	}

	private boolean UUIDMatch(String shipUUID, BlockPos rudderPosition, Player player) {
		BlockEntity entity = player.level.getBlockEntity(rudderPosition);
		if (!(entity instanceof RudderBlockEntity)) {
			player.sendSystemMessage(Component.translatable("message.wrongBlockEntity"));
			return false;
		}

		if (shipUUID.equals(((RudderBlockEntity) entity).getUuid())) {
			return true;
		}
		player.sendSystemMessage(Component.translatable("message.wrongRudderContext", rudderPosition.toShortString()));
		return false;
	}

	private boolean nonNullFirstPosition(BlockPos firstPosition, Player player) {
		if (firstPosition != null) {
			return true;
		}
		player.sendSystemMessage(Component.translatable("message.firstPositionNotSelected"));
		return false;
	}

	private boolean subRudderConnectedToRudder(BlockPos clickedPosition, BlockPos rudderPosition, Player player) {
		if (subRudderConnectedToRudderInternal(clickedPosition, rudderPosition, player)) {
			return true;
		}
		player.sendSystemMessage(Component.translatable("message.notConnectedToRudder", rudderPosition.toShortString()));
		return false;
	}

	private boolean subRudderConnectedToRudderInternal(BlockPos clickedPosition, BlockPos rudderPosition, Player player) {
		BlockEntity entity = player.level.getBlockEntity(clickedPosition);
		if (!(entity instanceof SubRudderBlockEntity)) {
			return false;
		}

		return rudderPosition.equals(((SubRudderBlockEntity) entity).getRudderPosition());
	}

	@Override
	public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag) {
		final Tag rudderPositionTag = itemStack.getOrCreateTag().get(SHIP_RUDDER_POSITION_TAG);
		final Tag firstPositionTag = itemStack.getOrCreateTag().get(FIRST_POSITION_TAG);

		final String shipUUID = itemStack.getOrCreateTag().getString(SHIP_UUID_TAG);
		final BlockPos rudderPosition = rudderPositionTag == null ? null : NbtUtils.readBlockPos((CompoundTag) rudderPositionTag);
		final BlockPos firstPosition = firstPositionTag == null ? null : NbtUtils.readBlockPos((CompoundTag) firstPositionTag);

		Component rudderInformation = Component.translatable("text.wand.invalidConfiguration")
				.withStyle(ChatFormatting.DARK_RED).withStyle(ChatFormatting.ITALIC);
		if (Minecraft.getInstance().level != null && rudderPosition != null) {
			BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(rudderPosition);
			if (blockEntity instanceof RudderBlockEntity) {
				if (((RudderBlockEntity) blockEntity).getUuid().equals(shipUUID)) {
					rudderInformation = Component.literal(rudderPosition.toShortString()).withStyle(ChatFormatting.GRAY);
				}
			}
		}

		if (Screen.hasShiftDown()) {
			components.add(Component.translatable("text.wand.moreInfo1").withStyle(ChatFormatting.LIGHT_PURPLE));
			components.add(Component.translatable("text.wand.moreInfo2").withStyle(ChatFormatting.DARK_PURPLE));

			if (rudderPosition != null) {
				components.add(Component.translatable("text.wand.shipCoordinate").withStyle(ChatFormatting.AQUA));
				components.add(rudderInformation);
			}

			if (firstPosition != null) {
				components.add(Component.translatable("text.wand.position").withStyle(ChatFormatting.AQUA));
				components.add(Component.literal(firstPosition.toShortString()).withStyle(ChatFormatting.GRAY));
			}
		} else {
			components.add(Component.translatable("text.wand.lessInfo").withStyle(ChatFormatting.DARK_AQUA));

			if (rudderPosition != null) {
				components.add(Component.translatable("text.wand.shipCoordinate").withStyle(ChatFormatting.AQUA));
				components.add(rudderInformation);
			}
		}

		super.appendHoverText(itemStack, level, components, tooltipFlag);
	}

	private enum WandMode {
		SELECT_SHIP, SELECT_FIRST_POS, ADD_REC, CONNECT, STRUCTURE_CHANGE, DELETION
	}
}
