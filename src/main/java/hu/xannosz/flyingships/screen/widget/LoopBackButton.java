package hu.xannosz.flyingships.screen.widget;

import hu.xannosz.flyingships.screen.RudderScreen;

public class LoopBackButton extends GraphicalButton {
	private final RudderScreen screen;

	public LoopBackButton(ButtonConfig config, RudderScreen screen) {
		super(config);
		this.screen = screen;
	}

	@Override
	public void onPress() {
		screen.clickButton(config.getButtonId());
	}
}
