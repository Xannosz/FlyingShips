package hu.xannosz.flyingships;

import hu.xannosz.flyingships.block.ModBlocks;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ModCreativeModeTab {
	public static final CreativeModeTab FLYING_SHIPS_TAB = new CreativeModeTab("flyingshipstab") {
		@Override
		public @NotNull ItemStack makeIcon() {
			return new ItemStack(ModBlocks.RUDDER.get());
		}
	};
}
