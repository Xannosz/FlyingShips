package hu.xannosz.flyingships.blockentity;

import hu.xannosz.flyingships.Util;
import hu.xannosz.flyingships.block.Rudder;
import hu.xannosz.flyingships.config.FlyingShipsConfiguration;
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
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static hu.xannosz.flyingships.Util.CLOUD_LEVEL;
import static net.minecraft.world.item.crafting.RecipeType.SMELTING;
import static net.minecraftforge.common.ForgeHooks.getBurnTime;

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

	public static final int WATER_KEY = 41;
	public static final int WATER_MAX_KEY = 42;
	public static final int STEAM_KEY = 43;
	public static final int STEAM_MAX_KEY = 44;
	public static final int HEAT_KEY = 45;
	public static final int HEAT_MAX_KEY = 46;
	public static final int ENDER_KEY = 47;
	public static final int ENDER_MAX_KEY = 48;
	public static final int FURNACE_KEY = 49;
	public static final int BURN_TIME_KEY = 50;
	public static final int BURN_TIME_MAX_KEY = 51;
	public static final int DATA_SLOT_SIZE = 52;

	public static final int[] STEPS = new int[]{1, 10, 100, 1000};

	// inside data
	@Getter
	private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
		}

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack) {
			return switch (slot) {
				case 0 -> stack.getItem() == Items.ENDER_PEARL || stack.getItem() == Items.ENDER_EYE;
				case 1 -> stack.getItem() == Items.WATER_BUCKET;
				case 2 -> getBurnTime(stack, SMELTING) > 0;
				default -> super.isItemValid(slot, stack);
			};
		}
	};
	@Getter
	private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
	private String uuid = null;
	private int selectedCoordinate = -1;
	private WarpDirection selectedWarpDirection;
	private TerrainScanResponseStruct terrainScanResult;
	private VehicleScanResponseStruct vehicleScanResult;
	@Getter
	private int clock = 0;

	// gui data
	private int speed = 50;
	private int step = 0;

	private int blockPositionsPage = 0;
	private int coordinatesPage = 0;
	private int coolDown = FlyingShipsConfiguration.COOL_DOWN_TIME.get();
	private int waterLine = 0;
	@Getter
	private boolean enableHeatEngine = false;
	@Getter
	private boolean enableSteamEngine = false;
	@Getter
	private boolean enableEnderEngine = false;
	private PowerState powerButtonState = PowerState.OFF;
	private GuiState guiState = GuiState.MAIN;
	private LandButtonSettings landButtonSettings = LandButtonSettings.VOID;
	private final ContainerData data = new SimpleContainerData(DATA_SLOT_SIZE);

	// jump data
	private List<BlockPosStruct> blockPositions = new ArrayList<>();
	@Setter
	private List<SavedCoordinate> coordinates = new ArrayList<>();
	private int necessarySpeed = 0;

	// engine data
	private int enderEnergy = 0;
	private int heatEnergy = 0;
	private int burnTime;
	private int maxBurnTime;
	private int steamEnergy = 0;
	private int waterContent = 0;

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

		tag.putInt("rudder.waterContent", waterContent);
		tag.putInt("rudder.steamEnergy", steamEnergy);
		tag.putInt("rudder.heatEnergy", heatEnergy);
		tag.putInt("rudder.enderEnergy", enderEnergy);
		tag.putInt("rudder.burnTime", burnTime);
		tag.putInt("rudder.maxBurnTime", maxBurnTime);

		tag.putBoolean("rudder.enableHeatEngine", enableHeatEngine);
		tag.putBoolean("rudder.enableSteamEngine", enableSteamEngine);
		tag.putBoolean("rudder.enableEnderEngine", enableEnderEngine);

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

		waterContent = nbt.getInt("rudder.waterContent");
		steamEnergy = nbt.getInt("rudder.steamEnergy");
		heatEnergy = nbt.getInt("rudder.heatEnergy");
		enderEnergy = nbt.getInt("rudder.enderEnergy");
		burnTime = nbt.getInt("rudder.burnTime");
		maxBurnTime = nbt.getInt("rudder.maxBurnTime");

		enableHeatEngine = nbt.getBoolean("rudder.enableHeatEngine");
		enableSteamEngine = nbt.getBoolean("rudder.enableSteamEngine");
		enableEnderEngine = nbt.getBoolean("rudder.enableEnderEngine");

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
		coolDown--;
		if (coolDown < 0) {
			coolDown = 0;
		}
		clock++;
		if (clock > 100) {
			clock = 0;
		}

		if (clock % 5 == 0) {
			vehicleScanResult = VehicleScanUtil.scanVehicle((ServerLevel) level, JumpUtil.createRectangles(getBlockPos(), blockPositions), getBlockState().getValue(Rudder.FACING));
			normalizeEnergies();

			consumeHeatToHoldSteam();

			if (powerButtonState.equals(PowerState.ON)) {
				consumeWaterBucket();
				createSteam();
			}

			if (!powerButtonState.equals(PowerState.OFF)) {
				keepFurnaceHeat();
			}
		}

		if (clock % 25 == 0) {
			if (powerButtonState.equals(PowerState.ON)) {
				consumeEnderPearl();
				calculateJumpData();
			}
		}

		updateData();
		setChanged();
	}

	private void normalizeEnergies() {
		if (heatEnergy > getMaxHeatEnergy()) {
			heatEnergy = getMaxHeatEnergy();
		}
		if (steamEnergy > getMaxSteamEnergy()) {
			steamEnergy = getMaxSteamEnergy();
		}
		if (waterContent > getMaxWaterContent()) {
			waterContent = getMaxWaterContent();
		}
		if (enderEnergy > getMaxEnderEnergy()) {
			enderEnergy = getMaxEnderEnergy();
		}

		heatEnergy--;
		if (heatEnergy < 0) {
			heatEnergy = 0;
		}
		burnTime--;
		if (burnTime < 0) {
			burnTime = 0;
		}
	}

	private void consumeHeatToHoldSteam() {
		int necessaryEnergy = (int) Math.ceil(steamEnergy / 350d);
		if (enableSteamEngine && getFurnaceHeat() > 100) {
			if (heatEnergy > necessaryEnergy) {
				heatEnergy -= necessaryEnergy;
				necessaryEnergy = 0;
			} else {
				necessaryEnergy -= heatEnergy;
				heatEnergy = 0;
			}
		}

		steamEnergy -= necessaryEnergy;
		if (steamEnergy < 0) {
			steamEnergy = 0;
		}
	}

	private void keepFurnaceHeat() {
		if (!enableHeatEngine) {
			return;
		}
		for (int i = 0; i < vehicleScanResult.getHeater() * 10; i++) {
			if (heatEnergy >= getMaxHeatEnergy()) {
				return;
			}
			if (burnTime <= 0) {
				consumeFuel();
			} else {
				burnTime--;
				heatEnergy++;
			}
		}
	}

	private void consumeFuel() {
		final int fuelValue = getBurnTime(itemHandler.getStackInSlot(2), SMELTING);
		if (itemHandler.getStackInSlot(2).getItem().equals(Items.LAVA_BUCKET)) {
			itemHandler.setStackInSlot(2, new ItemStack(Items.BUCKET, 1));
		} else {
			itemHandler.getStackInSlot(2).shrink(1);
		}
		burnTime = fuelValue;
		maxBurnTime = fuelValue;
	}

	private void consumeEnderPearl() {
		if (!enableEnderEngine) {
			return;
		}
		if (getMaxEnderEnergy() - enderEnergy > FlyingShipsConfiguration.ENERGY_PER_ENDER_PEARL.get() && itemHandler.getStackInSlot(0).getItem().equals(Items.ENDER_PEARL)) {
			itemHandler.getStackInSlot(0).shrink(1);
			enderEnergy += FlyingShipsConfiguration.ENERGY_PER_ENDER_PEARL.get();
		}
	}

	private void consumeWaterBucket() {
		if (!enableSteamEngine) {
			if (waterContent > 0) {
				waterContent--;
			}
			return;
		}
		if (getMaxWaterContent() >= waterContent + 1000 && itemHandler.getStackInSlot(1).getItem().equals(Items.WATER_BUCKET)) {
			waterContent += 1000;
			itemHandler.setStackInSlot(1, new ItemStack(Items.BUCKET, 1));
		}
	}

	private void createSteam() {
		if (!enableSteamEngine) {
			return;
		}
		if (getFurnaceHeat() > 100) {
			for (int i = 0; i < vehicleScanResult.getHeater(); i++) {
				if (heatEnergy >= 5 && waterContent > 0 && getMaxSteamEnergy() > steamEnergy + 1) {
					heatEnergy -= 5;
					waterContent--;
					steamEnergy += 2;
				} else {
					return;
				}
			}
		}
	}

	private void calculateJumpData() {
		LiveDataPackage terrainScanMasks = TerrainScanUtil.generateMasks(JumpUtil.createRectangles(getBlockPos(), blockPositions));
		terrainScanResult = TerrainScanUtil.scanTerrain((ServerLevel) level, terrainScanMasks);
	}

	public void executeButtonClick(ButtonId buttonId) {
		switch (buttonId) {
			case UP, DOWN, RIGHT, LEFT, FORWARD, BACKWARD ->
					selectedWarpDirection = WarpDirection.fromBlockDirection(buttonId, getBlockState().getValue(Rudder.FACING));
			case LAND -> {
				landButtonSettings = landButtonSettings.nextState(terrainScanResult);
				if (vehicleScanResult == null) {
					break;
				}
				if (!landButtonSettings.equals(LandButtonSettings.EMPTY)) {
					selectedWarpDirection = WarpDirection.fromBlockDirection(buttonId, getBlockState().getValue(Rudder.FACING));
					switch (landButtonSettings) {
						case VOID ->
								necessarySpeed = Math.abs(CLOUD_LEVEL + waterLine - vehicleScanResult.getBottomY());
						case LAND -> necessarySpeed = terrainScanResult.getHeightOfBottom();
						case TOUCH_CELLING -> necessarySpeed = terrainScanResult.getHeightOfCelling();
						case SWIM_LAVA, SWIM_WATER ->
								necessarySpeed = Math.abs(terrainScanResult.getHeightOfBottom() + waterLine);
					}
				} else {
					if (selectedWarpDirection.equals(WarpDirection.LAND)) {
						selectedWarpDirection = null;
					}
				}
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
			case PLUS -> speed += STEPS[step];
			case MINUS -> {
				speed -= STEPS[step];
				if (speed < 0) {
					speed = 0;
				}
			}
			case JUMP -> {
				calculateJumpData();
				if (isValidJumpContext()) {
					//store energy
					final int storedEnderEnergy = enderEnergy;
					final int storedHeatEnergy = heatEnergy;
					final int storedSteamEnergy = steamEnergy;
					final boolean isHyperJump = isHyperDriveEnabled();
					//consume energy
					consumeEnergy();
					setChanged();
					JumpUtil.jump((ServerLevel) level, getBlockPos(), blockPositions, getAdditional());
					//restore energy if jump not success
					enderEnergy = storedEnderEnergy;
					heatEnergy = storedHeatEnergy;
					steamEnergy = storedSteamEnergy;
					if (isHyperJump) {
						itemHandler.setStackInSlot(0, new ItemStack(Items.ENDER_EYE, 1));
					}
				}
			}
			case POWER -> {
				powerButtonState = powerButtonState.nextState(enableSteamEngine);
				if (!powerButtonState.equals(PowerState.ON)) {
					selectedWarpDirection = null;
					landButtonSettings = LandButtonSettings.VOID;
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
			case WATER_LINE_UP -> {
				waterLine++;
				if (waterLine > 0) {
					waterLine = 0;
				}
			}
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
				necessarySpeed = coordinates.get(selectedCoordinate).getCoordinate().distManhattan(getBlockPos());
			}
			case JUMP_TO_COORDINATE_2 -> {
				selectedCoordinate = coordinatesPage * 6 + 1;
				selectedWarpDirection = WarpDirection.COORDINATE;
				necessarySpeed = coordinates.get(selectedCoordinate).getCoordinate().distManhattan(getBlockPos());
			}
			case JUMP_TO_COORDINATE_3 -> {
				selectedCoordinate = coordinatesPage * 6 + 2;
				selectedWarpDirection = WarpDirection.COORDINATE;
				necessarySpeed = coordinates.get(selectedCoordinate).getCoordinate().distManhattan(getBlockPos());
			}
			case JUMP_TO_COORDINATE_4 -> {
				selectedCoordinate = coordinatesPage * 6 + 3;
				selectedWarpDirection = WarpDirection.COORDINATE;
				necessarySpeed = coordinates.get(selectedCoordinate).getCoordinate().distManhattan(getBlockPos());
			}
			case JUMP_TO_COORDINATE_5 -> {
				selectedCoordinate = coordinatesPage * 6 + 4;
				selectedWarpDirection = WarpDirection.COORDINATE;
				necessarySpeed = coordinates.get(selectedCoordinate).getCoordinate().distManhattan(getBlockPos());
			}
			case JUMP_TO_COORDINATE_6 -> {
				selectedCoordinate = coordinatesPage * 6 + 5;
				selectedWarpDirection = WarpDirection.COORDINATE;
				necessarySpeed = coordinates.get(selectedCoordinate).getCoordinate().distManhattan(getBlockPos());
			}
			case ENDER_ENGINE -> enableEnderEngine = !enableEnderEngine;
			case STEAM_ENGINE -> {
				enableSteamEngine = !enableSteamEngine;
				if (enableSteamEngine) {
					enableHeatEngine = true;
				}
			}
			case HEAT_ENGINE -> {
				enableHeatEngine = !enableHeatEngine;
				if (!enableHeatEngine) {
					enableSteamEngine = false;
				}
			}
		}
		updateSpeed();
		updateData();
		setChanged();
	}

	private void consumeEnergy() {
		int necessaryFloatingEnergy = Math.max(vehicleScanResult.getDensity() - getStructuralFloatingPower(), 0);
		int necessaryMovementEnergy = Math.max(getNecessaryMovementPower() - vehicleScanResult.getWindSurface() * FlyingShipsConfiguration.WIND_MULTIPLIER.get(), 0);
		if (enableSteamEngine) {
			necessaryFloatingEnergy -= steamEnergy;
			if (necessaryMovementEnergy >= steamEnergy) {
				necessaryMovementEnergy -= steamEnergy;
				steamEnergy = 0;
			} else {
				steamEnergy -= necessaryMovementEnergy;
				necessaryMovementEnergy = 0;
			}
		} else if (enableHeatEngine) {
			if (necessaryFloatingEnergy >= heatEnergy) {
				necessaryFloatingEnergy -= heatEnergy;
				heatEnergy = 0;
			} else {
				heatEnergy -= necessaryFloatingEnergy;
				necessaryFloatingEnergy = 0;
			}
		}

		if (enableEnderEngine) {
			if (necessaryFloatingEnergy >= enderEnergy) {
				enderEnergy = 0;
			} else {
				enderEnergy -= necessaryFloatingEnergy;
			}
			if (necessaryMovementEnergy >= enderEnergy) {
				enderEnergy = 0;
			} else {
				enderEnergy -= necessaryMovementEnergy;
			}
		}

		if (isHyperDriveEnabled()) {
			itemHandler.getStackInSlot(0).shrink(1);
		}
	}

	private void updateSpeed() {
		if (isNormalDirection()) {
			necessarySpeed = speed;
		}
	}

	private boolean isNormalDirection() {
		return Arrays.asList(WarpDirection.UP, WarpDirection.DOWN, WarpDirection.NORTH, WarpDirection.SOUTH, WarpDirection.EAST, WarpDirection.WEST).contains(selectedWarpDirection);
	}

	private boolean isValidJumpContext() {
		return selectedWarpDirection != null && necessarySpeed > 0 && powerButtonState.equals(PowerState.ON) &&
				getFloatingPower() >= vehicleScanResult.getDensity() &&
				hasEnoughPowerForMovement() && (!collusionOnY() || isHyperDriveEnabled()) && coolDown < 1 &&
				(isNormalDirection() || !isHyperDriveEnabled());
	}

	private boolean collusionOnY() {
		if (selectedWarpDirection.equals(WarpDirection.UP) && terrainScanResult.getHeightOfCelling() > -1) {
			return necessarySpeed > terrainScanResult.getHeightOfCelling();
		}
		if (selectedWarpDirection.equals(WarpDirection.DOWN) && terrainScanResult.getHeightOfBottom() > -1) {
			return necessarySpeed > terrainScanResult.getHeightOfBottom();
		}
		return false;
	}

	private Vec3 getAdditional() {
		Vec3 perturbation = new Vec3(
				isHyperDriveEnabled() ? new Random().nextInt(-300, 300) : 0, 0,
				isHyperDriveEnabled() ? new Random().nextInt(-300, 300) : 0);
		switch (selectedWarpDirection) {
			case UP -> {
				return new Vec3(0, necessarySpeed, 0);
			}
			case DOWN -> {
				return new Vec3(0, -necessarySpeed, 0);
			}
			case NORTH -> {
				return new Vec3(0, 0, -necessarySpeed).add(perturbation);
			}
			case SOUTH -> {
				return new Vec3(0, 0, necessarySpeed).add(perturbation);
			}
			case EAST -> {
				return new Vec3(necessarySpeed, 0, 0).add(perturbation);
			}
			case WEST -> {
				return new Vec3(-necessarySpeed, 0, 0).add(perturbation);
			}
			case LAND -> {
				switch (landButtonSettings) {
					case VOID -> {
						return new Vec3(0, CLOUD_LEVEL + waterLine - vehicleScanResult.getBottomY(), 0);
					}
					case LAND -> {
						return new Vec3(0, -terrainScanResult.getHeightOfBottom(), 0);
					}
					case TOUCH_CELLING -> {
						return new Vec3(0, terrainScanResult.getHeightOfCelling(), 0);
					}
					case SWIM_LAVA, SWIM_WATER -> {
						return new Vec3(0, -terrainScanResult.getHeightOfBottom() + waterLine, 0);
					}
				}
			}
			case COORDINATE -> {
				BlockPos coordinate = coordinates.get(selectedCoordinate).getCoordinate();
				return new Vec3(coordinate.getX() - getBlockPos().getX(), coordinate.getY() - getBlockPos().getY(), coordinate.getZ() - getBlockPos().getZ());
			}
		}
		return new Vec3(0, 0, 0);
	}

	private boolean hasEnoughPowerForMovement() {
		if (FlyingShipsConfiguration.ENABLE_SLIDING.get() && necessarySpeed <= 5) {
			return true;
		}

		return getMovementPower() > getNecessaryMovementPower();
	}

	private int getNecessaryMovementPower() {
		return (int) (vehicleScanResult.getDensity() * Math.pow(1 + necessarySpeed / FlyingShipsConfiguration.SPEED_CONSOLIDATOR.get(), 3) / (isHyperDriveEnabled() ? 200 : 1));
	}

	private int getMovementPower() {
		return vehicleScanResult.getWindSurface() * FlyingShipsConfiguration.WIND_MULTIPLIER.get() +
				steamEnergy + getLeftoverEnderEnergy();
	}

	private int getLeftoverEnderEnergy() {
		final int usedEnderEnergy = Math.max(vehicleScanResult.getDensity() - getStructuralFloatingPower() - getHeatSteamFloatingPower(), 0);
		return Math.max(enderEnergy - usedEnderEnergy, 0);
	}

	private int getFloatingPower() {
		return getStructuralFloatingPower() + enderEnergy + getHeatSteamFloatingPower();
	}

	private int getStructuralFloatingPower() {
		return vehicleScanResult.getLiftSurface() * FlyingShipsConfiguration.LIFT_MULTIPLIER.get() +
				vehicleScanResult.getWool() * FlyingShipsConfiguration.BALLOON_MULTIPLIER.get();
	}

	private int getHeatSteamFloatingPower() {
		return (enableSteamEngine ? steamEnergy : heatEnergy);
	}

	private int getMaxEnderEnergy() {
		return vehicleScanResult.getEnderOscillator() * FlyingShipsConfiguration.ENERGY_PER_OSCILLATOR.get();
	}

	private int getMaxHeatEnergy() {
		return vehicleScanResult.getHeater() * FlyingShipsConfiguration.ENERGY_PER_HEATER.get();
	}

	private int getMaxSteamEnergy() {
		return vehicleScanResult.getTank() * FlyingShipsConfiguration.STEAM_PER_TANK.get();
	}

	private int getMaxWaterContent() {
		return vehicleScanResult.getTank() * FlyingShipsConfiguration.WATER_PER_TANK.get();
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
			data.set(WIND_MAX_KEY, getNecessaryMovementPower());
			data.set(WIND_KEY, getMovementPower());
			data.set(FLOATING_MAX_KEY, vehicleScanResult.getDensity());
			data.set(FLOATING_KEY, getFloatingPower());

			data.set(WATER_KEY, waterContent);
			data.set(WATER_MAX_KEY, getMaxWaterContent());
			data.set(STEAM_KEY, steamEnergy);
			data.set(STEAM_MAX_KEY, getMaxSteamEnergy());
			data.set(HEAT_KEY, heatEnergy);
			data.set(HEAT_MAX_KEY, getMaxHeatEnergy());
			data.set(ENDER_KEY, enderEnergy);
			data.set(ENDER_MAX_KEY, getMaxEnderEnergy());
			data.set(FURNACE_KEY, getFurnaceHeat());
			data.set(BURN_TIME_KEY, burnTime);
			data.set(BURN_TIME_MAX_KEY, maxBurnTime);
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

	private int getFurnaceHeat() {
		return (int) ((heatEnergy) * 491d / getMaxHeatEnergy()) + 32;
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
		boolean button1 = selectedCoordinate % 6 == 0 && WarpDirection.COORDINATE.equals(selectedWarpDirection);
		boolean button2 = selectedCoordinate % 6 == 1 && WarpDirection.COORDINATE.equals(selectedWarpDirection);
		boolean button3 = selectedCoordinate % 6 == 2 && WarpDirection.COORDINATE.equals(selectedWarpDirection);
		boolean button4 = selectedCoordinate % 6 == 3 && WarpDirection.COORDINATE.equals(selectedWarpDirection);
		boolean button5 = selectedCoordinate % 6 == 4 && WarpDirection.COORDINATE.equals(selectedWarpDirection);
		boolean button6 = selectedCoordinate % 6 == 5 && WarpDirection.COORDINATE.equals(selectedWarpDirection);

		return new boolean[]{button1, button2, button3, button4, button5, button6};
	}

	private boolean[] getFunctionArray() {
		return new boolean[]{enableHeatEngine, enableSteamEngine, enableEnderEngine, isHyperDriveEnabled()};
	}

	private boolean isHyperDriveEnabled() {
		return itemHandler.getStackInSlot(0).getItem().equals(Items.ENDER_EYE) && vehicleScanResult.isHyperDriveEngineFound();
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
