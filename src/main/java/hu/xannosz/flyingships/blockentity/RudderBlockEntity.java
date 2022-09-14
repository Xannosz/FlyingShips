package hu.xannosz.flyingships.blockentity;

import hu.xannosz.flyingships.Configuration;
import hu.xannosz.flyingships.Util;
import hu.xannosz.flyingships.block.Rudder;
import hu.xannosz.flyingships.screen.GuiState;
import hu.xannosz.flyingships.screen.RudderMenu;
import hu.xannosz.flyingships.screen.widget.ButtonId;
import hu.xannosz.flyingships.warp.BlockPosStruct;
import hu.xannosz.flyingships.warp.PowerState;
import hu.xannosz.flyingships.warp.SavedCoordinate;
import hu.xannosz.flyingships.warp.WarpDirection;
import hu.xannosz.flyingships.warp.jump.JumpUtil;
import hu.xannosz.flyingships.warp.terrainscan.LandButtonSettings;
import hu.xannosz.flyingships.warp.terrainscan.LiveDataPackage;
import hu.xannosz.flyingships.warp.terrainscan.TerrainScanResponseStruct;
import hu.xannosz.flyingships.warp.terrainscan.TerrainScanUtil;
import hu.xannosz.flyingships.warp.vehiclescan.VehicleScanResponseStruct;
import hu.xannosz.flyingships.warp.vehiclescan.VehicleScanUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static hu.xannosz.flyingships.Util.CLOUD_LEVEL;

@Slf4j
public class RudderBlockEntity extends BlockEntity implements MenuProvider {

	public static final int SPEED_KEY = 0;
	public static final int STEP_KEY = 1;
	public static final int BLINK_KEY = 2;
	public static final int FUNCTION_KEY = 3;
	public static final int POWER_BUTTON_STATE_KEY = 4;
	public static final int POWER_BUTTON_NEXT_STATE_KEY = 5;

	public static final int POSITION_MARKER_TOP_KEY = 6;
	public static final int POSITION_MARKER_MID_KEY = 7;
	public static final int POSITION_MARKER_BOTTOM_KEY = 8;
	public static final int LANDING_BUTTON_KEY = 9;
	public static final int LANDING_BUTTON_NEXT_KEY = 10;
	public static final int GUI_STATE_KEY = 11;
	public static final int RECTANGLE_1_X_1_KEY = 12;
	public static final int RECTANGLE_1_X_2_KEY = 13;
	public static final int RECTANGLE_1_Y_1_KEY = 14;
	public static final int RECTANGLE_1_Y_2_KEY = 15;
	public static final int RECTANGLE_1_Z_1_KEY = 16;
	public static final int RECTANGLE_1_Z_2_KEY = 17;
	public static final int RECTANGLE_2_X_1_KEY = 18;
	public static final int RECTANGLE_2_X_2_KEY = 19;
	public static final int RECTANGLE_2_Y_1_KEY = 20;
	public static final int RECTANGLE_2_Y_2_KEY = 21;
	public static final int RECTANGLE_2_Z_1_KEY = 22;
	public static final int RECTANGLE_2_Z_2_KEY = 23;
	public static final int RECTANGLE_3_X_1_KEY = 24;
	public static final int RECTANGLE_3_X_2_KEY = 25;
	public static final int RECTANGLE_3_Y_1_KEY = 26;
	public static final int RECTANGLE_3_Y_2_KEY = 27;
	public static final int RECTANGLE_3_Z_1_KEY = 28;
	public static final int RECTANGLE_3_Z_2_KEY = 29;
	public static final int BLOCK_POS_PAGE_KEY = 30;
	public static final int BLOCK_POS_MAX_PAGE_KEY = 31;
	public static final int BLOCK_POS_ON_THIS_PAGE_KEY = 32;
	public static final int WIND_MAX_KEY = 33;
	public static final int WIND_KEY = 34;
	public static final int FLOATING_MAX_KEY = 35;
	public static final int FLOATING_KEY = 36;
	public static final int WATER_LINE_KEY = 37;
	public static final int COORDINATES_PAGE_KEY = 38;
	public static final int COORDINATES_MAX_PAGE_KEY = 39;
	public static final int COORDINATES_BLINK_KEY = 40;

	public static final int DATA_SLOT_SIZE = 41;

	public static final int[] STEPS = new int[]{1, 10, 100, 1000};
	private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
		}
	};

	private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

	private int speed = 50;
	private int step = 0;

	private String uuid = null;

	private int blockPositionsPage = 0;
	private int coordinatesPage = 0;

	private int selectedCoordinate = -1;

	private boolean enableHeatEngine = false;
	private boolean enableSteamEngine = false;
	private boolean enableEnderEngine = false;
	private boolean enableHyperDrive = false;

	private PowerState powerButtonState = PowerState.OFF;

	private WarpDirection selectedWarpDirection;
	private GuiState guiState = GuiState.MAIN;

	private List<BlockPosStruct> blockPositions = new ArrayList<>();
	@Setter
	private List<SavedCoordinate> coordinates = new ArrayList<>();

	private TerrainScanResponseStruct terrainScanResult;
	private VehicleScanResponseStruct vehicleScanResult;
	private LandButtonSettings landButtonSettings = LandButtonSettings.VOID;
	private final Configuration configuration = Configuration.getConfiguration();

	private int clock = 0;

	private int waterLine = 0;

	private final ContainerData data = new SimpleContainerData(DATA_SLOT_SIZE);


	public RudderBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ModBlockEntities.RUDDER_BLOCK_ENTITY.get(), blockPos, blockState);
		if (level != null && !level.isClientSide()) {
			updateData();
		}
	}

	@Override
	public @NotNull Component getDisplayName() {
		return Component.literal("Rudder block");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
		return new RudderMenu(containerId, inventory, this, data);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return lazyItemHandler.cast();
		}

		return super.getCapability(cap, side);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		lazyItemHandler = LazyOptional.of(() -> itemHandler);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		lazyItemHandler.invalidate();
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag) {
		tag.put("inventory", itemHandler.serializeNBT());
		tag.putInt("rudder.speed", speed);
		tag.putInt("rudder.step", step);
		tag.putInt("rudder.powerButtonState", powerButtonState.getKey());
		tag.putInt("rudder.waterLine", waterLine);

		tag.putInt("rudder.positions.size", blockPositions.size());
		for (int i = 0; i < blockPositions.size(); i++) {
			blockPositions.get(i).saveAdditional(tag, i);
		}

		tag.putInt("rudder.coordinate.size", coordinates.size());
		for (int i = 0; i < coordinates.size(); i++) {
			coordinates.get(i).saveAdditional(tag, i);
		}

		super.saveAdditional(tag);
	}

	@Override
	public void load(@NotNull CompoundTag nbt) {
		super.load(nbt);

		itemHandler.deserializeNBT(nbt.getCompound("inventory"));
		speed = nbt.getInt("rudder.speed");
		step = nbt.getInt("rudder.step");
		powerButtonState = PowerState.fromKey(nbt.getInt("rudder.powerButtonState"));
		waterLine = nbt.getInt("rudder.waterLine");

		int blockPositionsSize = nbt.getInt("rudder.positions.size");
		blockPositions = new ArrayList<>(blockPositionsSize);
		for (int i = 0; i < blockPositionsSize; i++) {
			blockPositions.add(new BlockPosStruct(nbt, i));
		}

		int coordinatesSize = nbt.getInt("rudder.coordinate.size");
		coordinates = new ArrayList<>(coordinatesSize);
		for (int i = 0; i < coordinatesSize; i++) {
			coordinates.add(new SavedCoordinate(nbt, i));
		}

		updateData();
	}

	public void drops() {
		SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			inventory.setItem(i, itemHandler.getStackInSlot(i));
		}

		Containers.dropContents(Objects.requireNonNull(level), worldPosition, inventory);
	}

	@SuppressWarnings("unused")
	public static void tick(Level level, BlockPos pos, BlockState state, RudderBlockEntity blockEntity) {
		if (!level.isClientSide()) {
			blockEntity.tick();
		}
	}

	private void tick() {
		if (!powerButtonState.equals(PowerState.ON)) {
			return;
		}

		clock++;
		if (clock % 25 == 0) {
			clock = 0;

			calculateJumpData();

			updateData();
		}
	}

	private void calculateJumpData() {
		LiveDataPackage terrainScanMasks = TerrainScanUtil.generateMasks(JumpUtil.createRectangles(getBlockPos(), blockPositions));
		terrainScanResult = TerrainScanUtil.scanTerrain((ServerLevel) level, terrainScanMasks);
		vehicleScanResult = VehicleScanUtil.scanVehicle((ServerLevel) level, JumpUtil.createRectangles(getBlockPos(), blockPositions), getBlockState().getValue(Rudder.FACING));
	}

	public void executeButtonClick(ButtonId buttonId) {
		switch (buttonId) {
			case UP, DOWN, RIGHT, LEFT, FORWARD, BACKWARD -> {
				selectedWarpDirection = WarpDirection.fromBlockDirection(buttonId, getBlockState().getValue(Rudder.FACING));
				selectedCoordinate = -1;
			}
			case LAND -> {
				landButtonSettings = landButtonSettings.nextState(terrainScanResult);
				if (vehicleScanResult == null) {
					break;
				}
				if (!landButtonSettings.equals(LandButtonSettings.EMPTY)) {
					selectedWarpDirection = WarpDirection.fromBlockDirection(buttonId, getBlockState().getValue(Rudder.FACING));
					switch (landButtonSettings) {
						case VOID -> speed = Math.abs(CLOUD_LEVEL + waterLine - vehicleScanResult.getBottomY());
						case LAND -> speed = terrainScanResult.getHeightOfBottom();
						case TOUCH_CELLING -> speed = terrainScanResult.getHeightOfCelling();
						case SWIM_LAVA, SWIM_WATER -> speed = Math.abs(terrainScanResult.getHeightOfBottom() + waterLine);
					}
				} else {
					if (selectedWarpDirection.equals(WarpDirection.LAND)) {
						selectedWarpDirection = null;
					}
				}
				selectedCoordinate = -1;
			}
			case INCREASE -> {
				if (step < STEPS.length - 1) {
					step++;
				}
			}
			case DECREASE -> {
				if (step > 0) {
					step--;
				}
			}
			case PLUS -> {
				speed += STEPS[step];
				if (selectedCoordinate >= 0) {
					selectedCoordinate = -1;
					selectedWarpDirection = null;
				}
				if (selectedWarpDirection != null && selectedWarpDirection.equals(WarpDirection.LAND)) {
					selectedWarpDirection = null;
				}
			}
			case MINUS -> {
				speed -= STEPS[step];
				if (speed < 0) {
					speed = 0;
				}
				if (selectedCoordinate >= 0) {
					selectedCoordinate = -1;
					selectedWarpDirection = null;
				}
				if (selectedWarpDirection != null && selectedWarpDirection.equals(WarpDirection.LAND)) {
					selectedWarpDirection = null;
				}
			}
			case JUMP -> {
				calculateJumpData();
				if (selectedWarpDirection != null && speed > 0 && powerButtonState.equals(PowerState.ON) &&
						vehicleScanResult.getFloatingQuotient() >= vehicleScanResult.getDensity() &&
						hasEnoughPowerForMovement()) {
					JumpUtil.jump(selectedWarpDirection, speed, landButtonSettings, terrainScanResult,
							(ServerLevel) level, getBlockPos(), blockPositions,
							selectedCoordinate == -1 ? null : coordinates.get(selectedCoordinate).getCoordinate(),
							waterLine, vehicleScanResult.getBottomY());
					selectedWarpDirection = null;
				}
			}
			case POWER -> {
				powerButtonState = powerButtonState.nextState(enableSteamEngine);
				if (!powerButtonState.equals(PowerState.ON)) {
					selectedWarpDirection = null;
					selectedCoordinate = -1;
					landButtonSettings = LandButtonSettings.VOID;
					vehicleScanResult = null;
				}
			}
			case MENU -> guiState = GuiState.SETTINGS;
			case BEACON -> guiState = GuiState.COORDINATES;
			case DIMENSION -> log.error("dimension button not implemented");
			case RECTANGLE_PAGE_UP -> {
				blockPositionsPage--;
				resetBlockPositionPager();
			}
			case RECTANGLE_PAGE_DOWN -> {
				blockPositionsPage++;
				resetBlockPositionPager();
			}
			case BACK -> guiState = GuiState.MAIN;
			case DELETE_REC_1 -> {
				blockPositions.remove(blockPositionsPage * 3);
				resetBlockPositionPager();
			}
			case DELETE_REC_2 -> {
				blockPositions.remove(blockPositionsPage * 3 + 1);
				resetBlockPositionPager();
			}
			case DELETE_REC_3 -> {
				blockPositions.remove(blockPositionsPage * 3 + 2);
				resetBlockPositionPager();
			}
			case WATER_LINE_UP -> waterLine++;
			case WATER_LINE_DOWN -> waterLine--;
			case COORDINATE_PAGE_UP -> {
				coordinatesPage--;
				resetCoordinatePager();
			}
			case COORDINATE_PAGE_DOWN -> {
				coordinatesPage++;
				resetCoordinatePager();
			}
			case JUMP_TO_COORDINATE_1 -> {
				selectedCoordinate = coordinatesPage * 6;
				selectedWarpDirection = WarpDirection.COORDINATE;
				speed = coordinates.get(selectedCoordinate).getCoordinate().distManhattan(getBlockPos());
			}
			case JUMP_TO_COORDINATE_2 -> {
				selectedCoordinate = coordinatesPage * 6 + 1;
				selectedWarpDirection = WarpDirection.COORDINATE;
				speed = coordinates.get(selectedCoordinate).getCoordinate().distManhattan(getBlockPos());
			}
			case JUMP_TO_COORDINATE_3 -> {
				selectedCoordinate = coordinatesPage * 6 + 2;
				selectedWarpDirection = WarpDirection.COORDINATE;
				speed = coordinates.get(selectedCoordinate).getCoordinate().distManhattan(getBlockPos());
			}
			case JUMP_TO_COORDINATE_4 -> {
				selectedCoordinate = coordinatesPage * 6 + 3;
				selectedWarpDirection = WarpDirection.COORDINATE;
				speed = coordinates.get(selectedCoordinate).getCoordinate().distManhattan(getBlockPos());
			}
			case JUMP_TO_COORDINATE_5 -> {
				selectedCoordinate = coordinatesPage * 6 + 4;
				selectedWarpDirection = WarpDirection.COORDINATE;
				speed = coordinates.get(selectedCoordinate).getCoordinate().distManhattan(getBlockPos());
			}
			case JUMP_TO_COORDINATE_6 -> {
				selectedCoordinate = coordinatesPage * 6 + 5;
				selectedWarpDirection = WarpDirection.COORDINATE;
				speed = coordinates.get(selectedCoordinate).getCoordinate().distManhattan(getBlockPos());
			}
		}
		updateData();
		setChanged();
	}

	private boolean hasEnoughPowerForMovement() {
		if (configuration.isEnableSliding() && speed <= 5) {
			return true;
		}

		return getMovementPower() > getNecessaryMovementPower();
	}

	private double getNecessaryMovementPower() {
		return vehicleScanResult.getDensity() * Math.pow(1 + speed / configuration.getSpeedConsolidator(), 3);
	}

	private int getMovementPower() {
		return vehicleScanResult.getWindSurface() * configuration.getWindMultiplier();
	}

	private void updateData() {
		data.set(SPEED_KEY, speed);
		data.set(STEP_KEY, step);
		data.set(BLINK_KEY, Util.convertBitArrayToInt(getBlinkArray()));
		data.set(FUNCTION_KEY, Util.convertBitArrayToInt(getFunctionArray()));
		data.set(POWER_BUTTON_STATE_KEY, powerButtonState.getKey());
		data.set(POWER_BUTTON_NEXT_STATE_KEY, powerButtonState.nextState(enableSteamEngine).getKey());

		if (terrainScanResult != null) {
			data.set(POSITION_MARKER_TOP_KEY, terrainScanResult.getCellingPosition().getKey());
			data.set(POSITION_MARKER_MID_KEY, terrainScanResult.getFloatingPosition().getKey());
			data.set(POSITION_MARKER_BOTTOM_KEY, terrainScanResult.getBottomPosition().getKey());
			data.set(LANDING_BUTTON_KEY, landButtonSettings.getKey());
			data.set(LANDING_BUTTON_NEXT_KEY, landButtonSettings.nextState(terrainScanResult).getKey());
		}

		data.set(GUI_STATE_KEY, guiState.getKey());
		data.set(BLOCK_POS_PAGE_KEY, blockPositionsPage);
		data.set(BLOCK_POS_MAX_PAGE_KEY, (int) Math.ceil(blockPositions.size() / 3.0));
		data.set(COORDINATES_PAGE_KEY, coordinatesPage);
		data.set(COORDINATES_MAX_PAGE_KEY, (int) Math.ceil(coordinates.size() / 6.0));

		int recNum = 0;
		if (blockPositionsPage * 3 < blockPositions.size()) {
			recNum++;
			BlockPosStruct rec1 = blockPositions.get(blockPositionsPage * 3);
			data.set(RECTANGLE_1_X_1_KEY, rec1.getPosition1().getX());
			data.set(RECTANGLE_1_X_2_KEY, rec1.getPosition2().getX());
			data.set(RECTANGLE_1_Y_1_KEY, rec1.getPosition1().getY());
			data.set(RECTANGLE_1_Y_2_KEY, rec1.getPosition2().getY());
			data.set(RECTANGLE_1_Z_1_KEY, rec1.getPosition1().getZ());
			data.set(RECTANGLE_1_Z_2_KEY, rec1.getPosition2().getZ());
		}
		if (blockPositionsPage * 3 + 1 < blockPositions.size()) {
			recNum++;
			BlockPosStruct rec2 = blockPositions.get(blockPositionsPage * 3 + 1);
			data.set(RECTANGLE_2_X_1_KEY, rec2.getPosition1().getX());
			data.set(RECTANGLE_2_X_2_KEY, rec2.getPosition2().getX());
			data.set(RECTANGLE_2_Y_1_KEY, rec2.getPosition1().getY());
			data.set(RECTANGLE_2_Y_2_KEY, rec2.getPosition2().getY());
			data.set(RECTANGLE_2_Z_1_KEY, rec2.getPosition1().getZ());
			data.set(RECTANGLE_2_Z_2_KEY, rec2.getPosition2().getZ());
		}
		if (blockPositionsPage * 3 + 2 < blockPositions.size()) {
			recNum++;
			BlockPosStruct rec3 = blockPositions.get(blockPositionsPage * 3 + 2);
			data.set(RECTANGLE_3_X_1_KEY, rec3.getPosition1().getX());
			data.set(RECTANGLE_3_X_2_KEY, rec3.getPosition2().getX());
			data.set(RECTANGLE_3_Y_1_KEY, rec3.getPosition1().getY());
			data.set(RECTANGLE_3_Y_2_KEY, rec3.getPosition2().getY());
			data.set(RECTANGLE_3_Z_1_KEY, rec3.getPosition1().getZ());
			data.set(RECTANGLE_3_Z_2_KEY, rec3.getPosition2().getZ());
		}
		data.set(BLOCK_POS_ON_THIS_PAGE_KEY, recNum);

		if (vehicleScanResult != null) {
			data.set(WIND_MAX_KEY, (int) getNecessaryMovementPower());
			data.set(WIND_KEY, getMovementPower());
			data.set(FLOATING_MAX_KEY, vehicleScanResult.getDensity());
			data.set(FLOATING_KEY, vehicleScanResult.getFloatingQuotient());
		} else {
			data.set(WIND_MAX_KEY, 0);
			data.set(WIND_KEY, 0);
			data.set(FLOATING_MAX_KEY, 0);
			data.set(FLOATING_KEY, 0);
		}

		data.set(WATER_LINE_KEY, waterLine);
		data.set(COORDINATES_BLINK_KEY, Util.convertBitArrayToInt(getCoordinateBlinkArray()));
	}

	public void addRectangle(int mode, BlockPos blockPos1, BlockPos blockPos2) {
		if (mode == 0) {
			blockPositions.add(new BlockPosStruct(blockPos1, blockPos2, getBlockPos()));
		}
		if (mode > 0 && mode < 4) {
			blockPositions.set(3 * blockPositionsPage + mode - 1, new BlockPosStruct(blockPos1, blockPos2));
		}
		if (mode == 4) {
			blockPositions.add(new BlockPosStruct(blockPos1, blockPos2));
		}
		updateData();
		setChanged();
	}

	public void addCoordinate(int mode, SavedCoordinate savedCoordinate) {
		if (mode == 0) {
			coordinates.add(savedCoordinate);
		}
		if (mode > 0 && mode < 7) {
			coordinates.set(6 * coordinatesPage + mode - 1, savedCoordinate);
		}
		updateData();
		setChanged();
	}

	public void removeCoordinate(int mode) {
		coordinates.remove(6 * coordinatesPage + mode - 1);
		selectedCoordinate = -1;
		if (selectedWarpDirection.equals(WarpDirection.COORDINATE)) {
			selectedWarpDirection = null;
		}
		updateData();
		setChanged();
	}

	public String getUuid() {
		if (uuid == null) {
			uuid = UUID.randomUUID().toString();
		}
		return uuid;
	}


	private boolean[] getBlinkArray() {
		boolean blinkForward = WarpDirection.toButtonId(selectedWarpDirection, getBlockState().getValue(Rudder.FACING)) == ButtonId.FORWARD;
		boolean blinkRight = WarpDirection.toButtonId(selectedWarpDirection, getBlockState().getValue(Rudder.FACING)) == ButtonId.RIGHT;
		boolean blinkBackward = WarpDirection.toButtonId(selectedWarpDirection, getBlockState().getValue(Rudder.FACING)) == ButtonId.BACKWARD;
		boolean blinkLeft = WarpDirection.toButtonId(selectedWarpDirection, getBlockState().getValue(Rudder.FACING)) == ButtonId.LEFT;
		boolean blinkUp = WarpDirection.toButtonId(selectedWarpDirection, getBlockState().getValue(Rudder.FACING)) == ButtonId.UP;
		boolean blinkDown = WarpDirection.toButtonId(selectedWarpDirection, getBlockState().getValue(Rudder.FACING)) == ButtonId.DOWN;

		boolean blinkLand = WarpDirection.toButtonId(selectedWarpDirection, getBlockState().getValue(Rudder.FACING)) == ButtonId.LAND;

		boolean blinkJump = selectedWarpDirection != null && speed > 0;

		boolean blinkBeacon = WarpDirection.toButtonId(selectedWarpDirection, getBlockState().getValue(Rudder.FACING)) == ButtonId.BEACON;

		return new boolean[]{blinkForward, blinkRight, blinkBackward, blinkLeft, blinkUp, blinkDown, blinkLand, blinkJump, blinkBeacon};
	}

	private boolean[] getCoordinateBlinkArray() {
		boolean button1 = selectedCoordinate % 6 == 0;
		boolean button2 = selectedCoordinate % 6 == 1;
		boolean button3 = selectedCoordinate % 6 == 2;
		boolean button4 = selectedCoordinate % 6 == 3;
		boolean button5 = selectedCoordinate % 6 == 4;
		boolean button6 = selectedCoordinate % 6 == 5;

		return new boolean[]{button1, button2, button3, button4, button5, button6};
	}

	private boolean[] getFunctionArray() {
		return new boolean[]{enableHeatEngine, enableSteamEngine, enableEnderEngine, enableHyperDrive};
	}

	private void resetBlockPositionPager() {
		if (blockPositionsPage * 3 >= blockPositions.size()) {
			blockPositionsPage--;
		}
		if (blockPositionsPage < 0) {
			blockPositionsPage = 0;
		}
	}

	private void resetCoordinatePager() {
		if (coordinatesPage * 6 >= coordinates.size()) {
			coordinatesPage--;
		}
		if (coordinatesPage < 0) {
			coordinatesPage = 0;
		}
	}

	public List<SavedCoordinate> getCoordinates() {
		if (Objects.requireNonNull(level).isClientSide()) {
			return coordinates;
		}
		List<SavedCoordinate> result = new ArrayList<>();
		if (coordinatesPage * 6 < coordinates.size()) {
			result.add(coordinates.get(coordinatesPage * 6));
		}
		if (coordinatesPage * 6 + 1 < coordinates.size()) {
			result.add(coordinates.get(coordinatesPage * 6 + 1));
		}
		if (coordinatesPage * 6 + 2 < coordinates.size()) {
			result.add(coordinates.get(coordinatesPage * 6 + 2));
		}
		if (coordinatesPage * 6 + 3 < coordinates.size()) {
			result.add(coordinates.get(coordinatesPage * 6 + 3));
		}
		if (coordinatesPage * 6 + 4 < coordinates.size()) {
			result.add(coordinates.get(coordinatesPage * 6 + 4));
		}
		if (coordinatesPage * 6 + 5 < coordinates.size()) {
			result.add(coordinates.get(coordinatesPage * 6 + 5));
		}
		return result;
	}
}
