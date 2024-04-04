package me.srrapero720.waterframes.common.block;

import me.srrapero720.waterframes.common.block.entity.TvTile;
import me.srrapero720.waterframes.common.screens.DisplayScreen;
import net.minecraft.MethodsReturnNonnullByDefault;
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
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings({"deprecated", "null", "unused"})
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TvBlock extends DisplayBlock {
    public static final DirectionProperty ATTACHED_FACE = DirectionProperty.create("attached_face", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public TvBlock() {
        super(Properties.of(Material.METAL).strength(1f).sound(SoundType.METAL).noOcclusion());
    }

    @Override
    public DirectionProperty getFacing() {
        return FACING;
    }

    public static AlignedBox box(Direction direction, Direction attachedBlockFace, boolean renderMode) {
        Facing facing = Facing.get(direction);
        Facing facingClockWise = Facing.get(direction.getClockWise());
        Facing wide = Facing.get(attachedBlockFace);
        AlignedBox box = new AlignedBox();

        // SETUP PROFUNDITY
        if (attachedBlockFace.getOpposite() == direction || attachedBlockFace == direction) {
            if (facing.positive) {
                box.setMax(facing.axis, 1f - (3f / 16.0f));
                box.setMin(facing.axis, 1f - (5f / 16.0f));
            } else {
                box.setMax(facing.axis, (5f / 16.0f));
                box.setMin(facing.axis, (3f / 16.0f));
            }
        } else {
            if (facing.positive) {
                box.setMax(facing.axis, (4.0f / 16.0f));
                box.setMin(facing.axis, (2.0f / 16.0f));
            } else {
                box.setMax(facing.axis, 1f - (2.0f / 16.0f));
                box.setMin(facing.axis, 1f - (4.0f / 16.0f));
            }
        }

        // SETUP HEIGHT
        float renderMargin = renderMode ? 1f : 0;
        if (attachedBlockFace == Direction.DOWN) {
            box.setMax(Facing.UP.axis, ((10f - renderMargin) / 16f)); // render: 9.5
            box.setMin(Facing.UP.axis, -((10f - renderMargin) / 16f)); // render: 9.5
        } else if (attachedBlockFace.getOpposite() == direction || attachedBlockFace == direction) {
            box.setMax(Facing.UP.axis, ((20f - renderMargin) / 16f));
            box.setMin(Facing.UP.axis, (0f + renderMargin) / 16f);
        } else {
            box.setMax(Facing.UP.axis, ((23.0f - renderMargin) / 16.0f)); // render: 22.5
            box.setMin(Facing.UP.axis, ((3.0f + renderMargin) / 16.0f)); // render: 3.5
        }

        // SETUP WIDE
        renderMargin = renderMode ? 1f : 0;
        if (attachedBlockFace == Direction.DOWN || attachedBlockFace == Direction.UP) {
            box.setMax(facingClockWise.axis, ((24f - renderMargin) / 16f)); // render: 23
            box.setMin(facingClockWise.axis, 1 - ((24f - renderMargin) / 16f)); // render: 23
        } else if (attachedBlockFace.getOpposite() == direction || attachedBlockFace == direction) {
            box.setMax(facingClockWise.axis, ((24f - renderMargin) / 16f));
            box.setMin(facingClockWise.axis, 1 - ((24f - renderMargin) / 16f));
        } else {
            if (wide.positive) {
                box.setMax(wide.axis, (32f - renderMargin) / 16f); // render: 31f / 16f
                box.setMin(wide.axis, (0f + renderMargin) / 16f); // render: 1f / 16f
            } else {
                box.setMax(wide.axis, (16f - renderMargin) / 16f); // render: 15f / 16f
                box.setMin(wide.axis, -((16f - renderMargin) / 16f)); // render: 15f / 16f
            }
        }

        if (!renderMode) box.scale(1.01f);
        else box.grow(facing.axis, 0.001f);
        return box;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction current = context.getHorizontalDirection();
        return this.defaultBlockState()
                .setValue(getFacing(), context.getPlayer().isCrouching() ? current.getOpposite() : current)
                .setValue(ATTACHED_FACE, context.getClickedFace());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(ATTACHED_FACE));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return TvBlock.box(state.getValue(getFacing()), state.getValue(ATTACHED_FACE), false).voxelShape();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return TvBlock.box(state.getValue(getFacing()), state.getValue(ATTACHED_FACE), false).voxelShape();
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return TvBlock.box(state.getValue(getFacing()), state.getValue(ATTACHED_FACE), false).voxelShape();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TvTile(pPos, pState);
    }

    @Override
    public GuiLayer create(CompoundTag compoundTag, Level level, BlockPos blockPos, BlockState blockState, Player player) {
        return (level.getBlockEntity(blockPos) instanceof TvTile projector) ? new DisplayScreen(projector) : null;
    }
}