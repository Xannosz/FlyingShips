package hu.xannosz.flyingships.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hu.xannosz.flyingships.FlyingShips;
import hu.xannosz.flyingships.networking.ModMessages;
import hu.xannosz.flyingships.networking.SendNewMarkerNamePacket;
import hu.xannosz.flyingships.screen.widget.ButtonId;
import hu.xannosz.flyingships.screen.widget.LoopBackEditBox;
import hu.xannosz.flyingships.screen.widget.ScreenWithButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class MarkerScreen extends AbstractContainerScreen<MarkerMenu> implements ScreenWithButton {

	private static final ResourceLocation TEXTURE =
			new ResourceLocation(FlyingShips.MOD_ID, "textures/gui/marker_gui.png");
	private int x;
	private int y;

	private LoopBackEditBox markerName;
	private boolean markerNameUpdated = false;

	public MarkerScreen(MarkerMenu rudderMenu, Inventory inventory, Component title) {
		super(rudderMenu, inventory, title);
		imageHeight = 126;
	}

	@Override
	protected void init() {
		super.init();

		x = (width - imageWidth) / 2;
		y = (height - imageHeight) / 2;

		menu.updateName();
		markerName = new LoopBackEditBox(font, x + 22, y + 25, 128, 10,
				Component.empty().withStyle(ChatFormatting.DARK_GRAY), ButtonId.MARKER_NAME_EDITION_DONE, this);
		markerName.setEditable(true);

		addRenderableWidget(markerName);
	}


	@Override
	protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		this.blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);
	}

	@Override
	public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
		//call built-in functions
		renderBackground(poseStack);
		super.render(poseStack, mouseX, mouseY, delta);

		//call built-in function
		renderTooltip(poseStack, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
		if (menu.getMarkerName() != null && !markerNameUpdated) {
			markerName.setValue(menu.getMarkerName());
			markerNameUpdated = true;
		}

		if (menu.getMarkerName() != null) {
			font.draw(poseStack, Component.literal(menu.getMarkerName()), 23,
					12, Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
		}
	}

	@Override
	public boolean keyPressed(int p_97765_, int p_97766_, int p_97767_) {
		if (p_97765_ == 69) {
			return false;
		}
		return super.keyPressed(p_97765_, p_97766_, p_97767_);
	}

	@Override
	public void clickButton(ButtonId buttonId) {
		if (buttonId.equals(ButtonId.MARKER_NAME_EDITION_DONE)) {
			ModMessages.sendToServer(new SendNewMarkerNamePacket(menu.getBlockEntity().getBlockPos(), markerName.getValue()));
			markerNameUpdated = false;
		}
		menu.updateName();
	}
}
