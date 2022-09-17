package hu.xannosz.flyingships;

import lombok.Getter;

@Getter
public class Configuration {
	private static Configuration configuration;

	public static Configuration getConfiguration() {
		if (configuration == null) {
			configuration = new Configuration();
		}
		return configuration;
	}

	private int windMultiplier = 2000; // /10
	private int liftMultiplier = 500; // /10
	private int balloonMultiplier = 60;
	private int liftOfInWater = 200;
	private int liftOfInLava = 500;
	private double speedConsolidator = 5000;
	private boolean enableFly = true;
	private boolean enableSliding = true;
}
