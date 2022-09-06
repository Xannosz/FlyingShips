package hu.xannosz.flyingships.item;

import hu.xannosz.flyingships.block.ModBlocks;
import hu.xannosz.flyingships.blockentity.RudderBlockEntity;
import hu.xannosz.flyingships.networking.AddRectanglePacket;
import hu.xannosz.flyingships.networking.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
import java.util.Objects;

public class Wand extends Item {

	private static final String SHIP_NAME_TAG = "shipName";
	private static final String SHIP_RUDDER_POSITION = "shipRudderPosition";
	private static final String POSITION_TAG = "position";

	public Wand(Properties properties) {
		super(properties);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext useOnContext) {

		if (!useOnContext.getLevel().isClientSide()) {
			return InteractionResult.PASS;
		}

		final ItemStack itemStack = useOnContext.getItemInHand();
		if (useOnContext.getPlayer() == null) {
			return InteractionResult.FAIL;
		}
		final Block block = useOnContext.getPlayer().level.getBlockState(useOnContext.getClickedPos()).getBlock();

		if (block.equals(ModBlocks.RUDDER.get())) {
			return setShipData(useOnContext, itemStack);
		}

		if (useOnContext.getPlayer().isShiftKeyDown()) {
			return handleShiftMode(useOnContext.getPlayer(), itemStack);
		}

		if (!itemStack.getOrCreateTag().contains(SHIP_NAME_TAG)) {
			return InteractionResult.FAIL;
		}
		if (!itemStack.getOrCreateTag().contains(POSITION_TAG)) {
			return setFirstPosition(useOnContext, itemStack);
		}

		return setRectangle(useOnContext, itemStack);
	}

	@NotNull
	private InteractionResult setShipData(UseOnContext useOnContext, ItemStack itemStack) {
		BlockEntity entity = Objects.requireNonNull(useOnContext.getPlayer()).level.getBlockEntity(useOnContext.getClickedPos());

		if (entity == null) {
			return InteractionResult.FAIL;
		}

		String shipName = ((RudderBlockEntity) entity).getShipNameSafe();
		itemStack.getOrCreateTag().putString(SHIP_NAME_TAG, shipName);
		itemStack.getOrCreateTag().put(SHIP_RUDDER_POSITION, NbtUtils.writeBlockPos(useOnContext.getClickedPos()));
		sendShipNameMessage(useOnContext.getPlayer(), shipName);

		return InteractionResult.SUCCESS;
	}

	@NotNull
	private InteractionResult setFirstPosition(UseOnContext useOnContext, ItemStack itemStack) {
		itemStack.getOrCreateTag().put(POSITION_TAG, NbtUtils.writeBlockPos(useOnContext.getClickedPos()));
		sendFirstPositionSetMessage(Objects.requireNonNull(useOnContext.getPlayer()), useOnContext.getClickedPos());
		return InteractionResult.SUCCESS;
	}

	@NotNull
	private InteractionResult setRectangle(UseOnContext useOnContext, ItemStack itemStack) {
		String shipName = itemStack.getOrCreateTag().getString(SHIP_NAME_TAG);
		BlockPos rudderPosition = NbtUtils.readBlockPos((CompoundTag) Objects.requireNonNull(itemStack.getOrCreateTag().get(SHIP_RUDDER_POSITION)));
		BlockPos pos1 = NbtUtils.readBlockPos((CompoundTag) Objects.requireNonNull(itemStack.getOrCreateTag().get(POSITION_TAG)));
		BlockPos pos2 = useOnContext.getClickedPos();

		if (!((RudderBlockEntity) Objects.requireNonNull(Objects.requireNonNull(useOnContext.getPlayer()).
				level.getBlockEntity(rudderPosition))).getShipNameSafe().equals(shipName)) {
			sendErrorMessage(Objects.requireNonNull(useOnContext.getPlayer()), shipName, rudderPosition);
			return InteractionResult.FAIL;
		}

		setRectangle(rudderPosition, pos1, pos2);
		sendSetRectangleMessage(Objects.requireNonNull(useOnContext.getPlayer()), shipName, pos1, pos2);
		itemStack.getOrCreateTag().remove(POSITION_TAG);

		return InteractionResult.SUCCESS;
	}

	private InteractionResult handleShiftMode(Player player, ItemStack itemStack) {
		if (itemStack.getOrCreateTag().contains(POSITION_TAG)) {
			itemStack.getOrCreateTag().remove(POSITION_TAG);
			sendFirstPositionUnsetMessage(player);
			return InteractionResult.SUCCESS;
		}

		if (itemStack.getOrCreateTag().contains(SHIP_NAME_TAG)) {
			itemStack.getOrCreateTag().remove(SHIP_NAME_TAG);
			itemStack.getOrCreateTag().remove(SHIP_RUDDER_POSITION);
			sendShipNameUnsetMessage(player);
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.FAIL;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag) {
		if (Screen.hasShiftDown()) {
			components.add(Component.translatable("text.wand.moreInfo1").withStyle(ChatFormatting.LIGHT_PURPLE));
			components.add(Component.translatable("text.wand.moreInfo2").withStyle(ChatFormatting.DARK_PURPLE));

			if (itemStack.getOrCreateTag().contains(SHIP_NAME_TAG)) {
				components.add(Component.translatable("text.wand.shipName").withStyle(ChatFormatting.AQUA));
				components.add(Component.literal(itemStack.getOrCreateTag().getString(SHIP_NAME_TAG)).withStyle(ChatFormatting.GRAY));
			}

			if (itemStack.getOrCreateTag().contains(POSITION_TAG)) {
				components.add(Component.translatable("text.wand.position").withStyle(ChatFormatting.AQUA));
				components.add(Component.literal(NbtUtils.readBlockPos((CompoundTag) Objects.requireNonNull(itemStack.getOrCreateTag().get(POSITION_TAG))).toShortString()).withStyle(ChatFormatting.GRAY));
			}
		} else {
			components.add(Component.translatable("text.wand.lessInfo").withStyle(ChatFormatting.DARK_AQUA));

			if (itemStack.getOrCreateTag().contains(SHIP_NAME_TAG)) {
				components.add(Component.translatable("text.wand.shipName").withStyle(ChatFormatting.AQUA));
				components.add(Component.literal(itemStack.getOrCreateTag().getString(SHIP_NAME_TAG)).withStyle(ChatFormatting.GRAY));
			}
		}

		super.appendHoverText(itemStack, level, components, tooltipFlag);
	}

	private void setRectangle(BlockPos rudderPos, BlockPos blockPos1, BlockPos blockPos2) {

		ModMessages.sendToServer(new AddRectanglePacket(rudderPos, 0, blockPos1, blockPos2));
	}

	private void sendShipNameMessage(Player player, String shipName) {
		player.sendSystemMessage(Component.translatable("message.shipNameSet", shipName));
	}

	private void sendShipNameUnsetMessage(Player player) {
		player.sendSystemMessage(Component.translatable("message.shipNameDeleted"));
	}

	private void sendFirstPositionSetMessage(Player player, BlockPos blockPos) {
		player.sendSystemMessage(Component.translatable("message.pos1Set", blockPos.toShortString()));
	}

	private void sendFirstPositionUnsetMessage(Player player) {
		player.sendSystemMessage(Component.translatable("message.pos1Deleted"));
	}

	private void sendSetRectangleMessage(Player player, String shipName, BlockPos blockPos1, BlockPos blockPos2) {
		player.sendSystemMessage(Component.translatable("message.rectangleSet", blockPos1.toShortString(), blockPos2.toShortString(), shipName));
	}

	private void sendErrorMessage(Player player, String shipName, BlockPos rudderPosition) {
		player.sendSystemMessage(Component.translatable("message.wrongRudderContext", shipName, rudderPosition.toShortString()));
	}
}
