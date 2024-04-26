package me.srrapero720.waterframes.client.rendering;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;

public class ProjectorRenderer extends DisplayRenderer {
    @Override
    public boolean invBoxFace(DisplayTile tile) {
        return false;
    }

    @Override
    public float grwSize() {
        return 0.999f;
    }
}
