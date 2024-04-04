package me.srrapero720.waterframes.client.rendering.core;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

public class RenderBox {
    public static AlignedBox simple(DisplayTile tile, Facing facing, float spacing) {
        var box = new AlignedBox();

        if (facing.positive) box.setMax(facing.axis, (tile.data.projectionDistance + spacing));
        else box.setMin(facing.axis, 1 - (tile.data.projectionDistance + spacing));

        Axis one = facing.one();
        Axis two = facing.two();

        if (facing.axis != Axis.Z) {
            one = facing.two();
            two = facing.one();
        }

        box.setMin(one, tile.data.min.x);
        box.setMax(one, tile.data.max.x);

        box.setMin(two, tile.data.min.y);
        box.setMax(two, tile.data.max.y);

        return box;
    }

    public static AlignedBox squared(DisplayTile tile, Facing facing, float spacing) {
        var box = new AlignedBox();

        if (facing.positive) box.setMax(facing.axis, (tile.data.projectionDistance + spacing));
        else box.setMin(facing.axis, 1 - (tile.data.projectionDistance + spacing));

        Axis one = facing.one();
        Axis two = facing.two();

        if (facing.axis != Axis.Z) {
            one = facing.two();
            two = facing.one();
        }

        box.setMin(one, tile.data.min.x);
        box.setMax(one, tile.data.max.x);

        box.setMin(two, tile.data.min.y);
        box.setMax(two, tile.data.max.y);

        float width = tile.data.getWidth();
        float height = tile.data.getHeight();

        // FIXME: corner pictures makes squared look closer to the block
        if (width > height) {
            switch (tile.data.getPosX()) {
                case LEFT -> {
                    box.setMin(one, 0f);
                    box.setMax(one, height);
                }
                case RIGHT -> {
                    box.setMin(one, 1 - height);
                    box.setMax(one, 1);
                }
                default -> {
                    float middle = height / 2;
                    box.setMin(one, 0.5f - middle);
                    box.setMax(one, 0.5f + middle);
                }
            }
        } else {
            switch (tile.data.getPosY()) {
                case TOP -> {
                    box.setMin(two, 0f);
                    box.setMax(two, width);
                }
                case BOTTOM -> {
                    box.setMin(two, 1 - width);
                    box.setMax(two, 1);
                }
                default -> {
                    float middle = width / 2;
                    box.setMin(two, 0.5f - middle);
                    box.setMax(two, 0.5f + middle);
                }
            }
        }

        return box;
    }
}