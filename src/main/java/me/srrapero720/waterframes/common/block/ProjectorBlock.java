package me.srrapero720.waterframes.common.block;

import com.mojang.serialization.MapCodec;
import me.srrapero720.waterframes.DisplaysRegistry;
import me.srrapero720.waterframes.common.block.entity.ProjectorTile;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import me.srrapero720.waterframes.common.util.geo.Facing;
import me.srrapero720.waterframes.common.util.geo.AlignedBox;

@SuppressWarnings({"deprecation", "null"})
@MethodsReturnNonnullByDefault
public class ProjectorBlock extends DisplayBlock {

    public static final MapCodec<ProjectorBlock> CODEC = simpleCodec(ProjectorBlock::new);

    public ProjectorBlock() {}
    public ProjectorBlock(BlockBehaviour.Properties p) {}

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public DirectionProperty getFacing() {
        return BlockStateProperties.HORIZONTAL_FACING;
    }

    @Override
    public PermissionNode<Boolean> getPermissionNode() {
        return DisplaysRegistry.PERM_DISPLAYS_INTERACT_PROJECTOR;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(getFacing());
        Facing facing = Facing.of(direction);
        var box = new AlignedBox();

        // fit projector model height
        box.maxY = 8f / 16f;

        // fit projector thickness
        float blockThickness = 4f / 16f;
        box.min(facing.axis, blockThickness);
        box.max(facing.axis, 1 - blockThickness);

        // fit anchor of it
        Facing clockWise = Facing.of(direction.getClockWise());
        box.min(clockWise.axis, 1f / 16f);
        box.max(clockWise.axis, 15f / 16f);

        return box.shape();
    }

    @Override
    public void registerDefaultState(BlockState state) {
        super.registerDefaultState(state.setValue(VISIBLE, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(VISIBLE));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ProjectorTile(pPos, pState);
    }
}