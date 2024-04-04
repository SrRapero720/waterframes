package me.srrapero720.waterframes.client.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.client.display.TextureDisplay;
import me.srrapero720.waterframes.client.rendering.core.RenderCore;
import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import me.srrapero720.watermedia.api.math.MathAPI;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxFace;

public abstract class DisplayRenderer implements BlockEntityRenderer<DisplayTile> {
    public static final ImageRenderer LOADING_TEX = ImageAPI.loadingGif(WaterFrames.ID);

    @Override
    public boolean shouldRenderOffScreen(DisplayTile tile) {
        return tile.data.getWidth() > 8 || tile.data.getHeight() > 8;
    }
    @Override
    public boolean shouldRender(DisplayTile tile, @NotNull Vec3 cameraPos) {
        return Vec3.atCenterOf(tile.getBlockPos()).closerThan(cameraPos, tile.data.renderDistance);
    }

    @Override
    public void render(DisplayTile tile, float partialTicks, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var display = tile.requestDisplay();
        if (display == null) return;

        // STORE AND CLEAN ANY "EARLY" STATE
        RenderCore.bufferPrepare();
        float[] color = RenderSystem.getShaderColor();

        // PREPARE RENDERING
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(tile.data.brightness, tile.data.brightness, tile.data.brightness, tile.data.alpha);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        // variables
        var direction = this.direction(tile);
        var facing = this.facing(tile, direction);
        var box = this.box(tile, direction, facing);

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(facing.rotation().rotation((float) Math.toRadians(-tile.data.rotation)));
        pose.translate(-0.5, -0.5, -0.5);

        // RENDERING
        this.render(pose, tile, display, box, facing, BoxFace.get(facing), -1);

        // POST RENDERING
        pose.popPose();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
    }

    public void render(PoseStack pose, DisplayTile tile, TextureDisplay display, AlignedBox box, Facing facing, BoxFace face, int colorARGB) {
        // VAR DECLARE
        final boolean flipX = this.flipX(tile);
        final boolean flipY = this.flipY(tile);
        final boolean front = this.inFront(tile);
        final boolean back = this.inBack(tile);

        if (display.isLoading()) {
            RenderCore.bufferBegin();
            this.renderLoading(pose, tile, facing, face, front, back, flipX, flipY, colorARGB);
            RenderCore.bufferEnd();
            return;
        }

        if (!display.canRender()) return;

        int tex = display.texture();
        if (tex != -1) {
            RenderCore.bufferBegin();
            RenderCore.bindTex(tex);
            if (front)
                RenderCore.vertexF(pose, box, face, flipX, flipY, colorARGB);

            if (back)
                RenderCore.vertexB(pose, box, face, flipX, flipY, colorARGB);
            RenderCore.bufferEnd();
        }

        if (display.isBuffering()) {
            RenderCore.bufferBegin();
            this.renderLoading(pose, tile, facing, face, front, back, flipX, flipY, colorARGB);
            RenderCore.bufferEnd();
        }
    }

    public void renderLoading(PoseStack pose, DisplayTile tile, Facing facing, BoxFace face, boolean front, boolean back, boolean flipX, boolean flipY, int colorARGB) {
        RenderCore.bindTex(LOADING_TEX.texture(WaterFrames.getTicks(), MathAPI.tickToMs(WaterFrames.deltaFrames()), true));
        AlignedBox squaredBox = DisplayBlock.getBox(tile, facing, this.grwSize(), true);

        if (front)
            RenderCore.vertexF(pose, squaredBox, face, flipX, flipY, colorARGB);

        if (back)
            RenderCore.vertexB(pose, squaredBox, face, flipX, flipY, colorARGB);
    }

    public boolean inFront(DisplayTile tile) {
        return !tile.canProject() || tile.data.renderBothSides;
    }

    public boolean inBack(DisplayTile tile) {
        return tile.canProject() || tile.data.renderBothSides;
    }

    public boolean flipX(DisplayTile tile) {
        return tile.canProject() != tile.data.flipX;
    }

    public boolean flipY(DisplayTile tile) {
        return tile.data.flipY;
    }

    public Direction direction(DisplayTile tile) {
        return tile.getDirection();
    }

    public abstract Facing facing(DisplayTile tile, final Direction direction);
    public abstract AlignedBox box(DisplayTile tile, Direction direction, final Facing facing);
    public abstract float grwSize();
}
