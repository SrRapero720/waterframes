package me.srrapero720.waterframes.common.block.data.types;

public enum PositionVertical {
    TOP, BOTTOM, CENTER;
    public static final PositionVertical[] VALUES = values();

    public PositionVertical down() {
        return switch (this) {
            case TOP -> CENTER;
            case BOTTOM -> TOP;
            case CENTER -> BOTTOM;
        };
    }

    public PositionVertical up() {
        return switch (this) {
            case TOP -> BOTTOM;
            case BOTTOM -> CENTER;
            case CENTER -> TOP;
        };
    }
}
