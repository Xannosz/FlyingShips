package hu.xannosz.flyingships.warp.terrainscan;

import hu.xannosz.flyingships.ThisCodeShouldBeNeverReach;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

@Slf4j
@RequiredArgsConstructor
public enum LandButtonSettings {

	CLOUD_LEVEL(0), SWIM_WATER(1), SWIM_LAVA(2), LAND(3), TOUCH_CELLING(4), EMPTY(-1);

	@Getter
	private final int key;

	@Nonnull
	public LandButtonSettings nextState(TerrainScanResponseStruct terrainScanResponseStruct, int toCloudLevel) {
		LandButtonSettings nextState = this.internalNextState();
		int i = 0;
		while (!nextState.isValidLanding(terrainScanResponseStruct, toCloudLevel) && i < 5) {
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
			case CLOUD_LEVEL -> {
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
				return CLOUD_LEVEL;
			}
		}
		throw new ThisCodeShouldBeNeverReach();
	}

	private boolean isValidLanding(TerrainScanResponseStruct terrainScanResponseStruct, int toCloudLevel) {
		switch (this) {
			case CLOUD_LEVEL -> {
				return ((toCloudLevel > 0 && toCloudLevel < terrainScanResponseStruct.getHeightOfCelling()) ||
						(toCloudLevel < 0 && -toCloudLevel < terrainScanResponseStruct.getHeightOfBottom())) &&
						!terrainScanResponseStruct.getCellingPosition().equals(CellingPosition.UNDER_WATER) &&
						!terrainScanResponseStruct.getCellingPosition().equals(CellingPosition.UNDER_LAVA);
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
