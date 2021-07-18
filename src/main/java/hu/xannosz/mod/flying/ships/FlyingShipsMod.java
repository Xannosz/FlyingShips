package hu.xannosz.mod.flying.ships;

import hu.xannosz.mod.flying.ships.setup.Registration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(FlyingShipsMod.MOD_ID)
public class FlyingShipsMod {
    public static final String MOD_ID = "flying_ships";

    public FlyingShipsMod() {
        Registration.register();
        MinecraftForge.EVENT_BUS.register(this);
    }
}
