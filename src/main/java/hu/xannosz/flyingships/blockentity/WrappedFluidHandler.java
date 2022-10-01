package hu.xannosz.flyingships.blockentity;

import lombok.RequiredArgsConstructor;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class WrappedFluidHandler implements IFluidHandler {

	private final RudderBlockEntity rudderBlockEntity;

	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public @NotNull FluidStack getFluidInTank(int tank) {
		if (tank == 0) {
			return new FluidStack(Fluids.WATER, rudderBlockEntity.getWaterContent());
		}
		return FluidStack.EMPTY;
	}

	@Override
	public int getTankCapacity(int tank) {
		if (tank == 0) {
			return rudderBlockEntity.getMaxWaterContent();
		}
		return 0;
	}

	@Override
	public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
		return tank == 0 && stack.getFluid().equals(Fluids.WATER);
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (!resource.getFluid().equals(Fluids.WATER)) {
			return 0;
		}
		final int amount = Math.min(resource.getAmount(),
				rudderBlockEntity.getMaxWaterContent() - rudderBlockEntity.getWaterContent());
		if (action.execute()) {
			rudderBlockEntity.setWaterContent(rudderBlockEntity.getWaterContent() + amount);
		}
		return amount;
	}

	@Override
	public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
		if (!resource.getFluid().equals(Fluids.WATER)) {
			return new FluidStack(Fluids.WATER, 0);
		}
		final int amount = Math.min(resource.getAmount(), rudderBlockEntity.getWaterContent());
		if (action.execute()) {
			rudderBlockEntity.setWaterContent(rudderBlockEntity.getWaterContent() - amount);
		}
		return new FluidStack(Fluids.WATER, amount);
	}

	@Override
	public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
		final int amount = Math.min(maxDrain, rudderBlockEntity.getWaterContent());
		if (action.execute()) {
			rudderBlockEntity.setWaterContent(rudderBlockEntity.getWaterContent() - amount);
		}
		return new FluidStack(Fluids.WATER, amount);
	}
}
