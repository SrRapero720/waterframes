package me.srrapero720.waterframes.common.block.data;

import com.mojang.serialization.Codec;
import me.srrapero720.waterframes.DisplaysConfig;
import me.srrapero720.waterframes.DisplaysRegistry;
import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.util.geo.AlignedBox;
import me.srrapero720.waterframes.common.util.geo.Axis;
import me.srrapero720.waterframes.common.util.geo.Facing;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.util.Locale;

/**
 * Everything that tells one display kind apart from another: which render paths it takes, which
 * data it keeps, how it sits in the world and the geometry of its picture and hitbox. The block
 * and the tile are shared by every kind and read all of it from here.
 */
public enum DisplayCaps implements StringRepresentable {
    FRAME(true, false, true, true, false, false, 0.001f, BlockStateProperties.FACING),
    PROJECTOR(false, true, true, true, false, false, 0.999f, BlockStateProperties.HORIZONTAL_FACING),
    TV(false, false, false, false, false, true, 0.001f, BlockStateProperties.HORIZONTAL_FACING),
    BIG_TV(false, false, false, false, false, true, 0.001f, BlockStateProperties.HORIZONTAL_FACING),
    TV_BOX(false, false, false, false, true, true, 0.001f, BlockStateProperties.HORIZONTAL_FACING);

    public static final DisplayCaps[] VALUES = values();
    public static final Codec<DisplayCaps> CODEC = StringRepresentable.fromEnum(() -> VALUES);

    // FRAME GEOMETRY: HALF A PIXEL THICK, THE PICTURE SITS ON ITS FACE
    public static final float FRAME_THICKNESS = 0.0625f / 2f;

    private final boolean renderBehind;
    private final boolean projects;
    private final boolean resizes;
    private final boolean hideModel;
    private final boolean solid;
    private final boolean flipPlacement;
    private final float growSize;
    private final DirectionProperty facing;

    DisplayCaps(final boolean renderBehind, final boolean projects, final boolean resizes, final boolean hideModel,
                final boolean solid, final boolean flipPlacement, final float growSize, final DirectionProperty facing) {
        this.renderBehind = renderBehind;
        this.projects = projects;
        this.resizes = resizes;
        this.hideModel = hideModel;
        this.solid = solid;
        this.flipPlacement = flipPlacement;
        this.growSize = growSize;
        this.facing = facing;
    }

    // ==========================================================================
    // PUBLIC API: FLAGS
    // ==========================================================================

    /** Whether the picture may be drawn on both faces. */
    public boolean renderBehind() { return this.renderBehind; }
    /** Whether the picture is thrown away from the block instead of sitting on it. */
    public boolean projects() { return this.projects; }
    /** Whether the player can size, anchor and rotate the picture. */
    public boolean resizes() { return this.resizes; }
    /** Whether the block model can be switched off, leaving only the picture. */
    public boolean hideModel() { return this.hideModel; }
    /** Whether the block behaves as a full cube (pistons, shading, collisions). */
    public boolean solid() { return this.solid; }
    /** Whether the block faces the player when placed instead of looking away from them. */
    public boolean flipPlacement() { return this.flipPlacement; }
    /** How far the render box is pushed off the block along its facing axis. */
    public float growSize() { return this.growSize; }
    /** The block state property holding the facing: six directions for frames, four for the rest. */
    public DirectionProperty facing() { return this.facing; }

    /** Permission node gating the screen of this kind. */
    public PermissionNode<Boolean> permission() {
        return switch (this) {
            case FRAME -> DisplaysRegistry.PERM_DISPLAYS_INTERACT_FRAME;
            case PROJECTOR -> DisplaysRegistry.PERM_DISPLAYS_INTERACT_PROJECTOR;
            case TV, BIG_TV, TV_BOX -> DisplaysRegistry.PERM_DISPLAYS_INTERACT_TV;
        };
    }

    /** Projection distance a freshly placed display of this kind starts with. */
    public float projectionDefault() {
        return switch (this) {
            case FRAME -> FRAME_THICKNESS;
            case PROJECTOR -> DisplaysConfig.maxProjDis(8f);
            case TV, BIG_TV, TV_BOX -> 0f;
        };
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    // ==========================================================================
    // PUBLIC API: GEOMETRY
    // ==========================================================================

    /** Whether the picture of this tile faces away from the block's own facing. Only a TV hung on its back does. */
    public boolean invertedFace(final DisplayTile tile) {
        return this == TV && tile.getAttachedFace().getOpposite() == tile.getDirection();
    }

    /** Which side of the render box the thickness grows towards, so the picture leaves the model instead of sinking in. */
    public boolean growMax(final DisplayTile tile, final Facing facing, final boolean invertedFace) {
        if (this == TV) return (facing.positive == invertedFace) == (tile.getAttachedFace().getOpposite() == tile.getDirection());
        return facing.positive != invertedFace;
    }

    /** Box the picture is drawn in, in block space. */
    public AlignedBox box(final DisplayTile tile, final Direction direction, final Direction attachedFace) {
        return switch (this) {
            case FRAME, PROJECTOR -> basicBox(tile);
            case TV -> tvBox(direction, attachedFace, true);
            case BIG_TV -> bigTvBox(direction, attachedFace, true);
            case TV_BOX -> tvBoxBox(direction);
        };
    }

    /** Hitbox of the block in this state. */
    public VoxelShape shape(final BlockState state) {
        final Direction direction = state.getValue(this.facing);
        return switch (this) {
            case FRAME -> {
                final Facing facing = Facing.of(direction);
                final AlignedBox box = new AlignedBox();
                if (facing.positive) box.max(facing.axis, FRAME_THICKNESS);
                else box.min(facing.axis, 1 - FRAME_THICKNESS);
                yield box.shape();
            }
            case PROJECTOR -> {
                // HALF A BLOCK TALL, A FOUR PIXEL BODY ALONG THE FACING AXIS AND A ONE PIXEL RIM SIDEWAYS
                final Facing facing = Facing.of(direction);
                final AlignedBox box = new AlignedBox();
                box.maxY = 8f / 16f;
                box.min(facing.axis, 4f / 16f);
                box.max(facing.axis, 12f / 16f);
                final Facing clockWise = Facing.of(direction.getClockWise());
                box.min(clockWise.axis, 1f / 16f);
                box.max(clockWise.axis, 15f / 16f);
                yield box.shape();
            }
            case TV -> tvBox(direction, state.getValue(DisplayBlock.ATTACHED_FACE), false).shape();
            case BIG_TV -> bigTvBox(direction, state.getValue(DisplayBlock.ATTACHED_FACE), false).shape();
            case TV_BOX -> Shapes.block();
        };
    }

    // ==========================================================================
    // UTILITY: BOXES
    // ==========================================================================

    /** The frame and projector picture: the display data sizes it, the facing throws it. */
    public static AlignedBox basicBox(final DisplayTile tile) {
        final Facing facing = Facing.of(tile.getDirection());
        final AlignedBox box = new AlignedBox();

        if (facing.positive) box.max(facing.axis, tile.data.projectionDistance);
        else box.min(facing.axis, 1f - tile.data.projectionDistance);

        final Axis one = facing.axis != Axis.Z ? facing.two() : facing.one();
        final Axis two = facing.axis != Axis.Z ? facing.one() : facing.two();

        box.min(one, tile.data.min.x);
        box.max(one, tile.data.max.x);
        box.min(two, tile.data.min.y);
        box.max(two, tile.data.max.y);

        // A PROJECTION IS SEEN FROM BEHIND AND A FRAME FROM THE FRONT, SO LEFT AND RIGHT SWAP SIDES
        // ON OPPOSITE HALVES OF THE COMPASS FOR EACH OF THEM
        final boolean mirrored = tile.caps.projects()
                ? facing == Facing.NORTH || facing == Facing.EAST
                : facing == Facing.WEST || facing == Facing.SOUTH;
        if (mirrored) {
            switch (tile.data.getPosX()) {
                case LEFT -> {
                    box.min(one, 1 - tile.data.getWidth());
                    box.max(one, 1);
                }
                case RIGHT -> {
                    box.max(one, tile.data.getWidth());
                    box.min(one, 0f);
                }
                case CENTER -> { }
            }
        }
        return box;
    }

    // THE TV SCREEN: ITS DEPTH, HEIGHT AND WIDTH DEPEND ON WHICH FACE IT HANGS FROM; renderMode SHRINKS
    // THE PICTURE A PIXEL INSIDE THE BEZEL, THE HITBOX IS THE WHOLE SET SLIGHTLY INFLATED
    private static AlignedBox tvBox(final Direction direction, final Direction attachedFace, final boolean renderMode) {
        final Facing facing = Facing.of(direction.getOpposite());
        final Facing wide = Facing.of(attachedFace);
        final AlignedBox box = new AlignedBox();
        final float margin = renderMode ? 1f : 0;

        // DEPTH
        if (attachedFace == direction) {
            if (facing.positive) {
                box.max(facing.axis, 1f - (4f / 16f));
                box.min(facing.axis, 1f - (6f / 16f));
            } else {
                box.max(facing.axis, 6f / 16f);
                box.min(facing.axis, 4f / 16f);
            }
        } else if (attachedFace.getOpposite() == direction) {
            if (facing.positive) {
                box.max(facing.axis, 1f - (1f / 16f));
                box.min(facing.axis, 1f - (3f / 16f));
            } else {
                box.max(facing.axis, 3f / 16f);
                box.min(facing.axis, 1f / 16f);
            }
        } else if (attachedFace == Direction.UP) {
            if (facing.positive) {
                box.max(facing.axis, 6f / 16f);
                box.min(facing.axis, 4f / 16f);
            } else {
                box.max(facing.axis, 1f - (4f / 16f));
                box.min(facing.axis, 1f - (6f / 16f));
            }
        } else {
            if (facing.positive) {
                box.max(facing.axis, -1f / 16f);
                box.min(facing.axis, -3f / 16f);
            } else {
                box.max(facing.axis, 1f - (-3f / 16f));
                box.min(facing.axis, 1f - (-1f / 16f));
            }
        }

        final Axis one = facing.axis != Axis.Z ? facing.two() : facing.one();
        final Axis two = facing.axis != Axis.Z ? facing.one() : facing.two();

        // HEIGHT
        if (attachedFace == Direction.DOWN) {
            box.max(two, (12f - margin) / 16f);
            box.min(two, -((9f - margin) / 16f));
        } else if (attachedFace == Direction.UP) {
            box.max(two, (24f - margin) / 16f);
            box.min(two, (3f + margin) / 16f);
        } else {
            box.max(two, (19f - margin) / 16f);
            box.min(two, (-2f + margin) / 16f);
        }

        // WIDTH
        if (attachedFace.getAxis().isVertical() || attachedFace.getAxis() == direction.getAxis()) {
            box.max(one, (25f - margin) / 16f);
            box.min(one, 1 - ((25f - margin) / 16f));
        } else if (wide.positive) {
            box.max(one, (32f - margin) / 16f);
            box.min(one, (-2f + margin) / 16f);
        } else {
            box.max(one, (18f - margin) / 16f);
            box.min(one, -((16f - margin) / 16f));
        }

        if (!renderMode) box.scale(1.01f);
        return box;
    }

    // THE BIG TV: TWO BLOCKS TALL, THREE WIDE, HUNG FROM ITS BACK OR FROM THE FLOOR
    private static AlignedBox bigTvBox(final Direction direction, final Direction attachedFace, final boolean renderMode) {
        final Facing facing = Facing.of(direction.getOpposite());
        final AlignedBox box = new AlignedBox();
        final float margin = renderMode ? 1f : 0;

        if (facing.positive) {
            box.max(facing.axis, 4f / 16f);
            box.min(facing.axis, 2f / 16f);
        } else {
            box.max(facing.axis, 1f - (2f / 16f));
            box.min(facing.axis, 1f - (4f / 16f));
        }

        final Axis one = facing.axis != Axis.Z ? facing.two() : facing.one();
        final Axis two = facing.axis != Axis.Z ? facing.one() : facing.two();

        if (direction == attachedFace) {
            box.min(two, margin / 16f);
            box.max(two, 2 - (2f / 16f) - (margin / 16f));
        } else {
            box.min(two, (2f + margin) / 16f);
            box.max(two, 2 - (margin / 16f));
        }

        box.min(one, (-14f + margin) / 16f);
        box.max(one, (30f - margin) / 16f);
        return box;
    }

    // THE TV BOX SCREEN: A WINDOW ON THE FRONT FACE OF A FULL CUBE
    private static AlignedBox tvBoxBox(final Direction direction) {
        final Facing facing = Facing.of(direction.getOpposite());
        final AlignedBox box = new AlignedBox();

        if (facing.positive) box.min(facing.axis, 1f / 16f);
        else box.max(facing.axis, 15f / 16f);

        final Axis one = facing.axis != Axis.Z ? facing.two() : facing.one();
        final Axis two = facing.axis != Axis.Z ? facing.one() : facing.two();

        box.min(two, 6f / 16f);
        box.max(two, 14f / 16f);
        box.min(one, 2f / 16f);
        box.max(one, 14f / 16f);
        return box;
    }
}
