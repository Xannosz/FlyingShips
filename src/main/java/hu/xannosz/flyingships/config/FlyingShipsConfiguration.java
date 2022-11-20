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
	public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_SLIDING;

	public static final ForgeConfigSpec.ConfigValue<Integer> COOL_DOWN_TIME;
	public static final ForgeConfigSpec.ConfigValue<Integer> ENERGY_PER_OSCILLATOR;
	public static final ForgeConfigSpec.ConfigValue<Integer> ENERGY_PER_HEATER;
	public static final ForgeConfigSpec.ConfigValue<Integer> STEAM_PER_TANK;
	public static final ForgeConfigSpec.ConfigValue<Integer> WATER_PER_TANK;
	public static final ForgeConfigSpec.ConfigValue<Integer> ENERGY_PER_ENDER_PEARL;
	public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_COLLUSION_DETECTION_ON_Y;
	public static final ForgeConfigSpec.ConfigValue<Integer> MARKER_RANGE;
	public static final ForgeConfigSpec.ConfigValue<Integer> RUNE_RANGE;


	static {
		BUILDER.push("Configs for Flying Ships Mod");

		WIND_MULTIPLIER = BUILDER.comment("Wind power per wool block")
				.define("windMultiplier", 40);
		LIFT_MULTIPLIER = BUILDER.comment("Buoyancy per block")
				.define("liftMultiplier", 20);
		BALLOON_MULTIPLIER = BUILDER.comment("How many buoyancy in a gas balloon (wool block)")
				.define("balloonMultiplier", 75);
		ARTIFICIAL_FLOATER_LIFT_MULTIPLIER = BUILDER.comment("How many buoyancy in a artificial floater")
				.define("artificialFloaterLiftMultiplier", 830);
		ARTIFICIAL_FLOATER_MOVEMENT_MULTIPLIER = BUILDER.comment("How many movement power in a artificial floater")
				.define("artificialFloaterMovementMultiplier", 340);
		LIFT_OF_IN_WATER = BUILDER.comment("Buoyancy in water")
				.define("liftOfInWater", 350);
		LIFT_OF_IN_LAVA = BUILDER.comment("Buoyancy in lava")
				.define("liftOfInLava", 650);
		SPEED_CONSOLIDATOR = BUILDER.comment("Divider for the energy calculating system")
				.defineInRange("speedConsolidator", 5000d, 1000d, 10000d);
		ENABLE_SLIDING = BUILDER.comment("Enable short movement without energy consuming")
				.define("enableSliding", true);
		COOL_DOWN_TIME = BUILDER.comment("Cool down time in tick")
				.define("coolDownTime", 500);
		ENERGY_PER_OSCILLATOR = BUILDER.comment("Energy capacity per ender oscillator")
				.define("energyPerOscillator", 12000);
		ENERGY_PER_HEATER = BUILDER.comment("Energy capacity per heater")
				.define("energyPerHeater", 1000);
		STEAM_PER_TANK = BUILDER.comment("Steam capacity per tank")
				.define("steamPerTank", 3000);
		WATER_PER_TANK = BUILDER.comment("Water capacity per tank")
				.define("waterPerTank", 1000);
		ENERGY_PER_ENDER_PEARL = BUILDER.comment("Energy per ender pearl")
				.define("energyPerEnderPearl", 2500);
		ENABLE_COLLUSION_DETECTION_ON_Y = BUILDER.comment("Enable collusion detection on Y")
				.define("enableCollusionDetectionOnY", true);
		MARKER_RANGE = BUILDER.comment("Detection range of markers in chunk")
				.defineInRange("markerRange", 12, 6, 32);
		RUNE_RANGE = BUILDER.comment("Detection range of runes in chunk")
				.defineInRange("runeRange", 12, 6, 32);

		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
