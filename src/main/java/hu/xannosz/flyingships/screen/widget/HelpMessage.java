package hu.xannosz.flyingships.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import hu.xannosz.flyingships.screen.RudderScreen;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.chat.Component;

@RequiredArgsConstructor
public class HelpMessage {
	private final int x;
	private final int y;
	private final int w;
	private final int h;

	private final int guiX;
	private final int guiY;

	private final int toolTipX;
	private final int toolTipY;
	private final RudderScreen rudderScreen;

	public void render(PoseStack poseStack, int mouseX, int mouseY, Component component) {
		if (x <= mouseX && mouseX <= x + w && y <= mouseY && mouseY <= y + h) {
			rudderScreen.renderTooltip(poseStack, component, guiX + toolTipX, guiY + toolTipY);
		}
	}
}
