package hu.xannosz.flyingships.warp;

import hu.xannosz.flyingships.ThisCodeShouldBeNeverReach;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public enum PowerState {
	ON(0), STANDBY(1), OFF(2);

	@Getter
	private final int key;

	@Nonnull
	public PowerState nextState(boolean enableSteamEngine) {
		switch (this) {
			case ON -> {
				if (enableSteamEngine) {
					return STANDBY;
				} else {
					return OFF;
				}
			}
			case STANDBY -> {
				return OFF;
			}
			case OFF -> {
				return ON;
			}
		}
		throw new ThisCodeShouldBeNeverReach();
	}

	@Nonnull
	public static PowerState fromKey(int key) {
		for (PowerState state : values()) {
			if (state.getKey() == key) {
				return state;
			}
		}
		throw new ThisCodeShouldBeNeverReach();
	}
}
