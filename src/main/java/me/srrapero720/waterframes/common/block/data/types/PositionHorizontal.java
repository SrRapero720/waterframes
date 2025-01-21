package me.srrapero720.waterframes.common.block.data.types;

public enum PositionHorizontal {
    LEFT, RIGHT, CENTER;
    public static final PositionHorizontal[] VALUES = values();

    public PositionHorizontal right() {
        return switch (this) {
            case LEFT -> CENTER;
            case CENTER -> RIGHT;
            case RIGHT -> LEFT;
        };
    }

    public PositionHorizontal left() {
        return switch (this) {
            case RIGHT -> CENTER;
            case CENTER -> LEFT;
            case LEFT -> RIGHT;
        };
    }
}
