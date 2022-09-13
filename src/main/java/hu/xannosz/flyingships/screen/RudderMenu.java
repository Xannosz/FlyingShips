package hu.xannosz.flyingships.screen;

import hu.xannosz.flyingships.Monad;
import hu.xannosz.flyingships.Util;
import hu.xannosz.flyingships.block.ModBlocks;
import hu.xannosz.flyingships.blockentity.RudderBlockEntity;
import hu.xannosz.flyingships.networking.GetSavedCoordinatesPacket;
import hu.xannosz.flyingships.networking.ModMessages;
import hu.xannosz.flyingships.screen.slot.ModInputSlot;
import hu.xannosz.flyingships.screen.widget.ButtonId;
import hu.xannosz.flyingships.warp.SavedCoordinate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hu.xannosz.flyingships.blockentity.RudderBlockEntity.*;

@Slf4j
public class RudderMenu extends AbstractContainerMenu {

	private static final int PLAYER_INVENTORY_HEIGHT = 111;

	@Getter
	private final RudderBlockEntity blockEntity;
	private final Level level;

	private final ContainerData data;

	@Getter
	private ModInputSlot enderInputSlot;
	@Getter
	private ModInputSlot steamInputSlot;
	@Getter
	private ModInputSlot heatInputSlot;

	public RudderMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
		this(containerId, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(DATA_SLOT_SIZE));
	}

	public RudderMenu(int containerId, Inventory inv, BlockEntity blockEntity, ContainerData data) {
		super(ModMenuTypes.RUDDER_MENU.get(), containerId);

		checkContainerSize(inv, 3);
		this.blockEntity = ((RudderBlockEntity) blockEntity);
		level = inv.player.level;

		this.data = data;

		addPlayerInventory(inv);
		addPlayerHotBar(inv);

		this.blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
			enderInputSlot = new ModInputSlot(handler, 0, 105, 90);
			steamInputSlot = new ModInputSlot(handler, 1, 128, 90);
			heatInputSlot = new ModInputSlot(handler, 2, 151, 90);
			this.addSlot(enderInputSlot);
			this.addSlot(steamInputSlot);
			this.addSlot(heatInputSlot);
		});

		addDataSlots(data);
	}


	// CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
	// must assign a slot number to each of the slots used by the GUI.
	// For this container, we can see both the tile inventory's slots and the player inventory slots and the hotBar.
	// Each time we add a Slot to the container, it automatically increases the slotIndex, which means
	//  0 - 8 = hotBar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
	//  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
	//  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
	private static final int HOT_BAR_SLOT_COUNT = 9;
	private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
	private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
	private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
	private static final int VANILLA_SLOT_COUNT = HOT_BAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
	private static final int VANILLA_FIRST_SLOT_INDEX = 0;
	private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

	// THIS YOU HAVE TO DEFINE!
	private static final int TE_INVENTORY_SLOT_COUNT = 3;  // must be the number of slots you have!

	@Monad
	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
		log.info("quickMoveStack");
		Slot sourceSlot = slots.get(index);
		if (!sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
		ItemStack sourceStack = sourceSlot.getItem();
		ItemStack copyOfSourceStack = sourceStack.copy();

		// Check if the slot clicked is one of the vanilla container slots
		if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
			// This is a vanilla container slot so merge the stack into the tile inventory
			if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
					+ TE_INVENTORY_SLOT_COUNT, false)) {
				return ItemStack.EMPTY;  // EMPTY_ITEM
			}
		} else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
			// This is a TE slot so merge the stack into the players inventory
			if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
				return ItemStack.EMPTY;
			}
		} else {
			System.out.println("Invalid slotIndex:" + index);
			return ItemStack.EMPTY;
		}
		// If stack size == 0 (the entire stack was moved) set slot contents to null
		if (sourceStack.getCount() == 0) {
			sourceSlot.set(ItemStack.EMPTY);
		} else {
			sourceSlot.setChanged();
		}
		sourceSlot.onTake(playerIn, sourceStack);
		return copyOfSourceStack;
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
				player, ModBlocks.RUDDER.get());
	}

	private void addPlayerInventory(Inventory playerInventory) {
		for (int i = 0; i < 3; ++i) {
			for (int l = 0; l < 9; ++l) {
				this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, PLAYER_INVENTORY_HEIGHT + i * 18));
			}
		}
	}

	private void addPlayerHotBar(Inventory playerInventory) {
		for (int i = 0; i < 9; ++i) {
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, PLAYER_INVENTORY_HEIGHT + 58));
		}
	}

	public int getSpeed() {
		return data.get(RudderBlockEntity.SPEED_KEY);
	}

	public int getStep() {
		return data.get(RudderBlockEntity.STEP_KEY);
	}

	public Map<ButtonId, Boolean> getBlinking() {
		boolean[] bits = Util.convertIntToBitArray(data.get(RudderBlockEntity.BLINK_KEY), 9);
		Map<ButtonId, Boolean> result = new HashMap<>();

		result.put(ButtonId.FORWARD, bits[0]);
		result.put(ButtonId.RIGHT, bits[1]);
		result.put(ButtonId.BACKWARD, bits[2]);
		result.put(ButtonId.LEFT, bits[3]);
		result.put(ButtonId.UP, bits[4]);
		result.put(ButtonId.DOWN, bits[5]);

		result.put(ButtonId.LAND, bits[6]);

		result.put(ButtonId.JUMP, bits[7]);

		result.put(ButtonId.BEACON, bits[8]);

		return result;
	}

	public Map<ButtonId, Boolean> getCoordinateBlinking() {
		boolean[] bits = Util.convertIntToBitArray(data.get(COORDINATES_BLINK_KEY), 6);
		Map<ButtonId, Boolean> result = new HashMap<>();

		result.put(ButtonId.JUMP_TO_COORDINATE_1, bits[0]);
		result.put(ButtonId.JUMP_TO_COORDINATE_2, bits[1]);
		result.put(ButtonId.JUMP_TO_COORDINATE_3, bits[2]);
		result.put(ButtonId.JUMP_TO_COORDINATE_4, bits[3]);
		result.put(ButtonId.JUMP_TO_COORDINATE_5, bits[4]);
		result.put(ButtonId.JUMP_TO_COORDINATE_6, bits[5]);

		return result;
	}

	public boolean[] getEnabledFunctions() {
		return Util.convertIntToBitArray(data.get(RudderBlockEntity.FUNCTION_KEY), 4);
	}

	public int getPowerButtonState() {
		return data.get(POWER_BUTTON_STATE_KEY);
	}

	public int getPowerButtonNextState() {
		return data.get(POWER_BUTTON_NEXT_STATE_KEY);
	}

	public int getPositionMarkerTop() {
		return data.get(POSITION_MARKER_TOP_KEY);
	}

	public int getPositionMarkerMid() {
		return data.get(POSITION_MARKER_MID_KEY);
	}

	public int getPositionMarkerBottom() {
		return data.get(POSITION_MARKER_BOTTOM_KEY);
	}

	public int getLandingButtonState() {
		return data.get(LANDING_BUTTON_KEY);
	}

	public int getLandingButtonNextState() {
		return data.get(LANDING_BUTTON_NEXT_KEY);
	}

	public GuiState getGuiState() {
		return GuiState.fromKey(data.get(GUI_STATE_KEY));
	}

	public int getBlockPosPage() {
		return data.get(BLOCK_POS_PAGE_KEY) + 1;
	}

	public int getBlockPosMaxPage() {
		return data.get(BLOCK_POS_MAX_PAGE_KEY);
	}

	public List<BlockPos> getBlockPosStruct() {
		int onThisPage = data.get(BLOCK_POS_ON_THIS_PAGE_KEY);
		List<BlockPos> result = new ArrayList<>();

		if (onThisPage > 0) {
			result.add(new BlockPos(data.get(RECTANGLE_1_X_1_KEY), data.get(RECTANGLE_1_Y_1_KEY), data.get(RECTANGLE_1_Z_1_KEY)));
			result.add(new BlockPos(data.get(RECTANGLE_1_X_2_KEY), data.get(RECTANGLE_1_Y_2_KEY), data.get(RECTANGLE_1_Z_2_KEY)));
		}
		if (onThisPage > 1) {
			result.add(new BlockPos(data.get(RECTANGLE_2_X_1_KEY), data.get(RECTANGLE_2_Y_1_KEY), data.get(RECTANGLE_2_Z_1_KEY)));
			result.add(new BlockPos(data.get(RECTANGLE_2_X_2_KEY), data.get(RECTANGLE_2_Y_2_KEY), data.get(RECTANGLE_2_Z_2_KEY)));
		}
		if (onThisPage > 2) {
			result.add(new BlockPos(data.get(RECTANGLE_3_X_1_KEY), data.get(RECTANGLE_3_Y_1_KEY), data.get(RECTANGLE_3_Z_1_KEY)));
			result.add(new BlockPos(data.get(RECTANGLE_3_X_2_KEY), data.get(RECTANGLE_3_Y_2_KEY), data.get(RECTANGLE_3_Z_2_KEY)));
		}

		return result;
	}

	public int getWindMax() {
		return data.get(WIND_MAX_KEY);
	}

	public int getWind() {
		return data.get(WIND_KEY);
	}

	public int getFloatingMax() {
		return data.get(FLOATING_MAX_KEY);
	}

	public int getFloating() {
		return data.get(FLOATING_KEY);
	}

	public int getWaterLine() {
		return data.get(WATER_LINE_KEY);
	}

	public void updateCoordinates() {
		ModMessages.sendToServer(new GetSavedCoordinatesPacket(blockEntity.getBlockPos()));
	}

	public List<SavedCoordinate> getCoordinates() {
		return blockEntity.getCoordinates();
	}

	public int getCoordinatesPage() {
		return data.get(COORDINATES_PAGE_KEY) + 1;
	}

	public int getCoordinatesMaxPage() {
		return data.get(COORDINATES_MAX_PAGE_KEY);
	}
}
