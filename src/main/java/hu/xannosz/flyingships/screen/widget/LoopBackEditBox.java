package hu.xannosz.flyingships.screen.widget;

import hu.xannosz.flyingships.screen.RudderScreen;
import lombok.Getter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class LoopBackEditBox extends EditBox {

	private final ButtonId buttonId;
	private final RudderScreen screen;
	@Getter
	private boolean editable;

	public LoopBackEditBox(Font p_94114_, int p_94115_, int p_94116_, int p_94117_, int p_94118_, Component p_94119_, ButtonId buttonId, RudderScreen screen) {
		super(p_94114_, p_94115_, p_94116_, p_94117_, p_94118_, p_94119_);
		this.buttonId = buttonId;
		this.screen = screen;
	}

	@Override
	public boolean keyPressed(int p_94132_, int p_94133_, int p_94134_) {
		if (p_94132_ == 257 && editable) {
			screen.clickButton(buttonId);
		}
		return super.keyPressed(p_94132_, p_94133_, p_94134_);
	}

	@Override
	public void setEditable(boolean editable) {
		this.editable = editable;
		super.setEditable(editable);
	}
}
