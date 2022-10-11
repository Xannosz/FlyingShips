package hu.xannosz.flyingships.warp.scan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BottomPosition {
	VOID(0), FLY_OVER_WATER(1), FLY_OVER_LAVA(2), FLY_OVER_FIELD(3), LANDED(4);

	@Getter
	private final int key;
}
