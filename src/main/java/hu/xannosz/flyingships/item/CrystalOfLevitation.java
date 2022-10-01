package hu.xannosz.flyingships.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class CrystalOfLevitation extends Item {
	public CrystalOfLevitation(Properties properties) {
		super(properties);
	}

	@Override
	public void inventoryTick(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull Entity entity, int p_41407_, boolean p_41408_) {
		if (entity instanceof Mob) {
			((Mob) entity).addEffect(new MobEffectInstance(MobEffects.JUMP, 20 * 3));
			((Mob) entity).addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 3));
		}
		if (entity instanceof Player) {
			((Player) entity).addEffect(new MobEffectInstance(MobEffects.JUMP, 20 * 3));
			((Player) entity).addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 3));
		}
	}
}
