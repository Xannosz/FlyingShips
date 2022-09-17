package hu.xannosz.flyingships.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class EnderOscillator extends Block {
	public EnderOscillator(Properties properties) {
		super(properties);
	}

	@SuppressWarnings("deprecation")
	public @NotNull VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext collisionContext) {
		return Shapes.or(
				Block.box(2, 0, 2, 14, 1, 14),
				Block.box(6, 1, 6, 10, 3, 10),

				Block.box(7, 1, 7, 9, 14, 9),
				Block.box(5, 9, 5, 11, 15, 11),

				Block.box(6, 8, 6, 10, 9, 10),
				Block.box(6, 15, 6, 10, 16, 10),
				Block.box(6, 10, 4, 10, 14, 5),
				Block.box(6, 10, 11, 10, 14, 12),
				Block.box(4, 10, 6, 5, 14, 10),
				Block.box(11, 10, 6, 12, 14, 10)
		);
	}
}
