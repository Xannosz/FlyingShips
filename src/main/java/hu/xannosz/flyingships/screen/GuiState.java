package hu.xannosz.flyingships.screen;

import hu.xannosz.flyingships.ThisCodeShouldBeNeverReach;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public enum GuiState {
	MAIN(0), SETTINGS(1), COORDINATES(2) ;

	@Getter
	private final int key;

	@Nonnull
	public static GuiState fromKey(int key) {
		for (GuiState state : values()) {
			if (state.getKey() == key) {
				return state;
			}
		}
		throw new ThisCodeShouldBeNeverReach();
	}
}
