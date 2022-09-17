package hu.xannosz.flyingships.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class FlyingShipsConfiguration {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Integer> WIND_MULTIPLIER;
	public static final ForgeConfigSpec.ConfigValue<Integer> LIFT_MULTIPLIER;
	public static final ForgeConfigSpec.ConfigValue<Integer> BALLOON_MULTIPLIER;
	public static final ForgeConfigSpec.ConfigValue<Integer> LIFT_OF_IN_WATER;
	public static final ForgeConfigSpec.ConfigValue<Integer> LIFT_OF_IN_LAVA;
	public static final ForgeConfigSpec.ConfigValue<Double> SPEED_CONSOLIDATOR;
	public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_FLY;
	public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_SLIDING;

	static {
		BUILDER.push("Configs for Flying Ships Mod");

		WIND_MULTIPLIER = BUILDER.comment("How strong the wind per wool block")
				.define("windMultiplier", 2000); // 20
		LIFT_MULTIPLIER = BUILDER.comment("How many lifting power in the wings")
				.define("liftMultiplier", 500); // 15
		BALLOON_MULTIPLIER = BUILDER.comment("How many lifting power in a gas balloon (wool block)")
				.define("balloonMultiplier", 60);
		LIFT_OF_IN_WATER = BUILDER.comment("How many lifting power in water")
				.define("liftOfInWater", 200 );
		LIFT_OF_IN_LAVA = BUILDER.comment("How many lifting power in lava")
				.define("liftOfInLava", 500);
		SPEED_CONSOLIDATOR = BUILDER.comment("Divider for the energy calculating system")
				.defineInRange("speedConsolidator", 5000d, 1000d, 10000d);
		ENABLE_FLY = BUILDER.comment("Enable fly")
				.define("enableFly", true);
		ENABLE_SLIDING = BUILDER.comment("Enable short movement without energy consuming")
				.define("enableSliding", true);

		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
