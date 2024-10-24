package me.srrapero720.waterframes.client.display;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.srrapero720.waterframes.*;
import me.srrapero720.waterframes.client.rendering.TextureWrapper;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import org.watermedia.api.image.ImageCache;
import org.watermedia.api.math.MathAPI;
import org.watermedia.api.network.NetworkAPI;
import org.watermedia.api.player.videolan.VideoPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.net.URI;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class Display {
    private static final Marker IT = MarkerManager.getMarker("Display");
    private static final Int2ObjectOpenHashMap<ResourceLocation> TEXTURES = new Int2ObjectOpenHashMap<>();

    // MEDIA AND DATA
    private VideoPlayer mediaPlayer;
    private final ImageCache imageCache;
    private final DisplayTile tile;
    private boolean notVideo;

    // CONFIG
    private int currentVolume = 0;
    private long currentLastTime = Long.MIN_VALUE;
    private Mode displayMode = Mode.PICTURE;
    private boolean stream = false;
    private boolean synced = false;
    private boolean released = false;

    public Display(DisplayTile tile) {
        this.tile = tile;
        this.imageCache = tile.imageCache;
        if (this.imageCache.isVideo()) this.switchVideoMode();
        else this.imageCache.addReleaseCallback(renderer -> {
            for (int tex: renderer.textures) {
                WFRegistry.unregisterTexture(TEXTURES.remove(tex));
            }
        });
    }

    private void switchVideoMode() {
        // DO NOT USE VIDEOLAN IF I DONT WANT
        if (!WFConfig.useMultimedia()) {
            this.displayMode = Mode.PICTURE;
            return;
        }

        if (this.notVideo) {
            this.displayMode = Mode.PICTURE;
            return;
        }

        // START
        this.displayMode = Mode.VIDEO;
        this.mediaPlayer = new VideoPlayer(Minecraft.getInstance());

        // CHECK IF VLC CAN BE USED
        if (mediaPlayer.isBroken()) {
            this.displayMode = Mode.PICTURE;
            this.notVideo = true;
            return;
        }

        this.currentVolume = rangedVol(this.tile.data.volume, this.tile.data.minVolumeDistance, this.tile.data.maxVolumeDistance);

        // PLAYER CONFIG
        this.mediaPlayer.setVolume(this.currentVolume);
        this.mediaPlayer.setRepeatMode(this.tile.data.loop);
        this.mediaPlayer.setPauseMode(this.tile.data.paused);
        this.mediaPlayer.setMuteMode(this.tile.data.muted);
        this.mediaPlayer.start(this.tile.data.uri);
        DisplayList.add(this);
    }

    public int width() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer() != null ? this.imageCache.getRenderer().width : 1;
            case VIDEO -> this.mediaPlayer.width();
            case AUDIO -> 0;
        };
    }

    public int height() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer() != null ? this.imageCache.getRenderer().height : 1;
            case VIDEO -> this.mediaPlayer.height();
            case AUDIO -> 0;
        };
    }

    public int texture() {
        return switch (displayMode) {
            case PICTURE -> this.imageCache.getRenderer().texture(tile.data.tick, (!tile.data.paused ? MathAPI.tickToMs(WaterFrames.deltaFrames()) : 0), tile.data.loop);
            case VIDEO -> this.mediaPlayer.texture();
            case AUDIO -> 0;
        };
    }

    public void preRender() {
        switch (displayMode) {
            case PICTURE -> {}
            case VIDEO -> this.mediaPlayer.preRender();
        }
    }

    public ResourceLocation getTextureId() {
        int texture = texture();
        if (texture != -1) {
            return TEXTURES.computeIfAbsent(texture, (Function<Integer, ResourceLocation>) integer -> {
                var id = WaterFrames.asResource(texture);
                WFRegistry.registerTexture(id, new TextureWrapper(texture));
                return id;
            });
        }
        return null;
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
            case PICTURE -> this.imageCache.getStatus() == ImageCache.Status.READY && !this.imageCache.isVideo() && tile.data.active;
            case VIDEO -> this.mediaPlayer.isValid() && tile.data.active;
            case AUDIO -> false;
        };
    }

    public void syncDuration() {
        if (tile.data.tickMax == -1) tile.data.tick = 0;
        tile.syncTime(true, tile.data.tick, durationInTicks());
    }

    public void tick() {
        switch (this.displayMode) {
            case PICTURE -> {
                if (this.imageCache.isVideo() && !this.notVideo) switchVideoMode();
            }
            case VIDEO, AUDIO -> {
                int volume = rangedVol(this.tile.data.volume, this.tile.data.minVolumeDistance, this.tile.data.maxVolumeDistance);

                if (this.currentVolume != volume) this.mediaPlayer.setVolume(this.currentVolume = volume);
                if (this.mediaPlayer.isSafeUse() && this.mediaPlayer.isValid()) {
                    if (this.mediaPlayer.getRepeatMode() != tile.data.loop) this.mediaPlayer.setRepeatMode(tile.data.loop);
                    if (this.mediaPlayer.isMuted() != tile.data.muted) this.mediaPlayer.setMuteMode(tile.data.muted);
                    if (!this.stream && this.mediaPlayer.isLive()) this.stream = true;

                    boolean mayPause = tile.data.paused || !tile.data.active || Minecraft.getInstance().isPaused();

                    if (this.mediaPlayer.isPaused() != mayPause) this.mediaPlayer.setPauseMode(mayPause);
                    if (!this.stream && this.mediaPlayer.isSeekAble()) {
                        long time = MathAPI.tickToMs(tile.data.tick) + (!mayPause ? MathAPI.tickToMs(WaterFrames.deltaFrames()) : 0);
                        if (time > mediaPlayer.getTime() && tile.data.loop) {
                            long mediaDuration = mediaPlayer.getMediaInfoDuration();
                            time = (time == 0 || mediaDuration == 0) ? 0 : Math.floorMod(time, this.mediaPlayer.getMediaInfoDuration());
                        }

                        if (Math.abs(time - mediaPlayer.getTime()) > WaterFrames.SYNC_TIME && Math.abs(time - currentLastTime) > WaterFrames.SYNC_TIME) {
                            this.currentLastTime = time;
                            this.mediaPlayer.seekTo(time);
                        }
                    }
                }
            }
        }
        if (!this.synced && this.canRender()) {
            this.syncDuration();
            this.synced = true;
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
            case VIDEO, AUDIO -> this.mediaPlayer.isBuffering() || this.mediaPlayer.isLoading();
        };
    }

    public boolean isNotVideo() {
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
            case VIDEO, AUDIO -> this.mediaPlayer.isLoading();
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
        if (this.isReleased()) return;
        this.released = true;
        imageCache.deuse();
        switch (displayMode) {
            case PICTURE -> {}
            case VIDEO, AUDIO -> {
                mediaPlayer.release();
                WFRegistry.unregisterTexture(TEXTURES.remove(mediaPlayer.texture()));
                DisplayList.remove(this);
            }
        }
    }

    public int rangedVol(int volume, int min, int max) { // Min and Max distances
        double distance = WaterFrames.getDistance(
                tile.level,
                tile.getBlockPos().relative(tile.getDirection(), (int) tile.data.audioOffset),
                Minecraft.getInstance().player.getPosition(WaterFrames.deltaFrames())
        );

        if (min > max) {
            int temp = max;
            max = min;
            min = temp;
        }

        if (distance > min)
            volume = (distance > max + 1) ? 0 : (int) (volume * (1 - ((distance - min) / ((1 + max) - min))));

        if (WFConfig.useMasterVolume()) {
            volume = (int) (volume * (Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER)));
        }

        return volume;
    }

    public enum Mode {
        VIDEO, PICTURE, AUDIO;
    }
}