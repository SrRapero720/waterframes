package me.srrapero720.waterframes.common.block;

import me.srrapero720.waterframes.common.block.entity.BigTvTile;
import me.srrapero720.waterframes.common.screens.DisplayScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

public class BigTvBlock extends DisplayBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BigTvBlock() {
        super(Properties.of(Material.METAL).strength(1f).sound(SoundType.METAL).noOcclusion());
    }

    @Override
    public DirectionProperty getFacing() {
        return FACING;
    }

    public static AlignedBox box(Direction direction, boolean renderMode) {
        Facing facing = Facing.get(direction);
        var box = new AlignedBox();

        Axis one = facing.one();
        Axis two = facing.two();

        if (facing.axis != Axis.Z) {
            one = facing.two();
            two = facing.one();
        }

        float renderMargin = renderMode ? 1f : 0;

        // fit
        if (facing.positive) {
            box.setMin(facing.axis, 1f - (14f / 16.0f));
            box.setMax(facing.axis, 1f - (12f / 16.0f));
        } else {
            box.setMin(facing.axis, (12f / 16.0f));
            box.setMax(facing.axis, (14f / 16.0f));
        }

        // fit height
        box.setMin(two, (2f + renderMargin) / 16f);
        box.setMax(two, 2 - (renderMargin / 16f));

        // fit width
        box.setMin(one, (-14f + renderMargin) / 16f);
        box.setMax(one, (30f - renderMargin) / 16f);

        return box;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction current = context.getHorizontalDirection();
        return super.getStateForPlacement(context)
                .setValue(getFacing(), context.getPlayer().isCrouching() ? current.getOpposite() : current);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BigTvBlock.box(state.getValue(getFacing()), false).voxelShape();
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return this.getShape(state, level, pos, null);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BigTvTile(pPos, pState);
    }

    @Override
    public GuiLayer create(CompoundTag compoundTag, Level level, BlockPos blockPos, BlockState blockState, Player player) {
        super.create(compoundTag, level, blockPos, blockState, player);
        return (level.getBlockEntity(blockPos) instanceof BigTvTile tile) ? new DisplayScreen(tile) : null;
    }
}
