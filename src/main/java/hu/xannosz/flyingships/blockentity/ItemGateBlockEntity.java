package hu.xannosz.flyingships.blockentity;

import hu.xannosz.flyingships.block.ItemGate;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;
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
import java.util.Map;

public class ItemGateBlockEntity extends BlockEntity {

	@Getter
	private BlockPos rudderPosition;

	public ItemGateBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ModBlockEntities.ITEM_GATE_BLOCK_ENTITY.get(), blockPos, blockState);
		rudderPosition = new BlockPos(0, 0, 0);
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag) {
		tag.putInt("rudder.x", rudderPosition.getX());
		tag.putInt("rudder.y", rudderPosition.getY());
		tag.putInt("rudder.z", rudderPosition.getZ());

		super.saveAdditional(tag);
	}

	@Override
	public void load(@NotNull CompoundTag nbt) {
		super.load(nbt);
		rudderPosition = new BlockPos(nbt.getInt("rudder.x"), nbt.getInt("rudder.y"), nbt.getInt("rudder.z"));
	}

	@Nonnull
	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {

			if (level == null) {
				return super.getCapability(cap, side);
			}

			BlockEntity entity = level.getBlockEntity(getBlockPos().offset(rudderPosition));
			if (entity instanceof RudderBlockEntity) {
				final LazyOptional<IItemHandler> lazyItemHandler = ((RudderBlockEntity) entity).getLazyItemHandler().cast();
				final ItemStackHandler itemHandler = ((RudderBlockEntity) entity).getItemHandler();

				final Map<Direction, LazyOptional<WrappedHandler>> directionWrappedHandlerMap =
						Map.of(Direction.DOWN, LazyOptional.of(() -> new WrappedHandler(itemHandler, (index, stack) ->
										(index == 1 || index == 2) && stack.getItem() == Items.BUCKET, (i, s) -> false)),
								Direction.UP, LazyOptional.of(() -> new WrappedHandler(itemHandler, (i, s) -> false,
										(index, stack) -> index == 1 && itemHandler.isItemValid(1, stack) && ((RudderBlockEntity) entity).isEnableSteamEngine())),
								Direction.NORTH, LazyOptional.of(() -> new WrappedHandler(itemHandler, (i, s) -> false, (i, s) -> false)),
								Direction.SOUTH, LazyOptional.of(() -> new WrappedHandler(itemHandler, (i, s) -> false, (i, s) -> false)),
								Direction.EAST, LazyOptional.of(() -> new WrappedHandler(itemHandler, (i, s) -> false,
										(index, stack) -> index == 2 && itemHandler.isItemValid(2, stack) && ((RudderBlockEntity) entity).isEnableHeatEngine())),
								Direction.WEST, LazyOptional.of(() -> new WrappedHandler(itemHandler, (i, s) -> false,
										(index, stack) -> index == 0 && itemHandler.isItemValid(0, stack) && ((RudderBlockEntity) entity).isEnableEnderEngine())));

				if (side == null) {
					return lazyItemHandler.cast();
				}

				if (directionWrappedHandlerMap.containsKey(side)) {
					Direction localDir = this.getBlockState().getValue(ItemGate.FACING);

					if (side == Direction.UP || side == Direction.DOWN) {
						return directionWrappedHandlerMap.get(side).cast();
					}

					return switch (localDir) {
						default -> directionWrappedHandlerMap.get(side.getOpposite()).cast();
						case EAST -> directionWrappedHandlerMap.get(side.getClockWise()).cast();
						case SOUTH -> directionWrappedHandlerMap.get(side).cast();
						case WEST -> directionWrappedHandlerMap.get(side.getCounterClockWise()).cast();
					};
				}
			}
		}

		return super.getCapability(cap, side);
	}

	public void setRudderPosition(BlockPos blockPos) {
		rudderPosition = blockPos;
		setChanged();
	}
}
