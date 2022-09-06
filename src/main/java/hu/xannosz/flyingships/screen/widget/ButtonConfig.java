package hu.xannosz.flyingships.screen.widget;

import lombok.Builder;
import lombok.Getter;
import net.minecraft.core.BlockPos;

@Getter
@Builder
public class ButtonConfig {
	private ButtonId buttonId;
	private int buttonX;
	private int buttonY;
	private int hoveredX;
	private int hoveredY;
	private int graphicalX;
	private int graphicalY;
	private int graphicalW;
	private int graphicalH;
	private int hitBoxX;
	private int hitBoxY;
	private int hitBoxW;
	private int hitBoxH;
	private BlockPos position;
	private boolean isDefaultButtonDrawNeeded;
	private boolean debugMode;
}
