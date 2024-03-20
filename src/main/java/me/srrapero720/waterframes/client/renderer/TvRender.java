package me.srrapero720.waterframes.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.client.display.TextureDisplay;
import me.srrapero720.waterframes.client.renderer.engine.RenderBox;
import me.srrapero720.waterframes.common.block.FrameBlock;
import me.srrapero720.waterframes.common.block.TvBlock;
import me.srrapero720.waterframes.common.block.entity.TvTile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

@OnlyIn(Dist.CLIENT)
public class TvRender implements BlockEntityRenderer<TvTile> {
    @Override
    public boolean shouldRenderOffScreen(TvTile frame) {
        return frame.data.getWidth() > 8 || frame.data.getHeight() > 8;
    }
    
    @Override
    public boolean shouldRender(TvTile block, @NotNull Vec3 playerPos) {
        return Vec3.atCenterOf(block.getBlockPos()).closerThan(playerPos, block.data.renderDistance);
    }

    @Override
    public void render(TvTile block, float pPartialTick, PoseStack pose, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        TextureDisplay display = block.requestDisplay();
        if (display == null) return;

        Direction direction = block.getBlockState().getValue(TvBlock.FACING);
        Facing facing = Facing.get(direction.getOpposite());
        AlignedBox alignedBox = TvBlock.box(direction, block.getBlockState().getValue(TvBlock.ATTACHED_FACE), true);
        alignedBox.grow(facing.axis, 0.01f);


        DisplayRender.render(pose, block, facing, alignedBox, true, false, false, false);

        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }
}