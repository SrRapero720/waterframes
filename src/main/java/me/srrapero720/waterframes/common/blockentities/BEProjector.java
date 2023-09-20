package me.srrapero720.waterframes.common.blockentities;

import me.srrapero720.waterframes.api.BEDisplay;
import me.srrapero720.waterframes.api.DataBlock;
import me.srrapero720.waterframes.common.blocks.FrameBlock;
import me.srrapero720.waterframes.common.blocks.ProjectorBlock;
import me.srrapero720.waterframes.core.WaterRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

public class BEProjector extends BEDisplay<DataBlock.Projector> {
    public BEProjector(BlockPos pos, BlockState state) {
        super(new DataBlock.Projector(), WaterRegistry.TILE_PROJECTOR.get(), pos, state);
    }

    public AlignedBox getBox() {
        Direction direction = getBlockState().getValue(ProjectorBlock.FACING);
        Facing facing = Facing.get(direction);
        AlignedBox box = FrameBlock.box(direction, (float) data.projectionDistance + 0.99f);

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
}