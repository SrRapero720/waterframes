package me.srrapero720.waterframes.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.srrapero720.waterframes.client.display.TextureDisplay;
import me.srrapero720.waterframes.client.renderer.engine.RenderBox;
import me.srrapero720.waterframes.common.block.ProjectorBlock;
import me.srrapero720.waterframes.common.block.entity.ProjectorTile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

@OnlyIn(Dist.CLIENT)
public class ProjectorRender implements BlockEntityRenderer<ProjectorTile> {
    @Override
    public boolean shouldRenderOffScreen(ProjectorTile frame) {
        return frame.data.getWidth() > 8 || frame.data.getHeight() > 8;
    }
    
    @Override
    public boolean shouldRender(ProjectorTile block, @NotNull Vec3 playerPos) {
        Direction direction = block.getBlockState().getValue(ProjectorBlock.FACING);
        BlockPos blockPos = block.getBlockPos();
        BlockPos projectionPos = blockPos.relative(direction, block.data.projectionDistance);
        return Vec3.atCenterOf(projectionPos).closerThan(playerPos, block.data.renderDistance) && Vec3.atCenterOf(blockPos).closerThan(playerPos, block.data.projectionDistance + (double) block.data.renderDistance / 2);
    }

    @Override
    public void render(ProjectorTile block, float pPartialTick, PoseStack pose, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        TextureDisplay display = block.requestDisplay();
        if (display == null) return;

        Facing facing = Facing.get(block.getBlockState().getValue(ProjectorBlock.FACING));
        AlignedBox alignedBox = RenderBox.getBasic(block, facing, block.data.projectionDistance);

        alignedBox.grow(facing.axis, 0.99f);
        DisplayRender.render(pose, block, facing, alignedBox, false, true, true, false);

        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }
}