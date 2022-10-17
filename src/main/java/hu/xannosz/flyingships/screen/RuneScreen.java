package hu.xannosz.flyingships.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hu.xannosz.flyingships.FlyingShips;
import hu.xannosz.flyingships.screen.widget.ButtonConfig;
import hu.xannosz.flyingships.screen.widget.ButtonId;
import hu.xannosz.flyingships.screen.widget.GraphicalButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static hu.xannosz.flyingships.screen.RudderScreen.DEBUG_MODE;
import static net.minecraftforge.client.gui.ScreenUtils.drawTexturedModalRect;

@OnlyIn(Dist.CLIENT)
public class RuneScreen extends AbstractContainerScreen<RuneMenu> {

	private static final ResourceLocation TEXTURE =
			new ResourceLocation(FlyingShips.MOD_ID, "textures/gui/rune_gui.png");
	private int x;
	private int y;

	private static final Map<Character, Integer> ABC = new HashMap<>();

	static {
		ABC.put('A', 0);
		ABC.put('Á', 1);
		ABC.put('B', 2);
		ABC.put('C', 3);
		ABC.put('D', 4);
		ABC.put('E', 5);
		ABC.put('É', 6);
		ABC.put('F', 7);
		ABC.put('G', 8);
		ABC.put('H', 9);
		ABC.put('I', 10);
		ABC.put('Í', 11);
		ABC.put('J', 12);
		ABC.put('K', 13);
		ABC.put('L', 14);
		ABC.put('M', 15);
		ABC.put('N', 16);
		ABC.put('O', 17);
		ABC.put('Ó', 18);
		ABC.put('Ö', 19);
		ABC.put('Ő', 20);
		ABC.put('P', 21);
		ABC.put('Q', 22);
		ABC.put('R', 23);
		ABC.put('S', 24);
		ABC.put('T', 25);
		ABC.put('U', 26);
		ABC.put('Ú', 27);
		ABC.put('Ü', 28);
		ABC.put('Ű', 29);
		ABC.put('V', 30);
		ABC.put('W', 31);
		ABC.put('X', 32);
		ABC.put('Y', 33);
		ABC.put('Z', 34);
		ABC.put(' ', 35);
	}

	public RuneScreen(RuneMenu runeMenu, Inventory inventory, Component title) {
		super(runeMenu, inventory, title);
		imageHeight = 88;
		imageWidth = 212;
	}

	@Override
	protected void init() {
		super.init();

		x = (width - imageWidth) / 2;
		y = (height - imageHeight) / 2;

		GraphicalButton up = new GraphicalButton(generateConfig(5, 5, ButtonId.RUNE_GO_UP));
		GraphicalButton yPlus = new GraphicalButton(generateConfig(73, 5, ButtonId.RUNE_GO_Y_P));
		GraphicalButton zMinus = new GraphicalButton(generateConfig(141, 5, ButtonId.RUNE_GO_Z_M));

		GraphicalButton xMinus = new GraphicalButton(generateConfig(5, 25, ButtonId.RUNE_GO_X_M));
		GraphicalButton near = new GraphicalButton(generateConfig(73, 25, ButtonId.RUNE_GO_NEAR));
		GraphicalButton xPlus = new GraphicalButton(generateConfig(141, 25, ButtonId.RUNE_GO_X_P));

		GraphicalButton zPlus = new GraphicalButton(generateConfig(5, 45, ButtonId.RUNE_GO_Z_P));
		GraphicalButton yMinus = new GraphicalButton(generateConfig(73, 45, ButtonId.RUNE_GO_Y_M));
		GraphicalButton down = new GraphicalButton(generateConfig(141, 45, ButtonId.RUNE_GO_DOWN));

		GraphicalButton wand = new GraphicalButton(generateConfig(5, 65, ButtonId.RUNE_SWITCH_WAND));
		GraphicalButton ok = new GraphicalButton(generateConfig(73, 65, ButtonId.RUNE_OK));
		GraphicalButton redstone = new GraphicalButton(generateConfig(141, 65, ButtonId.RUNE_SWITCH_REDSTONE));

		addRenderableWidget(up);
		addRenderableWidget(yPlus);
		addRenderableWidget(zMinus);

		addRenderableWidget(xMinus);
		addRenderableWidget(near);
		addRenderableWidget(xPlus);

		addRenderableWidget(zPlus);
		addRenderableWidget(yMinus);
		addRenderableWidget(down);

		addRenderableWidget(wand);
		addRenderableWidget(ok);
		addRenderableWidget(redstone);
	}

	@Override
	protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.enableBlend();
		this.blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);
	}

	@Override
	public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
		//call built-in functions
		renderBackground(poseStack);
		super.render(poseStack, mouseX, mouseY, delta);

		renderNames(poseStack, delta);

		//call built-in function
		renderTooltip(poseStack, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(@NotNull PoseStack poseStack, int mouseX, int mouseY) {

	}

	private ButtonConfig generateConfig(int buttonX, int buttonY, ButtonId buttonId) {
		return ButtonConfig.builder()
				.buttonId(buttonId)
				.position(menu.getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + buttonX)
				.hitBoxY(y + buttonY)
				.hitBoxW(66)
				.hitBoxH(18)
				.graphicalX(x + buttonX)
				.graphicalY(y + buttonY)
				.graphicalW(66)
				.graphicalH(18)
				.hoveredX(1)
				.hoveredY(89)
				.build();
	}

	private void renderNames(PoseStack poseStack, float partialTick) {
		renderCharArray("gui.text.rune.up", 8, 11, poseStack, partialTick);
		renderCharArray("gui.text.rune.zenith", 76, 11, poseStack, partialTick);
		renderCharArray("gui.text.rune.north", 144, 11, poseStack, partialTick);

		renderCharArray("gui.text.rune.west", 8, 31, poseStack, partialTick);
		renderCharArray("gui.text.rune.near", 76, 31, poseStack, partialTick);
		renderCharArray("gui.text.rune.east", 144, 31, poseStack, partialTick);

		renderCharArray("gui.text.rune.south", 8, 51, poseStack, partialTick);
		renderCharArray("gui.text.rune.nadir", 76, 51, poseStack, partialTick);
		renderCharArray("gui.text.rune.down", 144, 51, poseStack, partialTick);

		renderCharArray("gui.text.rune.ok", 76, 71, poseStack, partialTick);

		if (menu.isWandEnabled()) {
			renderCharArray("gui.text.rune.enable", 8, 71, poseStack, partialTick);
		} else {
			renderCharArray("gui.text.rune.disable", 8, 71, poseStack, partialTick);
		}

		switch (menu.getRedstoneType()) {
			case 1 -> renderCharArray("gui.text.rune.disable", 144, 71, poseStack, partialTick);
			case 3 -> renderCharArray("gui.text.rune.down", 144, 71, poseStack, partialTick);
			case 5 -> renderCharArray("gui.text.rune.up", 144, 71, poseStack, partialTick);
			case 7 -> renderCharArray("gui.text.rune.near", 144, 71, poseStack, partialTick);
		}
	}

	private void renderCharArray(String key, int initX, int initY, PoseStack poseStack, float partialTick) {
		final char[] chars = Component.translatable(key).getString().toCharArray();
		for (int i = 0; i < chars.length && i < 10; i++) {
			drawTexturedModalRect(poseStack, x + initX + i * 6, y + initY, 251, ABC.get(chars[i]) * 5, 5, 5, partialTick);
		}
	}
}
