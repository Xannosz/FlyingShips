package hu.xannosz.flyingships.screen;

import hu.xannosz.flyingships.block.ModBlocks;
import hu.xannosz.flyingships.blockentity.MarkerBlockEntity;
import hu.xannosz.flyingships.networking.GetMarkerNamePacket;
import hu.xannosz.flyingships.networking.ModMessages;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class MarkerMenu extends AbstractContainerMenu {

	@Getter
	private final MarkerBlockEntity blockEntity;
	private final Level level;

	private final ContainerData data;

	public MarkerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
		this(containerId, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(1));
	}

	public MarkerMenu(int containerId, Inventory inv, BlockEntity blockEntity, ContainerData data) {
		super(ModMenuTypes.MARKER_MENU.get(), containerId);

		checkContainerSize(inv, 0);
		this.blockEntity = ((MarkerBlockEntity) blockEntity);
		level = inv.player.level;

		this.data = data;

		addDataSlots(data);
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int num) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
				player, ModBlocks.MARKER.get());
	}

	public void updateName() {
		ModMessages.sendToServer(new GetMarkerNamePacket(blockEntity.getBlockPos()));
	}

	public String getMarkerName() {
		return blockEntity.getMarkerName();
	}

	public boolean isEnabled() {
		return data.get(0) == 1;
	}
}
