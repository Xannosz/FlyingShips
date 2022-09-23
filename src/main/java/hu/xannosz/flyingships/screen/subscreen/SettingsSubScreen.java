package hu.xannosz.flyingships.screen.subscreen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hu.xannosz.flyingships.FlyingShips;
import hu.xannosz.flyingships.networking.AddRectanglePacket;
import hu.xannosz.flyingships.networking.ModMessages;
import hu.xannosz.flyingships.screen.RudderScreen;
import hu.xannosz.flyingships.screen.widget.*;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

import static hu.xannosz.flyingships.screen.RudderScreen.DEBUG_MODE;
import static hu.xannosz.flyingships.screen.constant.SettingsScreenConstants.*;
import static net.minecraftforge.client.gui.ScreenUtils.drawTexturedModalRect;

@Slf4j
public class SettingsSubScreen extends SubScreen {

	private static final ResourceLocation TEXTURE =
			new ResourceLocation(FlyingShips.MOD_ID, "textures/gui/rudder_gui_settings.png");

	private GraphicalButton pageUp;
	private GraphicalButton pageDown;
	private GraphicalButton addNew;
	private GraphicalButton back;

	private GraphicalButton editRec1;
	private GraphicalButton deleteRec1;
	private GraphicalButton editRec2;
	private GraphicalButton deleteRec2;
	private GraphicalButton editRec3;
	private GraphicalButton deleteRec3;

	private GraphicalButton okEdition;
	private GraphicalButton exitEdition;

	private LoopBackEditBox rec1x;
	private LoopBackEditBox rec1y;
	private LoopBackEditBox rec1z;
	private LoopBackEditBox rec2x;
	private LoopBackEditBox rec2y;
	private LoopBackEditBox rec2z;

	private GraphicalButton waterLineUp;
	private GraphicalButton waterLineDown;

	private GraphicalButton enableHeatEngine;
	private GraphicalButton enableSteamEngine;
	private GraphicalButton enableEnderEngine;
	private HelpMessage waterLineMessage;

	private boolean addNewRec = false;

	private int recNum = 0;

	public SettingsSubScreen(RudderScreen rudderScreen) {
		super(rudderScreen);
	}

	@Override
	public void init(int x, int y) {
		pageUp = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.RECTANGLE_PAGE_UP)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_X)
				.hitBoxY(y + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_Y)
				.hitBoxW(SETTINGS_BIG_BUTTON_W_H)
				.hitBoxH(SETTINGS_BIG_BUTTON_W_H)
				.graphicalX(x + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_X)
				.graphicalY(y + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_Y)
				.graphicalW(SETTINGS_BIG_BUTTON_W_H)
				.graphicalH(SETTINGS_BIG_BUTTON_W_H)
				.hoveredX(SETTINGS_BIG_BUTTON_MAIN_ROW_X)
				.hoveredY(SETTINGS_BIG_BUTTON_MAIN_ROW_Y)
				.build());
		pageDown = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.RECTANGLE_PAGE_DOWN)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_X)
				.hitBoxY(y + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_Y + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_ADDITIONAL)
				.hitBoxW(SETTINGS_BIG_BUTTON_W_H)
				.hitBoxH(SETTINGS_BIG_BUTTON_W_H)
				.graphicalX(x + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_X)
				.graphicalY(y + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_Y + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_ADDITIONAL)
				.graphicalW(SETTINGS_BIG_BUTTON_W_H)
				.graphicalH(SETTINGS_BIG_BUTTON_W_H)
				.hoveredX(SETTINGS_BIG_BUTTON_MAIN_ROW_X)
				.hoveredY(SETTINGS_BIG_BUTTON_MAIN_ROW_Y + SETTINGS_BIG_BUTTON_MAIN_ROW_ADDITIONAL)
				.build());
		addNew = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.ADD_NEW_RECTANGLE)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_X)
				.hitBoxY(y + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_Y + 2 * SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_ADDITIONAL)
				.hitBoxW(SETTINGS_BIG_BUTTON_W_H)
				.hitBoxH(SETTINGS_BIG_BUTTON_W_H)
				.graphicalX(x + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_X)
				.graphicalY(y + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_Y + 2 * SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_ADDITIONAL)
				.graphicalW(SETTINGS_BIG_BUTTON_W_H)
				.graphicalH(SETTINGS_BIG_BUTTON_W_H)
				.hoveredX(SETTINGS_BIG_BUTTON_MAIN_ROW_X)
				.hoveredY(SETTINGS_BIG_BUTTON_MAIN_ROW_Y + 2 * SETTINGS_BIG_BUTTON_MAIN_ROW_ADDITIONAL)
				.build(), rudderScreen);
		back = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.BACK)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_X)
				.hitBoxY(y + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_Y + 3 * SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_ADDITIONAL)
				.hitBoxW(SETTINGS_BIG_BUTTON_W_H)
				.hitBoxH(SETTINGS_BIG_BUTTON_W_H)
				.graphicalX(x + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_X)
				.graphicalY(y + SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_Y + 3 * SETTINGS_BIG_BUTTON_GRAPHICAL_ROW_ADDITIONAL)
				.graphicalW(SETTINGS_BIG_BUTTON_W_H)
				.graphicalH(SETTINGS_BIG_BUTTON_W_H)
				.hoveredX(SETTINGS_BIG_BUTTON_MAIN_ROW_X)
				.hoveredY(SETTINGS_BIG_BUTTON_MAIN_ROW_Y + 3 * SETTINGS_BIG_BUTTON_MAIN_ROW_ADDITIONAL)
				.build());

		editRec1 = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.EDIT_REC_1)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_X)
				.hitBoxY(y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_Y)
				.hitBoxW(SETTINGS_SMALL_BUTTON_W_H)
				.hitBoxH(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalX(x + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_X)
				.graphicalY(y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_Y)
				.graphicalW(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalH(SETTINGS_SMALL_BUTTON_W_H)
				.buttonX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X)
				.buttonY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y)
				.hoveredX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.hoveredY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y)
				.build(), rudderScreen);
		deleteRec1 = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.DELETE_REC_1)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_X)
				.hitBoxY(y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_Y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL)
				.hitBoxW(SETTINGS_SMALL_BUTTON_W_H)
				.hitBoxH(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalX(x + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_X)
				.graphicalY(y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_Y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL)
				.graphicalW(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalH(SETTINGS_SMALL_BUTTON_W_H)
				.buttonX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X)
				.buttonY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.hoveredX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.hoveredY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.build());
		editRec2 = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.EDIT_REC_2)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_X)
				.hitBoxY(y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_Y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_REC_ADDITIONAL)
				.hitBoxW(SETTINGS_SMALL_BUTTON_W_H)
				.hitBoxH(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalX(x + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_X)
				.graphicalY(y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_Y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_REC_ADDITIONAL)
				.graphicalW(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalH(SETTINGS_SMALL_BUTTON_W_H)
				.buttonX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X)
				.buttonY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y)
				.hoveredX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.hoveredY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y)
				.build(), rudderScreen);
		deleteRec2 = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.DELETE_REC_2)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_X)
				.hitBoxY(y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_Y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_REC_ADDITIONAL + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL)
				.hitBoxW(SETTINGS_SMALL_BUTTON_W_H)
				.hitBoxH(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalX(x + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_X)
				.graphicalY(y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_Y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_REC_ADDITIONAL + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL)
				.graphicalW(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalH(SETTINGS_SMALL_BUTTON_W_H)
				.buttonX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X)
				.buttonY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.hoveredX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.hoveredY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.build());
		editRec3 = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.EDIT_REC_3)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_X)
				.hitBoxY(y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_Y + 2 * SETTINGS_REC_BUTTON_GRAPHICAL_ROW_REC_ADDITIONAL)
				.hitBoxW(SETTINGS_SMALL_BUTTON_W_H)
				.hitBoxH(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalX(x + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_X)
				.graphicalY(y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_Y + 2 * SETTINGS_REC_BUTTON_GRAPHICAL_ROW_REC_ADDITIONAL)
				.graphicalW(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalH(SETTINGS_SMALL_BUTTON_W_H)
				.buttonX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X)
				.buttonY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y)
				.hoveredX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.hoveredY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y)
				.build(), rudderScreen);
		deleteRec3 = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.DELETE_REC_3)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(true)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_X)
				.hitBoxY(y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_Y + 2 * SETTINGS_REC_BUTTON_GRAPHICAL_ROW_REC_ADDITIONAL + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL)
				.hitBoxW(SETTINGS_SMALL_BUTTON_W_H)
				.hitBoxH(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalX(x + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_X)
				.graphicalY(y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_Y + 2 * SETTINGS_REC_BUTTON_GRAPHICAL_ROW_REC_ADDITIONAL + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL)
				.graphicalW(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalH(SETTINGS_SMALL_BUTTON_W_H)
				.buttonX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X)
				.buttonY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.hoveredX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.hoveredY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.build());

		okEdition = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.OK_EDITION)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SETTINGS_ADD_NEW_REC_X + SETTINGS_ADD_NEW_REC_BUTTON_ADDITION_X)
				.hitBoxY(y + SETTINGS_ADD_NEW_REC_Y + SETTINGS_ADD_NEW_REC_BUTTON_ADDITION_Y)
				.hitBoxW(SETTINGS_SMALL_BUTTON_W_H)
				.hitBoxH(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalX(x + SETTINGS_ADD_NEW_REC_X + SETTINGS_ADD_NEW_REC_BUTTON_ADDITION_X)
				.graphicalY(y + SETTINGS_ADD_NEW_REC_Y + SETTINGS_ADD_NEW_REC_BUTTON_ADDITION_Y)
				.graphicalW(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalH(SETTINGS_SMALL_BUTTON_W_H)
				.hoveredX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.hoveredY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y + 3 * SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.build(), rudderScreen);
		exitEdition = new LoopBackButton(ButtonConfig.builder()
				.buttonId(ButtonId.EXIT_EDITION)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + SETTINGS_ADD_NEW_REC_X + SETTINGS_ADD_NEW_REC_BUTTON_ADDITION_X)
				.hitBoxY(y + SETTINGS_ADD_NEW_REC_Y + SETTINGS_ADD_NEW_REC_BUTTON_ADDITION_Y + SETTINGS_SMALL_BUTTON_W_H + 4)
				.hitBoxW(SETTINGS_SMALL_BUTTON_W_H)
				.hitBoxH(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalX(x + SETTINGS_ADD_NEW_REC_X + SETTINGS_ADD_NEW_REC_BUTTON_ADDITION_X)
				.graphicalY(y + SETTINGS_ADD_NEW_REC_Y + SETTINGS_ADD_NEW_REC_BUTTON_ADDITION_Y + SETTINGS_SMALL_BUTTON_W_H + 4)
				.graphicalW(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalH(SETTINGS_SMALL_BUTTON_W_H)
				.hoveredX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.hoveredY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y + 2 * SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.build(), rudderScreen);

		rec1x = new LoopBackEditBox(rudderScreen.getFont(), x + SETTINGS_ADD_NEW_REC_X + ADD_NEW_EDIT_BOX_X, y + SETTINGS_ADD_NEW_REC_Y + ADD_NEW_EDIT_BOX_Y, ADD_NEW_EDIT_BOX_W, EDIT_BOX_H,
				Component.empty().withStyle(ChatFormatting.DARK_GRAY), ButtonId.BLOCK_POS_EDITION_DOWN, rudderScreen);
		rec1y = new LoopBackEditBox(rudderScreen.getFont(), x + SETTINGS_ADD_NEW_REC_X + ADD_NEW_EDIT_BOX_X + ADD_NEW_EDIT_BOX_ADDITIONAL_X, y + SETTINGS_ADD_NEW_REC_Y + ADD_NEW_EDIT_BOX_Y, ADD_NEW_EDIT_BOX_W, EDIT_BOX_H,
				Component.empty().withStyle(ChatFormatting.DARK_GRAY), ButtonId.BLOCK_POS_EDITION_DOWN, rudderScreen);
		rec1z = new LoopBackEditBox(rudderScreen.getFont(), x + SETTINGS_ADD_NEW_REC_X + ADD_NEW_EDIT_BOX_X + 2 * ADD_NEW_EDIT_BOX_ADDITIONAL_X, y + SETTINGS_ADD_NEW_REC_Y + ADD_NEW_EDIT_BOX_Y, ADD_NEW_EDIT_BOX_W, EDIT_BOX_H,
				Component.empty().withStyle(ChatFormatting.DARK_GRAY), ButtonId.BLOCK_POS_EDITION_DOWN, rudderScreen);
		rec2x = new LoopBackEditBox(rudderScreen.getFont(), x + SETTINGS_ADD_NEW_REC_X + ADD_NEW_EDIT_BOX_X, y + SETTINGS_ADD_NEW_REC_Y + ADD_NEW_EDIT_BOX_Y + ADD_NEW_EDIT_BOX_ADDITIONAL_Y, ADD_NEW_EDIT_BOX_W, EDIT_BOX_H,
				Component.empty().withStyle(ChatFormatting.DARK_GRAY), ButtonId.BLOCK_POS_EDITION_DOWN, rudderScreen);
		rec2y = new LoopBackEditBox(rudderScreen.getFont(), x + SETTINGS_ADD_NEW_REC_X + ADD_NEW_EDIT_BOX_X + ADD_NEW_EDIT_BOX_ADDITIONAL_X, y + SETTINGS_ADD_NEW_REC_Y + ADD_NEW_EDIT_BOX_Y + ADD_NEW_EDIT_BOX_ADDITIONAL_Y, ADD_NEW_EDIT_BOX_W, EDIT_BOX_H,
				Component.empty().withStyle(ChatFormatting.DARK_GRAY), ButtonId.BLOCK_POS_EDITION_DOWN, rudderScreen);
		rec2z = new LoopBackEditBox(rudderScreen.getFont(), x + SETTINGS_ADD_NEW_REC_X + ADD_NEW_EDIT_BOX_X + 2 * ADD_NEW_EDIT_BOX_ADDITIONAL_X, y + SETTINGS_ADD_NEW_REC_Y + ADD_NEW_EDIT_BOX_Y + ADD_NEW_EDIT_BOX_ADDITIONAL_Y, ADD_NEW_EDIT_BOX_W, EDIT_BOX_H,
				Component.empty().withStyle(ChatFormatting.DARK_GRAY), ButtonId.BLOCK_POS_EDITION_DOWN, rudderScreen);

		waterLineUp = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.WATER_LINE_UP)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + CHECK_BOX_X)
				.hitBoxY(y + CHECK_BOX_Y)
				.hitBoxW(SETTINGS_SMALL_BUTTON_W_H)
				.hitBoxH(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalX(x + CHECK_BOX_X)
				.graphicalY(y + CHECK_BOX_Y)
				.graphicalW(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalH(SETTINGS_SMALL_BUTTON_W_H)
				.hoveredX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.hoveredY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y + 5 * SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.build());
		waterLineDown = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.WATER_LINE_DOWN)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + CHECK_BOX_X - SETTINGS_SMALL_BUTTON_W_H - 4)
				.hitBoxY(y + CHECK_BOX_Y)
				.hitBoxW(SETTINGS_SMALL_BUTTON_W_H)
				.hitBoxH(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalX(x + CHECK_BOX_X - SETTINGS_SMALL_BUTTON_W_H - 4)
				.graphicalY(y + CHECK_BOX_Y)
				.graphicalW(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalH(SETTINGS_SMALL_BUTTON_W_H)
				.hoveredX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.hoveredY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y + 4 * SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.build());

		enableHeatEngine = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.HEAT_ENGINE)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + CHECK_BOX_X)
				.hitBoxY(y + CHECK_BOX_Y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL)
				.hitBoxW(SETTINGS_SMALL_BUTTON_W_H)
				.hitBoxH(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalX(x + CHECK_BOX_X)
				.graphicalY(y + CHECK_BOX_Y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL)
				.graphicalW(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalH(SETTINGS_SMALL_BUTTON_W_H)
				.hoveredX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.hoveredY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y + 2 * SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.build());
		enableSteamEngine = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.STEAM_ENGINE)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + CHECK_BOX_X)
				.hitBoxY(y + CHECK_BOX_Y + 2 * SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL)
				.hitBoxW(SETTINGS_SMALL_BUTTON_W_H)
				.hitBoxH(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalX(x + CHECK_BOX_X)
				.graphicalY(y + CHECK_BOX_Y + 2 * SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL)
				.graphicalW(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalH(SETTINGS_SMALL_BUTTON_W_H)
				.hoveredX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.hoveredY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y + 2 * SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.build());
		enableEnderEngine = new GraphicalButton(ButtonConfig.builder()
				.buttonId(ButtonId.ENDER_ENGINE)
				.position(rudderScreen.getMenu().getBlockEntity().getBlockPos())
				.isDefaultButtonDrawNeeded(false)
				.debugMode(DEBUG_MODE)
				.hitBoxX(x + CHECK_BOX_X)
				.hitBoxY(y + CHECK_BOX_Y + 3 * SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL)
				.hitBoxW(SETTINGS_SMALL_BUTTON_W_H)
				.hitBoxH(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalX(x + CHECK_BOX_X)
				.graphicalY(y + CHECK_BOX_Y + 3 * SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL)
				.graphicalW(SETTINGS_SMALL_BUTTON_W_H)
				.graphicalH(SETTINGS_SMALL_BUTTON_W_H)
				.hoveredX(SETTINGS_SMALL_BUTTON_MAIN_ROW_X + SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.hoveredY(SETTINGS_SMALL_BUTTON_MAIN_ROW_Y + 2 * SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL)
				.build());

		waterLineMessage = new HelpMessage(170, 11, 22, 8, x, y, 173, 13, rudderScreen);
	}

	@Override
	public void registerRenderables() {
		rudderScreen.addRenderable(pageUp);
		rudderScreen.addRenderable(pageDown);
		rudderScreen.addRenderable(addNew);
		rudderScreen.addRenderable(back);

		rudderScreen.addRenderable(editRec1);
		rudderScreen.addRenderable(deleteRec1);
		rudderScreen.addRenderable(editRec2);
		rudderScreen.addRenderable(deleteRec2);
		rudderScreen.addRenderable(editRec3);
		rudderScreen.addRenderable(deleteRec3);

		rudderScreen.addRenderable(okEdition);
		rudderScreen.addRenderable(exitEdition);

		rudderScreen.addRenderable(rec1x);
		rudderScreen.addRenderable(rec1y);
		rudderScreen.addRenderable(rec1z);
		rudderScreen.addRenderable(rec2x);
		rudderScreen.addRenderable(rec2y);
		rudderScreen.addRenderable(rec2z);

		rudderScreen.addRenderable(waterLineUp);
		rudderScreen.addRenderable(waterLineDown);

		rudderScreen.addRenderable(enableHeatEngine);
		rudderScreen.addRenderable(enableSteamEngine);
		rudderScreen.addRenderable(enableEnderEngine);
	}

	@Override
	public void setVisibility(boolean visibility) {
		int blocksSize = rudderScreen.getMenu().getBlockPosStruct().size();

		if (visibility && !addNewRec) {
			pageUp.setVisibility(true);
			pageDown.setVisibility(true);
			addNew.setVisibility(true);
			back.setVisibility(true);

			editRec1.setVisibility(blocksSize > 0);
			deleteRec1.setVisibility(blocksSize > 0);
			editRec2.setVisibility(blocksSize > 2);
			deleteRec2.setVisibility(blocksSize > 2);
			editRec3.setVisibility(blocksSize > 4);
			deleteRec3.setVisibility(blocksSize > 4);

			waterLineUp.setVisibility(true);
			waterLineDown.setVisibility(true);
			enableHeatEngine.setVisibility(true);
			enableSteamEngine.setVisibility(true);
			enableEnderEngine.setVisibility(true);
		} else {
			pageUp.setVisibility(false);
			pageDown.setVisibility(false);
			addNew.setVisibility(false);
			back.setVisibility(false);

			editRec1.setVisibility(false);
			deleteRec1.setVisibility(false);
			editRec2.setVisibility(false);
			deleteRec2.setVisibility(false);
			editRec3.setVisibility(false);
			deleteRec3.setVisibility(false);

			waterLineUp.setVisibility(false);
			waterLineDown.setVisibility(false);
			enableHeatEngine.setVisibility(false);
			enableSteamEngine.setVisibility(false);
			enableEnderEngine.setVisibility(false);
		}

		if (visibility && addNewRec) {
			rec1x.setVisible(true);
			rec1y.setVisible(true);
			rec1z.setVisible(true);
			rec2x.setVisible(true);
			rec2y.setVisible(true);
			rec2z.setVisible(true);

			okEdition.setVisibility(true);
			exitEdition.setVisibility(true);
		} else {
			rec1x.setVisible(false);
			rec1y.setVisible(false);
			rec1z.setVisible(false);
			rec2x.setVisible(false);
			rec2y.setVisible(false);
			rec2z.setVisible(false);

			okEdition.setVisibility(false);
			exitEdition.setVisibility(false);
		}
	}

	@Override
	public void renderBg() {
		RenderSystem.setShaderTexture(0, TEXTURE);
		rudderScreen.setImageWidth(SETTINGS_GUI_WIDTH);
	}

	@Override
	public void renderDynamicImages(PoseStack poseStack, int x, int y, float partialTick) {
		if (addNewRec) {
			drawTexturedModalRect(poseStack, x + SETTINGS_ADD_NEW_REC_X, y + SETTINGS_ADD_NEW_REC_Y, 1, 194, SETTINGS_ADD_NEW_REC_W, SETTINGS_ADD_NEW_REC_H, partialTick);
		} else {
			boolean[] functions = rudderScreen.getMenu().getEnabledFunctions();
			if (functions[0]) {
				drawTexturedModalRect(poseStack, x + CHECK_BOX_X, y + CHECK_BOX_Y + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL,
						SETTINGS_SMALL_BUTTON_MAIN_ROW_X, SETTINGS_SMALL_BUTTON_MAIN_ROW_Y + 2 * SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL, SETTINGS_SMALL_BUTTON_W_H, SETTINGS_SMALL_BUTTON_W_H, partialTick);
			}
			if (functions[1]) {
				drawTexturedModalRect(poseStack, x + CHECK_BOX_X, y + CHECK_BOX_Y + 2 * SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL,
						SETTINGS_SMALL_BUTTON_MAIN_ROW_X, SETTINGS_SMALL_BUTTON_MAIN_ROW_Y + 2 * SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL, SETTINGS_SMALL_BUTTON_W_H, SETTINGS_SMALL_BUTTON_W_H, partialTick);
			}
			if (functions[2]) {
				drawTexturedModalRect(poseStack, x + CHECK_BOX_X, y + CHECK_BOX_Y + 3 * SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL,
						SETTINGS_SMALL_BUTTON_MAIN_ROW_X, SETTINGS_SMALL_BUTTON_MAIN_ROW_Y + 2 * SETTINGS_SMALL_BUTTON_MAIN_ROW_ADDITIONAL, SETTINGS_SMALL_BUTTON_W_H, SETTINGS_SMALL_BUTTON_W_H, partialTick);
			}
		}
	}

	@Override
	public void renderLabels(PoseStack poseStack, Font font) {
		if (!addNewRec) {
			List<BlockPos> blocks = rudderScreen.getMenu().getBlockPosStruct();
			for (int i = 0; i < blocks.size(); i += 2) {
				font.draw(poseStack, Component.literal("" + blocks.get(i).getX()), 8,
						16 + (int) (i * 14.5), Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
				font.draw(poseStack, Component.literal("" + blocks.get(i).getY()), 52,
						16 + (int) (i * 14.5), Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
				font.draw(poseStack, Component.literal("" + blocks.get(i).getZ()), 96,
						16 + (int) (i * 14.5), Objects.requireNonNull(ChatFormatting.WHITE.getColor()));

				font.draw(poseStack, Component.literal("" + blocks.get(i + 1).getX()), 8,
						16 + 13 + (int) (i * 14.5), Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
				font.draw(poseStack, Component.literal("" + blocks.get(i + 1).getY()), 52,
						16 + 13 + (int) (i * 14.5), Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
				font.draw(poseStack, Component.literal("" + blocks.get(i + 1).getZ()), 96,
						16 + 13 + (int) (i * 14.5), Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
			}
			font.draw(poseStack, Component.literal("" + rudderScreen.getMenu().getWaterLine()), 173,
					13, Objects.requireNonNull(ChatFormatting.WHITE.getColor()));

			font.draw(poseStack, Component.literal("heat"), 173,
					13 + SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL, Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
			font.draw(poseStack, Component.literal("steam"), 173,
					13 + 2 * SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL, Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
			font.draw(poseStack, Component.literal("ender"), 173,
					13 + 3 * SETTINGS_REC_BUTTON_GRAPHICAL_ROW_BUTTON_ADDITIONAL, Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
		}
		font.draw(poseStack, Component.literal("" + rudderScreen.getMenu().getBlockPosPage() + "/" + rudderScreen.getMenu().getBlockPosMaxPage()), 154,
				90, Objects.requireNonNull(ChatFormatting.WHITE.getColor()));
	}

	@Override
	public void renderToolTips(PoseStack poseStack, int mouseX, int mouseY, int x, int y) {
		waterLineMessage.render(poseStack, mouseX, mouseY, Component.translatable("gui.text.waterLine"));
	}

	@Override
	public void blink(boolean on) {

	}

	@Override
	public void clickButton(ButtonId buttonId) {
		switch (buttonId) {
			case ADD_NEW_RECTANGLE -> {
				addNewRec = true;
				recNum = 4;
				rec1x.setValue("");
				rec1y.setValue("");
				rec1z.setValue("");
				rec2x.setValue("");
				rec2y.setValue("");
				rec2z.setValue("");
			}
			case EDIT_REC_1 -> {
				addNewRec = true;
				recNum = 1;
				List<BlockPos> blocks = rudderScreen.getMenu().getBlockPosStruct();
				rec1x.setValue("" + blocks.get(0).getX());
				rec1y.setValue("" + blocks.get(0).getY());
				rec1z.setValue("" + blocks.get(0).getZ());
				rec2x.setValue("" + blocks.get(1).getX());
				rec2y.setValue("" + blocks.get(1).getY());
				rec2z.setValue("" + blocks.get(1).getZ());
			}
			case EDIT_REC_2 -> {
				addNewRec = true;
				recNum = 2;
				List<BlockPos> blocks = rudderScreen.getMenu().getBlockPosStruct();
				rec1x.setValue("" + blocks.get(2).getX());
				rec1y.setValue("" + blocks.get(2).getY());
				rec1z.setValue("" + blocks.get(2).getZ());
				rec2x.setValue("" + blocks.get(3).getX());
				rec2y.setValue("" + blocks.get(3).getY());
				rec2z.setValue("" + blocks.get(3).getZ());
			}
			case EDIT_REC_3 -> {
				addNewRec = true;
				recNum = 3;
				List<BlockPos> blocks = rudderScreen.getMenu().getBlockPosStruct();
				rec1x.setValue("" + blocks.get(4).getX());
				rec1y.setValue("" + blocks.get(4).getY());
				rec1z.setValue("" + blocks.get(4).getZ());
				rec2x.setValue("" + blocks.get(5).getX());
				rec2y.setValue("" + blocks.get(5).getY());
				rec2z.setValue("" + blocks.get(5).getZ());
			}
			case OK_EDITION -> {
				addNewRec = false;
				try {
					ModMessages.sendToServer(new AddRectanglePacket(rudderScreen.getMenu().getBlockEntity().getBlockPos(), recNum,
							new BlockPos(Integer.parseInt(rec1x.getValue()), Integer.parseInt(rec1y.getValue()), Integer.parseInt(rec1z.getValue())),
							new BlockPos(Integer.parseInt(rec2x.getValue()), Integer.parseInt(rec2y.getValue()), Integer.parseInt(rec2z.getValue()))));
					rudderScreen.getPlayer().sendSystemMessage(Component.translatable("message.rectangleSet",
							"[" + rec1x.getValue() + ", " + rec1y.getValue() + ", " + rec1z.getValue() + "]",
							"[" + rec2x.getValue() + ", " + rec2y.getValue() + ", " + rec2z.getValue() + "]"));
				} catch (Exception ex) {
					rudderScreen.getPlayer().sendSystemMessage(Component.translatable("message.wrongRectangle"));
				}
				recNum = 0;
				rec1x.setValue("");
				rec1y.setValue("");
				rec1z.setValue("");
				rec2x.setValue("");
				rec2y.setValue("");
				rec2z.setValue("");
			}
			case EXIT_EDITION -> {
				addNewRec = false;
				recNum = 0;
				rec1x.setValue("");
				rec1y.setValue("");
				rec1z.setValue("");
				rec2x.setValue("");
				rec2y.setValue("");
				rec2z.setValue("");
			}
		}
	}
}
