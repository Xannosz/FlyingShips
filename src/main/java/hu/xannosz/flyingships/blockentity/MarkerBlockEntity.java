package hu.xannosz.flyingships.blockentity;

import hu.xannosz.flyingships.screen.MarkerMenu;
import hu.xannosz.flyingships.screen.widget.ButtonId;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MarkerBlockEntity extends BlockEntity implements MenuProvider, ButtonUser {

	@Getter
	private String markerName;
	@Getter
	@Setter
	private boolean enabled = true;

	private final ContainerData data = new SimpleContainerData(1);

	public MarkerBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ModBlockEntities.MARKER_BLOCK_ENTITY.get(), blockPos, blockState);
		markerName = null;
		data.set(0, enabled ? 1 : 0);
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag) {
		if (markerName == null) {
			markerName = "";
		}
		tag.putString("marker.name", markerName);
		tag.putBoolean("marker.enabled", enabled);

		super.saveAdditional(tag);
	}

	@Override
	public void load(@NotNull CompoundTag nbt) {
		super.load(nbt);
		markerName = nbt.getString("marker.name");
		enabled = nbt.getBoolean("marker.enabled");
		data.set(0, enabled ? 1 : 0);
	}

	public void setMarkerName(String markerName) {
		this.markerName = markerName;
		setChanged();
	}

	@Override
	public @NotNull Component getDisplayName() {
		return Component.literal("Marker block");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
		data.set(0, enabled ? 1 : 0);
		return new MarkerMenu(containerId, inventory, this, data);
	}

	@Override
	public void executeButtonClick(ButtonId buttonId) {
		if (buttonId == ButtonId.MARKER_SET_ENABLED) {
			enabled = !enabled;
		}
		data.set(0, enabled ? 1 : 0);
		setChanged();
	}
}
