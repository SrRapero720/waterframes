package me.srrapero720.waterframes.client.display;

import me.lib720.caprica.vlcj.player.base.State;
import me.srrapero720.waterframes.common.block.ProjectorBlock;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.block.entity.ProjectorTile;
import me.srrapero720.waterframes.util.FrameNet;
import me.srrapero720.waterframes.util.FrameTools;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageCache;
import me.srrapero720.watermedia.api.math.MathAPI;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class TextureDisplay {
    // MEDIA AND DATA
    private SyncVideoPlayer mediaPlayer;
    private ImageCache imageCache;
    private final DisplayTile<?> block;

    // CONFIG
    private Vec3d blockPos;
    private int currentVolume = 0;
    private final AtomicLong currentLastTime = new AtomicLong(Long.MIN_VALUE);
    private Mode displayMode = Mode.PICTURE;
    private boolean stream = false;
    private boolean synced = false;

    public TextureDisplay(ImageCache cache, Vec3d blockPos, DisplayTile<?> block) {
        this.imageCache = cache;
        this.blockPos = blockPos;
        this.block = block;
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

        if (block instanceof ProjectorTile projector) {
            Direction direction = projector.getBlockState().getValue(ProjectorBlock.FACING);
            this.currentVolume = FrameTools.floorVolume(this.blockPos, direction, projector.data.audioOffset, this.block.data.volume, this.block.data.minVolumeDistance, this.block.data.maxVolumeDistance);
        } else {
            this.currentVolume = FrameTools.floorVolume(this.blockPos, this.block.data.volume, this.block.data.minVolumeDistance, this.block.data.maxVolumeDistance);
        }

        // PLAYER CONFIG
        this.mediaPlayer.setVolume(this.currentVolume);
        this.mediaPlayer.setRepeatMode(block.data.loop);
        this.mediaPlayer.setPauseMode(!block.data.playing);
        this.mediaPlayer.start(block.data.url);
        DisplayControl.add(this);
    }

    
    public int width() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer().width;
            case VIDEO -> this.mediaPlayer.getWidth();
            case AUDIO -> 0;
        };
    }

    
    public int height() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer().height;
            case VIDEO -> this.mediaPlayer.getHeight();
            case AUDIO -> 0;
        };
    }

    
    public int texture() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer().texture(block.data.tick, (block.data.playing ? MathAPI.tickToMs(FrameTools.deltaFrames()) : 0), block.data.loop);
            case VIDEO -> this.mediaPlayer.getGlTexture();
            case AUDIO -> -1;
        };
    }

    
    public int durationInTicks() {
        return MathAPI.msToTick(duration());
    }

    
    public long duration() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer().duration;
            case VIDEO -> this.mediaPlayer.getDuration();
            case AUDIO -> 0;
        };
    }

    
    public boolean canTick() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getStatus().equals(ImageCache.Status.READY);
            case VIDEO -> this.mediaPlayer.isSafeUse() && this.mediaPlayer.isValid();
            case AUDIO -> this.mediaPlayer.isSafeUse(); // MISSING IMPL
        };
    }

    
    public boolean canRender() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer() != null;
            case VIDEO -> this.mediaPlayer.isValid();
            case AUDIO -> false;
        };
    }

    
    public void syncDuration(BlockPos pos) {
        if (block.data.tickMax == -1) block.data.tick = 0;
        FrameNet.syncMaxTickTime(pos, durationInTicks());
    }

    
    public void tick(BlockPos pos) {
        if (!synced && canRender()) {
            syncDuration(pos);
            synced = true;
        }
        switch (displayMode) {
            case PICTURE -> {
                if (imageCache.isVideo()) switchVideoMode();
            }
            case VIDEO -> {
                this.blockPos = new Vec3d(pos);

                int volume;
                if (block instanceof ProjectorTile projectorTile) {
                    Direction direction = projectorTile.getBlockState().getValue(ProjectorBlock.FACING);
                    volume = FrameTools.floorVolume(this.blockPos, direction, projectorTile.data.audioOffset, this.block.data.volume, this.block.data.minVolumeDistance, this.block.data.maxVolumeDistance);
                } else {
                    volume = FrameTools.floorVolume(this.blockPos, this.block.data.volume, this.block.data.minVolumeDistance, this.block.data.maxVolumeDistance);
                }

                if (currentVolume != volume) mediaPlayer.setVolume(currentVolume = volume);
                if (mediaPlayer.isSafeUse() && mediaPlayer.isValid()) {
                    if (mediaPlayer.getRepeatMode() != block.data.loop) mediaPlayer.setRepeatMode(block.data.loop);
                    if (!stream && mediaPlayer.isLive()) stream = true;

                    boolean canPlay = block.data.playing && !Minecraft.getInstance().isPaused();

                    mediaPlayer.setPauseMode(!canPlay);
                    if (!stream && mediaPlayer.isSeekAble()) {
                        long time = MathAPI.tickToMs(block.data.tick) + (canPlay ? MathAPI.tickToMs(FrameTools.deltaFrames()) : 0);
                        if (time > mediaPlayer.getTime() && block.data.loop) time = FrameTools.floorMod(time, mediaPlayer.getMediaInfoDuration());

                        if (Math.abs(time - mediaPlayer.getTime()) > DisplayControl.SYNC_TIME && Math.abs(time - currentLastTime.get()) > DisplayControl.SYNC_TIME) {
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

    public State getPlayerStateIfExists() {
        return switch (displayMode) {
            case PICTURE -> State.NOTHING_SPECIAL;
            case VIDEO, AUDIO -> mediaPlayer.getRawPlayerState();
        };
    }

    
    public boolean isBuffering() {
        return switch (displayMode) {
            case PICTURE -> false;
            case VIDEO, AUDIO -> this.mediaPlayer.isBuffering() || this.mediaPlayer.getRawPlayerState() == State.OPENING;
        };
    }

    
    public boolean isLoading() {
        return imageCache.getStatus() == ImageCache.Status.LOADING;
    }

    
    public void pause() {
        switch (displayMode) {
            case PICTURE -> {}
            case VIDEO, AUDIO -> {
                mediaPlayer.seekTo(MathAPI.tickToMs(this.block.data.tick));
                mediaPlayer.setPauseMode(true);
            }
        }
    }

    
    public void resume() {
        switch (displayMode) {
            case PICTURE -> {}
            case VIDEO, AUDIO -> {
                mediaPlayer.seekTo(MathAPI.tickToMs(this.block.data.tick));
                mediaPlayer.setPauseMode(false);
            }
        }
    }

    
    public void stop() {
        switch (displayMode) {
            case PICTURE -> {}
            case VIDEO, AUDIO -> {
                mediaPlayer.seekTo(0);
                mediaPlayer.setPauseMode(true);
            }
        }
    }

    
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