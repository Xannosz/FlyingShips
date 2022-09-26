package hu.xannosz.flyingships.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.xannosz.flyingships.item.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;

public class CrystalOfLevitationModifier extends LootModifier {

	public static final Codec<CrystalOfLevitationModifier> CODEC =
			RecordCodecBuilder.create(inst -> LootModifier.codecStart(inst).
					apply(inst, CrystalOfLevitationModifier::new));

	public CrystalOfLevitationModifier(LootItemCondition[] conditionsIn) {
		super(conditionsIn);
	}

	@Nonnull
	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		generatedLoot.add(new ItemStack(ModItems.CRYSTAL_OF_LEVITATION.get(), 2));
		return generatedLoot;
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}
}