package me.srrapero720.waterframes.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector3d;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.client.display.DisplayControl;
import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.data.DisplayData;
import me.srrapero720.waterframes.mixin.creativecore.IBoxFaceAccessor;
import me.srrapero720.waterframes.util.FrameTools;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import me.srrapero720.watermedia.api.math.MathAPI;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.math.vec.Vec3f;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL;

public class RenderEngine {
    static final ImageRenderer LOADING_GIF = ImageAPI.loadingGif(WaterFrames.ID);

    public static void vertexFrontSide(AlignedBox box, BoxFace face, PoseStack pose, BufferBuilder builder, DisplayData data) {
        Vec3i normal = face.facing.normal;
        for (BoxCorner corner : face.corners) {
            builder.vertex(pose.last().pose(), box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .uv(corner.isFacing(face.getTexU()) != data.flipX ? 1 : 0, corner.isFacing(face.getTexV()) != data.flipY ? 1 : 0).color(-1)
                    .normal(pose.last().normal(), normal.getX(), normal.getY(), normal.getZ()).endVertex();
        }
    }

    public static void vertexBackSide(AlignedBox box, BoxFace face, PoseStack pose, BufferBuilder builder, DisplayData data) {
        Vec3i normal = face.facing.normal;
        for (int i = face.corners.length - 1; i >= 0; i--) {
            BoxCorner corner = face.corners[i];
            builder.vertex(pose.last().pose(), box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .uv(corner.isFacing(face.getTexU()) != data.flipX ? 1 : 0, corner.isFacing(face.getTexV()) != data.flipY ? 1 : 0).color(-1)
                    .normal(pose.last().normal(), normal.getX(), normal.getY(), normal.getZ()).endVertex();
        }
    }

    public static void vertexBackSideXFlipped(AlignedBox box, BoxFace face, PoseStack pose, BufferBuilder builder, DisplayData data) {
        Vec3i normal = face.facing.normal;
        for (int i = face.corners.length - 1; i >= 0; i--) {
            BoxCorner corner = face.corners[i];
            builder.vertex(pose.last().pose(), box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .uv(corner.isFacing(face.getTexU()) != data.flipX ? 0 : 1, corner.isFacing(face.getTexV()) != data.flipY ? 1 : 0).color(-1)
                    .normal(pose.last().normal(), normal.getX(), normal.getY(), normal.getZ()).endVertex();
        }
    }

    static void renderVertexGif(PoseStack pose, BufferBuilder builder, DisplayTile<?> block, Facing blockFacing, IBoxFaceAccessor boxFace, boolean inverted, float thickness) {
        Vec3i normal = boxFace.getFacing().normal;
        int texture = LOADING_GIF.texture(DisplayControl.getTickTime(), MathAPI.tickToMs(FrameTools.deltaFrames()), true);

        RenderSystem.bindTexture(texture);
        RenderSystem.setShaderTexture(0, texture);

        AlignedBox box = getSquaredBox(block, blockFacing, thickness);
        Vec3f cornerVec = new Vec3f();
        Vec3d size = new Vec3d(box.getSize());
        if (inverted) {
            BoxCorner[] corners = boxFace.getCorners();
            for (int i = corners.length - 1; i >= 0; i--) {
                BoxCorner corner = corners[i];
                cornerVec.set(box.get(corner.x), box.get(corner.y), box.get(corner.z));
                cornerVec.set(boxFace.getOne(), cornerVec.get(boxFace.getOne()) + (float) size.get(boxFace.getOne()) * 0.2F * (corner.isFacingPositive(boxFace.getOne()) ? -1 : 1));
                cornerVec.set(boxFace.getTwo(), cornerVec.get(boxFace.getTwo()) + (float) size.get(boxFace.getTwo()) * 0.2F * (corner.isFacingPositive(boxFace.getTwo()) ? -1 : 1));
                builder.vertex(pose.last().pose(), cornerVec.x, cornerVec.y, cornerVec.z)
                        .uv(corner.isFacing(boxFace.texU()) != block.data.flipX ? 1 : 0, corner.isFacing(boxFace.texV()) != block.data.flipY ? 1 : 0).color(-1)
                        .normal(pose.last().normal(), normal.getX(), normal.getY(), normal.getZ()).endVertex();
            }
        } else {
            for (BoxCorner corner: boxFace.getCorners()) {
                cornerVec.set(box.get(corner.x), box.get(corner.y), box.get(corner.z));
                cornerVec.set(boxFace.getOne(), cornerVec.get(boxFace.getOne()) + (float) size.get(boxFace.getOne()) * 0.2F * (corner.isFacingPositive(boxFace.getOne()) ? -1 : 1));
                cornerVec.set(boxFace.getTwo(), cornerVec.get(boxFace.getTwo()) + (float) size.get(boxFace.getTwo()) * 0.2F * (corner.isFacingPositive(boxFace.getTwo()) ? -1 : 1));
                builder.vertex(pose.last().pose(), cornerVec.x, cornerVec.y, cornerVec.z)
                        .uv(corner.isFacing(boxFace.texU()) != block.data.flipX ? 1 : 0, corner.isFacing(boxFace.texV()) != block.data.flipY ? 1 : 0).color(-1)
                        .normal(pose.last().normal(), normal.getX(), normal.getY(), normal.getZ()).endVertex();
            }
        }
    }

    public static AlignedBox getBasicRenderBox(DisplayTile<?> block, Direction direction, float thickness) {
        Facing facing = Facing.get(direction);
        AlignedBox box = DisplayBlock.getBlockBox(direction, thickness); // LESS CODE

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

    private static AlignedBox getSquaredBox(DisplayTile<?> block, Facing blockFacing, float thickness) {
        AlignedBox box = new AlignedBox();
        Axis axisX = blockFacing.one();
        Axis axisY = blockFacing.two();

        if (blockFacing.axis != Axis.Z) {
            axisX = blockFacing.two();
            axisY = blockFacing.one();
        }

        float size;
        if (block.data.getSizeX() > (size = block.data.getSizeY())) {
            float half = size / 2;
            box.setMin(axisX, 0.5F - half);
            box.setMax(axisX, 0.5F + half);

            box.setMin(axisY, 0.5F - half);
            box.setMax(axisY, 0.5F + half);
        }

        if (block.data.getSizeY() > (size = block.data.getSizeX())) {
            float half = size / 2;
            box.setMin(axisX, 0.5F - half);
            box.setMax(axisX, 0.5F + half);

            box.setMin(axisY, 0.5F - half);
            box.setMax(axisY, 0.5F + half);
        }
        box.grow(blockFacing.axis, thickness);
        return box;
    }
}