package me.srrapero720.waterframes.common.util.geo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

/**
 * Mutable axis-aligned box in float precision, indexable by {@link Axis} and {@link Facing}.
 * Vanilla's {@link AABB} is immutable and double precision, so displays that rebuild their
 * box every frame would allocate on each edit; this is the mutable companion and converts
 * back to vanilla on demand.
 */
public final class AlignedBox {
    public float minX, minY, minZ;
    public float maxX, maxY, maxZ;

    /** Unit cube, the starting point of every block box in this mod. */
    public AlignedBox() {
        this(0f, 0f, 0f, 1f, 1f, 1f);
    }

    public AlignedBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public AlignedBox(AlignedBox other) {
        this(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    public float min(Axis axis) {
        return switch (axis) {
            case X -> minX;
            case Y -> minY;
            case Z -> minZ;
        };
    }

    public void min(Axis axis, float value) {
        switch (axis) {
            case X -> minX = value;
            case Y -> minY = value;
            case Z -> minZ = value;
        }
    }

    public float max(Axis axis) {
        return switch (axis) {
            case X -> maxX;
            case Y -> maxY;
            case Z -> maxZ;
        };
    }

    public void max(Axis axis, float value) {
        switch (axis) {
            case X -> maxX = value;
            case Y -> maxY = value;
            case Z -> maxZ = value;
        }
    }

    /** Coordinate of the plane this face lies on: the max side for positive facings, min otherwise. */
    public float edge(Facing facing) {
        return facing.positive ? max(facing.axis) : min(facing.axis);
    }

    public float size(Axis axis) {
        return max(axis) - min(axis);
    }

    public Vector3f corner(BoxCorner corner) {
        return new Vector3f(edge(corner.x), edge(corner.y), edge(corner.z));
    }

    /** Scales every component around the block origin, not around the box center. */
    public void scale(float factor) {
        minX *= factor;
        minY *= factor;
        minZ *= factor;
        maxX *= factor;
        maxY *= factor;
        maxZ *= factor;
    }

    public AABB aabb() {
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public AABB aabb(BlockPos pos) {
        return new AABB(minX + pos.getX(), minY + pos.getY(), minZ + pos.getZ(),
                maxX + pos.getX(), maxY + pos.getY(), maxZ + pos.getZ());
    }

    /** Boxes here routinely exceed the unit cube; Shapes.box keeps them unclamped. */
    public VoxelShape shape() {
        return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public String toString() {
        return "AlignedBox[" + minX + ", " + minY + ", " + minZ + " -> " + maxX + ", " + maxY + ", " + maxZ + "]";
    }
}
