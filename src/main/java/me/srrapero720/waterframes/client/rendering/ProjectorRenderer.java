package me.srrapero720.waterframes.client.rendering;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.Direction;
import team.creative.creativecore.common.util.math.base.Facing;

public class ProjectorRenderer extends DisplayRenderer {
    @Override
    public boolean invBoxFace(DisplayTile tile) {
        return false;
    }
    @Override
    public Facing facing(DisplayTile tile, Direction direction) {
        return Facing.get(direction);
    }

    @Override
    public float grwSize() {
        return 0.999f;
    }
}
