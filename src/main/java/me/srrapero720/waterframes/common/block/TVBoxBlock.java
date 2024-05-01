package me.srrapero720.waterframes.common.block;

import com.mojang.serialization.MapCodec;
import me.srrapero720.waterframes.common.block.entity.TVBoxTile;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

public class TVBoxBlock extends DisplayBlock {
    public static final MapCodec<TVBoxBlock> CODEC = simpleCodec(TVBoxBlock::new);
    private static final AlignedBox STATIC_BOX = new AlignedBox();
    protected static final Properties TV_BOX_PROPERTIES = FabricBlockSettings.create()
            .luminance(value -> value.getValue(LIGHT_LEVEL))
            .strength(1f)
            .sound(SoundType.WOOD);

    public TVBoxBlock() {
        super(TV_BOX_PROPERTIES);
    }
    public TVBoxBlock(BlockBehaviour.Properties p) {}

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public DirectionProperty getFacing() {
        return BlockStateProperties.HORIZONTAL_FACING;
    }

    public static AlignedBox box(Direction direction, boolean renderMode) {
        if (!renderMode) return STATIC_BOX;

        Facing facing = Facing.get(direction.getOpposite());
        var box = new AlignedBox();

        // fit
        if (facing.positive) {
            box.setMin(facing.axis, (1f / 16f));
        } else {
            box.setMax(facing.axis, (15f / 16f));
        }

        Axis one = facing.one();
        Axis two = facing.two();

        if (facing.axis != Axis.Z) {
            one = facing.two();
            two = facing.one();
        }

        // fit height
        box.setMin(two, 6f / 16f);
        box.setMax(two, 14f / 16f);

        // fit width
        box.setMin(one,  2f / 16f);
        box.setMax(one, 14f / 16f);

        return box;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction current = context.getHorizontalDirection();
        Player player = context.getPlayer();
        return super.getStateForPlacement(context)
                .setValue(this.getFacing(), player != null && player.isCrouching() ? current : current.getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.block();
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isCollisionShapeFullBlock(level, pos) ? 0.2F : 1.0F;
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TVBoxTile(pos, state);
    }
}
