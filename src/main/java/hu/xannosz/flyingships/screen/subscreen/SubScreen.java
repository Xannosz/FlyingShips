package hu.xannosz.flyingships.screen.subscreen;

import com.mojang.blaze3d.vertex.PoseStack;
import hu.xannosz.flyingships.screen.RudderScreen;
import hu.xannosz.flyingships.screen.widget.ButtonId;
import net.minecraft.client.gui.Font;

public abstract class SubScreen {

	protected final RudderScreen rudderScreen;

	public SubScreen(RudderScreen rudderScreen) {
		this.rudderScreen = rudderScreen;
	}

	public abstract void init(int x, int y);

	public abstract void registerRenderables();

	public abstract void setVisibility(boolean visibility);

	public abstract void renderBg();

	public abstract void renderDynamicImages(PoseStack poseStack, int x, int y, float partialTick);

	public abstract void renderLabels(PoseStack poseStack, Font font);

	public abstract void renderToolTips(PoseStack poseStack, int mouseX, int mouseY, int x, int y);

	public abstract void blink(boolean on);

	public abstract void clickButton(ButtonId buttonId);
}
