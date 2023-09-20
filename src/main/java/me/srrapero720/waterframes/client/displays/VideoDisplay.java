package me.srrapero720.waterframes.client.displays;

import com.mojang.blaze3d.platform.MemoryTracker;
import me.srrapero720.waterframes.api.DataBlock;
import me.srrapero720.waterframes.core.tools.TimerTool;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class VideoDisplay extends IDisplay {
    private static final List<VideoDisplay> DISPLAYS = new ArrayList<>();
    private static final int SYNC_TIME = 1500;

    public SyncVideoPlayer videoPlayer;
    private final Vec3d pos;
    private float lastSetVolume = 0;
    private long lastCorrectedTime = Long.MIN_VALUE;
    private boolean stream = false;
    public VideoDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop, boolean playing) {
        super();
        this.pos = pos;
        this.videoPlayer = new SyncVideoPlayer(null, Minecraft.getInstance(), MemoryTracker::create);

        lastSetVolume = getVolume(volume, minDistance, maxDistance);
        videoPlayer.setVolume((int) lastSetVolume);
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
        return (int) (volume * 100F);
    }

    @Override
    public int maxTick() {
        if (videoPlayer != null) return WaterMediaAPI.math_millisToTicks(videoPlayer.getDuration());
        return 0;
    }

    @Override
    public void tick(DataBlock data) {
        if (videoPlayer == null) return;

        var volume = getVolume(data.volume, data.minVolumeDistance, data.maxVolumeDistance);
        if (volume != lastSetVolume) {
            videoPlayer.setVolume(volume);
            lastSetVolume = volume;
        }

        if (videoPlayer.isValid()) {
            boolean realPlaying = data.playing && !Minecraft.getInstance().isPaused();

            if (videoPlayer.getRepeatMode() != data.loop) videoPlayer.setRepeatMode(data.loop);
            long tickTime = 50;
            long currentDuration = videoPlayer.getMediaInfoDuration();

            if (!stream && currentDuration != -1 && currentDuration != 0 && videoPlayer.getMediaInfoDuration() == 0) stream = true;
            if (!stream && videoPlayer.isLive()) stream = true;

            if (stream) {
                if (videoPlayer.isPlaying() != realPlaying) videoPlayer.setPauseMode(!realPlaying);
            } else {
                if (currentDuration > 0) {
                    if (videoPlayer.isPlaying() != realPlaying) videoPlayer.setPauseMode(!realPlaying);

                    if (videoPlayer.isSeekAble()) {
                        long time = data.tick * tickTime + (realPlaying ? (long) (TimerTool.deltaFrames() * tickTime) : 0);
                        if (time > videoPlayer.getTime() && data.loop) time %= currentDuration;

                        if (Math.abs(time - videoPlayer.getTime()) > SYNC_TIME && Math.abs(time - lastCorrectedTime) > SYNC_TIME) {
                            lastCorrectedTime = time;
                            videoPlayer.seekTo(time);
                        }
                    }
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
    public void pause(DataBlock data) {
        if (videoPlayer == null) return;
        videoPlayer.seekTo(WaterMediaAPI.math_ticksToMillis(data.tick));
        videoPlayer.pause();
    }

    @Override
    public void resume(DataBlock data) {
        if (videoPlayer == null) return;
        videoPlayer.seekTo(WaterMediaAPI.math_ticksToMillis(data.tick));
        videoPlayer.play();
    }

    @Override public int width() { return videoPlayer.getWidth(); }
    @Override public int height() { return videoPlayer.getHeight(); }
    @Override public void prepare(DataBlock data) { videoPlayer.prepareTexture(); }

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