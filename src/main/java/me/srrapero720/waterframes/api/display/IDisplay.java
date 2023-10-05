package me.srrapero720.waterframes.api.display;

import me.srrapero720.waterframes.core.WaterNet;
import net.minecraft.core.BlockPos;

public interface IDisplay {
    // PROPERTIES
    public abstract int width();
    public abstract int height();
    public abstract int texture();
    public abstract int durationInTicks();
    public abstract long duration();

    // RENDERING
    public abstract void preRender();

    // TICK
    public abstract boolean canTick();
    public abstract void tick(BlockPos pos);

    // STATUS
    public abstract boolean isBuffering();
    public abstract boolean isLoading();
    public abstract boolean canRender();

    // ACTION
    public default void syncDuration(BlockPos pos) { WaterNet.syncMaxTickTime(pos, durationInTicks()); }
    public abstract void pause();
    public abstract void resume();
    public abstract void stop();
    public abstract void release(boolean quiet);
}