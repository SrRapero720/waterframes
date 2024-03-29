package me.srrapero720.waterframes.client.renderer.engine;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Vec3i;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.math.vec.Vec3f;

public class RenderVertex {
    public static void front(AlignedBox box, BoxFace face, PoseStack pose, BufferBuilder builder, boolean flipX, boolean flipY) {
        Vec3i normal = face.facing.normal;
        for (int i = 0; i < face.corners.length; i++) {
            BoxCorner corner = face.corners[i];
            builder.vertex(pose.last().pose(), box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .uv(corner.isFacing(face.getTexU()) != flipX ? 1 : 0, corner.isFacing(face.getTexV()) != flipY ? 1 : 0).color(-1)
                    .normal(pose.last().normal(), normal.getX(), normal.getY(), normal.getZ()).endVertex();
        }
    }

    public static void back(AlignedBox box, BoxFace face, PoseStack pose, BufferBuilder builder, boolean flipX, boolean flipY) {
        Vec3i normal = face.facing.normal;
        for (int i = face.corners.length - 1; i >= 0; i--) {
            BoxCorner corner = face.corners[i];
            builder.vertex(pose.last().pose(), box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .uv(corner.isFacing(face.getTexU()) != flipX ? 1 : 0, corner.isFacing(face.getTexV()) != flipY ? 1 : 0).color(-1)
                    .normal(pose.last().normal(), normal.getX(), normal.getY(), normal.getZ()).endVertex();
        }
    }

    public static void frontHalf(AlignedBox squaredBox, BoxFace face, PoseStack pose, BufferBuilder builder, boolean flipX, boolean flipY) {
        Vec3i normal = face.facing.normal;

        Vec3f cornerVec = new Vec3f();
        Vec3d size = new Vec3d(squaredBox.getSize());
        for (int i = 0; i < face.corners.length; i++) {
            BoxCorner corner = face.corners[i];
            cornerVec.set(squaredBox.get(corner.x), squaredBox.get(corner.y), squaredBox.get(corner.z));
            cornerVec.set(face.getOne(), cornerVec.get(face.getOne()) + (float) size.get(face.getOne()) * 0.25f * (corner.isFacingPositive(face.getOne()) ? -1 : 1));
            cornerVec.set(face.getTwo(), cornerVec.get(face.getTwo()) + (float) size.get(face.getTwo()) * 0.25f * (corner.isFacingPositive(face.getTwo()) ? -1 : 1));

            builder.vertex(pose.last().pose(), cornerVec.x, cornerVec.y, cornerVec.z)
                    .uv(corner.isFacing(face.getTexU()) != flipX ? 1 : 0, corner.isFacing(face.getTexV()) != flipY ? 1 : 0).color(-1)
                    .normal(pose.last().normal(), normal.getX(), normal.getY(), normal.getZ()).endVertex();
        }
    }

    public static void backHalf(AlignedBox squaredBox, BoxFace face, PoseStack pose, BufferBuilder builder, boolean flipX, boolean flipY) {
        Vec3i normal = face.facing.normal;

        Vec3f cornerVec = new Vec3f();
        Vec3d size = new Vec3d(squaredBox.getSize());
        for (int i = face.corners.length - 1; i >= 0; i--) {
            BoxCorner corner = face.corners[i];
            cornerVec.set(squaredBox.get(corner.x), squaredBox.get(corner.y), squaredBox.get(corner.z));
            cornerVec.set(face.getOne(), cornerVec.get(face.getOne()) + (float) size.get(face.getOne()) * 0.25f * (corner.isFacingPositive(face.getOne()) ? -1 : 1));
            cornerVec.set(face.getTwo(), cornerVec.get(face.getTwo()) + (float) size.get(face.getTwo()) * 0.25f * (corner.isFacingPositive(face.getTwo()) ? -1 : 1));

            builder.vertex(pose.last().pose(), cornerVec.x, cornerVec.y, cornerVec.z)
                    .uv(corner.isFacing(face.getTexU()) != flipX ? 1 : 0, corner.isFacing(face.getTexV()) != flipY ? 1 : 0).color(-1)
                    .normal(pose.last().normal(), normal.getX(), normal.getY(), normal.getZ()).endVertex();
        }
    }
}