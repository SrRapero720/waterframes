package me.srrapero720.waterframes.api.display;

import me.srrapero720.waterframes.api.data.BasicData;
import me.srrapero720.waterframes.core.WaterNet;
import me.srrapero720.waterframes.core.tools.list.DisplayArray;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.math.vec.Vec3d;

public abstract class IDisplay {
    public static final DisplayArray ACTIVE = new DisplayArray();
    public static final int SYNC_TIME = 1500;
    public static boolean IS_PAUSED = false;

    public static IDisplay create(Vec3d pos, BasicData data) {
        VideoDisplay display = new VideoDisplay(pos, data);
        if (display.player.raw() != null) {
            ACTIVE.add(display);
            return display;
        }
        return TextureDisplay.VIDEO_FAILED;
    }

    public static void tick() {
        boolean paused = Minecraft.getInstance().isPaused();
        if (IS_PAUSED != paused && (IS_PAUSED = paused)) {
            synchronized (ACTIVE) {
                ACTIVE.pauseAll();
            }
        }
    }

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

    // ACTION
    public void syncDuration(BlockPos pos) { WaterNet.syncMaxTickTime(pos, durationInTicks()); }
    public abstract void pause();
    public abstract void resume();
    public abstract void stop();
    public abstract void release();
}