package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.client.rendering.RenderEngine;
import me.srrapero720.waterframes.common.block.FrameBlock;
import me.srrapero720.waterframes.common.data.FrameData;
import me.srrapero720.waterframes.util.FrameRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FrameTile extends DisplayTile<FrameData> {
    public FrameTile(BlockPos pos, BlockState state) {
        super(new FrameData(), FrameRegistry.TILE_FRAME.get(), pos, state);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        Direction direction = this.getBlockState().getValue(FrameBlock.FACING);
        return RenderEngine.getBasicRenderBox(this, direction, FrameBlock.THICKNESS).getBB(getBlockPos());
    }
}