package me.srrapero720.waterframes.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.srrapero720.waterframes.client.display.TextureDisplay;
import me.srrapero720.waterframes.client.renderer.engine.RenderBox;
import me.srrapero720.waterframes.common.block.FrameBlock;
import me.srrapero720.waterframes.common.block.entity.FrameTile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

@OnlyIn(Dist.CLIENT)
public class FrameRender implements BlockEntityRenderer<FrameTile> {
    @Override
    public boolean shouldRenderOffScreen(FrameTile frame) {
        return frame.data.getWidth() > 8 || frame.data.getHeight() > 8;
    }
    @Override
    public boolean shouldRender(FrameTile frame, @NotNull Vec3 vec) {
        return Vec3.atCenterOf(frame.getBlockPos()).closerThan(vec, frame.data.renderDistance);
    }

    @Override
    public void render(FrameTile block, float pPartialTick, PoseStack pose, MultiBufferSource bufferSource, int pPackedLight, int pPackedOverlay) {
        TextureDisplay display = block.requestDisplay();
        if (display == null) return;

        Facing facing = Facing.get(block.getBlockState().getValue(FrameBlock.FACING));
        AlignedBox alignedBox = RenderBox.getBasic(block, facing, FrameBlock.THICKNESS);
        alignedBox.grow(facing.axis, 0.01f);

        DisplayRender.render(pose, block, facing, alignedBox, true, block.data.renderBothSides, false, false);

        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }
}