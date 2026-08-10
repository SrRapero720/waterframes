package me.srrapero720.waterframes.client.rendering;

import com.mojang.blaze3d.vertex.*;
import me.srrapero720.waterframes.DisplaysConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.Util;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import me.srrapero720.waterframes.common.util.geo.Axis;
import me.srrapero720.waterframes.common.util.geo.Facing;
import me.srrapero720.waterframes.common.util.geo.AlignedBox;
import me.srrapero720.waterframes.common.util.geo.BoxCorner;
import me.srrapero720.waterframes.common.util.geo.BoxFace;

import java.util.function.Function;

public class DisplayRenderer implements BlockEntityRenderer<DisplayTile> {

    private static final Function<ResourceLocation, RenderType> BLOCK_TRANSLUCENT_CULL_CUSTOM_TEXTURE = Util.memoize((p_173198_) -> {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RenderType.RENDERTYPE_TRANSLUCENT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_173198_, false, false)).setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY).setLightmapState(RenderType.LIGHTMAP).setOverlayState(RenderType.NO_OVERLAY).createCompositeState(false);
        return RenderType.create("block_translucent_cull_custom_texture", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, true, true, rendertype$compositestate);
    });

    public DisplayRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public boolean shouldRenderOffScreen(DisplayTile tile) {
        return tile.data.getWidth() > 16 || tile.data.getHeight() > 16;
    }

    @Override
    public boolean shouldRender(DisplayTile tile, @NotNull Vec3 cameraPos) {
        BlockPos tilePos = tile.getBlockPos().relative(tile.getDirection(), (int) tile.data.projectionDistance);
        return Vec3.atCenterOf(tilePos).closerThan(cameraPos, tile.data.renderDistance);
    }

    @Override
    public AABB getRenderBoundingBox(DisplayTile tile) {
        return tile.getRenderBox().aabb(tile.getBlockPos());
    }

    @Override
    public void render(DisplayTile tile, float partialTicks, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var display = tile.activeDisplay();
        if (display == null || !DisplaysConfig.keepsRendering()) return;

        var direction = tile.getDirection();
        var box = tile.getRenderBox();
        var invertedFace = tile.caps.invertedFace(tile);
        var boxFace = BoxFace.of(Facing.of(invertedFace ? direction.getOpposite() : direction));
        var facing = boxFace.facing;

        boolean front = !tile.caps.projects() || tile.data.renderBothSides;
        boolean back = tile.caps.projects() || tile.data.renderBothSides;
        boolean flipX = tile.caps.projects() != tile.data.flipX;
        boolean flipY = tile.data.flipY;
        int brightness = tile.data.brightness;
        int alpha = tile.data.alpha;

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(facing.rotation().rotation((float) Math.toRadians(-tile.data.rotation)));
        pose.translate(-0.5, -0.5, -0.5);

        // TWEAK FOR "EXTRA-RESIZING"
        if (tile.caps.growMax(tile, facing, invertedFace)) {
            box.max(facing.axis, box.max(facing.axis) + tile.caps.growSize());
        } else {
            box.min(facing.axis, box.min(facing.axis) - tile.caps.growSize());
        }

        // RENDERING
        if (display.isLoading()) {
            // TODO: Loading animation rendering
//            this.vertex(pose, bufferSource, getLoadingBox(tile, box, facing), boxFace, facing,
//                    front, back, flipX, flipY, brightness, alpha, WaterFrames.LOADING_ANIMATION);
        } else if (display.canRender()) {
            var tex = display.textureId();
            if (tex != null) {
                this.vertex(pose, bufferSource, box, boxFace, facing,
                        front, back, flipX, flipY, brightness, alpha, tex);
            }

            // TODO: Buffering strip waits on the same loading animation; the texture behind
            //  WaterFrames.LOADING_ANIMATION does not exist yet and rendered as missing squares
//            if (display.isBuffering()) {
//                this.vertex(pose, bufferSource, getLoadingBox(tile, box, facing), boxFace, facing,
//                        front, back, flipX, flipY, brightness, alpha, WaterFrames.LOADING_ANIMATION);
//            }
        }

        pose.popPose();
    }

    private void vertex(PoseStack pose, MultiBufferSource source, AlignedBox box, BoxFace boxface, Facing facing,
                        boolean front, boolean back, boolean flipX, boolean flipY, int brightness, int alpha, ResourceLocation texture) {

        VertexConsumer builder = source.getBuffer(DisplaysConfig.shaderMode() ? RenderType.entityTranslucentCull(texture) : BLOCK_TRANSLUCENT_CULL_CUSTOM_TEXTURE.apply(texture));
        Vec3i normal = facing.normal;
        if (front) {
            for (BoxCorner corner: boxface.corners) {
                this.vertex(pose, builder, box, boxface, corner, normal.getX(), normal.getY(), normal.getZ(), flipX, flipY, brightness, alpha);
            }
        }
        if (back) {
            for (int i = boxface.corners.length - 1; i >= 0; i--) {
                this.vertex(pose, builder, box, boxface, boxface.corners[i], -normal.getX(), -normal.getY(), -normal.getZ(), flipX, flipY, brightness, alpha);
            }
        }
    }

    private void vertex(PoseStack pose, VertexConsumer builder, AlignedBox box, BoxFace boxface, BoxCorner corner,
                        int nx, int ny, int nz, boolean flipX, boolean flipY, int brightness, int alpha) {
        builder.addVertex(pose.last().pose(), box.edge(corner.x), box.edge(corner.y), box.edge(corner.z))
                .setColor(brightness, brightness, brightness, alpha)
                .setUv(corner.faces(boxface.texU) != flipX ? 1f : 0f, corner.faces(boxface.texV) != flipY ? 1f : 0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose.last(), nx, ny, nz);
    }

    private AlignedBox getLoadingBox(DisplayTile tile, AlignedBox parent, Facing facing) {
        AlignedBox box = new AlignedBox(parent);

        Axis one = facing.one();
        Axis two = facing.two();
        float width = box.size(one);
        float height = box.size(two);

        // SPINNER IS SQUARED TO THE SHORT SIDE AND KEEPS A QUARTER OF IT AS MARGIN ALL AROUND
        if (width != height) {
            Axis longAxis = width > height ? one : two;
            Axis shortAxis = width > height ? two : one;
            float centering = Math.abs(width - height) / 2f;
            float margin = Math.min(width, height) / 4f;
            box.min(longAxis, box.min(longAxis) + centering + margin);
            box.max(longAxis, box.max(longAxis) - centering - margin);
            box.min(shortAxis, box.min(shortAxis) + margin);
            box.max(shortAxis, box.max(shortAxis) - margin);
        }

        // NUDGED OFF THE MEDIA PLANE TOWARDS THE VIEWER SIDE, WHICH IS THE BACK ONE FOR PROJECTORS
        float nudge = tile.caps.projects() ? -0.001f : 0.001f;
        if (facing.positive) box.max(facing.axis, parent.max(facing.axis) + nudge);
        else box.min(facing.axis, parent.min(facing.axis) - nudge);

        return box;
    }
}
