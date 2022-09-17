package hu.xannosz.flyingships.blockentity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class SubRudderBlockEntity extends BlockEntity {

	@Getter
	private BlockPos rudderPosition;

	public SubRudderBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ModBlockEntities.SUB_RUDDER_BLOCK_ENTITY.get(), blockPos, blockState);
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

	public void setRudderPosition(BlockPos blockPos) {
		rudderPosition = blockPos;
		setChanged();
	}
}
