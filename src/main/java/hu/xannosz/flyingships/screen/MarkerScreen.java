package hu.xannosz.flyingships.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hu.xannosz.flyingships.FlyingShips;
import hu.xannosz.flyingships.networking.ModMessages;
import hu.xannosz.flyingships.networking.SendNewMarkerNamePacket;
import hu.xannosz.flyingships.screen.widget.*;
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

import static hu.xannosz.flyingships.screen.RudderScreen.DEBUG_MODE;

@OnlyIn(Dist.CLIENT)
public class MarkerScreen extends AbstractContainerScreen<MarkerMenu> implements ScreenWithButton {

	private static final ResourceLocation TEXTURE =
			new ResourceLocation(FlyingShips.MOD_ID, "textures/gui/marker_gui.png");
	private int x;
	private int y;

	private GraphicalButton enabledButton;
	private GraphicalButton disabledButton;
	private LoopBackEditBox markerName;
	private boolean markerNameUpdated = false;

	public MarkerScreen(MarkerMenu markerMenu, Inventory inventory, Component title) {
		super(markerMenu, inventory, title);
		imageHeight = 45;
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

		enabledButton = new GraphicalButton(generateConfig(188));
		disabledButton = new GraphicalButton(generateConfig(176));

		addRenderableWidget(enabledButton);
		addRenderableWidget(disabledButton);
		addRenderableWidget(markerName);
	}


	@Override
	protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		this.blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);
		if (menu.isEnabled()) {
			enabledButton.setVisibility(true);
			disabledButton.setVisibility(false);
		} else {
			enabledButton.setVisibility(false);
			disabledButton.setVisibility(true);
		}
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

	private ButtonConfig generateConfig(int u) {
		return ButtonConfig.builder()
				.buttonId(ButtonId.MARKER_SET_ENABLED)
				.position(menu.getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + 156)
				.hitBoxY(y + 9)
				.hitBoxW(12)
				.hitBoxH(12)
				.graphicalX(x + 156)
				.graphicalY(y + 9)
				.graphicalW(12)
				.graphicalH(12)
				.buttonX(u)
				.buttonY(0)
				.hoveredX(u)
				.hoveredY(12)
				.build();
	}
}
