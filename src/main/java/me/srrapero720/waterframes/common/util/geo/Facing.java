package me.srrapero720.waterframes.common.util.geo;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

/**
 * Sugar over {@link Direction}: keeps the vanilla identity but exposes the axis split
 * ({@link #one()}/{@link #two()}), the sign and the rotation axis used by the renderer.
 * Declared in vanilla order so {@link #of(Direction)} is a plain array lookup.
 */
public enum Facing {
    DOWN(Direction.DOWN, Axis.Y, false),
    UP(Direction.UP, Axis.Y, true),
    NORTH(Direction.NORTH, Axis.Z, false),
    SOUTH(Direction.SOUTH, Axis.Z, true),
    WEST(Direction.WEST, Axis.X, false),
    EAST(Direction.EAST, Axis.X, true);

    public static final Facing[] VALUES = values();

    public final Direction vanilla;
    public final Axis axis;
    public final boolean positive;
    public final Vec3i normal;

    Facing(Direction vanilla, Axis axis, boolean positive) {
        this.vanilla = vanilla;
        this.axis = axis;
        this.positive = positive;
        this.normal = vanilla.getNormal();
    }

    public static Facing of(Direction direction) {
        return VALUES[direction.ordinal()];
    }

    public static Facing of(Axis axis, boolean positive) {
        return VALUES[Direction.fromAxisAndDirection(axis.vanilla,
                positive ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE).ordinal()];
    }

    public Facing opposite() {
        return of(vanilla.getOpposite());
    }

    public Axis one() {
        return axis.one();
    }

    public Axis two() {
        return axis.two();
    }

    /** Rotation axis matching this direction's normal, for {@code PoseStack.mulPose}. */
    public com.mojang.math.Axis rotation() {
        return switch (this) {
            case DOWN -> com.mojang.math.Axis.YN;
            case UP -> com.mojang.math.Axis.YP;
            case NORTH -> com.mojang.math.Axis.ZN;
            case SOUTH -> com.mojang.math.Axis.ZP;
            case WEST -> com.mojang.math.Axis.XN;
            case EAST -> com.mojang.math.Axis.XP;
        };
    }
}
