package me.srrapero720.waterframes.common.block.data;

import me.srrapero720.waterframes.common.block.BigTvBlock;
import me.srrapero720.waterframes.common.block.TvBlock;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.Direction;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

public record DisplayCaps(boolean renderBehind, boolean projects, boolean resizes, float growSize, DisplayGrowMax growMax, Display2BoolFunction invertedFace, BoxFunction box) {

    public static final Display2BoolFunction FALSE = tile -> false;
    public static final Display2BoolFunction TRUE = tile -> true;

    public static final DisplayGrowMax DEFAULT_GROW = (tile, facing, inv) -> facing.positive != tile.caps.invertedFace(tile);

    public DisplayCaps(boolean renderBothSides, boolean projects, boolean resizes, float growSize, BoxFunction box) {
        this(renderBothSides, projects, resizes, growSize, DEFAULT_GROW, FALSE, box);
    }

    public static final DisplayCaps
            FRAME       = new DisplayCaps(true, false, true, 0.001F, (t, d, a, r) -> DisplayTile.getBasicBox(t)),
            PROJECTOR   = new DisplayCaps(false, true, true, 0.999F, (t, d, a, r) -> DisplayTile.getBasicBox(t)),
            TV          = new DisplayCaps(false, false, false, 0.001f, (tile, facing, invertedFace) -> (facing.positive == invertedFace) == (tile.getAttachedFace().getOpposite() == tile.getDirection()), tile -> tile.getAttachedFace().getOpposite() == tile.getDirection(), (t, d, a, r) -> TvBlock.box(d, a, r)),
            BIG_TV      = new DisplayCaps(false, false, false, 0.001F, (t, d, a, r) -> BigTvBlock.box(d, a, r));

    public boolean invertedFace(DisplayTile tile) {
        return invertedFace.get(tile);
    }

    public boolean growMax(DisplayTile tile, Facing facing, boolean invertedFace) {
        return growMax.get(tile, facing, invertedFace);
    }

    public AlignedBox getBox(DisplayTile tile, Direction direction, Direction attachedFace, boolean renderMode) {
        return box.get(tile, direction, attachedFace, renderMode);
    }

    public interface DisplayGrowMax {
        boolean get(DisplayTile tile, Facing facing, boolean invertedFace);
    }

    @FunctionalInterface
    public interface Display2BoolFunction {
        boolean get(DisplayTile tile);
    }

    @FunctionalInterface
    public interface BoxFunction {
        AlignedBox get(DisplayTile tile, Direction direction, Direction attachedFace, boolean renderMode);
    }
}
