package hu.xannosz.flyingships.blockentity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MarkerBlockEntity extends BlockEntity {

	@Getter
	private String markerName;

	public MarkerBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ModBlockEntities.MARKER_BLOCK_ENTITY.get(), blockPos, blockState);
		markerName = "";
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag) {
		tag.putString("marker.name", markerName);

		super.saveAdditional(tag);
	}

	@Override
	public void load(@NotNull CompoundTag nbt) {
		super.load(nbt);
		markerName = nbt.getString("marker.name");
	}

	public void setRudderPosition(String markerName) {
		this.markerName = markerName;
		setChanged();
	}
}
