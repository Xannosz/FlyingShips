package hu.xannosz.flyingships.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import hu.xannosz.flyingships.networking.ButtonClickedPacket;
import hu.xannosz.flyingships.networking.ModMessages;
import lombok.Setter;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import static net.minecraftforge.client.gui.ScreenUtils.drawTexturedModalRect;

@OnlyIn(Dist.CLIENT)
public class GraphicalButton extends AbstractButton {

	protected final ButtonConfig config;
	@Setter
	private boolean isBlink = false;

	public GraphicalButton(ButtonConfig config) {
		super(config.getHitBoxX(), config.getHitBoxY(),
				config.getHitBoxW(), config.getHitBoxH(), Component.empty());
		this.config = config;
	}

	@Override
	public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			if (isHovered || isBlink) {
				drawTexturedModalRect(poseStack, config.getGraphicalX(), config.getGraphicalY(),
						config.getHoveredX(), config.getHoveredY(),
						config.getGraphicalW(), config.getGraphicalH(), partialTicks);
			} else if (config.isDebugMode()) {
				drawTexturedModalRect(poseStack, x, y, 215, 215, width, height, partialTicks);
			} else if (config.isDefaultButtonDrawNeeded()) {
				drawTexturedModalRect(poseStack, config.getGraphicalX(), config.getGraphicalY(),
						config.getButtonX(), config.getButtonY(),
						config.getGraphicalW(), config.getGraphicalH(), partialTicks);
			}
		}
	}

	@Override
	public void onPress() {
		ModMessages.sendToServer(new ButtonClickedPacket(config.getButtonId(), config.getPosition()));
	}

	@Override
	public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {

	}

	public void setVisibility(boolean visible) {
		this.visible = visible;
		active = visible;
	}
}
