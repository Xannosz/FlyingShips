package hu.xannosz.flyingships.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.AllArgsConstructor;
import lombok.Setter;

import static net.minecraftforge.client.gui.ScreenUtils.drawTexturedModalRect;

@AllArgsConstructor
public class Gauge {
	private final int x;
	private final int y;
	@Setter
	private int u;
	private final int v;
	private final int w;
	private final int h;

	public void render(PoseStack poseStack, int value, int max, float partialTick) {
		if (max == 0) {
			return;
		}
		if (value > max) {
			value = max;
		}
		int t = (value * h) / max;
		drawTexturedModalRect(poseStack, x, y + h - t, u, v + h - t, w, t, partialTick);
	}
}
