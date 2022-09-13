package hu.xannosz.flyingships.warp;

import hu.xannosz.flyingships.screen.widget.ButtonId;
import net.minecraft.core.Direction;

public enum WarpDirection {

	UP, DOWN, NORTH, SOUTH, EAST, WEST, LAND, COORDINATE;

	public static WarpDirection fromBlockDirection(ButtonId buttonId, Direction blockDirection) {
		switch (buttonId) {
			case UP -> {
				return UP;
			}
			case DOWN -> {
				return DOWN;
			}
			case RIGHT -> {
				return fromDirection(blockDirection.getCounterClockWise());
			}
			case LEFT -> {
				return fromDirection(blockDirection.getClockWise());
			}
			case FORWARD -> {
				return fromDirection(blockDirection.getOpposite());
			}
			case BACKWARD -> {
				return fromDirection(blockDirection);
			}
			case LAND -> {
				return LAND;
			}
			case BEACON -> {
				return COORDINATE;
			}
		}
		return null;
	}

	public static ButtonId toButtonId(WarpDirection warpDirection, Direction blockDirection) {
		if (warpDirection == null) {
			return null;
		}
		switch (warpDirection) {
			case UP -> {
				return ButtonId.UP;
			}
			case DOWN -> {
				return ButtonId.DOWN;
			}
			case NORTH -> {
				switch (blockDirection) {
					case NORTH -> {
						return ButtonId.BACKWARD;
					}
					case SOUTH -> {
						return ButtonId.FORWARD;
					}
					case WEST -> {
						return ButtonId.LEFT;
					}
					case EAST -> {
						return ButtonId.RIGHT;
					}
				}
			}
			case SOUTH -> {
				switch (blockDirection) {
					case NORTH -> {
						return ButtonId.FORWARD;
					}
					case SOUTH -> {
						return ButtonId.BACKWARD;
					}
					case WEST -> {
						return ButtonId.RIGHT;
					}
					case EAST -> {
						return ButtonId.LEFT;
					}
				}
			}
			case EAST -> {
				switch (blockDirection) {
					case NORTH -> {
						return ButtonId.LEFT;
					}
					case SOUTH -> {
						return ButtonId.RIGHT;
					}
					case WEST -> {
						return ButtonId.FORWARD;
					}
					case EAST -> {
						return ButtonId.BACKWARD;
					}
				}
			}
			case WEST -> {
				switch (blockDirection) {
					case NORTH -> {
						return ButtonId.RIGHT;
					}
					case SOUTH -> {
						return ButtonId.LEFT;
					}
					case WEST -> {
						return ButtonId.BACKWARD;
					}
					case EAST -> {
						return ButtonId.FORWARD;
					}
				}
			}
			case LAND -> {
				return ButtonId.LAND;
			}
			case COORDINATE -> {
				return ButtonId.BEACON;
			}
		}
		return null;
	}

	private static WarpDirection fromDirection(Direction blockDirection) {
		switch (blockDirection) {
			case NORTH -> {
				return NORTH;
			}
			case SOUTH -> {
				return SOUTH;
			}
			case WEST -> {
				return WEST;
			}
			case EAST -> {
				return EAST;
			}
		}
		return null;
	}
}
