package hu.xannosz.flyingships;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class Util {
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
}
