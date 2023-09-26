package me.srrapero720.waterframes.api.displays;

import com.mojang.blaze3d.platform.MemoryTracker;
import me.srrapero720.waterframes.api.data.BasicData;
import me.srrapero720.waterframes.core.WaterNet;
import me.srrapero720.waterframes.core.tools.MathTool;
import me.srrapero720.waterframes.core.tools.TimerTool;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class VideoDisplay extends BaseDisplay {
    private static final List<VideoDisplay> DISPLAYS = new ArrayList<>();
    private static final int SYNC_TIME = 1500;

    public SyncVideoPlayer videoPlayer;
    private Vec3d pos;
    private final AtomicInteger currentVolumen = new AtomicInteger(0);
    private long lastCorrectedTime = Long.MIN_VALUE;
    private boolean stream = false;

    public VideoDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop, boolean playing) {
        this.pos = pos;
        this.videoPlayer = new SyncVideoPlayer(null, Minecraft.getInstance(), MemoryTracker::create);

        currentVolumen.set(getVolume(volume, minDistance, maxDistance));
        videoPlayer.setVolume(currentVolumen.get());
        videoPlayer.setRepeatMode(loop);
        videoPlayer.setPauseMode(!playing);
        videoPlayer.start(url);
        DISPLAYS.add(this);
    }

    public int getVolume(float volume, float minDistance, float maxDistance) {
        if (videoPlayer == null) return 0;
        assert Minecraft.getInstance().player != null;
        float distance = (float) pos.distance(Minecraft.getInstance().player.getPosition(TimerTool.deltaFrames()));
        if (minDistance > maxDistance) {
            float temp = maxDistance;
            maxDistance = minDistance;
            minDistance = temp;
        }

        if (distance > minDistance)
            volume = (distance > maxDistance) ? 0 : volume * (1 - ((distance - minDistance) / (maxDistance - minDistance)));
        return (int) (volume);
    }

    @Override
    public int maxTick() {
        if (videoPlayer != null) return WaterMediaAPI.math_millisToTicks(videoPlayer.getDuration());
        return 0;
    }

    public void syncTick() {
        if (syncTick) return;
        WaterNet.syncMaxTickTime(pos.toBlockPos(), maxTick());
        syncTick = true;
    }

    @Override
    public void tick(BlockPos pos, BasicData data) {
        if (videoPlayer == null) return;
        this.pos = new Vec3d(pos);

        this.syncTick();
        data.tickMax = maxTick();

        int volume = getVolume(data.volume, data.minVolumeDistance, data.maxVolumeDistance);
        if (!currentVolumen.compareAndSet(volume, volume)) videoPlayer.setVolume(volume);

        if (videoPlayer.isValid()) {
            if (videoPlayer.getRepeatMode() != data.loop) videoPlayer.setRepeatMode(data.loop);
            if (!stream && videoPlayer.isLive()) stream = true;

            boolean realPlaying = data.playing && !Minecraft.getInstance().isPaused();

            videoPlayer.setPauseMode(!realPlaying);
            if (!stream && videoPlayer.isSeekAble()) {
                long time = WaterMediaAPI.math_ticksToMillis(data.tick) + (realPlaying ? WaterMediaAPI.math_ticksToMillis((int) TimerTool.deltaFrames()) : 0);
                if (time > videoPlayer.getTime() && data.loop) time = MathTool.floorMod(time, videoPlayer.getMediaInfoDuration());

                if (Math.abs(time - videoPlayer.getTime()) > SYNC_TIME && Math.abs(time - lastCorrectedTime) > SYNC_TIME) {
                    lastCorrectedTime = time;
                    videoPlayer.seekTo(time);
                }
            }
        }
    }

    @Override
    public boolean canTick() {
        return videoPlayer.isSafeUse();
    }

    @Override
    public int texture() {
        return videoPlayer.getTexture();
    }

    @Override
    public void pause(BasicData data) {
        if (videoPlayer == null) return;
        videoPlayer.seekTo(WaterMediaAPI.math_ticksToMillis(data.tick));
        videoPlayer.pause();
    }

    @Override
    public void resume(BasicData data) {
        if (videoPlayer == null) return;
        videoPlayer.seekTo(WaterMediaAPI.math_ticksToMillis(data.tick));
        videoPlayer.play();
    }

    @Override public int width() { return videoPlayer.getWidth(); }
    @Override public int height() { return videoPlayer.getHeight(); }
    @Override public void prepare(BasicData data) { videoPlayer.prepareTexture(); }

    public void cleanup() {
        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer.release();
        }
        videoPlayer = null;
    }

    @Override
    public void release() {
        this.cleanup();
        synchronized (DISPLAYS) {
            DISPLAYS.remove(this);
        }
    }

    public static void tick() {
        if (Minecraft.getInstance().isPaused()) {
            synchronized (DISPLAYS) {
                for (VideoDisplay display : DISPLAYS) {
                    SyncVideoPlayer player = display.videoPlayer;
                    if (player != null && player.isSafeUse() && player.isPlaying()) player.setPauseMode(true);
                }
            }
        }
    }

    public static void unload() {
        synchronized (DISPLAYS) {
            DISPLAYS.forEach(VideoDisplay::cleanup);
        }
    }
}