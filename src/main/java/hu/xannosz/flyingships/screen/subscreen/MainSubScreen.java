package hu.xannosz.flyingships.screen.subscreen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hu.xannosz.flyingships.FlyingShips;
import hu.xannosz.flyingships.block.Rudder;
import hu.xannosz.flyingships.blockentity.RudderBlockEntity;
import hu.xannosz.flyingships.config.FlyingShipsConfiguration;
import hu.xannosz.flyingships.screen.RudderScreen;
import hu.xannosz.flyingships.screen.widget.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static hu.xannosz.flyingships.screen.RudderScreen.DEBUG_MODE;
import static hu.xannosz.flyingships.screen.constant.MainScreenConstants.*;
import static net.minecraftforge.client.gui.ScreenUtils.drawTexturedModalRect;

public class MainSubScreen extends SubScreen {

	private static final ResourceLocation TEXTURE =
			new ResourceLocation(FlyingShips.MOD_ID, "textures/gui/rudder_gui.png");
	private static final ResourceLocation INNER_ROUND_TEXTURE =
			new ResourceLocation(FlyingShips.MOD_ID, "textures/gui/rudder_gui_inner_round.png");
	private GraphicalButton right;
	private GraphicalButton forward;
	private GraphicalButton left;
	private GraphicalButton backward;

	private GraphicalButton up;
	private GraphicalButton down;
	private LandButton land;

	private GraphicalButton jump;
	private GraphicalButton menuButton;
	private GraphicalButton beaconButton;

	private GraphicalButton plus;
	private GraphicalButton minus;
	private GraphicalButton stepUp;
	private GraphicalButton stepDown;

	private OnOffButton power;

	private Gauge wind;
	private Gauge floating;
	private Gauge steam;
	private Gauge water;
	private Gauge heat;
	private Gauge ender;
	private Gauge burn;
	private Gauge coolDown;

	private HelpMessage speedStepMessage;
	private HelpMessage windMessage;
	private HelpMessage floatingMessage;
	private HelpMessage steamMessage;
	private HelpMessage waterMessage;
	private HelpMessage heatMessage;
	private HelpMessage enderMessage;
	private HelpMessage forwardMessage;
	private HelpMessage backMessage;
	private HelpMessage leftMessage;
	private HelpMessage rightMessage;
	private HelpMessage upMessage;
	private HelpMessage downMessage;

	private final Random random = new Random();

	public MainSubScreen(RudderScreen rudderScreen) {
		super(rudderScreen);
	}

	@Override
	public void init(int x, int y) {
		right = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.RIGHT)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + MAIN_ROUND_X + 36)
				.hitBoxY(y + MAIN_ROUND_Y + 13)
				.hitBoxW(MAIN_ROUND_HORIZONTAL_H_W)
				.hitBoxH(MAIN_ROUND_HORIZONTAL_H_H)
				.graphicalX(x + MAIN_ROUND_X + 35)
				.graphicalY(y + MAIN_ROUND_Y + 7)
				.graphicalW(MAIN_ROUND_VERTICAL_G_H_HORIZONTAL_G_W)
				.graphicalH(MAIN_ROUND_VERTICAL_G_W_HORIZONTAL_G_H)
				.hoveredX(MAIN_ROUND_G_X + 13)
				.hoveredY(MAIN_ROUND_G_Y)
				.build());
		forward = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.FORWARD)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + MAIN_ROUND_X + 13)
				.hitBoxY(y + MAIN_ROUND_Y + 1)
				.hitBoxW(MAIN_ROUND_VERTICAL_H_W)
				.hitBoxH(MAIN_ROUND_VERTICAL_H_H)
				.graphicalX(x + MAIN_ROUND_X + 7)
				.graphicalY(y + MAIN_ROUND_Y)
				.graphicalW(MAIN_ROUND_VERTICAL_G_W_HORIZONTAL_G_H)
				.graphicalH(MAIN_ROUND_VERTICAL_G_H_HORIZONTAL_G_W)
				.hoveredX(MAIN_ROUND_G_X + 26)
				.hoveredY(MAIN_ROUND_G_Y)
				.build());
		left = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.LEFT)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + MAIN_ROUND_X + 1)
				.hitBoxY(y + MAIN_ROUND_Y + 13)
				.hitBoxW(MAIN_ROUND_HORIZONTAL_H_W)
				.hitBoxH(MAIN_ROUND_HORIZONTAL_H_H)
				.graphicalX(x + MAIN_ROUND_X)
				.graphicalY(y + MAIN_ROUND_Y + 7)
				.graphicalW(MAIN_ROUND_VERTICAL_G_H_HORIZONTAL_G_W)
				.graphicalH(MAIN_ROUND_VERTICAL_G_W_HORIZONTAL_G_H)
				.hoveredX(MAIN_ROUND_G_X)
				.hoveredY(MAIN_ROUND_G_Y)
				.build());
		backward = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.BACKWARD)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + MAIN_ROUND_X + 13)
				.hitBoxY(y + MAIN_ROUND_Y + 36)
				.hitBoxW(MAIN_ROUND_VERTICAL_H_W)
				.hitBoxH(MAIN_ROUND_VERTICAL_H_H)
				.graphicalX(x + MAIN_ROUND_X + 7)
				.graphicalY(y + MAIN_ROUND_Y + 35)
				.graphicalW(MAIN_ROUND_VERTICAL_G_W_HORIZONTAL_G_H)
				.graphicalH(MAIN_ROUND_VERTICAL_G_H_HORIZONTAL_G_W)
				.hoveredX(MAIN_ROUND_G_X + 26)
				.hoveredY(MAIN_ROUND_G_Y + 13)
				.build());

		up = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.UP)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + MAIN_ROUND_X + MAIN_ROUND_UP_DOWN_X_ADDITIONAL + 3)
				.hitBoxY(y + MAIN_ROUND_Y + MAIN_ROUND_UP_DOWN_Y_ADDITIONAL)
				.hitBoxW(MAIN_ROUND_UP_DOWN_H_W)
				.hitBoxH(MAIN_ROUND_UP_DOWN_H_H)
				.graphicalX(x + MAIN_ROUND_X + MAIN_ROUND_UP_DOWN_X_ADDITIONAL)
				.graphicalY(y + MAIN_ROUND_Y + MAIN_ROUND_UP_DOWN_Y_ADDITIONAL)
				.graphicalW(MAIN_ROUND_UP_DOWN_G_W)
				.graphicalH(MAIN_ROUND_UP_DOWN_G_H)
				.hoveredX(MAIN_ROUND_G_X + MAIN_ROUND_UP_DOWN_H_X_ADDITIONAL)
				.hoveredY(MAIN_ROUND_G_Y)
				.build());
		down = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.DOWN)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + MAIN_ROUND_X + MAIN_ROUND_UP_DOWN_X_ADDITIONAL + 3)
				.hitBoxY(y + MAIN_ROUND_Y + MAIN_ROUND_UP_DOWN_Y_ADDITIONAL + MAIN_ROUND_UP_DOWN_G_H + 1)
				.hitBoxW(MAIN_ROUND_UP_DOWN_H_W)
				.hitBoxH(MAIN_ROUND_UP_DOWN_H_H)
				.graphicalX(x + MAIN_ROUND_X + MAIN_ROUND_UP_DOWN_X_ADDITIONAL)
				.graphicalY(y + MAIN_ROUND_Y + MAIN_ROUND_UP_DOWN_Y_ADDITIONAL + MAIN_ROUND_UP_DOWN_G_H + 1)
				.graphicalW(MAIN_ROUND_UP_DOWN_G_W)
				.graphicalH(MAIN_ROUND_UP_DOWN_G_H)
				.hoveredX(MAIN_ROUND_G_X + MAIN_ROUND_UP_DOWN_H_X_ADDITIONAL)
				.hoveredY(MAIN_ROUND_G_Y + MAIN_ROUND_UP_DOWN_G_H + 1)
				.build());
		land = new LandButton(x, y, rudderScreen.getMenu().getBlockEntity().getBlockPos(), DEBUG_MODE);

		jump = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.JUMP)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + MENU_ROW_X)
				.hitBoxY(y + MENU_ROW_Y)
				.hitBoxW(MENU_ROW_H_W_H)
				.hitBoxH(MENU_ROW_H_W_H)
				.graphicalX(x + MENU_ROW_X)
				.graphicalY(y + MENU_ROW_Y)
				.graphicalW(MENU_ROW_H_W_H)
				.graphicalH(MENU_ROW_H_W_H)
				.buttonX(MENU_ROW_G_X + MENU_ROW_H_W_H + 2)
				.buttonY(MENU_ROW_G_Y)
				.hoveredX(MENU_ROW_G_X + MENU_ROW_H_W_H + 2)
				.hoveredY(MENU_ROW_G_Y + MENU_ROW_H_W_H + 2)
				.build());
		menuButton = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.MENU)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + MENU_ROW_X)
				.hitBoxY(y + MENU_ROW_Y)
				.hitBoxW(MENU_ROW_H_W_H)
				.hitBoxH(MENU_ROW_H_W_H)
				.graphicalX(x + MENU_ROW_X)
				.graphicalY(y + MENU_ROW_Y)
				.graphicalW(MENU_ROW_H_W_H)
				.graphicalH(MENU_ROW_H_W_H)
				.buttonX(MENU_ROW_G_X)
				.buttonY(MENU_ROW_G_Y)
				.hoveredX(MENU_ROW_G_X)
				.hoveredY(MENU_ROW_G_Y + MENU_ROW_H_W_H + 2)
				.build());
		beaconButton = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.BEACON)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + MENU_ROW_X + MENU_ROW_H_W_H + 5)
				.hitBoxY(y + MENU_ROW_Y)
				.hitBoxW(MENU_ROW_H_W_H)
				.hitBoxH(MENU_ROW_H_W_H)
				.graphicalX(x + MENU_ROW_X + MENU_ROW_H_W_H + 5)
				.graphicalY(y + MENU_ROW_Y)
				.graphicalW(MENU_ROW_H_W_H)
				.graphicalH(MENU_ROW_H_W_H)
				.hoveredX(MENU_ROW_G_X + 2 * MENU_ROW_H_W_H + 4)
				.hoveredY(MENU_ROW_G_Y + MENU_ROW_H_W_H + 2)
				.build());

		plus = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.PLUS)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SPEED_ROW_X + SPEED_ROW_ADDITIONAL_X)
				.hitBoxY(y + SPEED_ROW_Y)
				.hitBoxW(SPEED_ROW_H_W)
				.hitBoxH(SPEED_ROW_H_H)
				.graphicalX(x + SPEED_ROW_X + SPEED_ROW_ADDITIONAL_X)
				.graphicalY(y + SPEED_ROW_Y)
				.graphicalW(SPEED_ROW_G_W)
				.graphicalH(SPEED_ROW_G_H)
				.hoveredX(SPEED_ROW_G_X + 2 * SPEED_ROW_G_W + 3)
				.hoveredY(SPEED_ROW_G_Y)
				.build());
		minus = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.MINUS)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SPEED_ROW_X + SPEED_ROW_G_W + 1)
				.hitBoxY(y + SPEED_ROW_Y)
				.hitBoxW(SPEED_ROW_H_W)
				.hitBoxH(SPEED_ROW_H_H)
				.graphicalX(x + SPEED_ROW_X + SPEED_ROW_G_W + 1)
				.graphicalY(y + SPEED_ROW_Y)
				.graphicalW(SPEED_ROW_G_W)
				.graphicalH(SPEED_ROW_G_H)
				.hoveredX(SPEED_ROW_G_X + SPEED_ROW_G_W + 1)
				.hoveredY(SPEED_ROW_G_Y)
				.build());
		stepUp = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.INCREASE)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SPEED_ROW_X + SPEED_ROW_ADDITIONAL_X + SPEED_ROW_G_W + 1)
				.hitBoxY(y + SPEED_ROW_Y)
				.hitBoxW(SPEED_ROW_H_W)
				.hitBoxH(SPEED_ROW_H_H)
				.graphicalX(x + SPEED_ROW_X + SPEED_ROW_ADDITIONAL_X + SPEED_ROW_G_W + 1)
				.graphicalY(y + SPEED_ROW_Y)
				.graphicalW(SPEED_ROW_G_W)
				.graphicalH(SPEED_ROW_G_H)
				.hoveredX(SPEED_ROW_G_X + 3 * SPEED_ROW_G_W + 4)
				.hoveredY(SPEED_ROW_G_Y)
				.build());
		stepDown = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.DECREASE)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SPEED_ROW_X)
				.hitBoxY(y + SPEED_ROW_Y)
				.hitBoxW(SPEED_ROW_H_W)
				.hitBoxH(SPEED_ROW_H_H)
				.graphicalX(x + SPEED_ROW_X)
				.graphicalY(y + SPEED_ROW_Y)
				.graphicalW(SPEED_ROW_G_W)
				.graphicalH(SPEED_ROW_G_H)
				.hoveredX(SPEED_ROW_G_X)
				.hoveredY(SPEED_ROW_G_Y)
				.build());

		power = new OnOffButton(x, y, rudderScreen.getMenu().getBlockEntity().getBlockPos(), DEBUG_MODE);

		wind = new Gauge(x + 65, y + 7, 177, 1, 15, 87);
		floating = new Gauge(x + 46, y + 7, 177, 1, 15, 87);
		steam = new Gauge(x + 8, y + 7, 195, 2, 15, 87);
		water = new Gauge(x + 27, y + 7, 214, 2, 15, 87);
		heat = new Gauge(x + 85, y + 7, 233, 2, 3, 87);
		ender = new Gauge(x + 94, y + 7, 240, 2, 3, 87);
		burn = new Gauge(x + 152, y + 73, 177, 90, 13, 13);
		coolDown = new Gauge(x + 112, y + 15, 1, 33, 30, 30);

		speedStepMessage = new HelpMessage(113, 59, 44, 8, x, y, 105, 90, rudderScreen);
		speedStepMessage.setDelay(300);
		windMessage = new HelpMessage(65, 7, 15, 87, x, y, 65, 95, rudderScreen);
		floatingMessage = new HelpMessage(46, 7, 15, 87, x, y, 46, 95, rudderScreen);
		steamMessage = new HelpMessage(8, 7, 15, 87, x, y, 8, 95, rudderScreen);
		waterMessage = new HelpMessage(27, 7, 15, 87, x, y, 27, 95, rudderScreen);
		heatMessage = new HelpMessage(85, 7, 3, 87, x, y, 85, 95, rudderScreen);
		enderMessage = new HelpMessage(94, 7, 3, 87, x, y, 94, 95, rudderScreen);

		forwardMessage = new HelpMessage(MAIN_ROUND_X + 13, MAIN_ROUND_Y + 1, MAIN_ROUND_VERTICAL_H_W, MAIN_ROUND_VERTICAL_H_H, x, y, MAIN_ROUND_X + 13 - 20, MAIN_ROUND_Y + 1 + 25, rudderScreen);
		backMessage = new HelpMessage(MAIN_ROUND_X + 13, MAIN_ROUND_Y + 36, MAIN_ROUND_VERTICAL_H_W, MAIN_ROUND_VERTICAL_H_H, x, y, MAIN_ROUND_X + 13 - 20, MAIN_ROUND_Y + 36 + 25, rudderScreen);
		leftMessage = new HelpMessage(MAIN_ROUND_X + 1, MAIN_ROUND_Y + 13, MAIN_ROUND_HORIZONTAL_H_W, MAIN_ROUND_HORIZONTAL_H_H, x, y, MAIN_ROUND_X + 1 - 20, MAIN_ROUND_Y + 13 + 25, rudderScreen);
		rightMessage = new HelpMessage(MAIN_ROUND_X + 36, MAIN_ROUND_Y + 13, MAIN_ROUND_HORIZONTAL_H_W, MAIN_ROUND_HORIZONTAL_H_H, x, y, MAIN_ROUND_X + 36 - 20, MAIN_ROUND_Y + 13 + 25, rudderScreen);
		upMessage = new HelpMessage(MAIN_ROUND_X + MAIN_ROUND_UP_DOWN_X_ADDITIONAL + 3, MAIN_ROUND_Y + MAIN_ROUND_UP_DOWN_Y_ADDITIONAL, MAIN_ROUND_UP_DOWN_H_W, MAIN_ROUND_UP_DOWN_H_H, x, y, MAIN_ROUND_X + MAIN_ROUND_UP_DOWN_X_ADDITIONAL + 3 - 20, MAIN_ROUND_Y + MAIN_ROUND_UP_DOWN_Y_ADDITIONAL + 25, rudderScreen);
		downMessage = new HelpMessage(MAIN_ROUND_X + MAIN_ROUND_UP_DOWN_X_ADDITIONAL + 3, MAIN_ROUND_Y + MAIN_ROUND_UP_DOWN_Y_ADDITIONAL + MAIN_ROUND_UP_DOWN_G_H + 1, MAIN_ROUND_UP_DOWN_H_W, MAIN_ROUND_UP_DOWN_H_H, x, y, MAIN_ROUND_X + MAIN_ROUND_UP_DOWN_X_ADDITIONAL + 3 - 20, MAIN_ROUND_Y + MAIN_ROUND_UP_DOWN_Y_ADDITIONAL + MAIN_ROUND_UP_DOWN_G_H + 1 + 25, rudderScreen);
	}

	@Override
	public void registerRenderables() {
		rudderScreen.addRenderable(right);
		rudderScreen.addRenderable(forward);
		rudderScreen.addRenderable(left);
		rudderScreen.addRenderable(backward);

		rudderScreen.addRenderable(up);
		rudderScreen.addRenderable(down);
		rudderScreen.addRenderable(land);

		rudderScreen.addRenderable(jump);
		rudderScreen.addRenderable(menuButton);
		rudderScreen.addRenderable(beaconButton);

		rudderScreen.addRenderable(plus);
		rudderScreen.addRenderable(minus);
		rudderScreen.addRenderable(stepUp);
		rudderScreen.addRenderable(stepDown);

		rudderScreen.addRenderable(power);
	}

	@Override
	public void setVisibility(boolean visibility) {
		right.setVisibility(visibility);
		forward.setVisibility(visibility);
		left.setVisibility(visibility);
		backward.setVisibility(visibility);
		up.setVisibility(visibility);
		down.setVisibility(visibility);
		land.setVisibility(visibility);
		plus.setVisibility(visibility);
		minus.setVisibility(visibility);
		stepUp.setVisibility(visibility);
		stepDown.setVisibility(visibility);
		power.setVisibility(visibility);
		beaconButton.setVisibility(visibility);

		if (visibility) {
			if (rudderScreen.getMenu().getPowerButtonState() == 0) {
				menuButton.setVisibility(false);

				jump.setVisibility(true);
				beaconButton.setVisibility(true);
				land.setVisibility(true);
				forward.setVisibility(true);
				backward.setVisibility(true);
				left.setVisibility(true);
				right.setVisibility(true);
				up.setVisibility(true);
				down.setVisibility(true);
			} else {
				jump.setVisibility(false);
				beaconButton.setVisibility(false);
				land.setVisibility(false);
				forward.setVisibility(false);
				backward.setVisibility(false);
				left.setVisibility(false);
				right.setVisibility(false);
				up.setVisibility(false);
				down.setVisibility(false);

				menuButton.setVisibility(true);
			}
		} else {
			jump.setVisibility(false);
			menuButton.setVisibility(false);
			rudderScreen.getMenu().getHeatInputSlot().setActive(false);
			rudderScreen.getMenu().getSteamInputSlot().setActive(false);
			rudderScreen.getMenu().getEnderInputSlot().setActive(false);
		}
	}

	@Override
	public void renderBg() {
		RenderSystem.setShaderTexture(0, TEXTURE);
		rudderScreen.setImageWidth(MAIN_GUI_WIDTH);
		power.setState(rudderScreen.getMenu().getPowerButtonState());
		power.setNextState(rudderScreen.getMenu().getPowerButtonNextState());
		land.setState(rudderScreen.getMenu().getLandingButtonState());
		land.setNextState(rudderScreen.getMenu().getLandingButtonNextState());
	}

	@Override
	public void renderDynamicImages(PoseStack poseStack, int x, int y, float partialTick) {
		boolean[] enabledFunctions = rudderScreen.getMenu().getEnabledFunctions();
		//enableHeatEngine
		if (!enabledFunctions[0]) {
			rudderScreen.getMenu().getHeatInputSlot().setActive(false);
			drawTexturedModalRect(poseStack, x + 85, y + 7, 194, 106, 3, 87, partialTick);
			drawTexturedModalRect(poseStack, x + 150, y + 72, 199, 106, 18, 35, partialTick);
		} else {
			rudderScreen.getMenu().getHeatInputSlot().setActive(true);
			heat.render(poseStack, rudderScreen.getMenu().getFurnace(), 120, partialTick);
			burn.render(poseStack, rudderScreen.getMenu().getBurnTime(), rudderScreen.getMenu().getBurnTimeMax(), partialTick);
		}
		//enableSteamEngine
		if (!enabledFunctions[1]) {
			rudderScreen.getMenu().getSteamInputSlot().setActive(false);
			drawTexturedModalRect(poseStack, x + 127, y + 89, 199, 123, 18, 18, partialTick);
			drawTexturedModalRect(poseStack, x + 8, y + 7, 177, 106, 15, 87, partialTick);
			drawTexturedModalRect(poseStack, x + 27, y + 7, 177, 106, 15, 87, partialTick);
		} else {
			rudderScreen.getMenu().getSteamInputSlot().setActive(true);
			steam.render(poseStack, rudderScreen.getMenu().getSteam(), rudderScreen.getMenu().getSteamMax(), partialTick);
			water.render(poseStack, rudderScreen.getMenu().getWater(), rudderScreen.getMenu().getWaterMax(), partialTick);
		}
		//enableEnderEngine
		if (!enabledFunctions[2]) {
			rudderScreen.getMenu().getEnderInputSlot().setActive(false);
			drawTexturedModalRect(poseStack, x + 104, y + 89, 199, 123, 18, 18, partialTick);
			drawTexturedModalRect(poseStack, x + 94, y + 7, 194, 106, 3, 87, partialTick);
		} else {
			rudderScreen.getMenu().getEnderInputSlot().setActive(true);
			ender.render(poseStack, rudderScreen.getMenu().getEnder(), rudderScreen.getMenu().getEnderMax(), partialTick);
		}
		//enableHyperDrive
		if (!enabledFunctions[3]) {
			drawTexturedModalRect(poseStack, x + 135, y + 75, 219, 106, 10, 10, partialTick);
		}

		//position marker
		drawTexturedModalRect(poseStack, x + 152, y + 13, 96 + rudderScreen.getMenu().getPositionMarkerTop() * 19, 244, 17, 3, partialTick);
		drawTexturedModalRect(poseStack, x + 152, y + 16, 96 + rudderScreen.getMenu().getPositionMarkerMid() * 19, 247, 17, 3, partialTick);
		drawTexturedModalRect(poseStack, x + 152, y + 19, 96 + rudderScreen.getMenu().getPositionMarkerBottom() * 19, 250, 17, 5, partialTick);

		wind.render(poseStack, (int) (random.nextFloat(0.9f, 1.1f) * rudderScreen.getMenu().getWind()), (int) (1.2 * rudderScreen.getMenu().getWindMax()), partialTick);
		floating.render(poseStack, (int) (random.nextFloat(0.9f, 1.1f) * rudderScreen.getMenu().getFloating()), (int) (1.2 * rudderScreen.getMenu().getFloatingMax()), partialTick);

		RenderSystem.setShaderTexture(0, INNER_ROUND_TEXTURE);
		drawTexturedModalRect(poseStack, x + 112, y + 15, 1 + rudderScreen.getMenu().getInnerRoundType() * 32, 1, 30, 30, partialTick);
		coolDown.setU(1 + rudderScreen.getMenu().getInnerRoundType() * 32);
		coolDown.render(poseStack, rudderScreen.getMenu().getCoolDown(), FlyingShipsConfiguration.COOL_DOWN_TIME.get(), partialTick);
		RenderSystem.setShaderTexture(0, TEXTURE);
	}

	@Override
	public void renderLabels(PoseStack poseStack, Font font) {
		font.draw(poseStack, Component.literal("" + rudderScreen.getMenu().getSpeed() + " b"), 114,
				60, Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
	}

	@Override
	public void renderToolTips(PoseStack poseStack, int mouseX, int mouseY, int x, int y) {
		speedStepMessage.render(poseStack, mouseX, mouseY, Component.literal("+/- " + RudderBlockEntity.STEPS[rudderScreen.getMenu().getStep()]));
		windMessage.render(poseStack, mouseX, mouseY,
				Component.literal(rudderScreen.getMenu().getWind() + "/" + rudderScreen.getMenu().getWindMax()),
				Component.literal("" + rudderScreen.getMenu().usedStructuralEnergyForMovement()).withStyle(ChatFormatting.DARK_GRAY),
				Component.literal("" + rudderScreen.getMenu().usedSteamEnergyForMovement()).withStyle(ChatFormatting.GRAY),
				Component.literal("" + rudderScreen.getMenu().usedEnderEnergyForMovement()).withStyle(ChatFormatting.DARK_AQUA)
		);
		floatingMessage.render(poseStack, mouseX, mouseY,
				Component.literal(rudderScreen.getMenu().getFloating() + "/" + rudderScreen.getMenu().getFloatingMax()),
				Component.literal("" + rudderScreen.getMenu().usedStructuralEnergyForFloating()).withStyle(ChatFormatting.DARK_GRAY),
				Component.literal("" + rudderScreen.getMenu().usedHeatEnergyForFloating()).withStyle(ChatFormatting.DARK_RED),
				Component.literal("" + rudderScreen.getMenu().usedSteamEnergyForFloating()).withStyle(ChatFormatting.GRAY),
				Component.literal("" + rudderScreen.getMenu().usedEnderEnergyForFloating()).withStyle(ChatFormatting.DARK_AQUA)
		);

		steamMessage.render(poseStack, mouseX, mouseY, Component.literal(rudderScreen.getMenu().getSteam() + "/" + rudderScreen.getMenu().getSteamMax()));
		waterMessage.render(poseStack, mouseX, mouseY, Component.literal(rudderScreen.getMenu().getWater() + "/" + rudderScreen.getMenu().getWaterMax()));
		heatMessage.render(poseStack, mouseX, mouseY, Component.literal(rudderScreen.getMenu().getFurnace() + "/523 C°"), Component.literal(rudderScreen.getMenu().getHeat() + "/" + rudderScreen.getMenu().getHeatMax()));
		enderMessage.render(poseStack, mouseX, mouseY, Component.literal(rudderScreen.getMenu().getEnder() + "/" + rudderScreen.getMenu().getEnderMax()));

		Direction rudderDirection = rudderScreen.getMenu().getBlockEntity().getBlockState().getValue(Rudder.FACING);

		if (rudderScreen.getMenu().getPowerButtonState() == 0) {
			forwardMessage.render(poseStack, mouseX, mouseY, Component.translatable("gui.text." + rudderDirection.getOpposite()));
			backMessage.render(poseStack, mouseX, mouseY, Component.translatable("gui.text." + rudderDirection));
			leftMessage.render(poseStack, mouseX, mouseY, Component.translatable("gui.text." + rudderDirection.getClockWise()));
			rightMessage.render(poseStack, mouseX, mouseY, Component.translatable("gui.text." + rudderDirection.getCounterClockWise()));
			upMessage.render(poseStack, mouseX, mouseY, Component.translatable("gui.text.up"));
			downMessage.render(poseStack, mouseX, mouseY, Component.translatable("gui.text.down"));
		}
	}

	@Override
	public void blink(boolean on) {
		if (on) {
			Map<ButtonId, Boolean> blinking = rudderScreen.getMenu().getBlinking();
			forward.setBlink(blinking.get(ButtonId.FORWARD));
			right.setBlink(blinking.get(ButtonId.RIGHT));
			backward.setBlink(blinking.get(ButtonId.BACKWARD));
			left.setBlink(blinking.get(ButtonId.LEFT));
			up.setBlink(blinking.get(ButtonId.UP));
			down.setBlink(blinking.get(ButtonId.DOWN));

			land.setBlink(blinking.get(ButtonId.LAND));

			jump.setBlink(blinking.get(ButtonId.JUMP));

			beaconButton.setBlink(blinking.get(ButtonId.BEACON));
		} else {
			forward.setBlink(false);
			right.setBlink(false);
			backward.setBlink(false);
			left.setBlink(false);
			up.setBlink(false);
			down.setBlink(false);

			land.setBlink(false);

			jump.setBlink(false);

			beaconButton.setBlink(false);
		}
		land.setSelected(rudderScreen.getMenu().getBlinking().get(ButtonId.LAND));
	}

	@Override
	public void clickButton(ButtonId buttonId) {

	}
}
