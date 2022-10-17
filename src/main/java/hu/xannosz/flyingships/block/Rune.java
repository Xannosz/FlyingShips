package hu.xannosz.flyingships.block;

import hu.xannosz.flyingships.blockentity.RuneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static hu.xannosz.flyingships.Util.RUNE_TYPES;

public class Rune extends BaseEntityBlock {

	public static final IntegerProperty TYPE = IntegerProperty.create("type", 0, RUNE_TYPES);

	protected Rune(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(TYPE, 0));
	}

	@Override
	@SuppressWarnings("deprecation")
	public @NotNull InteractionResult use(@NotNull BlockState blockState, Level level, @NotNull BlockPos pos,
										  @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
		if (!level.isClientSide()) {
			BlockEntity entity = level.getBlockEntity(pos);
			if (entity instanceof RuneBlockEntity) {
				NetworkHooks.openScreen(((ServerPlayer) player), (RuneBlockEntity) entity, pos);
			} else {
				throw new IllegalStateException("Our Container provider is missing!");
			}
		}

		return InteractionResult.sidedSuccess(level.isClientSide());
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
		return new RuneBlockEntity(blockPos, blockState);
	}

	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	@SuppressWarnings("deprecation")
	public @NotNull VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext collisionContext) {
		return Block.box(0, 0, 0, 16, 1, 16);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
		blockStateBuilder.add(TYPE);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void neighborChanged(@NotNull BlockState blockState, Level level, @NotNull BlockPos blockPos, @NotNull Block block, @NotNull BlockPos p_55670_, boolean p_55671_) {
		if (!level.isClientSide) {
			if (level.hasNeighborSignal(blockPos)) {
				final BlockEntity blockEntity = level.getBlockEntity(blockPos);
				if (blockEntity instanceof RuneBlockEntity) {
					((RuneBlockEntity) blockEntity).redstoneSignal();
				}
			}
		}
	}
}
