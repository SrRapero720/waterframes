package me.srrapero720.waterframes.custom.block.entity;

import me.srrapero720.waterframes.api.block.entity.BasicBlockEntity;
import me.srrapero720.waterframes.custom.data.FrameData;
import me.srrapero720.waterframes.custom.block.FrameBlock;
import me.srrapero720.waterframes.core.WaterRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

public class FrameTile extends BasicBlockEntity<FrameData> {
    public FrameTile(BlockPos pos, BlockState state) {
        super(new FrameData(), WaterRegistry.TILE_FRAME.get(), pos, state);
    }

    public AlignedBox getBox() {
        Direction direction = getBlockState().getValue(FrameBlock.FACING);
        Facing facing = Facing.get(direction);
        AlignedBox box = FrameBlock.box(direction, FrameBlock.THICKNESS);

        Axis one = facing.one();
        Axis two = facing.two();

        if (facing.axis != Axis.Z) {
            one = facing.two();
            two = facing.one();
        }

        box.setMin(one, this.data.min.x);
        box.setMax(one, this.data.max.x);

        box.setMin(two, this.data.min.y);
        box.setMax(two, this.data.max.y);
        return box;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() { return getBox().getBB(getBlockPos()); }

    public static void tick(Level level, BlockPos pos, BlockState state, FrameTile frame) {
        if (state.getValue(FrameBlock.VISIBLE) != frame.data.visibleFrame) {
            var brandNewState = state.setValue(FrameBlock.VISIBLE, frame.data.visibleFrame);
            level.setBlock(pos, brandNewState, 0);
        }
    }
}