package hu.xannosz.flyingships;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class Util {

	private static final String[] SHIP_NAME_PREFIX = new String[]{"", "Great ", "Saint ", "Main ", "The ",
			"Poppy ", "Argosy ", "Arnprior "};
	private static final String[] SHIP_NAME_SUFFIX = new String[]{"Enterprise", "Commander", "Orion", "Assurance",
			"Axford", "Morris", "Raleigh", "Albatross", "Experiment", "Observer", "Anguilla", "Vulture",
			"Apollo", "Sunflower", "Linnet"};

	public static int convertBitArrayToInt(boolean[] booleans) {
		int value = 0;
		for (int i = 0; i < booleans.length; ++i) {
			if (booleans[i]) value |= (1 << i);
		}
		return value;
	}

	public static boolean[] convertIntToBitArray(int value, int size) {
		boolean[] booleans = new boolean[size];
		for (int i = 0; i < size; ++i) {
			booleans[i] = (value & (1 << i)) != 0;
		}
		return booleans;
	}

	public static String generateNewShipName() {
		return SHIP_NAME_PREFIX[new Random().nextInt(SHIP_NAME_PREFIX.length)] + SHIP_NAME_SUFFIX[new Random().nextInt(SHIP_NAME_SUFFIX.length)];
	}
}
