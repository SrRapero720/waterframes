package me.srrapero720.waterframes.common.item.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Where a remote control points: a block position in a named dimension. */
public record RemoteData(String dimension, int x, int y, int z) {

    public RemoteData(final Level level, final BlockPos pos) {
        this(level.dimension().location().toString(), pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPos pos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    /** Whether this binding points at that very block of that very level. */
    public boolean matches(final Level level, final BlockPos pos) {
        return this.x == pos.getX() && this.y == pos.getY() && this.z == pos.getZ()
                && this.dimension.equals(level.dimension().location().toString());
    }
}
