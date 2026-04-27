package dev.ryanhcode.sable.companion;

import net.minecraft.core.Position;
import net.minecraft.world.level.Level;

public interface SableCompanion {
    SableCompanion INSTANCE = null;

    double distanceSquaredWithSubLevels(Level level, Position pos1, Position pos2);
}
