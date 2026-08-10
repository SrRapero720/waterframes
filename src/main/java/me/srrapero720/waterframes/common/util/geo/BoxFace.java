package me.srrapero720.waterframes.common.util.geo;

/**
 * A face of an {@link AlignedBox}: its 4 corners in counter-clockwise winding plus the
 * faces that mark the positive U and V texture directions.
 */
public enum BoxFace {
    EAST(Facing.EAST, Facing.NORTH, Facing.DOWN, BoxCorner.EUS, BoxCorner.EDS, BoxCorner.EDN, BoxCorner.EUN),
    WEST(Facing.WEST, Facing.SOUTH, Facing.DOWN, BoxCorner.WUN, BoxCorner.WDN, BoxCorner.WDS, BoxCorner.WUS),
    UP(Facing.UP, Facing.EAST, Facing.SOUTH, BoxCorner.WUN, BoxCorner.WUS, BoxCorner.EUS, BoxCorner.EUN),
    DOWN(Facing.DOWN, Facing.EAST, Facing.NORTH, BoxCorner.WDS, BoxCorner.WDN, BoxCorner.EDN, BoxCorner.EDS),
    SOUTH(Facing.SOUTH, Facing.EAST, Facing.DOWN, BoxCorner.WUS, BoxCorner.WDS, BoxCorner.EDS, BoxCorner.EUS),
    NORTH(Facing.NORTH, Facing.WEST, Facing.DOWN, BoxCorner.EUN, BoxCorner.EDN, BoxCorner.WDN, BoxCorner.WUN);

    // INDEXED BY Facing.ordinal(), NOT BY THIS ENUM'S OWN ORDER
    private static final BoxFace[] BY_FACING = new BoxFace[Facing.VALUES.length];

    static {
        for (BoxFace face: values()) BY_FACING[face.facing.ordinal()] = face;
    }

    public final Facing facing;
    public final Facing texU;
    public final Facing texV;
    public final BoxCorner[] corners;

    BoxFace(Facing facing, Facing texU, Facing texV, BoxCorner... corners) {
        this.facing = facing;
        this.texU = texU;
        this.texV = texV;
        this.corners = corners;
    }

    public static BoxFace of(Facing facing) {
        return BY_FACING[facing.ordinal()];
    }

    public Axis one() {
        return facing.one();
    }

    public Axis two() {
        return facing.two();
    }
}
