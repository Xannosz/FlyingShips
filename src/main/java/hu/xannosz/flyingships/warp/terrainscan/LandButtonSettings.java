package hu.xannosz.flyingships.warp.terrainscan;

import hu.xannosz.flyingships.ThisCodeShouldBeNeverReach;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public enum LandButtonSettings {

	VOID(0), SWIM_WATER(1), SWIM_LAVA(2), LAND(3), TOUCH_CELLING(4), EMPTY(-1);

	@Getter
	private final int key;

	@Nonnull
	public LandButtonSettings nextState(TerrainScanResponseStruct terrainScanResponseStruct) {
		LandButtonSettings nextState = this.internalNextState();
		int i = 0;
		while (!nextState.isValidLanding(terrainScanResponseStruct) && i < 5) {
			i++;
			nextState = nextState.internalNextState();
		}
		if (i >= 5) {
			return EMPTY;
		}
		return nextState;
	}

	@Nonnull
	private LandButtonSettings internalNextState() {
		switch (this) {
			case VOID -> {
				return SWIM_WATER;
			}
			case SWIM_WATER -> {
				return SWIM_LAVA;
			}
			case SWIM_LAVA -> {
				return LAND;
			}
			case LAND -> {
				return TOUCH_CELLING;
			}
			case TOUCH_CELLING, EMPTY -> {
				return VOID;
			}
		}
		throw new ThisCodeShouldBeNeverReach();
	}

	private boolean isValidLanding(TerrainScanResponseStruct terrainScanResponseStruct) {
		switch (this) {
			case VOID -> {
				return terrainScanResponseStruct.getCellingPosition().equals(CellingPosition.VOID);
			}
			case SWIM_WATER -> {
				return terrainScanResponseStruct.getCellingPosition().equals(CellingPosition.UNDER_WATER) ||
						terrainScanResponseStruct.getBottomPosition().equals(BottomPosition.FLY_OVER_WATER);
			}
			case SWIM_LAVA -> {
				return terrainScanResponseStruct.getCellingPosition().equals(CellingPosition.UNDER_LAVA) ||
						terrainScanResponseStruct.getBottomPosition().equals(BottomPosition.FLY_OVER_LAVA);
			}
			case LAND -> {
				return terrainScanResponseStruct.getBottomPosition().equals(BottomPosition.FLY_OVER_FIELD);
			}
			case TOUCH_CELLING -> {
				return terrainScanResponseStruct.getCellingPosition().equals(CellingPosition.UNDER_FIELD);
			}
		}
		return false;
	}
}
