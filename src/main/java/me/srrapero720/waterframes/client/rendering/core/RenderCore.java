package me.srrapero720.waterframes.client.rendering.core;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.Vec3i;
import org.lwjgl.opengl.GL11;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL;

public class RenderCore {
    private static final Tesselator tesselator = Tesselator.getInstance();
    private static BufferBuilder builder;

    public static void cleanShader() {
        ShaderInstance shader = RenderSystem.getShader();
        shader.apply();
        shader.clear();
    }

    public static void bufferPrepare() {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
    }

    public static BufferBuilder bufferBegin() {
        if (builder != null) bufferFinish();
        builder = tesselator.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR_NORMAL);
        return builder;
    }

    public static void bufferFinish() {
        BufferUploader.drawWithShader(builder.buildOrThrow());
        builder = null;
    }

    public static void bindTex(int texture) {
        RenderSystem.bindTexture(texture);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
    }

    public static void unbindTex() {
        RenderSystem.bindTexture(0);
        RenderSystem.setShaderTexture(0, 0);
    }

    public static void vertexF(PoseStack pose, AlignedBox box, BoxFace face, boolean flipX, boolean flipY, int a, int r, int g, int b) {
        for (int i = 0; i < face.corners.length; i++) {
            vertex(pose, box, face, face.corners[i], flipX, flipY, a, r, g, b);
        }
    }

    public static void vertexB(PoseStack pose, AlignedBox box, BoxFace face, boolean flipX, boolean flipY, int a, int r, int g, int b) {
        for (int i = face.corners.length - 1; i >= 0; i--) {
            vertex(pose, box, face, face.corners[i], flipX, flipY, a, r, g, b);
        }
    }

    private static void vertex(PoseStack pose, AlignedBox box, BoxFace face, BoxCorner corner, boolean flipX, boolean flipY, int a, int r, int g, int b) {
        Vec3i normal = face.facing.normal;
        builder.addVertex(pose.last().pose(), box.get(corner.x), box.get(corner.y), box.get(corner.z))
                .setUv(corner.isFacing(face.getTexU()) != flipX ? 1 : 0, corner.isFacing(face.getTexV()) != flipY ? 1 : 0)
                .setUv1(corner.isFacing(face.getTexU()) != flipX ? 1 : 0, corner.isFacing(face.getTexV()) != flipY ? 1 : 0)
                .setUv2(corner.isFacing(face.getTexU()) != flipX ? 1 : 0, corner.isFacing(face.getTexV()) != flipY ? 1 : 0)
                .setColor(r, g, b, a)
                .setNormal(pose.last(), normal.getX(), normal.getY(), normal.getZ());
    }
}
