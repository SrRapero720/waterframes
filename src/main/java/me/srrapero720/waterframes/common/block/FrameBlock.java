package me.srrapero720.waterframes.common.block;

import com.mojang.serialization.MapCodec;
import me.srrapero720.waterframes.DisplaysRegistry;
import me.srrapero720.waterframes.common.block.entity.FrameTile;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

@MethodsReturnNonnullByDefault
public class FrameBlock extends DisplayBlock {
    public static final float THICKNESS = 0.0625F / 2F;

    public static final MapCodec<FrameBlock> CODEC = simpleCodec(FrameBlock::new);

    protected FrameBlock(BlockBehaviour.Properties p) {
        super(p);
    }

    public FrameBlock(ResourceKey<Block> resourceKey) {
        super(resourceKey);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public EnumProperty<Direction> getFacing() {
        return BlockStateProperties.FACING;
    }

    @Override
    public PermissionNode<Boolean> getPermissionNode() {
        return DisplaysRegistry.PERM_DISPLAYS_INTERACT_FRAME;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var facing = Facing.get(state.getValue(getFacing()));
        var box = new AlignedBox();

        if (facing.positive) box.setMax(facing.axis, THICKNESS);
        else box.setMin(facing.axis, 1 - THICKNESS);
        return box.voxelShape();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(VISIBLE));
    }

    @Override
    public void registerDefaultState(BlockState state) {
        super.registerDefaultState(state.setValue(VISIBLE, true));
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(getFacing(), context.getClickedFace());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FrameTile(pos, state);
    }
}