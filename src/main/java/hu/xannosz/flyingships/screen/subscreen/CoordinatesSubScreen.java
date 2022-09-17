package hu.xannosz.flyingships.screen.subscreen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hu.xannosz.flyingships.FlyingShips;
import hu.xannosz.flyingships.networking.ButtonClickedPacket;
import hu.xannosz.flyingships.networking.ModMessages;
import hu.xannosz.flyingships.networking.SendNewCoordinatePacket;
import hu.xannosz.flyingships.screen.RudderScreen;
import hu.xannosz.flyingships.screen.widget.*;
import hu.xannosz.flyingships.warp.SavedCoordinate;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static hu.xannosz.flyingships.screen.RudderScreen.DEBUG_MODE;
import static hu.xannosz.flyingships.screen.constant.CoordinatesScreenConstants.*;
import static net.minecraftforge.client.gui.ScreenUtils.drawTexturedModalRect;

@Slf4j
public class CoordinatesSubScreen extends SubScreen {

	private static final ResourceLocation TEXTURE =
			new ResourceLocation(FlyingShips.MOD_ID, "textures/gui/rudder_gui_coordinates.png");

	private GraphicalButton pageUp;
	private GraphicalButton pageDown;
	private GraphicalButton addNew;
	private GraphicalButton back;

	private GraphicalButton jump1;
	private GraphicalButton edit1;
	private GraphicalButton jump2;
	private GraphicalButton edit2;
	private GraphicalButton jump3;
	private GraphicalButton edit3;
	private GraphicalButton jump4;
	private GraphicalButton edit4;
	private GraphicalButton jump5;
	private GraphicalButton edit5;
	private GraphicalButton jump6;
	private GraphicalButton edit6;

	private GraphicalButton ok;
	private GraphicalButton cancel;
	private GraphicalButton delete;
	private GraphicalButton markerUp;
	private GraphicalButton markerDown;
	private GraphicalButton absoluteCoordinates;

	private LoopBackEditBox name;

	private boolean addNewCoordinate = false;
	private int coordinateNum = 0;
	private SavedCoordinate newCoordinate;

	public CoordinatesSubScreen(RudderScreen rudderScreen) {
		super(rudderScreen);
	}

	@Override
	public void init(int x, int y) {

		rudderScreen.getMenu().updateCoordinates();

		pageUp = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.COORDINATE_PAGE_UP)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + BIG_BUTTONS_X)
				.hitBoxY(y + BIG_BUTTONS_Y)
				.hitBoxW(BIG_BUTTONS_W_H)
				.hitBoxH(BIG_BUTTONS_W_H)
				.graphicalX(x + BIG_BUTTONS_X)
				.graphicalY(y + BIG_BUTTONS_Y)
				.graphicalW(BIG_BUTTONS_W_H)
				.graphicalH(BIG_BUTTONS_W_H)
				.hoveredX(BIG_BUTTONS_G_X)
				.hoveredY(BIG_BUTTONS_G_Y)
				.build(), rudderScreen);
		pageDown = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.COORDINATE_PAGE_DOWN)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + BIG_BUTTONS_X)
				.hitBoxY(y + BIG_BUTTONS_Y + BIG_BUTTONS_Y_ADDITIONAL)
				.hitBoxW(BIG_BUTTONS_W_H)
				.hitBoxH(BIG_BUTTONS_W_H)
				.graphicalX(x + BIG_BUTTONS_X)
				.graphicalY(y + BIG_BUTTONS_Y + BIG_BUTTONS_Y_ADDITIONAL)
				.graphicalW(BIG_BUTTONS_W_H)
				.graphicalH(BIG_BUTTONS_W_H)
				.hoveredX(BIG_BUTTONS_G_X)
				.hoveredY(BIG_BUTTONS_G_Y + BIG_BUTTONS_G_Y_ADDITIONAL)
				.build(), rudderScreen);
		addNew = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.ADD_NEW_COORDINATE)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + BIG_BUTTONS_X)
				.hitBoxY(y + BIG_BUTTONS_Y + 2 * BIG_BUTTONS_Y_ADDITIONAL)
				.hitBoxW(BIG_BUTTONS_W_H)
				.hitBoxH(BIG_BUTTONS_W_H)
				.graphicalX(x + BIG_BUTTONS_X)
				.graphicalY(y + BIG_BUTTONS_Y + 2 * BIG_BUTTONS_Y_ADDITIONAL)
				.graphicalW(BIG_BUTTONS_W_H)
				.graphicalH(BIG_BUTTONS_W_H)
				.hoveredX(BIG_BUTTONS_G_X)
				.hoveredY(BIG_BUTTONS_G_Y + 2 * BIG_BUTTONS_G_Y_ADDITIONAL)
				.build(), rudderScreen);
		back = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.BACK)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + BIG_BUTTONS_X)
				.hitBoxY(y + BIG_BUTTONS_Y + 3 * BIG_BUTTONS_Y_ADDITIONAL)
				.hitBoxW(BIG_BUTTONS_W_H)
				.hitBoxH(BIG_BUTTONS_W_H)
				.graphicalX(x + BIG_BUTTONS_X)
				.graphicalY(y + BIG_BUTTONS_Y + 3 * BIG_BUTTONS_Y_ADDITIONAL)
				.graphicalW(BIG_BUTTONS_W_H)
				.graphicalH(BIG_BUTTONS_W_H)
				.hoveredX(BIG_BUTTONS_G_X)
				.hoveredY(BIG_BUTTONS_G_Y + 3 * BIG_BUTTONS_G_Y_ADDITIONAL)
				.build());

		jump1 = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.JUMP_TO_COORDINATE_1)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SMALL_BUTTONS_X)
				.hitBoxY(y + SMALL_BUTTONS_Y)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + SMALL_BUTTONS_X)
				.graphicalY(y + SMALL_BUTTONS_Y)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.buttonX(SMALL_BUTTONS_G_X)
				.buttonY(SMALL_BUTTONS_G_Y)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y)
				.build());
		edit1 = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.EDIT_COORDINATE_1)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SMALL_BUTTONS_X + SMALL_BUTTONS_X_ADDITIONAL)
				.hitBoxY(y + SMALL_BUTTONS_Y)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + SMALL_BUTTONS_X + SMALL_BUTTONS_X_ADDITIONAL)
				.graphicalY(y + SMALL_BUTTONS_Y)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.buttonX(SMALL_BUTTONS_G_X)
				.buttonY(SMALL_BUTTONS_G_Y + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.build(), rudderScreen);
		jump2 = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.JUMP_TO_COORDINATE_2)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SMALL_BUTTONS_X)
				.hitBoxY(y + SMALL_BUTTONS_Y + SMALL_BUTTONS_Y_ADDITIONAL)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + SMALL_BUTTONS_X)
				.graphicalY(y + SMALL_BUTTONS_Y + SMALL_BUTTONS_Y_ADDITIONAL)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.buttonX(SMALL_BUTTONS_G_X)
				.buttonY(SMALL_BUTTONS_G_Y)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y)
				.build());
		edit2 = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.EDIT_COORDINATE_2)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SMALL_BUTTONS_X + SMALL_BUTTONS_X_ADDITIONAL)
				.hitBoxY(y + SMALL_BUTTONS_Y + SMALL_BUTTONS_Y_ADDITIONAL)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + SMALL_BUTTONS_X + SMALL_BUTTONS_X_ADDITIONAL)
				.graphicalY(y + SMALL_BUTTONS_Y + SMALL_BUTTONS_Y_ADDITIONAL)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.buttonX(SMALL_BUTTONS_G_X)
				.buttonY(SMALL_BUTTONS_G_Y + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.build(), rudderScreen);
		jump3 = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.JUMP_TO_COORDINATE_3)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SMALL_BUTTONS_X)
				.hitBoxY(y + SMALL_BUTTONS_Y + 2 * SMALL_BUTTONS_Y_ADDITIONAL)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + SMALL_BUTTONS_X)
				.graphicalY(y + SMALL_BUTTONS_Y + 2 * SMALL_BUTTONS_Y_ADDITIONAL)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.buttonX(SMALL_BUTTONS_G_X)
				.buttonY(SMALL_BUTTONS_G_Y)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y)
				.build());
		edit3 = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.EDIT_COORDINATE_3)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SMALL_BUTTONS_X + SMALL_BUTTONS_X_ADDITIONAL)
				.hitBoxY(y + SMALL_BUTTONS_Y + 2 * SMALL_BUTTONS_Y_ADDITIONAL)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + SMALL_BUTTONS_X + SMALL_BUTTONS_X_ADDITIONAL)
				.graphicalY(y + SMALL_BUTTONS_Y + 2 * SMALL_BUTTONS_Y_ADDITIONAL)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.buttonX(SMALL_BUTTONS_G_X)
				.buttonY(SMALL_BUTTONS_G_Y + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.build(), rudderScreen);
		jump4 = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.JUMP_TO_COORDINATE_4)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SMALL_BUTTONS_X)
				.hitBoxY(y + SMALL_BUTTONS_Y + 3 * SMALL_BUTTONS_Y_ADDITIONAL)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + SMALL_BUTTONS_X)
				.graphicalY(y + SMALL_BUTTONS_Y + 3 * SMALL_BUTTONS_Y_ADDITIONAL)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.buttonX(SMALL_BUTTONS_G_X)
				.buttonY(SMALL_BUTTONS_G_Y)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y)
				.build());
		edit4 = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.EDIT_COORDINATE_4)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SMALL_BUTTONS_X + SMALL_BUTTONS_X_ADDITIONAL)
				.hitBoxY(y + SMALL_BUTTONS_Y + 3 * SMALL_BUTTONS_Y_ADDITIONAL)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + SMALL_BUTTONS_X + SMALL_BUTTONS_X_ADDITIONAL)
				.graphicalY(y + SMALL_BUTTONS_Y + 3 * SMALL_BUTTONS_Y_ADDITIONAL)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.buttonX(SMALL_BUTTONS_G_X)
				.buttonY(SMALL_BUTTONS_G_Y + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.build(), rudderScreen);
		jump5 = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.JUMP_TO_COORDINATE_5)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SMALL_BUTTONS_X)
				.hitBoxY(y + SMALL_BUTTONS_Y + 4 * SMALL_BUTTONS_Y_ADDITIONAL)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + SMALL_BUTTONS_X)
				.graphicalY(y + SMALL_BUTTONS_Y + 4 * SMALL_BUTTONS_Y_ADDITIONAL)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.buttonX(SMALL_BUTTONS_G_X)
				.buttonY(SMALL_BUTTONS_G_Y)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y)
				.build());
		edit5 = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.EDIT_COORDINATE_5)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SMALL_BUTTONS_X + SMALL_BUTTONS_X_ADDITIONAL)
				.hitBoxY(y + SMALL_BUTTONS_Y + 4 * SMALL_BUTTONS_Y_ADDITIONAL)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + SMALL_BUTTONS_X + SMALL_BUTTONS_X_ADDITIONAL)
				.graphicalY(y + SMALL_BUTTONS_Y + 4 * SMALL_BUTTONS_Y_ADDITIONAL)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.buttonX(SMALL_BUTTONS_G_X)
				.buttonY(SMALL_BUTTONS_G_Y + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.build(), rudderScreen);
		jump6 = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.JUMP_TO_COORDINATE_6)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SMALL_BUTTONS_X)
				.hitBoxY(y + SMALL_BUTTONS_Y + 5 * SMALL_BUTTONS_Y_ADDITIONAL)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + SMALL_BUTTONS_X)
				.graphicalY(y + SMALL_BUTTONS_Y + 5 * SMALL_BUTTONS_Y_ADDITIONAL)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.buttonX(SMALL_BUTTONS_G_X)
				.buttonY(SMALL_BUTTONS_G_Y)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y)
				.build());
		edit6 = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.EDIT_COORDINATE_6)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SMALL_BUTTONS_X + SMALL_BUTTONS_X_ADDITIONAL)
				.hitBoxY(y + SMALL_BUTTONS_Y + 5 * SMALL_BUTTONS_Y_ADDITIONAL)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + SMALL_BUTTONS_X + SMALL_BUTTONS_X_ADDITIONAL)
				.graphicalY(y + SMALL_BUTTONS_Y + 5 * SMALL_BUTTONS_Y_ADDITIONAL)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.buttonX(SMALL_BUTTONS_G_X)
				.buttonY(SMALL_BUTTONS_G_Y + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.build(), rudderScreen);

		ok = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.ADD_COORDINATE)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + EDIT_PANEL_X + EDIT_PANEL_BUTTONS_X)
				.hitBoxY(y + EDIT_PANEL_Y + EDIT_PANEL_BUTTONS_Y)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + EDIT_PANEL_X + EDIT_PANEL_BUTTONS_X)
				.graphicalY(y + EDIT_PANEL_Y + EDIT_PANEL_BUTTONS_Y)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y + 4 * SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.build(), rudderScreen);
		cancel = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.EXIT_COORDINATE_EDITION)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + EDIT_PANEL_X + EDIT_PANEL_BUTTONS_X + EDIT_PANEL_BUTTONS_X_ADDITIONAL)
				.hitBoxY(y + EDIT_PANEL_Y + EDIT_PANEL_BUTTONS_Y)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + EDIT_PANEL_X + EDIT_PANEL_BUTTONS_X + EDIT_PANEL_BUTTONS_X_ADDITIONAL)
				.graphicalY(y + EDIT_PANEL_Y + EDIT_PANEL_BUTTONS_Y)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y + 3 * SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.build(), rudderScreen);
		delete = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.DELETE_COORDINATE)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + EDIT_PANEL_X + EDIT_PANEL_BUTTONS_X + 2 * EDIT_PANEL_BUTTONS_X_ADDITIONAL)
				.hitBoxY(y + EDIT_PANEL_Y + EDIT_PANEL_BUTTONS_Y)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + EDIT_PANEL_X + EDIT_PANEL_BUTTONS_X + 2 * EDIT_PANEL_BUTTONS_X_ADDITIONAL)
				.graphicalY(y + EDIT_PANEL_Y + EDIT_PANEL_BUTTONS_Y)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.buttonX(SMALL_BUTTONS_G_X)
				.buttonY(SMALL_BUTTONS_G_Y + 2 * SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y + 2 * SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.build(), rudderScreen);
		markerUp = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.MARKER_UP)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + EDIT_PANEL_X + EDIT_PANEL_BUTTONS_X)
				.hitBoxY(y + EDIT_PANEL_Y + EDIT_PANEL_BUTTONS_Y + EDIT_PANEL_BUTTONS_Y_ADDITIONAL)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + EDIT_PANEL_X + EDIT_PANEL_BUTTONS_X)
				.graphicalY(y + EDIT_PANEL_Y + EDIT_PANEL_BUTTONS_Y + EDIT_PANEL_BUTTONS_Y_ADDITIONAL)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.buttonX(SMALL_BUTTONS_G_X)
				.buttonY(SMALL_BUTTONS_G_Y + 5 * SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y + 5 * SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.build(), rudderScreen);
		markerDown = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.MARKER_DOWN)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + EDIT_PANEL_X + EDIT_PANEL_BUTTONS_X + EDIT_PANEL_BUTTONS_X_ADDITIONAL)
				.hitBoxY(y + EDIT_PANEL_Y + EDIT_PANEL_BUTTONS_Y + EDIT_PANEL_BUTTONS_Y_ADDITIONAL)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + EDIT_PANEL_X + EDIT_PANEL_BUTTONS_X + EDIT_PANEL_BUTTONS_X_ADDITIONAL)
				.graphicalY(y + EDIT_PANEL_Y + EDIT_PANEL_BUTTONS_Y + EDIT_PANEL_BUTTONS_Y_ADDITIONAL)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.buttonX(SMALL_BUTTONS_G_X)
				.buttonY(SMALL_BUTTONS_G_Y + 6 * SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y + 6 * SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.build(), rudderScreen);
		absoluteCoordinates = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.ABSOLUTE_COORDINATE)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + EDIT_PANEL_X + EDIT_PANEL_BUTTONS_X + 2 * EDIT_PANEL_BUTTONS_X_ADDITIONAL)
				.hitBoxY(y + EDIT_PANEL_Y + EDIT_PANEL_BUTTONS_Y + EDIT_PANEL_BUTTONS_Y_ADDITIONAL)
				.hitBoxW(SMALL_BUTTONS_W_H)
				.hitBoxH(SMALL_BUTTONS_W_H)
				.graphicalX(x + EDIT_PANEL_X + EDIT_PANEL_BUTTONS_X + 2 * EDIT_PANEL_BUTTONS_X_ADDITIONAL)
				.graphicalY(y + EDIT_PANEL_Y + EDIT_PANEL_BUTTONS_Y + EDIT_PANEL_BUTTONS_Y_ADDITIONAL)
				.graphicalW(SMALL_BUTTONS_W_H)
				.graphicalH(SMALL_BUTTONS_W_H)
				.buttonX(SMALL_BUTTONS_G_X)
				.buttonY(SMALL_BUTTONS_G_Y + 7 * SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredX(SMALL_BUTTONS_G_X + SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.hoveredY(SMALL_BUTTONS_G_Y + 7 * SMALL_BUTTONS_G_ADDITIONAL_X_Y)
				.build(), rudderScreen);

		name = new LoopBackEditBox(rudderScreen.getFont(), x + EDIT_PANEL_X + EDIT_PANEL_NAME_X,
				y + EDIT_PANEL_Y + EDIT_PANEL_NAME_Y_1, EDIT_PANEL_NAME_W, EDIT_PANEL_NAME_H,
				Component.empty().withStyle(ChatFormatting.DARK_GRAY), ButtonId.EDITION_COORDINATE_DONE, rudderScreen);
	}

	@Override
	public void registerRenderables() {
		rudderScreen.addRenderable(pageUp);
		rudderScreen.addRenderable(pageDown);
		rudderScreen.addRenderable(addNew);
		rudderScreen.addRenderable(back);

		rudderScreen.addRenderable(jump1);
		rudderScreen.addRenderable(edit1);
		rudderScreen.addRenderable(jump2);
		rudderScreen.addRenderable(edit2);
		rudderScreen.addRenderable(jump3);
		rudderScreen.addRenderable(edit3);
		rudderScreen.addRenderable(jump4);
		rudderScreen.addRenderable(edit4);
		rudderScreen.addRenderable(jump5);
		rudderScreen.addRenderable(edit5);
		rudderScreen.addRenderable(jump6);
		rudderScreen.addRenderable(edit6);

		rudderScreen.addRenderable(ok);
		rudderScreen.addRenderable(cancel);
		rudderScreen.addRenderable(delete);
		rudderScreen.addRenderable(markerUp);
		rudderScreen.addRenderable(markerDown);
		rudderScreen.addRenderable(absoluteCoordinates);

		rudderScreen.addRenderable(name);
	}

	@Override
	public void setVisibility(boolean visibility) {
		int coordinatesSize = rudderScreen.getMenu().getCoordinates().size();

		jump1.setVisibility(visibility && coordinatesSize > 0);
		edit1.setVisibility(visibility && coordinatesSize > 0);
		jump2.setVisibility(visibility && coordinatesSize > 1);
		edit2.setVisibility(visibility && coordinatesSize > 1);
		jump6.setVisibility(visibility && coordinatesSize > 5);
		edit6.setVisibility(visibility && coordinatesSize > 5);

		if (visibility && !addNewCoordinate) {
			pageUp.setVisibility(true);
			pageDown.setVisibility(true);
			addNew.setVisibility(true);
			back.setVisibility(true);

			jump3.setVisibility(coordinatesSize > 2);
			edit3.setVisibility(coordinatesSize > 2);
			jump4.setVisibility(coordinatesSize > 3);
			edit4.setVisibility(coordinatesSize > 3);
			jump5.setVisibility(coordinatesSize > 4);
			edit5.setVisibility(coordinatesSize > 4);
		} else {
			pageUp.setVisibility(false);
			pageDown.setVisibility(false);
			addNew.setVisibility(false);
			back.setVisibility(false);

			jump3.setVisibility(false);
			edit3.setVisibility(false);
			jump4.setVisibility(false);
			edit4.setVisibility(false);
			jump5.setVisibility(false);
			edit5.setVisibility(false);
		}

		if (visibility && addNewCoordinate) {
			ok.setVisibility(true);
			cancel.setVisibility(true);
			delete.setVisibility(coordinateNum != 0);
			markerUp.setVisibility(coordinateNum == 0);
			markerDown.setVisibility(coordinateNum == 0);
			absoluteCoordinates.setVisibility(coordinateNum == 0);

			name.setVisible(true);
		} else {
			ok.setVisibility(false);
			cancel.setVisibility(false);
			delete.setVisibility(false);
			markerUp.setVisibility(false);
			markerDown.setVisibility(false);
			absoluteCoordinates.setVisibility(false);

			name.setVisible(false);
		}
	}

	@Override
	public void renderBg() {
		RenderSystem.setShaderTexture(0, TEXTURE);
		rudderScreen.setImageWidth(COORDINATES_GUI_WIDTH);
	}

	@Override
	public void renderDynamicImages(PoseStack poseStack, int x, int y, float partialTick) {
		if (addNewCoordinate) {
			drawTexturedModalRect(poseStack, x + EDIT_PANEL_X, y + EDIT_PANEL_Y, EDIT_PANEL_G_X, EDIT_PANEL_G_Y, EDIT_PANEL_W, EDIT_PANEL_H, partialTick);
		}
	}

	@Override
	public void renderLabels(PoseStack poseStack, Font font) {
		List<SavedCoordinate> savedCoordinates = rudderScreen.getMenu().getCoordinates();

		for (int i = 0; i < savedCoordinates.size(); i++) {
			if (addNewCoordinate && (i == 2 || i == 3 || i == 4)) {
				continue;
			}
			font.draw(poseStack, Component.literal(savedCoordinates.get(i).getName()), NAMES_X,
					NAMES_Y + i * NAMES_ADDITIONAL, Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
		}
		if (addNewCoordinate) {
			if (newCoordinate != null) {
				String marker = newCoordinate.getMarker();
				font.draw(poseStack, Component.literal(marker == null ? "" : marker), EDIT_PANEL_NAME_X,
						EDIT_PANEL_NAME_Y_2, Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
			}
		}
		font.draw(poseStack, Component.literal(rudderScreen.getMenu().getCoordinatesPage() + "/" + rudderScreen.getMenu().getCoordinatesMaxPage()), 154,
				92, Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
	}

	@Override
	public void renderToolTips(PoseStack poseStack, int mouseX, int mouseY, int x, int y) {

	}

	@Override
	public void blink(boolean on) {
		if (on) {
			Map<ButtonId, Boolean> blinking = rudderScreen.getMenu().getCoordinateBlinking();
			jump1.setBlink(blinking.get(ButtonId.JUMP_TO_COORDINATE_1));
			jump2.setBlink(blinking.get(ButtonId.JUMP_TO_COORDINATE_2));
			jump3.setBlink(blinking.get(ButtonId.JUMP_TO_COORDINATE_3));
			jump4.setBlink(blinking.get(ButtonId.JUMP_TO_COORDINATE_4));
			jump5.setBlink(blinking.get(ButtonId.JUMP_TO_COORDINATE_5));
			jump6.setBlink(blinking.get(ButtonId.JUMP_TO_COORDINATE_6));
		} else {
			jump1.setBlink(false);
			jump2.setBlink(false);
			jump3.setBlink(false);
			jump4.setBlink(false);
			jump5.setBlink(false);
			jump6.setBlink(false);
		}
	}

	@Override
	public void clickButton(ButtonId buttonId) {
		switch (buttonId) {
			case COORDINATE_PAGE_UP ->
					ModMessages.sendToServer(new ButtonClickedPacket(ButtonId.COORDINATE_PAGE_UP, rudderScreen.getMenu().getBlockEntity().getBlockPos()));
			case COORDINATE_PAGE_DOWN ->
					ModMessages.sendToServer(new ButtonClickedPacket(ButtonId.COORDINATE_PAGE_DOWN, rudderScreen.getMenu().getBlockEntity().getBlockPos()));
			case ADD_NEW_COORDINATE -> {
				addNewCoordinate = true;
				coordinateNum = 0;
				newCoordinate = new SavedCoordinate();
				name.setValue("");
			}
			case EDIT_COORDINATE_1 -> {
				addNewCoordinate = true;
				coordinateNum = 1;
				newCoordinate = rudderScreen.getMenu().getCoordinates().get(0);
				name.setValue(newCoordinate.getName());
			}
			case EDIT_COORDINATE_2 -> {
				addNewCoordinate = true;
				coordinateNum = 2;
				newCoordinate = rudderScreen.getMenu().getCoordinates().get(1);
				name.setValue(newCoordinate.getName());
			}
			case EDIT_COORDINATE_3 -> {
				addNewCoordinate = true;
				coordinateNum = 3;
				newCoordinate = rudderScreen.getMenu().getCoordinates().get(2);
				name.setValue(newCoordinate.getName());
			}
			case EDIT_COORDINATE_4 -> {
				addNewCoordinate = true;
				coordinateNum = 4;
				newCoordinate = rudderScreen.getMenu().getCoordinates().get(3);
				name.setValue(newCoordinate.getName());
			}
			case EDIT_COORDINATE_5 -> {
				addNewCoordinate = true;
				coordinateNum = 5;
				newCoordinate = rudderScreen.getMenu().getCoordinates().get(4);
				name.setValue(newCoordinate.getName());
			}
			case EDIT_COORDINATE_6 -> {
				addNewCoordinate = true;
				coordinateNum = 6;
				newCoordinate = rudderScreen.getMenu().getCoordinates().get(5);
				name.setValue(newCoordinate.getName());
			}
			case ADD_COORDINATE -> {
				newCoordinate.setName(name.getValue());
				ModMessages.sendToServer(
						new SendNewCoordinatePacket(coordinateNum,
								rudderScreen.getMenu().getBlockEntity().getBlockPos(), newCoordinate));
				addNewCoordinate = false;
				newCoordinate = null;
			}
			case EXIT_COORDINATE_EDITION -> {
				addNewCoordinate = false;
				newCoordinate = null;
			}
			case DELETE_COORDINATE -> {
				ModMessages.sendToServer(
						new SendNewCoordinatePacket(coordinateNum,
								rudderScreen.getMenu().getBlockEntity().getBlockPos(), null));
				addNewCoordinate = false;
				newCoordinate = null;
			}
			case MARKER_UP -> log.error("marker up not implemented");
			case MARKER_DOWN -> log.error("marker down not implemented");
			case ABSOLUTE_COORDINATE -> log.error("absolute coordinate not implemented");
		}
		rudderScreen.getMenu().updateCoordinates();
	}

	@Override
	public boolean keyPressed(int p_97765_, int p_97766_, int p_97767_) {
		if (p_97765_ == 69 && addNewCoordinate) {
			return false;
		}
		return super.keyPressed(p_97765_, p_97766_, p_97767_);
	}
}
