package me.srrapero720.waterframes.client.display;

import me.srrapero720.waterframes.*;
import me.srrapero720.waterframes.client.rendering.TextureWrapper;
import me.srrapero720.waterframes.client.sound.DisplaySound;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.media.DisplayBridge;
import org.watermedia.api.media.MRL;
import org.watermedia.api.media.MediaAPI;
import org.watermedia.api.media.engines.GFXEngine;
import org.watermedia.api.media.engines.SFXEngine;
import org.watermedia.api.media.players.MediaPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.lwjgl.openal.AL10;
import org.watermedia.api.util.MediaType;

import java.util.function.Supplier;

public class Display {
    private static final Marker IT = MarkerManager.getMarker("Display");

    // ENGINE FACTORIES - ONE INSTANCE PER PLAYER; THE GL ONE IS PINNED TO MINECRAFT'S RENDER THREAD
    private static final Supplier<GFXEngine> GFX_ENGINE = () -> MediaAPI.glEngine(Minecraft.getInstance().gameThread, Minecraft.getInstance());
    private static final Supplier<SFXEngine> SFX_ENGINE = MediaAPI::alEngine;

    // MEDIA AND DATA
    private MediaPlayer mediaPlayer;
    private MRL.Source currentSource;
    private final DisplayTile tile;
    /** Which source of the media this player opened; the playlist entry decides it. */
    private final int sourceIndex;
    private boolean noEngine;

    // PLAYBACK STATE
    private int currentVolume = 0;
    private boolean released = false;

    // TEXTURE REGISTRATION, OWNED PER DISPLAY SO EVERY ID THIS VIEWER REGISTERS IS RELEASED WITH IT
    private int glTexture = -1;
    private ResourceLocation glLocation;

    // SOUND REGISTRATION, THE AL SOURCE ADOPTED BY MINECRAFT'S ENGINE THE WAY THE TEXTURE IS
    private DisplaySound sound;
    private int soundRetry;

    public Display(DisplayTile tile) {
        this.tile = tile;
        this.sourceIndex = tile.data.getSource();
        DisplayList.add(this);
        this.openPlayer();
    }

    private void openPlayer() {
        // THE PLAYLIST ENTRY NAMES THE SOURCE; ONE THAT IS NOT THERE ANYMORE PLAYS NOTHING, AND THE
        // ROW THAT POINTS AT IT SAYS SO INSTEAD OF QUIETLY SHOWING SOMETHING ELSE
        this.currentSource = tile.mrl.source(this.sourceIndex);
        if (this.currentSource == null) {
            this.noEngine = true;
            WaterFrames.LOGGER.warn(IT, "Source {} is not part of {}", sourceIndex, tile.mrl.uri);
            return;
        }

        // THE BRIDGE MAKES THIS PLAYER A FOLLOWER OF THE SERVER SESSION: TIME, PLAY STATE AND LOOP
        // ARRIVE FROM THERE, AND THE DRIFT CORRECTION IS WATERMEDIA'S FROM HERE ON
        this.mediaPlayer = MediaAPI.createPlayer(tile.mrl, sourceIndex, GFX_ENGINE, SFX_ENGINE,
                new DisplayBridge(tile.getLevel(), tile.getBlockPos()));

        if (this.mediaPlayer == null) {
            this.noEngine = true;
            WaterFrames.LOGGER.warn(IT, "Failed to create media player for source {} - no engine available", sourceIndex);
            return;
        }

        // VOLUME AND MUTE STAY PRIVATE TO THIS VIEWER, THE SESSION GRANTS NO SAY OVER THEM. NOTHING
        // IS STARTED HERE EITHER: A START REQUEST WOULD REWIND THE MEDIA FOR EVERYONE WATCHING
        if (DisplaysConfig.soundIntegration()) {
            // THE GAME OWNS THE GAIN ONCE THE SOUND REGISTERS; STAY SILENT UNTIL THEN
            this.mediaPlayer.mute(true);
        } else {
            // A RAW SOURCE SITS AT THE WORLD ORIGIN UNDER OPENAL'S DEFAULT DISTANCE MODEL; MADE
            // LISTENER-RELATIVE SO MONO MEDIA DOESN'T FADE AND PAN AGAINST THE SPAWN COORDINATES
            int source = this.mediaPlayer.audioSource();
            if (source != MediaPlayer.NO_SOURCE) AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);

            this.currentVolume = this.rangedVol(this.tile.data.volume, this.tile.data.minVolumeDistance, this.tile.data.maxVolumeDistance);
            this.mediaPlayer.volume(this.currentVolume);
            this.mediaPlayer.mute(this.tile.data.muted);
        }

        WaterFrames.LOGGER.debug(IT, "Created media player for source type: {}", this.currentSource.type());
    }

    // =========================================================================
    // SOURCE/QUALITY ACCESS
    // =========================================================================

    /** The player following the session: the network hands it its traffic, the screens drive it. */
    public MediaPlayer player() {
        return this.mediaPlayer;
    }

    /**
     * Gets the current source being played.
     */
    public MRL.Source getSource() {
        return this.currentSource;
    }

    /** Index of that source inside the media, which is what the playlist entry pointed at. */
    public int sourceIndex() {
        return this.sourceIndex;
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
        return this.currentSource != null && this.currentSource.type() == MediaType.VIDEO;
    }

    /**
     * Checks if the current source is an image.
     */
    public boolean isImage() {
        return this.currentSource != null && this.currentSource.type() == MediaType.IMAGE;
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

    // GL TEXTURE 0 IS "NONE": PLAYERS ANSWER IT BEFORE THE FIRST FRAME AND IT MUST NEVER BE DRAWN
    public int texture() {
        int texture = this.mediaPlayer != null ? (int) this.mediaPlayer.texture() : -1;
        return texture <= 0 ? -1 : texture;
    }

    public ResourceLocation textureId() {
        int texture = texture();
        if (texture == -1) return null;

        // A PLAYER MAY SWAP ITS GL TEXTURE MID-PLAY; RE-REGISTER AND DROP THE STALE ID
        if (texture != this.glTexture) {
            if (this.glLocation != null) DisplaysRegistry.unregisterTexture(this.glLocation);
            this.glTexture = texture;
            this.glLocation = WaterFrames.asResource(texture);
            DisplaysRegistry.registerTexture(this.glLocation, new TextureWrapper(texture));
        }
        return this.glLocation;
    }

    public long time() {
        return this.mediaPlayer != null ? this.mediaPlayer.time() : 0;
    }

    public long duration() {
        return this.mediaPlayer != null ? this.mediaPlayer.duration() : 0;
    }

    public boolean canTick() {
        return this.mediaPlayer != null && this.mediaPlayer.canPlay();
    }

    public boolean canRender() {
        return this.mediaPlayer != null && this.mediaPlayer.canPlay() && this.texture() != -1;
    }

    // =========================================================================
    // TICK LOOP
    // =========================================================================

    /**
     * Only what this viewer owns. Time, play state and loop belong to the session and are
     * mirrored by WaterMedia on its own clock, several times per game tick.
     */
    public void tick() {
        if (this.mediaPlayer == null || !this.mediaPlayer.canPlay()) return;

        if (!DisplaysConfig.soundIntegration()) {
            if (this.sound != null) {
                this.sound.drop();
                this.sound = null;
            }

            int volume = this.rangedVol(this.tile.data.volume, this.tile.data.minVolumeDistance, this.tile.data.maxVolumeDistance);
            if (this.currentVolume != volume) {
                this.mediaPlayer.volume(this.currentVolume = volume);
            }

            if (this.mediaPlayer.mute() != tile.data.muted) {
                this.mediaPlayer.mute(tile.data.muted);
            }
            return;
        }

        // MINECRAFT OWNS THE GAIN WHILE THE SOUND IS REGISTERED; A DROPPED ONE (STOPSOUND,
        // CATEGORY AT ZERO, ENGINE RELOAD) LEAVES THE PLAYER MUTED AND RETRIES EVERY SECOND
        if (this.sound != null && !this.sound.dropped()) return;
        if (this.sound != null) {
            this.sound = null;
            this.mediaPlayer.mute(true);
        }
        if (--this.soundRetry > 0) return;
        this.soundRetry = 20;
        if (!this.mediaPlayer.withAudio()) return;

        int source = this.mediaPlayer.audioSource();
        Options options = Minecraft.getInstance().options;
        if (source == MediaPlayer.NO_SOURCE
                || options.getSoundSourceVolume(SoundSource.MASTER) <= 0
                || options.getSoundSourceVolume(SoundSource.RECORDS) <= 0) return;

        // THE PLAYER STAYS FLAGGED MUTED FOR ITS WHOLE LIFE: WATERMEDIA NEVER TOUCHES THE GAIN
        // AGAIN AND THE ENGINE BECOMES ITS ONLY WRITER, STARTING WITH THE SWAP SETUP PASS
        this.sound = DisplaySound.create(this, this.tile, source);
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
        if (!this.tile.mrl.status().loaded()) return true;
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
        return this.mediaPlayer != null && this.mediaPlayer.liveSource();
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

    /**
     * The pause menu holds the whole session, not just this screen. Only true singleplayer ever
     * gets here, a remote server never stops for one player, and a display already paused by
     * hand stays that way when the game resumes.
     */
    public void gamePaused(boolean paused) {
        if (this.mediaPlayer == null) return;
        this.mediaPlayer.pause(paused || this.tile.data.paused);
    }

    public void setMuteMode(boolean mute) {
        // UNDER ENGINE INTEGRATION THE PACKET'S DATA CHANGE ALREADY DRIVES THE INSTANCE GAIN
        if (this.mediaPlayer == null || DisplaysConfig.soundIntegration()) return;
        this.mediaPlayer.mute(mute);
    }

    // =========================================================================
    // LIFECYCLE
    // =========================================================================

    public void release() {
        if (this.isReleased()) return;
        this.released = true;

        if (this.sound != null) {
            this.sound.drop();
            this.sound = null;
        }

        if (this.mediaPlayer != null) {
            try {
                this.mediaPlayer.release();
            } catch (Throwable t) {
                // A BACKEND THAT DIES ON ITS WAY OUT MUST NOT TAKE THE GAME WITH IT, AND MUST NOT
                // LEAVE THIS DISPLAY HALF RELEASED EITHER: THE TEXTURE AND THE LIST STILL FOLLOW
                WaterFrames.LOGGER.error(IT, "Media player failed to release", t);
            }
            this.mediaPlayer = null;
        }

        if (this.glLocation != null) {
            DisplaysRegistry.unregisterTexture(this.glLocation);
            this.glLocation = null;
            this.glTexture = -1;
        }

        this.currentSource = null;
        DisplayList.remove(this);
    }

    // =========================================================================
    // AUDIO UTILITIES
    // =========================================================================

    public int rangedVol(int volume, int min, int max) {
        volume = this.falloffVol(volume, min, max);

        if (DisplaysConfig.useMasterVolume()) {
            volume = (int) (volume * (Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER)));
        }

        return volume;
    }

    /**
     * Gain for the Minecraft sound instance; master and category belong to the engine, and so
     * does the distance since OpenAL attenuates the source itself. {@code ranged} folds the
     * emulated falloff back in for the distances the engine cannot measure.
     */
    public float soundVol(boolean ranged) {
        if (this.tile.data.muted) return 0;
        int volume = ranged
                ? this.falloffVol(this.tile.data.volume, this.tile.data.minVolumeDistance, this.tile.data.maxVolumeDistance)
                : this.tile.data.volume;
        return Math.min(volume, 100) / 100f;
    }

    // THE DISPLAY'S OWN DISTANCE CURVE: FULL UNTIL min, LINEAR TO ZERO AT max
    private int falloffVol(int volume, int min, int max) {
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

        return volume;
    }

    public enum Mode {
        VIDEO, PICTURE, AUDIO
    }
}
