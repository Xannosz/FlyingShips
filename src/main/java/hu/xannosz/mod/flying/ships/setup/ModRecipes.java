package hu.xannosz.mod.flying.ships.setup;

import hu.xannosz.mod.flying.ships.FlyingShipsMod;
import hu.xannosz.mod.flying.ships.crafting.recipe.PressingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraftforge.fml.RegistryObject;

public final class ModRecipes {
    public static final class Types {
        public static final IRecipeType<PressingRecipe> PRESSING = IRecipeType.register(
                FlyingShipsMod.MOD_ID + ":pressing");

        private Types() {}
    }

    public static final class Serializers {
        public static final RegistryObject<IRecipeSerializer<?>> PRESSING = Registration.RECIPE_SERIALIZERS.register(
                "pressing", PressingRecipe.Serializer::new);

        private Serializers() {}
    }

    private ModRecipes() {}

    static void register() {}
}
