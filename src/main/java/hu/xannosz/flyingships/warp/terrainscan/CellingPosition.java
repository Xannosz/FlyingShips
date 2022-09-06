package hu.xannosz.flyingships.warp.terrainscan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CellingPosition {
	VOID(0), UNDER_WATER(1), UNDER_LAVA(2), UNDER_FIELD(3), TOUCH_CELLING(4);

	@Getter
	private final int key;
}
