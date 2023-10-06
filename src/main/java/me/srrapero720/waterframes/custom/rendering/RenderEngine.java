package me.srrapero720.waterframes.custom.rendering;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import me.srrapero720.waterframes.api.data.BasicData;
import me.srrapero720.watermedia.api.math.MathAPI;
import net.minecraft.core.Vec3i;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;

public class RenderEngine {
    public static void vertexFrontSide(AlignedBox box, BoxFace face, PoseStack pose, VertexFormat vertexFormat, BasicData data) {
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

    public static void vertexBackSide(AlignedBox box, BoxFace face, PoseStack pose, VertexFormat vertexFormat, BasicData data) {
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

    public static void vextexFillFrontSide(AlignedBox box, BoxFace face, PoseStack pose, VertexFormat vertexFormat, int color) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, vertexFormat);

        for (BoxCorner corner : face.corners)
            builder.vertex(pose.last().pose(), box.get(corner.x), box.get(corner.y), box.get(corner.z)).color(color);
        tesselator.end();
    }

    public static void vextexFillBackSide(AlignedBox box, BoxFace face, PoseStack pose, VertexFormat vertexFormat, int color) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, vertexFormat);

        for (int i = face.corners.length - 1; i >= 0; i--) {
            BoxCorner corner = face.corners[i];
            builder.vertex(pose.last().pose(), box.get(corner.x), box.get(corner.y), box.get(corner.z)).color(color);
        }
        tesselator.end();
    }
}