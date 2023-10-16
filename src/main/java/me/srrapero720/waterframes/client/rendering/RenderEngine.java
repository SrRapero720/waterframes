package me.srrapero720.waterframes.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.srrapero720.waterframes.client.display.DisplayControl;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.data.DisplayData;
import me.srrapero720.waterframes.util.FrameTools;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import me.srrapero720.watermedia.api.math.MathAPI;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Vec3i;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL;

public class RenderEngine {
    static final ImageRenderer LOADING_GIF = ImageAPI.loadingGif("watermedia");

    public static void vertexFrontSide(AlignedBox box, BoxFace face, PoseStack pose, VertexFormat vertexFormat, DisplayData data) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, vertexFormat);

        Vec3i normal = face.facing.normal;
        for (BoxCorner corner : face.corners) {
            builder.vertex(pose.last().pose(), box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .uv(corner.isFacing(face.getTexU()) != data.flipX ? 1 : 0, corner.isFacing(face.getTexV()) != data.flipY ? 1 : 0).color(-1)
                    .normal(pose.last().normal(), normal.getX(), normal.getY(), normal.getZ()).endVertex();
        }
        tesselator.end();
    }

    public static void vertexBackSide(AlignedBox box, BoxFace face, PoseStack pose, VertexFormat vertexFormat, DisplayData data) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, vertexFormat);

        Vec3i normal = face.facing.normal;
        for (int i = face.corners.length - 1; i >= 0; i--) {
            BoxCorner corner = face.corners[i];
            builder.vertex(pose.last().pose(), box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .uv(corner.isFacing(face.getTexU()) != data.flipX ? 1 : 0, corner.isFacing(face.getTexV()) != data.flipY ? 1 : 0).color(-1)
                    .normal(pose.last().normal(), normal.getX(), normal.getY(), normal.getZ()).endVertex();
        }
        tesselator.end();
    }

    public static void vertexBackSideXFlipped(AlignedBox box, BoxFace face, PoseStack pose, VertexFormat vertexFormat, DisplayData data) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, vertexFormat);

        Vec3i normal = face.facing.normal;
        for (int i = face.corners.length - 1; i >= 0; i--) {
            BoxCorner corner = face.corners[i];
            builder.vertex(pose.last().pose(), box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .uv(corner.isFacing(face.getTexU()) != data.flipX ? 0 : 1, corner.isFacing(face.getTexV()) != data.flipY ? 1 : 0).color(-1)
                    .normal(pose.last().normal(), normal.getX(), normal.getY(), normal.getZ()).endVertex();
        }
        tesselator.end();
    }

    static void renderVertexGif(DisplayTile<?> block, PoseStack pose, BoxFace boxFace, boolean inverted) {
        pose.pushPose();
        Vec3i normal = boxFace.facing.normal;
        AlignedBox alignedBox = block.getRenderGifBox();
        alignedBox.grow(boxFace.facing.axis, 0.02f);

        int texture = LOADING_GIF.texture(DisplayControl.getTickTime(), MathAPI.tickToMs(FrameTools.deltaFrames()), true);
        RenderSystem.bindTexture(texture);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR_NORMAL);
        if (inverted) {
            for (int i = boxFace.corners.length - 1; i >= 0; i--) {
                BoxCorner corner = boxFace.corners[i];
                builder.vertex(pose.last().pose(), alignedBox.get(corner.x), alignedBox.get(corner.y), alignedBox.get(corner.z))
                        .uv(corner.isFacing(boxFace.getTexU()) != block.data.flipX ? 1 : 0, corner.isFacing(boxFace.getTexV()) != block.data.flipY ? 1 : 0).color(-1)
                        .normal(pose.last().normal(), normal.getX(), normal.getY(), normal.getZ()).endVertex();
            }
        } else {
            for (BoxCorner corner : boxFace.corners) {
                builder.vertex(pose.last().pose(), alignedBox.get(corner.x), alignedBox.get(corner.y), alignedBox.get(corner.z))
                        .uv(corner.isFacing(boxFace.getTexU()) != block.data.flipX ? 1 : 0, corner.isFacing(boxFace.getTexV()) != block.data.flipY ? 1 : 0).color(-1)
                        .normal(pose.last().normal(), normal.getX(), normal.getY(), normal.getZ()).endVertex();
            }
        }
        tesselator.end();

        pose.popPose();
    }
}