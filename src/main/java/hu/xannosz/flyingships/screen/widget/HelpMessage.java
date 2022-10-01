package hu.xannosz.flyingships.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import hu.xannosz.flyingships.screen.RudderScreen;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

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

	private int mouseX = 0;
	private int mouseY = 0;
	private long currentClock = 0;
	@Setter
	private int delay = 800;

	public void render(PoseStack poseStack, int mouseX, int mouseY, Component... components) {
		if (x <= mouseX && mouseX <= x + w && y <= mouseY && mouseY <= y + h) {
			if (this.mouseX != mouseX || this.mouseY != mouseY) {
				this.mouseX = mouseX;
				this.mouseY = mouseY;
				currentClock = System.currentTimeMillis();
				return;
			}
			if (System.currentTimeMillis() - currentClock < delay) {
				return;
			}
			rudderScreen.renderComponentTooltip(poseStack, Arrays.asList(components), guiX + toolTipX, guiY + toolTipY);
		}
	}
}
