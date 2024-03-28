package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.client.renderer.engine.RenderBox;
import me.srrapero720.waterframes.common.block.FrameBlock;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.WFRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;

public class FrameTile extends DisplayTile {
    public FrameTile(BlockPos pos, BlockState state) {
        super(new DisplayData(){}, WFRegistry.TILE_FRAME.get(), pos, state);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return RenderBox.getBasic(this, Facing.get(this.getBlockState().getValue(FrameBlock.FACING)), FrameBlock.THICKNESS).getBB(getBlockPos());
    }

    @Override
    public boolean canHideModel() {
        return true;
    }

    @Override
    public boolean canProject() {
        return false;
    }

    @Override
    public boolean canRenderBackside() {
        return true;
    }

    @Override
    public boolean canResize() {
        return true;
    }
}