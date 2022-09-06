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
public class LandButton extends AbstractButton {

	private static final int GRAPHICAL_X = 152;
	private static final int GRAPHICAL_Y = 13;
	private static final int GRAPHICAL_W = 17;
	private static final int GRAPHICAL_H = 11;
	private static final int HOVER_X = 1;
	private static final int HOVER_Y = 244;
	private static final int ADDITIONAL_W = 19;

	private final BlockPos position;
	private final boolean debugMode;
	private final int guiX;
	private final int guiY;
	@Setter
	private int state = 0;
	@Setter
	private int nextState = 0;
	@Setter
	private boolean isBlink = false;

	public LandButton(int guiX, int guiY, BlockPos position, boolean debugMode) {
		super(guiX + 153, guiY + 13, 15, 9, Component.empty());
		this.position = position;
		this.debugMode = debugMode;
		this.guiX = guiX;
		this.guiY = guiY;
	}

	@Override
	public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			if (isHovered && nextState >= 0) {
				drawTexturedModalRect(poseStack, guiX + GRAPHICAL_X, guiY + GRAPHICAL_Y,
						HOVER_X + nextState * ADDITIONAL_W, HOVER_Y, GRAPHICAL_W, GRAPHICAL_H, partialTicks);
			} else if (isBlink && state >= 0) {
				drawTexturedModalRect(poseStack, guiX + GRAPHICAL_X, guiY + GRAPHICAL_Y,
						HOVER_X + state * ADDITIONAL_W, HOVER_Y, GRAPHICAL_W, GRAPHICAL_H, partialTicks);
			} else if (debugMode) {
				drawTexturedModalRect(poseStack, x, y, 215, 215, width, height, partialTicks);
			}
		}
	}

	@Override
	public void onPress() {
		ModMessages.sendToServer(new ButtonClickedPacket(ButtonId.LAND, position));
	}

	@Override
	public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {

	}

	public void setVisibility(boolean visible) {
		this.visible = visible;
		active = visible;
	}
}
