package hu.xannosz.flyingships.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import hu.xannosz.flyingships.networking.ButtonClickedPacket;
import hu.xannosz.flyingships.networking.ModMessages;
import lombok.Setter;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import static net.minecraftforge.client.gui.ScreenUtils.drawTexturedModalRect;

@OnlyIn(Dist.CLIENT)
public class OnOffButton extends AbstractButton {
	private static final int GRAPHICAL_X = 153;
	private static final int GRAPHICAL_Y = 7;
	private static final int GRAPHICAL_W = 15;
	private static final int GRAPHICAL_H = 5;

	private static final int BUTTON_X = 193;
	private static final int BUTTON_Y = 92;
	private static final int HOVER_X = 193;
	private static final int HOVER_Y = 99;
	private static final int ADDITIONAL_W = 17;

	private final BlockPos position;
	private final boolean debugMode;
	private final int guiX;
	private final int guiY;
	@Setter
	private int state = 0;
	@Setter
	private int nextState = 0;

	public OnOffButton(int guiX, int guiY, BlockPos position, boolean debugMode) {
		super(guiX + 155, guiY + 7, 11, 5, Component.empty());
		this.position = position;
		this.debugMode = debugMode;
		this.guiX = guiX;
		this.guiY = guiY;
	}

	@Override
	public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			if (isHovered) {
				drawTexturedModalRect(poseStack, guiX + GRAPHICAL_X, guiY + GRAPHICAL_Y,
						HOVER_X + nextState * ADDITIONAL_W, HOVER_Y, GRAPHICAL_W, GRAPHICAL_H, partialTicks);
			} else if (debugMode) {
				drawTexturedModalRect(poseStack, x, y, 215, 215, width, height, partialTicks);
			} else {
				drawTexturedModalRect(poseStack, guiX + GRAPHICAL_X, guiY + GRAPHICAL_Y,
						BUTTON_X + state * ADDITIONAL_W, BUTTON_Y, GRAPHICAL_W, GRAPHICAL_H, partialTicks);
			}
		}
	}

	@Override
	public void onPress() {
		ModMessages.sendToServer(new ButtonClickedPacket(ButtonId.POWER, position));
	}

	@Override
	public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {

	}

	public void setVisibility(boolean visible) {
		this.visible = visible;
		active = visible;
	}
}
