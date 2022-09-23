package hu.xannosz.flyingships.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hu.xannosz.flyingships.screen.subscreen.CoordinatesSubScreen;
import hu.xannosz.flyingships.screen.subscreen.MainSubScreen;
import hu.xannosz.flyingships.screen.subscreen.SettingsSubScreen;
import hu.xannosz.flyingships.screen.subscreen.SubScreen;
import hu.xannosz.flyingships.screen.widget.ButtonId;
import hu.xannosz.flyingships.screen.widget.ScreenWithButton;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@Slf4j
@OnlyIn(Dist.CLIENT)
public class RudderScreen extends AbstractContainerScreen<RudderMenu> implements ScreenWithButton {

	public static final boolean DEBUG_MODE = false;

	private int x;
	private int y;

	private final SubScreen mainSubScreen;
	private final SubScreen settingsSubScreen;
	private final SubScreen coordinatesSubScreen;
	private SubScreen actualSubScreen = null;

	public RudderScreen(RudderMenu rudderMenu, Inventory inventory, Component title) {
		super(rudderMenu, inventory, title);
		imageHeight = 193;
		mainSubScreen = new MainSubScreen(this);
		settingsSubScreen = new SettingsSubScreen(this);
		coordinatesSubScreen = new CoordinatesSubScreen(this);
	}

	@Override
	protected void init() {
		super.init();

		x = (width - imageWidth) / 2;
		y = (height - imageHeight) / 2;

		mainSubScreen.init(x, y);
		settingsSubScreen.init(x, y);
		coordinatesSubScreen.init(x, y);

		mainSubScreen.registerRenderables();
		settingsSubScreen.registerRenderables();
		coordinatesSubScreen.registerRenderables();
	}


	@Override
	protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		actualSubScreen.renderBg();
		this.blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);
		actualSubScreen.renderDynamicImages(poseStack, x, y, partialTick);
	}

	@Override
	public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
		//set actual sub screen
		GuiState guiState = menu.getGuiState();
		switch (guiState) {
			case MAIN -> actualSubScreen = mainSubScreen;
			case SETTINGS -> actualSubScreen = settingsSubScreen;
			case COORDINATES -> actualSubScreen = coordinatesSubScreen;
		}

		//call built-in functions
		renderBackground(poseStack);
		super.render(poseStack, mouseX, mouseY, delta);

		//set visibility
		mainSubScreen.setVisibility(guiState.equals(GuiState.MAIN));
		settingsSubScreen.setVisibility(guiState.equals(GuiState.SETTINGS));
		coordinatesSubScreen.setVisibility(guiState.equals(GuiState.COORDINATES));

		blinking();

		//call built-in function
		renderTooltip(poseStack, mouseX, mouseY);

		actualSubScreen.renderToolTips(poseStack, mouseX - x, mouseY - y, x, y);
	}

	@Override
	protected void renderLabels(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
		actualSubScreen.renderLabels(poseStack, font);
	}

	@Override
	public boolean keyPressed(int p_97765_, int p_97766_, int p_97767_) {
		return actualSubScreen.keyPressed(p_97765_, p_97766_, p_97767_);
	}

	public boolean byPassedKeyPressed(int p_97765_, int p_97766_, int p_97767_) {
		return super.keyPressed(p_97765_, p_97766_, p_97767_);
	}

	private void blinking() {
		final int clock = menu.getBlockEntity().getClock();
		actualSubScreen.blink(clock < 16 || (33 < clock && clock < 49) || (66 < clock && clock < 82));
	}

	@Override
	public void clickButton(ButtonId buttonId) {
		actualSubScreen.clickButton(buttonId);
	}

	public <T extends GuiEventListener & Widget & NarratableEntry> void addRenderable(T widget) {
		addRenderableWidget(widget);
	}

	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	public Font getFont() {
		return font;
	}

	public Player getPlayer() {
		return getMinecraft().player;
	}
}
