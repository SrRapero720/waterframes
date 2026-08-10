package me.srrapero720.waterframes.common.util.geo;

import net.minecraft.core.Direction;

/**
 * Sugar over {@link Direction.Axis} adding the pair of perpendicular axes, which vanilla
 * does not expose and every box builder in this mod needs.
 */
public enum Axis {
    X(Direction.Axis.X),
    Y(Direction.Axis.Y),
    Z(Direction.Axis.Z);

    public static final Axis[] VALUES = values();

    // PERPENDICULAR PAIRS, RIGHT HANDED: X -> (Y, Z), Y -> (Z, X), Z -> (X, Y)
    private static final Axis[] ONE = { Y, Z, X };
    private static final Axis[] TWO = { Z, X, Y };

    public final Direction.Axis vanilla;

    Axis(Direction.Axis vanilla) {
        this.vanilla = vanilla;
    }

    public static Axis of(Direction.Axis axis) {
        return VALUES[axis.ordinal()];
    }

    public Axis one() {
        return ONE[ordinal()];
    }

    public Axis two() {
        return TWO[ordinal()];
    }

    public float of(float x, float y, float z) {
        return switch (this) {
            case X -> x;
            case Y -> y;
            case Z -> z;
        };
    }
}
