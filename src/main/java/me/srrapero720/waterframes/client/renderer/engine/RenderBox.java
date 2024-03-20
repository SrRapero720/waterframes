package me.srrapero720.waterframes.client.renderer.engine;

import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

public class RenderBox {
    public static AlignedBox getBasic(DisplayTile block, Facing facing, float thickness) {
        AlignedBox box = DisplayBlock.getBlockBox(facing, thickness); // LESS CODE

        Axis one = facing.one();
        Axis two = facing.two();

        if (facing.axis != Axis.Z) {
            one = facing.two();
            two = facing.one();
        }

        box.setMin(one, block.data.min.x);
        box.setMax(one, block.data.max.x);

        box.setMin(two, block.data.min.y);
        box.setMax(two, block.data.max.y);

        return box;
    }

    public static AlignedBox squaredOf(DisplayTile block, Facing blockFacing, AlignedBox renderBox) {
        AlignedBox box = new AlignedBox(renderBox);
        Axis axisX = blockFacing.one();
        Axis axisY = blockFacing.two();

        if (blockFacing.axis != Axis.Z) {
            axisX = blockFacing.two();
            axisY = blockFacing.one();
        }

        float width = renderBox.getSize(axisX);
        float height = renderBox.getSize(axisY);

        if (width > height) {
            switch (block.data.getPosX()) {
                case LEFT -> {
                    box.setMin(axisX, 0f);
                    box.setMax(axisX, height);
                }
                case RIGHT -> {
                    box.setMin(axisX, 1 - height);
                    box.setMax(axisX, 1);
                }
                default -> {
                    float middle = height / 2;
                    box.setMin(axisX, 0.5f - middle);
                    box.setMax(axisX, 0.5f + middle);
                }
            }
        }

        if (height > width) {
            switch (block.data.getPosY()) {
                case TOP -> {
                    box.setMin(axisY, 0f);
                    box.setMax(axisY, width);
                }
                case BOTTOM -> {
                    box.setMin(axisY, 1 - width);
                    box.setMax(axisY, 1);
                }
                default -> {
                    float middle = width / 2;
                    box.setMin(axisY, 0.5f - middle);
                    box.setMax(axisY, 0.5f + middle);
                }
            }
        }
        return box;
    }
}