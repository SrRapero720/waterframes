package me.srrapero720.waterframes.api.display;

import me.srrapero720.waterframes.api.data.BasicData;
import me.srrapero720.waterframes.core.tools.MathTool;
import me.srrapero720.waterframes.core.tools.TimerTool;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageCache;
import me.srrapero720.watermedia.api.math.MathAPI;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MediaDisplay implements IDisplay {
    // MEDIA AND DATA
    private SyncVideoPlayer mediaPlayer;
    private ImageCache imageCache;
    private final BasicData blockData;

    // CONFIG
    private Vec3d blockPos;
    private final AtomicInteger currentVolume = new AtomicInteger(0);
    private final AtomicLong currentLastTime = new AtomicLong(Long.MIN_VALUE);
    private Mode displayMode = Mode.PICTURE;
    private boolean stream = false;

    public MediaDisplay(ImageCache cache, Vec3d blockPos, BasicData blockData) {
        this.imageCache = cache;
        this.blockPos = blockPos;
        this.blockData = blockData;
        if (cache.isVideo()) this.switchVideoMode();
    }

    private void switchVideoMode() {
        // START
        this.displayMode = Mode.VIDEO;
        this.mediaPlayer = new SyncVideoPlayer(Minecraft.getInstance());

        // CHECK IF VLC CAN BE USED
        if (mediaPlayer.isBroken()) {
            imageCache.deuse();
            imageCache = new ImageCache(ImageAPI.failedVLC());
            this.displayMode = Mode.PICTURE;
        }

        // PLAYER CONFIG
        this.currentVolume.set(MathTool.floorVolume(blockPos, blockData.volume, blockData.minVolumeDistance, blockData.maxVolumeDistance));
        this.mediaPlayer.setRepeatMode(blockData.loop);
        this.mediaPlayer.setPauseMode(!blockData.playing);
        this.mediaPlayer.start(blockData.url);
    }

    @Override
    public int width() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer().width;
            case VIDEO -> this.mediaPlayer.getWidth();
            case AUDIO -> 0;
        };
    }

    @Override
    public int height() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer().height;
            case VIDEO -> this.mediaPlayer.getHeight();
            case AUDIO -> 0;
        };
    }

    @Override
    public int texture() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer().texture(blockData.tick, (blockData.playing ? MathAPI.tickToMs(TimerTool.deltaFrames()) : 0), blockData.loop);
            case VIDEO -> this.mediaPlayer.getWidth();
            case AUDIO -> -1;
        };
    }

    @Override
    public int durationInTicks() {
        return MathAPI.msToTick(duration());
    }

    @Override
    public long duration() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer().duration;
            case VIDEO -> this.mediaPlayer.getDuration();
            case AUDIO -> 0;
        };
    }

    @Override
    public void preRender() {
        switch (displayMode) {
            case PICTURE -> {} // Deprecated feature
            case VIDEO -> this.mediaPlayer.preRender();
            case AUDIO -> {} // Why?
        };
    }

    @Override
    public boolean canTick() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getStatus().equals(ImageCache.Status.READY);
            case VIDEO -> this.mediaPlayer.isSafeUse();
            case AUDIO -> this.mediaPlayer.isSafeUse(); // MISSING IMPL
        };
    }

    @Override
    public boolean canRender() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer() != null;
            case VIDEO -> true;
            case AUDIO -> false;
        };
    }

    @Override
    public void tick(BlockPos pos) {
        switch (displayMode) {
            case PICTURE -> {
                if (imageCache.isVideo()) switchVideoMode();
            }
            case VIDEO -> {
                this.blockPos = new Vec3d(pos);

                int volume = MathTool.floorVolume(this.blockPos, this.blockData.volume, this.blockData.minVolumeDistance, this.blockData.maxVolumeDistance);
                if (!currentVolume.compareAndSet(volume, volume)) mediaPlayer.setVolume(volume);

                if (mediaPlayer.isSafeUse() && mediaPlayer.isValid()) {
                    if (mediaPlayer.getRepeatMode() != blockData.loop) mediaPlayer.setRepeatMode(blockData.loop);
                    if (!stream && mediaPlayer.isLive()) stream = true;

                    boolean currentPlaying = blockData.playing && !Minecraft.getInstance().isPaused();

                    mediaPlayer.setPauseMode(!currentPlaying);
                    if (!stream && mediaPlayer.isSeekAble()) {
                        long time = MathAPI.msToTick(blockData.tick) + (currentPlaying ? MathAPI.msToTick((int) TimerTool.deltaFrames()) : 0);
                        if (time > mediaPlayer.getTime() && blockData.loop) time = MathTool.floorMod(time, mediaPlayer.getMediaInfoDuration());

                        if (Math.abs(time - mediaPlayer.getTime()) > DisplayManager.SYNC_TIME && Math.abs(time - currentLastTime.get()) > DisplayManager.SYNC_TIME) {
                            currentLastTime.set(time);
                            mediaPlayer.seekTo(time);
                        }
                    }
                }
            }
            case AUDIO -> {
                // MISSING IMPL
            }
        }
    }

    @Override
    public boolean isBuffering() {
        return switch (displayMode) {
            case PICTURE -> false;
            case VIDEO, AUDIO -> this.mediaPlayer.isBuffering();
        };
    }

    @Override
    public boolean isLoading() {
        return imageCache.getStatus() == ImageCache.Status.LOADING;
    }

    @Override
    public void pause() {
        switch (displayMode) {
            case PICTURE -> {}
            case VIDEO, AUDIO -> {
                mediaPlayer.seekTo(MathAPI.tickToMs(this.blockData.tick));
                mediaPlayer.pause();
            }
        }
    }

    @Override
    public void resume() {
        switch (displayMode) {
            case PICTURE -> {}
            case VIDEO, AUDIO -> {
                mediaPlayer.seekTo(MathAPI.tickToMs(this.blockData.tick));
                mediaPlayer.play();
            }
        }
    }

    @Override
    public void stop() {
        switch (displayMode) {
            case PICTURE -> {}
            case VIDEO, AUDIO -> {
                mediaPlayer.seekTo(0);
                mediaPlayer.pause();
            }
        }
    }

    @Override
    public void release(boolean quiet) {
        switch (displayMode) {
            case PICTURE -> {
                if (imageCache != null && !quiet) imageCache.deuse();
            }
            case VIDEO, AUDIO -> mediaPlayer.release();
        }
    }


    public enum Mode {
        VIDEO,
        PICTURE,
        AUDIO;
    }
}