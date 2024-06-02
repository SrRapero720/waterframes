package me.srrapero720.waterframes.common.block.data;

import me.srrapero720.waterframes.common.block.BigTvBlock;
import me.srrapero720.waterframes.common.block.TvBlock;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.Direction;
import team.creative.creativecore.common.util.math.box.AlignedBox;

public record DisplayCaps(boolean renderBehind, boolean projects, boolean resizes, float growSize, BooleanCap invertedFace, BoxCap box) {

    public static final BooleanCap FALSE = tile -> false;

    public DisplayCaps(boolean renderBothSides, boolean projects, boolean resizes, float growSize, BoxCap box) {
        this(renderBothSides, projects, resizes, growSize, FALSE, box);
    }

    public static final DisplayCaps
            FRAME       = new DisplayCaps(true, false, true, 0.001F, (t, d, a, r) -> DisplayTile.getBasicBox(t)),
            PROJECTOR   = new DisplayCaps(false, true, true, 0.999F, (t, d, a, r) -> DisplayTile.getBasicBox(t)),
            TV          = new DisplayCaps(false, false, false, 0.001F, tile -> tile.getAttachedFace() != tile.getDirection(), (t, d, a, r) -> TvBlock.box(d, a, r)),
            BIG_TV      = new DisplayCaps(false, false, false, 0.001F, tile -> true, (t, d, a, r) -> BigTvBlock.box(d, r));


    public boolean invertedFace(DisplayTile tile) {
        return invertedFace.get(tile);
    }

    public AlignedBox getBox(DisplayTile tile, Direction direction, Direction attachedFace, boolean renderMode) {
        return box.get(tile, direction, attachedFace, renderMode);
    }

    @FunctionalInterface
    public interface BooleanCap {
        boolean get(DisplayTile tile);
    }

    @FunctionalInterface
    public interface BoxCap {
        AlignedBox get(DisplayTile tile, Direction direction, Direction attachedFace, boolean renderMode);
    }
}
