package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.data.ProjectorData;
import me.srrapero720.waterframes.common.block.ProjectorBlock;
import me.srrapero720.waterframes.util.FrameRegistry;
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

public class ProjectorTile extends DisplayTile<ProjectorData> {
    public ProjectorTile(BlockPos pos, BlockState state) {
        super(new ProjectorData(), FrameRegistry.TILE_PROJECTOR.get(), pos, state);
    }

    public static DirectionProperty getDefaultDirectionalProperty() {
        return BlockStateProperties.HORIZONTAL_FACING;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() { return getRenderBox().getBB(getBlockPos()); }

    @Override
    public AlignedBox getRenderBox() {
        return getRenderBox(ProjectorBlock.FACING, data.projectionDistance + 0.99f);
    }

    @Override
    public AlignedBox getRenderGifBox() {
        Direction direction = getBlockState().getValue(ProjectorBlock.FACING);
        Facing facing = Facing.get(direction);
        AlignedBox box = DisplayBlock.getBlockBox(direction, data.projectionDistance + 0.97f);

        Axis one = facing.one();
        Axis two = facing.two();

        box.setMin(one, -1f);
        box.setMax(one, 2f);

        box.setMin(two, -1f);
        box.setMax(two, 2f);

        return box;
    }
}