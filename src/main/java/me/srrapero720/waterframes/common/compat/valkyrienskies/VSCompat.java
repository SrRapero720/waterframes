package me.srrapero720.waterframes.common.compat.valkyrienskies;

import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class VSCompat {
    public static final boolean VS_MODE = WaterFrames.isInstalled("valkyrienskies");

    public static boolean installed() {
        return VS_MODE;
    }

    public static double getSquaredDistance(Level level, BlockPos pos, Position pos2) {
        return getSquaredDistance(level, Vec3.atCenterOf(pos), pos2);
    }

    public static double getSquaredDistance(Level level, Vec3 pos, Position pos2) {
        return VSGameUtilsKt.squaredDistanceBetweenInclShips(level, pos.x, pos.y, pos.z, pos2.x(), pos2.y(), pos2.z());
    }
}