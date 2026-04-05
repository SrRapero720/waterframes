package me.srrapero720.waterframes.client.display;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.srrapero720.waterframes.*;
import me.srrapero720.waterframes.client.rendering.TextureWrapper;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import org.watermedia.api.media.MRL;
import org.watermedia.api.media.engines.ALEngine;
import org.watermedia.api.media.engines.GLEngine;
import org.watermedia.api.media.players.MediaPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.watermedia.api.util.MathUtil;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class Display {
    private static final Marker IT = MarkerManager.getMarker("Display");
    private static final Int2ObjectOpenHashMap<ResourceLocation> TEXTURES = new Int2ObjectOpenHashMap<>();

    // ENGINE BUILDERS - Configured once, built per-player
    private static final GLEngine.Builder GL_BUILDER = new GLEngine.Builder(Minecraft.getInstance().gameThread,
            Minecraft.getInstance())
            .setGenTexture(GlStateManager::_genTexture)
            .setBindTexture((target, tex) -> GlStateManager._bindTexture(tex))
            .setTexParameter(GlStateManager::_texParameter)
            .setPixelStore(GlStateManager::_pixelStore)
            .setDelTexture(GlStateManager::_deleteTexture);

    private static final ALEngine.Builder AL_BUILDER = new ALEngine.Builder();

    // MEDIA AND DATA
    private MediaPlayer mediaPlayer;
    private MRL.Source currentSource;
    private final DisplayTile tile;
    private boolean noEngine;

    // PLAYBACK STATE
    private int currentVolume = 0;
    private boolean stream = false;
    private boolean synced = false;
    private boolean released = false;

    public Display(DisplayTile tile) {
        this.tile = tile;
        DisplayList.add(this);
        this.openPlayer(0);
    }

    private void openPlayer(final int sourceIndex) {
        // Get the source from MRL
        MRL.Source[] sources = tile.mrl.sources();
        if (sources == null || sources.length == 0) {
            this.noEngine = true;
            WaterFrames.LOGGER.warn(IT, "No sources available in MRL");
            return;
        }

        // Select source (prefer video, fall back to first available)
        this.currentSource = sourceIndex < sources.length ? sources[sourceIndex] : sources[0];
        if (this.currentSource == null) {
            MRL.Source videoSource = tile.mrl.videoSource();
            MRL.Source imageSource = tile.mrl.imageSource();
            this.currentSource = videoSource != null ? videoSource : imageSource;
        }

        if (this.currentSource == null) {
            this.noEngine = true;
            WaterFrames.LOGGER.warn(IT, "No valid source found in MRL");
            return;
        }

        // Create player from MRL (uses source index internally)
        this.mediaPlayer = tile.mrl.createPlayer(
                sourceIndex,
                GL_BUILDER.build(),
                AL_BUILDER.build(),
                true
        );

        if (this.mediaPlayer == null) {
            this.noEngine = true;
            WaterFrames.LOGGER.warn(IT, "Failed to create media player for source {} - no engine available", sourceIndex);
            return;
        }

        // Initialize player state
        this.currentVolume = this.rangedVol(this.tile.data.volume, this.tile.data.minVolumeDistance, this.tile.data.maxVolumeDistance);
        this.mediaPlayer.volume(this.currentVolume);
        this.mediaPlayer.repeat(this.tile.data.loop);
        this.mediaPlayer.mute(this.tile.data.muted);

        if (this.tile.data.paused) {
            this.mediaPlayer.startPaused();
        } else {
            this.mediaPlayer.start();
        }

        WaterFrames.LOGGER.debug(IT, "Created media player for source type: {}", this.currentSource.type());
    }

    // =========================================================================
    // SOURCE/QUALITY ACCESS
    // =========================================================================

    /**
     * Gets the current source being played.
     */
    public MRL.Source getSource() {
        return this.currentSource;
    }

    /**
     * Gets the number of available sources in the MRL.
     */
    public int getSourceCount() {
        return tile.mrl != null ? tile.mrl.sourceCount() : 0;
    }

    /**
     * Checks if the current source is a video.
     */
    public boolean isVideo() {
        return this.currentSource != null && this.currentSource.isVideo();
    }

    /**
     * Checks if the current source is an image.
     */
    public boolean isImage() {
        return this.currentSource != null && this.currentSource.isImage();
    }

    // =========================================================================
    // MEDIA INFO
    // =========================================================================

    public int width() {
        return this.mediaPlayer != null ? this.mediaPlayer.width() : 0;
    }

    public int height() {
        return this.mediaPlayer != null ? this.mediaPlayer.height() : 0;
    }

    public int texture() {
        return this.mediaPlayer != null ? (int) this.mediaPlayer.texture() : -1;
    }

    public ResourceLocation textureId() {
        int texture = texture();
        if (texture != -1) {
            return TEXTURES.computeIfAbsent(texture, (Function<Integer, ResourceLocation>) integer -> {
                var id = WaterFrames.asResource(texture);
                DisplaysRegistry.registerTexture(id, new TextureWrapper(texture));
                return id;
            });
        }
        return null;
    }

    public int durationInTicks() {
        return MathUtil.msToTick(this.duration());
    }

    public long duration() {
        return this.mediaPlayer != null ? this.mediaPlayer.duration() : 0;
    }

    public boolean canTick() {
        return this.mediaPlayer != null && this.mediaPlayer.canPlay();
    }

    public boolean canRender() {
        return this.mediaPlayer != null && this.mediaPlayer.canPlay() && this.mediaPlayer.texture() != -1;
    }

    // =========================================================================
    // SEEKING - Optimized for Watermedia
    // =========================================================================

    /**
     * Syncs duration to tile data on first successful render.
     */
    public void syncDuration() {
        if (tile.data.tickMax == -1) tile.data.tick = 0;
        this.tile.syncTime(true, tile.data.tick, this.durationInTicks());
        this.synced = true;
    }

    /**
     * Forces a PRECISE seek to the current tile tick position.
     * Use this for user-initiated seeks where accuracy matters.
     * DISABLED: Seeking is completely disabled.
     */
    public void forceSeek() {
    }

    // =========================================================================
    // TICK LOOP
    // =========================================================================

    public void tick() {
        if (this.mediaPlayer == null || this.mediaPlayer.error()) return;

        // Handle ended state for non-looping media
        if (this.mediaPlayer.ended() && !tile.data.loop) {
            return;
        }

        // Wait for player to be ready
        if (!this.mediaPlayer.canPlay()) return;

        // Volume sync
        int volume = this.rangedVol(this.tile.data.volume, this.tile.data.minVolumeDistance, this.tile.data.maxVolumeDistance);
        if (this.currentVolume != volume) {
            this.mediaPlayer.volume(this.currentVolume = volume);
        }

        // Sync repeat/loop state
        if (this.mediaPlayer.repeat() != tile.data.loop) {
            this.mediaPlayer.repeat(tile.data.loop);
        }

        // Sync mute state
        if (this.mediaPlayer.mute() != tile.data.muted) {
            this.mediaPlayer.mute(tile.data.muted);
        }

        // Detect live source
        if (!this.stream && this.mediaPlayer.liveSource()) {
            this.stream = true;
        }

        // Determine if we should pause
        boolean shouldPause = tile.data.paused || !tile.data.active || Minecraft.getInstance().isPaused();

        // Sync pause state
        if (this.mediaPlayer.paused() != shouldPause) {
            this.mediaPlayer.pause(shouldPause);
        }

        // Sync duration on first render
        if (!this.synced && this.canRender()) {
            this.syncDuration();
        }
    }

    // =========================================================================
    // STATUS METHODS
    // =========================================================================

    public boolean isBuffering() {
        return this.mediaPlayer != null && this.mediaPlayer.buffering();
    }

    public boolean isBroken() {
        return this.mediaPlayer != null && this.mediaPlayer.error();
    }

    public boolean isNoEngine() {
        return this.noEngine || this.mediaPlayer == null;
    }

    public boolean isLoading() {
        if (this.tile.mrl.busy()) return true;
        return this.mediaPlayer != null && (this.mediaPlayer.loading() || this.mediaPlayer.waiting());
    }

    public boolean isPlaying() {
        return this.mediaPlayer != null && this.mediaPlayer.playing();
    }

    public boolean isPaused() {
        return this.mediaPlayer != null && this.mediaPlayer.paused();
    }

    public boolean isEnded() {
        return this.mediaPlayer != null && this.mediaPlayer.ended();
    }

    public boolean isStream() {
        return this.stream;
    }

    public MediaPlayer.Status status() {
        return this.mediaPlayer != null ? this.mediaPlayer.status() : MediaPlayer.Status.ERROR;
    }

    public boolean isReleased() {
        return released;
    }

    // =========================================================================
    // EXTERNAL CONTROL
    // =========================================================================

    public void setPauseMode(boolean pause) {
        if (this.mediaPlayer == null) return;
        this.mediaPlayer.pause(pause);
        this.mediaPlayer.mute(this.tile.data.muted);
    }

    public void setMuteMode(boolean mute) {
        if (this.mediaPlayer == null) return;
        this.mediaPlayer.mute(mute);
    }

    // =========================================================================
    // LIFECYCLE
    // =========================================================================

    public void release() {
        if (this.isReleased()) return;
        this.released = true;

        if (this.mediaPlayer != null) {
            int texture = (int) this.mediaPlayer.texture();
            this.mediaPlayer.release();
            if (texture != -1) {
                DisplaysRegistry.unregisterTexture(TEXTURES.remove(texture));
            }
        }

        this.currentSource = null;
        DisplayList.remove(this);
    }

    // =========================================================================
    // AUDIO UTILITIES
    // =========================================================================

    public int rangedVol(int volume, int min, int max) {
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

        if (DisplaysConfig.useMasterVolume()) {
            volume = (int) (volume * (Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER)));
        }

        return volume;
    }

    public enum Mode {
        VIDEO, PICTURE, AUDIO
    }
}
