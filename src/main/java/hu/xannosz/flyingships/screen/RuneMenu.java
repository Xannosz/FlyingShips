package hu.xannosz.flyingships.screen;

import hu.xannosz.flyingships.block.ModBlocks;
import hu.xannosz.flyingships.blockentity.RuneBlockEntity;
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

public class RuneMenu extends AbstractContainerMenu {

	@Getter
	private final RuneBlockEntity blockEntity;
	private final Level level;

	private final ContainerData data;

	public RuneMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
		this(containerId, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(1));
	}

	public RuneMenu(int containerId, Inventory inv, BlockEntity blockEntity, ContainerData data) {
		super(ModMenuTypes.RUNE_MENU.get(), containerId);

		checkContainerSize(inv, 0);
		this.blockEntity = ((RuneBlockEntity) blockEntity);
		level = inv.player.level;
		this.data = data;

		addDataSlots(data);
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
				player, ModBlocks.RUNE.get());
	}

	public boolean isWandEnabled() {
		return data.get(0) % 2 == 0;
	}

	public int getRedstoneType() {
		return data.get(0) % 2 == 0 ? data.get(0) / 2 : data.get(0);
	}
}
