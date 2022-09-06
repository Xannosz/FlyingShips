package hu.xannosz.flyingships.warp.terrainscan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FloatingPosition {
	VOID(0), SWIM_WATER(1), SWIM_LAVA(2);

	@Getter
	private final int key;
}
