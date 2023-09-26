package me.srrapero720.waterframes.api.display;

import com.mojang.blaze3d.platform.MemoryTracker;
import me.srrapero720.waterframes.api.data.BasicData;
import me.srrapero720.waterframes.core.tools.MathTool;
import me.srrapero720.waterframes.core.tools.TimerTool;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import java.util.concurrent.atomic.AtomicInteger;

public class VideoDisplay extends IDisplay {

    protected final SyncVideoPlayer player;
    private Vec3d vecPos;
    private final BasicData data;
    private final AtomicInteger currentVolume = new AtomicInteger(0);
    private long currentCorrectedTime = Long.MIN_VALUE;
    private boolean stream = false;

    VideoDisplay(Vec3d pos, BasicData data) {
        this.vecPos = pos;
        this.data = data;
        this.player = new SyncVideoPlayer(Minecraft.getInstance(), MemoryTracker::create);
        this.currentVolume.set(MathTool.floorVolume(pos, data.volume, data.minVolumeDistance, data.maxVolumeDistance));

        // PLAYER CONFIG
        this.player.setRepeatMode(data.loop);
        this.player.setPauseMode(!data.playing);
        this.player.start(data.url);
    }

    @Override
    public int width() {
        return this.player.getWidth();
    }

    @Override
    public int height() {
        return this.player.getHeight();
    }

    @Override
    public int texture() {
        return this.player.getTexture();
    }

    @Override
    public void preRender() {
        this.player.prepareTexture();
    }

    @Override
    public int durationInTicks() {
        return WaterMediaAPI.math_millisToTicks(duration());
    }

    @Override
    public long duration() {
        return player.getDuration();
    }

    @Override
    public boolean canTick() {
        return this.player.isSafeUse();
    }

    @Override
    public void tick(BlockPos pos) {
        this.vecPos = new Vec3d(pos);

        this.syncDuration(pos);
        data.tickMax = durationInTicks();

        int volume = MathTool.floorVolume(this.vecPos, this.data.volume, this.data.minVolumeDistance, this.data.maxVolumeDistance);
        if (!currentVolume.compareAndSet(volume, volume)) player.setVolume(volume);

        if (player.isSafeUse() && player.isValid()) {
            if (player.getRepeatMode() != data.loop) player.setRepeatMode(data.loop);
            if (!stream && player.isLive()) stream = true;

            boolean currentPlaying = data.playing && !Minecraft.getInstance().isPaused();

            player.setPauseMode(!currentPlaying);
            if (!stream && player.isSeekAble()) {
                long time = WaterMediaAPI.math_ticksToMillis(data.tick) + (currentPlaying ? WaterMediaAPI.math_ticksToMillis((int) TimerTool.deltaFrames()) : 0);
                if (time > player.getTime() && data.loop) time = MathTool.floorMod(time, player.getMediaInfoDuration());

                if (Math.abs(time - player.getTime()) > SYNC_TIME && Math.abs(time - currentCorrectedTime) > SYNC_TIME) {
                    currentCorrectedTime = time;
                    player.seekTo(time);
                }
            }
        }
    }

    @Override
    public boolean isBuffering() { return player.isBuffering(); }

    @Override
    public boolean isLoading() { return !player.isSafeUse(); }

    @Override
    public void pause() {
        player.seekTo(WaterMediaAPI.math_ticksToMillis(this.data.tick));
        player.pause();
    }

    @Override
    public void resume() {
        player.seekTo(WaterMediaAPI.math_ticksToMillis(this.data.tick));
        player.play();
    }

    @Override
    public void stop() {
        player.seekTo(0);
        player.pause();
    }

    @Override
    public void release() {
        player.release();
    }
}