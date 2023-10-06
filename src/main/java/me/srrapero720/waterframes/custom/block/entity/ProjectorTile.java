package me.srrapero720.waterframes.custom.block.entity;

import me.srrapero720.waterframes.api.block.entity.BasicBlockEntity;
import me.srrapero720.waterframes.custom.data.ProjectorData;
import me.srrapero720.waterframes.custom.block.FrameBlock;
import me.srrapero720.waterframes.custom.block.ProjectorBlock;
import me.srrapero720.waterframes.core.WaterRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

public class ProjectorTile extends BasicBlockEntity<ProjectorData> {
    public ProjectorTile(BlockPos pos, BlockState state) {
        super(new ProjectorData(), WaterRegistry.TILE_PROJECTOR.get(), pos, state);
    }

    @Override
    public AlignedBox getBox() {
        Direction direction = getBlockState().getValue(ProjectorBlock.FACING);
        Facing facing = Facing.get(direction);
        AlignedBox box = FrameBlock.box(direction, (float) data.projectionDistance + 0.991f);

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

    public static DirectionProperty getDefaultDirectionalProperty() {
        return BlockStateProperties.HORIZONTAL_FACING;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() { return getBox().getBB(getBlockPos()); }
}