package hu.xannosz.flyingships.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ModdedTrapDoorBlock extends HorizontalDirectionalBlock {
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	protected static final VoxelShape EAST_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);
	protected static final VoxelShape WEST_OPEN_AABB = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	protected static final VoxelShape SOUTH_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
	protected static final VoxelShape NORTH_OPEN_AABB = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
	protected static final VoxelShape BOTTOM_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D);
	protected static final VoxelShape TOP_AABB = Block.box(0.0D, 13.0D, 0.0D, 16.0D, 16.0D, 16.0D);

	public ModdedTrapDoorBlock(BlockBehaviour.Properties p_57526_) {
		super(p_57526_);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, Boolean.FALSE).setValue(HALF, Half.BOTTOM).setValue(POWERED, Boolean.FALSE));
	}

	@SuppressWarnings("deprecation")
	public @NotNull VoxelShape getShape(BlockState p_57563_, @NotNull BlockGetter p_57564_, @NotNull BlockPos p_57565_, @NotNull CollisionContext p_57566_) {
		if (!p_57563_.getValue(OPEN)) {
			return p_57563_.getValue(HALF) == Half.TOP ? TOP_AABB : BOTTOM_AABB;
		} else {
			return switch (p_57563_.getValue(FACING)) {
				case SOUTH -> SOUTH_OPEN_AABB;
				case WEST -> WEST_OPEN_AABB;
				case EAST -> EAST_OPEN_AABB;
				default -> NORTH_OPEN_AABB;
			};
		}
	}

	@SuppressWarnings("deprecation")
	public boolean isPathfindable(BlockState p_57535_, @NotNull BlockGetter p_57536_, @NotNull BlockPos p_57537_, @NotNull PathComputationType p_57538_) {
		return p_57535_.getValue(OPEN);
	}

	@SuppressWarnings("deprecation")
	public @NotNull InteractionResult use(@NotNull BlockState p_57540_, @NotNull Level p_57541_, @NotNull BlockPos p_57542_, @NotNull Player p_57543_, @NotNull InteractionHand p_57544_, @NotNull BlockHitResult p_57545_) {
		if (this.material == Material.METAL) {
			return InteractionResult.PASS;
		} else {
			p_57540_ = p_57540_.cycle(OPEN);
			p_57541_.setBlock(p_57542_, p_57540_, 2);

			this.playSound(p_57543_, p_57541_, p_57542_, p_57540_.getValue(OPEN));
			return InteractionResult.sidedSuccess(p_57541_.isClientSide);
		}
	}

	protected void playSound(@Nullable Player p_57528_, Level p_57529_, BlockPos p_57530_, boolean p_57531_) {
		if (p_57531_) {
			int i = this.material == Material.METAL ? 1037 : 1007;
			p_57529_.levelEvent(p_57528_, i, p_57530_, 0);
		} else {
			int j = this.material == Material.METAL ? 1036 : 1013;
			p_57529_.levelEvent(p_57528_, j, p_57530_, 0);
		}

		p_57529_.gameEvent(p_57528_, p_57531_ ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, p_57530_);
	}

	@SuppressWarnings("deprecation")
	public void neighborChanged(@NotNull BlockState p_57547_, Level p_57548_, @NotNull BlockPos p_57549_, @NotNull Block p_57550_, @NotNull BlockPos p_57551_, boolean p_57552_) {
		if (!p_57548_.isClientSide) {
			boolean flag = p_57548_.hasNeighborSignal(p_57549_);
			if (flag != p_57547_.getValue(POWERED)) {
				if (p_57547_.getValue(OPEN) != flag) {
					p_57547_ = p_57547_.setValue(OPEN, flag);
					this.playSound(null, p_57548_, p_57549_, flag);
				}

				p_57548_.setBlock(p_57549_, p_57547_.setValue(POWERED, flag), 2);
			}

		}
	}

	public BlockState getStateForPlacement(BlockPlaceContext p_57533_) {
		BlockState blockstate = this.defaultBlockState();
		Direction direction = p_57533_.getClickedFace();
		if (!p_57533_.replacingClickedOnBlock() && direction.getAxis().isHorizontal()) {
			blockstate = blockstate.setValue(FACING, direction).setValue(HALF, p_57533_.getClickLocation().y - (double) p_57533_.getClickedPos().getY() > 0.5D ? Half.TOP : Half.BOTTOM);
		} else {
			blockstate = blockstate.setValue(FACING, p_57533_.getHorizontalDirection().getOpposite()).setValue(HALF, direction == Direction.UP ? Half.BOTTOM : Half.TOP);
		}

		if (p_57533_.getLevel().hasNeighborSignal(p_57533_.getClickedPos())) {
			blockstate = blockstate.setValue(OPEN, Boolean.TRUE).setValue(POWERED, Boolean.TRUE);
		}

		return blockstate;
	}

	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_57561_) {
		p_57561_.add(FACING, OPEN, HALF, POWERED);
	}

	@Override
	public boolean isLadder(BlockState state, net.minecraft.world.level.LevelReader world, BlockPos pos, net.minecraft.world.entity.LivingEntity entity) {
		if (state.getValue(OPEN)) {
			BlockPos downPos = pos.below();
			BlockState down = world.getBlockState(downPos);
			return down.getBlock().makesOpenTrapdoorAboveClimbable(down, world, downPos, state);
		}
		return false;
	}
}
