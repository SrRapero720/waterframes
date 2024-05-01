package me.srrapero720.waterframes.client.display;

import me.lib720.caprica.vlcj.player.base.State;
import me.srrapero720.waterframes.*;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageCache;
import me.srrapero720.watermedia.api.math.MathAPI;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextureDisplay {
    private static final ImageCache VLC_NOT_FOUND = new ImageCache(ImageAPI.failedVLC());

    // MEDIA AND DATA
    private SyncVideoPlayer mediaPlayer;
    private ImageCache imageCache;
    private final DisplayTile tile;

    // CONFIG
    private int currentVolume = 0;
    private long currentLastTime = Long.MIN_VALUE;
    private Mode displayMode = Mode.PICTURE;
    private boolean stream = false;
    private boolean synced = false;
    private boolean released = false;

    public TextureDisplay(DisplayTile tile) {
        this.tile = tile;
        this.imageCache = tile.imageCache;
        if (this.imageCache.isVideo()) this.switchVideoMode();
    }

    private void switchVideoMode() {
        // DO NOT USE VIDEOLAN IF I DONT WANT
        if (!WFConfig.useMultimedia()) {
            this.imageCache = VLC_NOT_FOUND;
            this.displayMode = Mode.PICTURE;
            return;
        }

        // START
        this.displayMode = Mode.VIDEO;
        this.mediaPlayer = new SyncVideoPlayer(Minecraft.getInstance());

        // CHECK IF VLC CAN BE USED
        if (mediaPlayer.isBroken()) {
            this.imageCache = VLC_NOT_FOUND;
            this.displayMode = Mode.PICTURE;
            return;
        }

        this.currentVolume = rangedVol(this.tile.data.volume, this.tile.data.minVolumeDistance, this.tile.data.maxVolumeDistance);

        // PLAYER CONFIG
        this.mediaPlayer.setVolume(this.currentVolume);
        this.mediaPlayer.setRepeatMode(this.tile.data.loop);
        this.mediaPlayer.setPauseMode(this.tile.data.paused);
        this.mediaPlayer.setMuteMode(this.tile.data.muted);
        this.mediaPlayer.start(this.tile.data.url);
        DisplayControl.add(this);
    }

    public int width() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer() != null ? this.imageCache.getRenderer().width : 1;
            case VIDEO -> this.mediaPlayer.getWidth();
            case AUDIO -> 0;
        };
    }

    public int height() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer() != null ? this.imageCache.getRenderer().height : 1;
            case VIDEO -> this.mediaPlayer.getHeight();
            case AUDIO -> 0;
        };
    }

    public int texture() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer().texture(tile.data.tick, (!tile.data.paused ? MathAPI.tickToMs(WaterFrames.deltaFrames()) : 0), tile.data.loop);
            case VIDEO -> this.mediaPlayer.getGlTexture();
            case AUDIO -> -1;
        };
    }

    public int durationInTicks() {
        return MathAPI.msToTick(duration());
    }

    public long duration() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer() != null ? this.imageCache.getRenderer().duration : 0;
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
            case PICTURE -> this.imageCache.getRenderer() != null && tile.data.active;
            case VIDEO -> this.mediaPlayer.isValid() && tile.data.active;
            case AUDIO -> false;
        };
    }

    public void syncDuration() {
        if (tile.data.tickMax == -1) tile.data.tick = 0;
        tile.syncTime(true, tile.data.tick, durationInTicks());
    }

    public void tick() {
        if (!synced && canRender()) {
            syncDuration();
            synced = true;
        }
        switch (displayMode) {
            case PICTURE -> {
                if (imageCache.isVideo()) switchVideoMode();
            }
            case VIDEO, AUDIO -> {
                int volume = rangedVol(this.tile.data.volume, this.tile.data.minVolumeDistance, this.tile.data.maxVolumeDistance);

                if (currentVolume != volume) mediaPlayer.setVolume(currentVolume = volume);
                if (mediaPlayer.isSafeUse() && mediaPlayer.isValid()) {
                    if (mediaPlayer.getRepeatMode() != tile.data.loop) mediaPlayer.setRepeatMode(tile.data.loop);
                    if (mediaPlayer.isMuted() != tile.data.muted) mediaPlayer.setMuteMode(tile.data.muted);
                    if (!stream && mediaPlayer.isLive()) stream = true;

                    boolean mayPause = tile.data.paused || !tile.data.active || Minecraft.getInstance().isPaused();

                    if (mediaPlayer.isPaused() != mayPause) mediaPlayer.setPauseMode(mayPause);
                    if (!stream && mediaPlayer.isSeekAble()) {
                        long time = MathAPI.tickToMs(tile.data.tick) + (!mayPause ? MathAPI.tickToMs(WaterFrames.deltaFrames()) : 0);
                        if (time > mediaPlayer.getTime() && tile.data.loop) {
                            long mediaDuration = mediaPlayer.getMediaInfoDuration();
                            time = (time == 0 || mediaDuration == 0) ? 0 : Math.floorMod(time, mediaPlayer.getMediaInfoDuration());
                        }

                        if (Math.abs(time - mediaPlayer.getTime()) > WaterFrames.SYNC_TIME && Math.abs(time - currentLastTime) > WaterFrames.SYNC_TIME) {
                            currentLastTime = time;
                            mediaPlayer.seekTo(time);
                        }
                    }
                }
            }
        }
    }

    public boolean isReady() {
        if (this.imageCache.getStatus() != ImageCache.Status.READY) {
            return false;
        }
        return switch (displayMode) {
            case PICTURE -> true;
            case VIDEO, AUDIO -> this.imageCache.getStatus() == ImageCache.Status.READY && this.mediaPlayer.isReady();
        };
    }

    public boolean isBuffering() {
        return switch (displayMode) {
            case PICTURE -> false;
            case VIDEO, AUDIO -> this.mediaPlayer.isBuffering() || this.mediaPlayer.getRawPlayerState() == State.OPENING;
        };
    }

    public boolean isBroken() {
        if (this.imageCache.getStatus() == ImageCache.Status.FAILED)
            return true;

        return switch (displayMode) {
            case PICTURE -> false;
            case VIDEO, AUDIO -> this.mediaPlayer.isBroken();
        };
    }

    public boolean isLoading() {
        if (imageCache.getStatus() == ImageCache.Status.LOADING)
            return true;

        return switch (displayMode) {
            case PICTURE -> false;
            case VIDEO, AUDIO -> this.mediaPlayer.getRawPlayerState() == State.OPENING;
        };
    }

    public boolean isReleased() {
        return released;
    }

    public void setPauseMode(boolean pause) {
        switch (displayMode) {
            case PICTURE -> {}
            case VIDEO, AUDIO -> {
                mediaPlayer.seekTo(MathAPI.tickToMs(this.tile.data.tick));
                mediaPlayer.setPauseMode(pause);
                mediaPlayer.setMuteMode(this.tile.data.muted);
            }
        }
    }

    public void setMuteMode(boolean mute) {
        switch (displayMode) {
            case PICTURE -> {}
            case VIDEO, AUDIO -> mediaPlayer.setMuteMode(mute);
        }
    }

    public void release() {
        this.released = true;
        switch (displayMode) {
            case PICTURE -> {
                if (imageCache != null) imageCache.deuse();
            }
            case VIDEO, AUDIO -> {
                mediaPlayer.release();
                DisplayControl.remove(this);
            }
        }
    }

    public int rangedVol(int volume, int min, int max) { // Min and Max distances
        Player player = Minecraft.getInstance().player;
        double distance = Math.sqrt(tile.getBlockPos().relative(tile.getDirection(), (int) tile.data.audioOffset).distToCenterSqr(player.getPosition(WaterFrames.deltaFrames())));
        if (min > max) {
            int temp = max;
            max = min;
            min = temp;
        }

        if (distance > min)
            volume = (distance > max + 1) ? 0 : (int) (volume * (1 - ((distance - min) / ((1 + max) - min))));
        return volume;
    }

    public enum Mode {
        VIDEO, PICTURE, AUDIO;
    }
}