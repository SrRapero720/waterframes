package me.srrapero720.waterframes.common.util.geo;

/**
 * The 8 corners of an {@link AlignedBox}, each one described by the face it touches on
 * every axis. Naming is East/West + Up/Down + North/South.
 */
public enum BoxCorner {
    EUN(Facing.EAST, Facing.UP, Facing.NORTH),
    EUS(Facing.EAST, Facing.UP, Facing.SOUTH),
    EDN(Facing.EAST, Facing.DOWN, Facing.NORTH),
    EDS(Facing.EAST, Facing.DOWN, Facing.SOUTH),
    WUN(Facing.WEST, Facing.UP, Facing.NORTH),
    WUS(Facing.WEST, Facing.UP, Facing.SOUTH),
    WDN(Facing.WEST, Facing.DOWN, Facing.NORTH),
    WDS(Facing.WEST, Facing.DOWN, Facing.SOUTH);

    public final Facing x;
    public final Facing y;
    public final Facing z;

    BoxCorner(Facing x, Facing y, Facing z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** True when this corner sits on the given face, used to pick the 0 or 1 UV coordinate. */
    public boolean faces(Facing facing) {
        return x == facing || y == facing || z == facing;
    }
}
