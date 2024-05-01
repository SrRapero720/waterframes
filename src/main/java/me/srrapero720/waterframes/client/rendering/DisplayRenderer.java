package me.srrapero720.waterframes.client.rendering;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.Tesselator;
import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.Util;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;

import java.util.function.Function;

public class DisplayRenderer implements BlockEntityRenderer<DisplayTile> {

    private static final Function<ResourceLocation, RenderType> BLOCK_TRANSLUCENT_CULL_CUSTOM_TEXTURE = Util.memoize((p_173198_) -> {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RenderType.RENDERTYPE_TRANSLUCENT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_173198_, false, false)).setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY).setLightmapState(RenderType.LIGHTMAP).setOverlayState(RenderType.NO_OVERLAY).createCompositeState(false);
        return RenderType.create("block_translucent_cull_custom_texture", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, true, true, rendertype$compositestate);
    });

    private final BlockEntityRendererProvider.Context context;
    public DisplayRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public boolean shouldRenderOffScreen(DisplayTile tile) {
        return tile.data.getWidth() > 16 || tile.data.getHeight() > 16;
    }

    @Override
    public boolean shouldRender(DisplayTile tile, @NotNull Vec3 cameraPos) {
        return Vec3.atCenterOf(tile.getBlockPos()).closerThan(cameraPos, tile.data.renderDistance);
    }

    @Override
    public AABB getRenderBoundingBox(DisplayTile tile) {
        return tile.getRenderBox().getBB(tile.getBlockPos());
    }

    @Override
    public void render(DisplayTile tile, float partialTicks, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var display = tile.activeDisplay();
        if (display == null || !WFConfig.keepsRendering()) return;

        var direction = tile.getDirection();
        var box = tile.getRenderBox();
        var invertedFace = tile.caps.invertedFace(tile);
        var boxFace = BoxFace.get(Facing.get(invertedFace ? direction.getOpposite() : direction));
        var facing = boxFace.facing;
        packedLight = LightTexture.pack(15, 15);

        boolean front = !tile.caps.projects() || tile.data.renderBothSides;
        boolean back = tile.caps.projects() || tile.data.renderBothSides;
        boolean flipX = tile.caps.projects() != tile.data.flipX;
        boolean flipY = tile.data.flipY;
        int r, b, g;
        r = g = b= tile.data.brightness;
        int a = tile.data.alpha;

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(facing.rotation().rotation((float) Math.toRadians(-tile.data.rotation)));
        pose.translate(-0.5, -0.5, -0.5);

        // TWEAK FOR "EXTRA-RESIZING"
        if (tile.caps.growMax(tile, facing, invertedFace)) {
            box.setMax(facing.axis,box.getMax(facing.axis) + tile.caps.growSize());
        } else {
            box.setMin(facing.axis, box.getMin(facing.axis) - tile.caps.growSize());
        }

        // RENDERING
        if (display.isLoading()) {
            this.vertex(pose, bufferSource, getLoadingBox(tile, box, facing), boxFace, facing, packedLight, packedOverlay,
                    front, back, flipX, flipY, r, g, b, a, WaterFrames.LOADING_ANIMATION);
        } else if (display.canRender()) {
            int tex = display.texture();
            if (tex != -1) {
                this.vertex(pose, bufferSource, box, boxFace, facing, packedLight, packedOverlay,
                        front, back, flipX, flipY, r, g, b, a, display.getTextureId());
            }

            if (display.isBuffering()) {
                this.vertex(pose, bufferSource, getLoadingBox(tile, box, facing), boxFace, facing, packedLight, packedOverlay,
                        front, back, flipX, flipY, r, g, b, a, WaterFrames.LOADING_ANIMATION);
            }
        }

        pose.popPose();
    }

    public void vertex(PoseStack pose, MultiBufferSource source, AlignedBox box, BoxFace boxface, Facing facing, int packedLight, int packedOverlay,
                       boolean front, boolean back, boolean flipX, boolean flipY, int r, int g, int b, int a, ResourceLocation texture) {

        VertexConsumer builder = source.getBuffer(BLOCK_TRANSLUCENT_CULL_CUSTOM_TEXTURE.apply(texture));
        if (front) {
            for (int i = 0; i < boxface.corners.length; i++) {
                BoxCorner corner = boxface.corners[i];
                this.vertex(pose, builder, box, boxface, corner, facing, packedLight, packedOverlay, flipX, flipY, r, g, b, a);
            }
        }
        if (back) {
            for (int i = boxface.corners.length - 1; i >= 0; i--) {
                BoxCorner corner = boxface.corners[i];
                this.vertex(pose, builder, box, boxface, corner, facing, packedLight, packedOverlay, flipX, flipY, r, g, b, a);
            }
        }
    }

    public void vertex(PoseStack pose, VertexConsumer builder, AlignedBox box, BoxFace boxface, BoxCorner corner, Facing facing, int packedLight, int packedOverlay, boolean flipX, boolean flipY, int r, int g, int b, int a) {
        Vec3i normal = facing.normal;
        builder.addVertex(pose.last().pose(), box.get(corner.x), box.get(corner.y), box.get(corner.z))
                .setColor(r, g, b, a)
                .setUv(corner.isFacing(boxface.getTexU()) != flipX ? 1f : 0f, corner.isFacing(boxface.getTexV()) != flipY ? 1f : 0f)
                .setLight(packedLight)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(pose.last(), normal.getX(), normal.getY(), normal.getZ());
    }

    public AlignedBox getLoadingBox(DisplayTile tile, AlignedBox parent, Facing facing) {
        AlignedBox box = new AlignedBox(parent);

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
            box.setMax(facing.axis, parent.getMax(facing.axis) + (tile.caps.projects() ? -0.001f : 0.001f));
        } else {
            box.setMin(facing.axis, parent.getMin(facing.axis) - (tile.caps.projects() ? -0.001f : 0.001f));
        }

        return box;
    }
}
