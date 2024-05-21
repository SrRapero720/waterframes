package me.srrapero720.waterframes.client.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.client.display.DisplayControl;
import me.srrapero720.waterframes.client.display.TextureDisplay;
import me.srrapero720.waterframes.client.rendering.core.RenderCore;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import me.srrapero720.watermedia.api.math.MathAPI;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxFace;

public class DisplayRenderer implements BlockEntityRenderer<DisplayTile> {
    public static final ImageRenderer LOADING_TEX = ImageAPI.loadingGif(WaterFrames.ID);

    public DisplayRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public boolean shouldRenderOffScreen(DisplayTile tile) {
        return tile.data.getWidth() > 8 || tile.data.getHeight() > 8;
    }
    @Override
    public boolean shouldRender(DisplayTile tile, @NotNull Vec3 cameraPos) {
        Vec3 vec3 = Vec3.atCenterOf(tile.getBlockPos());
        if (!vec3.closerThan(cameraPos, tile.data.renderDistance + 10f)) {
            tile.flushDisplay();
        }
        return vec3.closerThan(cameraPos, tile.data.renderDistance);
    }

    @Override
    public void render(DisplayTile tile, float partialTicks, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var display = tile.requestDisplay();
        if (display == null || !WFConfig.keepsRendering()) return;

        // STORE AND CLEAN ANY "EARLY" STATE
        RenderCore.cleanShader();
        RenderCore.bufferPrepare();
        RenderCore.cleanShader();

        // PREPARE RENDERING
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // variables
        var direction = this.direction(tile);
        var facing = Facing.get(direction);
        var box = tile.getRenderBox();

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(facing.rotation().rotation((float) Math.toRadians(-tile.data.rotation)));
        pose.translate(-0.5, -0.5, -0.5);

        if (facing.positive) {
            if (!tile.flip3DFace()) box.setMax(facing.axis, box.getMax(facing.axis) + tile.growSize());
            else box.setMin(facing.axis, box.getMin(facing.axis) - tile.growSize());
        } else {
            if (!tile.flip3DFace()) box.setMin(facing.axis, box.getMin(facing.axis) - tile.growSize());
            else box.setMax(facing.axis, box.getMax(facing.axis) + tile.growSize());
        }

        // RENDERING
        final int brightness = tile.data.brightness;
        this.render(pose, tile, display, box, BoxFace.get(tile.flip3DFace() ? facing.opposite() : facing), tile.data.alpha, brightness, brightness, brightness);

        // POST RENDERING
        pose.popPose();
        RenderCore.cleanShader();

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.bindTexture(0);
    }

    public void render(PoseStack pose, DisplayTile tile, TextureDisplay display, AlignedBox box, BoxFace face, int a, int r, int g, int b) {
        // VAR DECLARE
        final boolean flipX = this.flipX(tile);
        final boolean flipY = this.flipY(tile);
        final boolean front = this.inFront(tile);
        final boolean back = this.inBack(tile);

        if (display.isLoading()) {
            RenderCore.bufferBegin();
            this.renderLoading(pose, tile, box, face, front, back, flipX, flipY);
            RenderCore.bufferEnd();
            return;
        }

        if (!display.canRender()) return;

        int tex = display.texture();
        if (tex != -1) {
            RenderCore.bufferBegin();
            RenderCore.bindTex(tex);
            if (front)
                RenderCore.vertexF(pose, box, face, flipX, flipY, a, r, g, b);

            if (back)
                RenderCore.vertexB(pose, box, face, flipX, flipY, a, r, g, b);
            RenderCore.bufferEnd();
        }

        if (display.isBuffering()) {
            RenderCore.bufferBegin();
            this.renderLoading(pose, tile, box, face, front, back, flipX, flipY);
            RenderCore.bufferEnd();
        }
    }

    public void renderLoading(PoseStack pose, DisplayTile tile, AlignedBox alignedBox, BoxFace face, boolean front, boolean back, boolean flipX, boolean flipY) {
        RenderCore.bindTex(LOADING_TEX.texture(DisplayControl.getTicks(), MathAPI.tickToMs(WaterFrames.deltaFrames()), true));

        AlignedBox box = new AlignedBox(alignedBox);
        Facing facing = face.getFacing();

        Axis one = facing.one();
        Axis two = facing.two();

        float width = box.getSize(one);
        float height = box.getSize(two);

        if (width > height) {
            float subtracts = ((width - height) / 2f);
            float marginSubstract = height / 4;
            box.setMin(one, (box.getMin(one) + subtracts) + marginSubstract);
            box.setMax(one, (box.getMax(one) - subtracts) - marginSubstract);
            box.setMin(two, box.getMin(two) + marginSubstract);
            box.setMax(two, box.getMax(two) - marginSubstract);
        } else if (height > width) {
            float subtracts = ((height - width) / 2f);
            float marginSubstract = width / 4;
            box.setMin(two, (box.getMin(two) + subtracts) + marginSubstract);
            box.setMax(two, (box.getMax(two) - subtracts) - marginSubstract);
            box.setMin(one, box.getMin(one) + marginSubstract);
            box.setMax(one, box.getMax(one) - marginSubstract);
        }

        if (facing.positive) {
            box.setMax(face.getFacing().axis, alignedBox.getMax(facing.axis) + (tile.canProject() ? -0.001f : 0.001f));
        } else {
            box.setMin(facing.axis, alignedBox.getMin(facing.axis) - (tile.canProject() ? -0.001f : 0.001f));
        }

        if (front)
            RenderCore.vertexF(pose, box, face, flipX, flipY, 255, 255, 255, 255);

        if (back)
            RenderCore.vertexB(pose, box, face, flipX, flipY, 255, 255, 255, 255);
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
}
