package me.srrapero720.waterframes.common.compat.sable;

import dev.ryanhcode.sable.companion.SableCompanion;
import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SableCompat {
    public static final boolean SABLE_MODE = WaterFrames.isInstalled("sable");

    public static boolean installed() {
        return SABLE_MODE;
    }

    public static double getSquaredDistance(Level level, BlockPos pos, Position pos2) {
        return getSquaredDistance(level, Vec3.atCenterOf(pos), pos2);
    }

    public static double getSquaredDistance(Level level, Vec3 pos, Position pos2) {
        return SableCompanion.INSTANCE.distanceSquaredWithSubLevels(level, pos, pos2);
    }
}
