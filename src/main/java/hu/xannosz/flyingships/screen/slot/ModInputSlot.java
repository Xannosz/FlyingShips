package hu.xannosz.flyingships.screen.slot;

import lombok.Setter;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ModInputSlot extends SlotItemHandler {

	@Setter
	private boolean isActive = true;

	public ModInputSlot(IItemHandler itemHandler, int index, int x, int y) {
		super(itemHandler, index, x, y);
	}

	@Override
	public boolean isActive() {
		return isActive;
	}
}
