package hu.xannosz.flyingships.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class FlyingShipsConfiguration {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Integer> WIND_MULTIPLIER;
	public static final ForgeConfigSpec.ConfigValue<Integer> LIFT_MULTIPLIER;
	public static final ForgeConfigSpec.ConfigValue<Integer> BALLOON_MULTIPLIER;
	public static final ForgeConfigSpec.ConfigValue<Integer> ARTIFICIAL_FLOATER_LIFT_MULTIPLIER;
	public static final ForgeConfigSpec.ConfigValue<Integer> ARTIFICIAL_FLOATER_MOVEMENT_MULTIPLIER;
	public static final ForgeConfigSpec.ConfigValue<Integer> LIFT_OF_IN_WATER;
	public static final ForgeConfigSpec.ConfigValue<Integer> LIFT_OF_IN_LAVA;
	public static final ForgeConfigSpec.ConfigValue<Double> SPEED_CONSOLIDATOR;
	public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_FLY;
	public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_SLIDING;

	public static final ForgeConfigSpec.ConfigValue<Integer> COOL_DOWN_TIME;
	public static final ForgeConfigSpec.ConfigValue<Integer> ENERGY_PER_OSCILLATOR;
	public static final ForgeConfigSpec.ConfigValue<Integer> ENERGY_PER_HEATER;
	public static final ForgeConfigSpec.ConfigValue<Integer> STEAM_PER_TANK;
	public static final ForgeConfigSpec.ConfigValue<Integer> WATER_PER_TANK;
	public static final ForgeConfigSpec.ConfigValue<Integer> ENERGY_PER_ENDER_PEARL;
	public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_COLLUSION_DETECTION_ON_Y;


	static {
		BUILDER.push("Configs for Flying Ships Mod");

		WIND_MULTIPLIER = BUILDER.comment("How strong the wind per wool block")
				.define("windMultiplier", 20);
		LIFT_MULTIPLIER = BUILDER.comment("How many lifting power in the wings")
				.define("liftMultiplier", 15);
		BALLOON_MULTIPLIER = BUILDER.comment("How many lifting power in a gas balloon (wool block)")
				.define("balloonMultiplier", 60);
		ARTIFICIAL_FLOATER_LIFT_MULTIPLIER = BUILDER.comment("How many lifting power in a artificial floater")
				.define("artificialFloaterLiftMultiplier", 720);
		ARTIFICIAL_FLOATER_MOVEMENT_MULTIPLIER = BUILDER.comment("How many movement power in a artificial floater")
				.define("artificialFloaterMovementMultiplier", 210);
		LIFT_OF_IN_WATER = BUILDER.comment("How many lifting power in water")
				.define("liftOfInWater", 200);
		LIFT_OF_IN_LAVA = BUILDER.comment("How many lifting power in lava")
				.define("liftOfInLava", 500);
		SPEED_CONSOLIDATOR = BUILDER.comment("Divider for the energy calculating system")
				.defineInRange("speedConsolidator", 5000d, 1000d, 10000d);
		ENABLE_FLY = BUILDER.comment("Enable fly")
				.define("enableFly", true);
		ENABLE_SLIDING = BUILDER.comment("Enable short movement without energy consuming")
				.define("enableSliding", true);
		COOL_DOWN_TIME = BUILDER.comment("Cool down time in tick")
				.define("coolDownTime", 200); //500
		ENERGY_PER_OSCILLATOR = BUILDER.comment("Energy per oscillator")
				.define("energyPerOscillator", 12000);
		ENERGY_PER_HEATER = BUILDER.comment("Energy per heater")
				.define("energyPerHeater", 1000);
		STEAM_PER_TANK = BUILDER.comment("Steam per tank")
				.define("steamPerTank", 3000);
		WATER_PER_TANK = BUILDER.comment("Water per tank")
				.define("waterPerTank", 1000);
		ENERGY_PER_ENDER_PEARL = BUILDER.comment("Energy per ender pearl")
				.define("energyPerEnderPearl", 2500);
		ENABLE_COLLUSION_DETECTION_ON_Y = BUILDER.comment("Enable collusion detection on Y")
				.define("enableCollusionDetectionOnY", true);

		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
