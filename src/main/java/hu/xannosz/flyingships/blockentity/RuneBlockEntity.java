package hu.xannosz.flyingships.blockentity;

import hu.xannosz.flyingships.config.FlyingShipsConfiguration;
import hu.xannosz.flyingships.screen.RuneMenu;
import hu.xannosz.flyingships.screen.widget.ButtonId;
import hu.xannosz.flyingships.warp.jump.JumpUtil;
import hu.xannosz.flyingships.warp.scan.Scanner;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class RuneBlockEntity extends BlockEntity implements MenuProvider, ButtonUser {

	private boolean enableWand = true;
	private int redstone = 1; // disable, down, up, near

	private ButtonId buttonId;
	private int distance = 0;
	private final ContainerData data = new SimpleContainerData(1);

	private long lastSignal = 0;

	public RuneBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ModBlockEntities.RUNE_BLOCK_ENTITY.get(), blockPos, blockState);
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag) {
		tag.putBoolean("rune.enableWand", enableWand);
		tag.putInt("rune.redstone", redstone);
		super.saveAdditional(tag);
	}

	@Override
	public void load(@NotNull CompoundTag nbt) {
		super.load(nbt);
		enableWand = nbt.getBoolean("rune.enableWand");
		redstone = nbt.getInt("rune.redstone");
		data.set(0, enableWand ? redstone * 2 : redstone);
	}

	@Override
	public @NotNull Component getDisplayName() {
		return Component.literal("Rune block");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
		data.set(0, enableWand ? redstone * 2 : redstone);
		return new RuneMenu(containerId, inventory, this, data);
	}

	@Override
	public void executeButtonClick(ButtonId buttonId) {
		switch (buttonId) {
			case RUNE_GO_UP, RUNE_GO_Y_P, RUNE_GO_Z_M,
					RUNE_GO_X_M, RUNE_GO_NEAR, RUNE_GO_X_P,
					RUNE_GO_Z_P, RUNE_GO_Y_M, RUNE_GO_DOWN -> {
				if (this.buttonId != null && this.buttonId.equals(buttonId)) {
					distance++;
				} else {
					distance = 1;
				}
				this.buttonId = buttonId;
			}
			case RUNE_SWITCH_WAND -> enableWand = !enableWand;
			case RUNE_OK -> jump();
			case RUNE_SWITCH_REDSTONE -> {
				redstone = redstone + 2;
				if (redstone > 7) {
					redstone = 1;
				}
			}
		}
		data.set(0, enableWand ? redstone * 2 : redstone);
		setChanged();
	}

	public void redstoneSignal() {
		long time = System.currentTimeMillis();
		if (time - lastSignal > 2500) {
			lastSignal = time;
			distance = 1;
			switch (redstone) {
				case 1 -> buttonId = null;
				case 3 -> buttonId = ButtonId.RUNE_GO_DOWN;
				case 5 -> buttonId = ButtonId.RUNE_GO_UP;
				case 7 -> buttonId = ButtonId.RUNE_GO_NEAR;
			}
			jump();
		}
	}

	public void wandSignalUp() {
		buttonId = ButtonId.RUNE_GO_UP;
		jump();
	}

	public void wandSignalDown() {
		buttonId = ButtonId.RUNE_GO_DOWN;
		jump();
	}

	private void jump() {
		if (buttonId == null) {
			return;
		}

		final BlockPos target = buttonId == ButtonId.RUNE_GO_DOWN || buttonId == ButtonId.RUNE_GO_UP ?
				getTargetY() : getTargetRune();

		if (target != null) {
			JumpUtil.useRune((ServerLevel) level, getBlockPos(), target);
		}

		buttonId = null;
		distance = 0;
	}

	private BlockPos getTargetY() {
		if (level == null) {
			return null;
		}

		if (buttonId == ButtonId.RUNE_GO_DOWN) {
			int y = getBlockPos().getY();
			boolean inAir = false;
			while (y > Scanner.BOTTOM_OF_MAP) {
				y--;
				final Block block = level.getBlockState(new BlockPos(getBlockPos().getX(), y, getBlockPos().getZ())).getBlock();
				if (inAir) {
					if (!block.equals(Blocks.AIR) && !block.equals(Blocks.VOID_AIR)) {
						return new BlockPos(getBlockPos().getX(), y + 1, getBlockPos().getZ());
					}
				} else {
					if (block.equals(Blocks.AIR) || block.equals(Blocks.VOID_AIR)) {
						inAir = true;
					}
				}
			}
		} else {
			int y = getBlockPos().getY();
			boolean inAir = true;
			while (y < Scanner.TOP_OF_MAP) {
				y++;
				final Block block = level.getBlockState(new BlockPos(getBlockPos().getX(), y, getBlockPos().getZ())).getBlock();
				if (inAir) {
					if (!block.equals(Blocks.AIR) && !block.equals(Blocks.VOID_AIR)) {
						inAir = false;
					}
				} else {
					if (block.equals(Blocks.AIR) || block.equals(Blocks.VOID_AIR)) {
						return new BlockPos(getBlockPos().getX(), y, getBlockPos().getZ());
					}
				}
			}
		}

		return null;
	}

	private BlockPos getTargetRune() {
		final Map<Integer, BlockPos> positions = new HashMap<>();
		int maxDistance = 0;
		for (BlockPos rune : getRunesInRange()) {
			BlockPos dist = rune.subtract(getBlockPos());
			switch (buttonId) {
				case RUNE_GO_X_P -> {
					if (dist.getX() > 0) {
						positions.put(dist.getX(), rune);
						if (dist.getX() > maxDistance) {
							maxDistance = dist.getX();
						}
					}
				}
				case RUNE_GO_X_M -> {
					if (dist.getX() < 0) {
						positions.put(-dist.getX(), rune);
						if (-dist.getX() > maxDistance) {
							maxDistance = -dist.getX();
						}
					}
				}
				case RUNE_GO_Y_P -> {
					if (dist.getY() > 0) {
						positions.put(dist.getY(), rune);
						if (dist.getY() > maxDistance) {
							maxDistance = dist.getY();
						}
					}
				}
				case RUNE_GO_Y_M -> {
					if (dist.getY() < 0) {
						positions.put(-dist.getY(), rune);
						if (-dist.getY() > maxDistance) {
							maxDistance = -dist.getY();
						}
					}
				}
				case RUNE_GO_Z_P -> {
					if (dist.getZ() > 0) {
						positions.put(dist.getZ(), rune);
						if (dist.getZ() > maxDistance) {
							maxDistance = dist.getZ();
						}
					}
				}
				case RUNE_GO_Z_M -> {
					if (dist.getZ() < 0) {
						positions.put(-dist.getZ(), rune);
						if (-dist.getZ() > maxDistance) {
							maxDistance = -dist.getZ();
						}
					}
				}
				case RUNE_GO_NEAR -> {
					final int distNum = Math.abs(dist.getX()) + Math.abs(dist.getY()) + Math.abs(dist.getZ());
					positions.put(distNum, rune);
					if (distNum > maxDistance) {
						maxDistance = distNum;
					}
				}
			}
		}

		for (int i = 1; i <= maxDistance; i++) { // 0 is itself
			if (positions.containsKey(i)) {
				if (distance == 1) {
					return positions.get(i);
				} else {
					distance--;
				}
			}
		}

		return null;
	}

	public Set<BlockPos> getRunesInRange() {
		final Set<BlockPos> result = new HashSet<>();
		if (level == null) {
			return result;
		}

		final ChunkPos chunkPos = new ChunkPos(getBlockPos());
		final int range = FlyingShipsConfiguration.RUNE_RANGE.get();
		for (int x = chunkPos.x - range; x <= chunkPos.x + range; x++) {
			for (int z = chunkPos.z - range; z <= chunkPos.z + range; z++) {
				level.getChunk(x, z).getBlockEntities().forEach(
						(blockPos, entity) -> {
							if (entity instanceof RuneBlockEntity) {
								result.add(blockPos);
							}
						}
				);
			}
		}

		return result;
	}
}
