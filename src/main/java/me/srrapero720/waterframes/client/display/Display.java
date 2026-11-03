package me.srrapero720.waterframes.client.display;

import me.srrapero720.waterframes.*;
import me.srrapero720.waterframes.client.sound.DisplaySound;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import me.srrapero720.waterframes.common.network.packets.MediaReportPacket;
import me.srrapero720.waterframes.common.util.AlignedBox;
import me.srrapero720.waterframes.common.util.Axis;
import org.watermedia.api.player.ALEngine;
import org.watermedia.api.player.MRL;
import org.watermedia.api.player.MediaPlayer;
import org.watermedia.api.player.PlayerAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.lwjgl.openal.AL10;

import java.util.List;
import java.util.Locale;

public class Display extends AbstractTexture {
    private static final Marker IT = MarkerManager.getMarker("Display");

    // MEDIA AND DATA
    private MediaPlayer mediaPlayer;
    private final DisplayTile tile;
    /** Which source of the media this player opened; the playlist entry decides it. */
    private final int sourceIndex;

    // PLAYBACK STATE
    private int currentVolume = 0;
    private boolean released = false;
    // WHETHER THE SERVER CLOCK ALREADY HEARD FROM THIS VIEWER WHAT THE MEDIA IS
    private boolean reported;
    private boolean sinkWarned;

    // TEXTURE REGISTRATION, THE DISPLAY IS THE TEXTURE: ONE LOCATION PER BLOCK POSITION FOR ITS WHOLE LIFE
    private final ResourceLocation location;

    // SOUND REGISTRATION, ONE AL SINK PER SIDE ADOPTED BY MINECRAFT'S ENGINE THE WAY THE TEXTURE IS
    private DisplaySound[] sounds;
    private int soundRetry;

    public Display(DisplayTile tile) {
        this.tile = tile;
        this.sourceIndex = tile.data.getSource();
        // THE BLOCK POSITION IS THE DISPLAY'S IDENTITY: ONE TEXTURE NAME PER BLOCK FOR ITS WHOLE LIFE
        BlockPos pos = tile.getBlockPos();
        this.location = WaterFrames.asResource("display_at_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ());
        WaterFramesClient.registerTexture(this.location, this);
        DisplayList.add(this);
        this.openPlayer();
    }

    private void openPlayer() {
        // THE PLAYLIST ENTRY NAMES THE SOURCE; ONE THAT IS NOT THERE ANYMORE PLAYS NOTHING, AND THE
        // ROW THAT POINTS AT IT SAYS SO INSTEAD OF QUIETLY SHOWING SOMETHING ELSE
        final MRL.Source source = tile.mrl.source(this.sourceIndex);
        if (source == null) {
            WaterFrames.LOGGER.warn(IT, "Source {} is not part of {}", sourceIndex, tile.mrl.uri());
            return;
        }

        // THE PLAYER FOLLOWS THE SERVER SESSION: TIME, PLAY STATE AND LOOP ARRIVE AS SNAPSHOTS OVER THE NETWORK,
        // AND THE DRIFT CORRECTION IS WATERMEDIA'S. THE ENGINES ARE BUILT INSIDE THE SUPPLIERS AND OWNED BY THE PLAYER
        // A SPATIAL ENGINE PLAYS THROUGH POSITIONAL MONO SINKS, WHICH IS WHAT THE STEREO PAIR BELOW NEEDS
        final Minecraft client = Minecraft.getInstance();
        this.mediaPlayer = PlayerAPI.player(tile.mrl, sourceIndex, () -> PlayerAPI.glEngine(client.gameThread, client), () -> PlayerAPI.alEngine(true));

        if (this.mediaPlayer == null) {
            WaterFrames.LOGGER.warn(IT, "Failed to create media player for source {}, no engine available", sourceIndex);
            return;
        }

        // DUBBED MEDIA FOLLOWS THE GAME LANGUAGE (es_mx BECOMES es-MX); WATERMEDIA STILL PREFERS THE ORIGINAL TRACK
        // WHEN NOTHING MATCHES, AND A PICK FROM THE MEDIA TAB OUTRANKS BOTH
        final String[] language = client.getLanguageManager().getSelected().split("_", 2);
        this.mediaPlayer.audioLanguage(language.length > 1 ? language[0] + '-' + language[1].toUpperCase(Locale.ROOT) : language[0]);

        // VOLUME AND MUTE STAY PRIVATE TO THIS VIEWER, THE SESSION GRANTS NO SAY OVER THEM. NOTHING
        // IS STARTED HERE EITHER: A START REQUEST WOULD REWIND THE MEDIA FOR EVERYONE WATCHING
        if (WaterFramesConfig.soundIntegration()) {
            // THE GAME OWNS THE GAIN ONCE THE SOUND REGISTERS; STAY SILENT UNTIL THEN
            this.mediaPlayer.mute(true);
        } else {
            // A RAW SOURCE SITS AT THE WORLD ORIGIN UNDER OPENAL'S DEFAULT DISTANCE MODEL; MADE
            // LISTENER-RELATIVE SO MONO MEDIA DOESN'T FADE AND PAN AGAINST THE SPAWN COORDINATES
            final int alSource = this.mediaPlayer.audioSource();
            if (alSource != MediaPlayer.NO_SOURCE) AL10.alSourcei(alSource, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);

            this.currentVolume = this.rangedVol(this.tile.data.volume, this.tile.data.minVolumeDistance, this.tile.data.maxVolumeDistance);
            this.mediaPlayer.volume(this.currentVolume);
            this.mediaPlayer.mute(this.tile.data.muted);
        }

        WaterFrames.LOGGER.debug(IT, "Created media player for source type: {}", source.type());
    }

    // =========================================================================
    // SOURCE/QUALITY ACCESS
    // =========================================================================

    /** The player following the session: the network hands it its traffic, the screens drive it. */
    public MediaPlayer player() {
        return this.mediaPlayer;
    }

    /** Index of that source inside the media, which is what the playlist entry pointed at. */
    public int sourceIndex() {
        return this.sourceIndex;
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
        return this.texture() == -1 ? null : this.location;
    }

    // MINECRAFT'S ENGINE ADOPTS THIS TEXTURE BUT THE MEDIA PLAYER OWNS THE GL ID; NOTHING HERE
    // ALLOCATES OR FREES, AND A PLAYER SWAPPING ITS TEXTURE MID-PLAY IS PICKED UP LIVE
    @Override public int getId() {
        return Math.max(this.texture(), 0);
    }

    @Override public void load(ResourceManager manager) { /* NO OP */ }
    @Override public void releaseId() { /* NO OP */ }
    @Override public void close() { /* NO OP */ }

    public long time() {
        return this.mediaPlayer != null ? this.mediaPlayer.time() : 0;
    }

    public long duration() {
        return this.mediaPlayer != null ? this.mediaPlayer.duration() : 0;
    }

    /** The tile this display draws for. */
    public DisplayTile tile() {
        return this.tile;
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

        // THE SERVER CLOCK DECODES NOTHING: THE FIRST VIEWER TO LEARN THE LENGTH OF THE MEDIA TELLS
        // IT, AND A LIVE STREAM SAYS SO INSTEAD
        if (!this.reported) {
            long duration = this.mediaPlayer.duration();
            boolean live = this.mediaPlayer.live();
            if (duration > 0 || live) {
                this.reported = true;
                DisplayNetwork.sendServer(new MediaReportPacket(this.tile.getBlockPos(), duration, live));
            }
        }

        if (!WaterFramesConfig.soundIntegration()) {
            this.dropSounds();

            // A MUTED PLAYER GETS NO GAIN UPDATES; THE BACKEND RESTARTS ITS BUFFERS ON EVERY GAIN WRITE
            if (this.mediaPlayer.mute() != this.tile.data.muted) this.mediaPlayer.mute(this.tile.data.muted);
            if (this.tile.data.muted) return;
            final int volume = this.rangedVol(this.tile.data.volume, this.tile.data.minVolumeDistance, this.tile.data.maxVolumeDistance);
            if (this.currentVolume != volume) this.mediaPlayer.volume(this.currentVolume = volume);
            return;
        }

        // MINECRAFT OWNS THE GAIN WHILE THE SOUNDS ARE REGISTERED; ONE DROPPED SIDE (STOPSOUND, CATEGORY AT
        // ZERO, ENGINE RELOAD) TAKES THE PAIR DOWN, LEAVES THE PLAYER MUTED AND RETRIES EVERY SECOND
        if (this.sounds != null) {
            boolean alive = true;
            for (final DisplaySound sound: this.sounds) alive &= !sound.dropped();
            if (alive) return;
            this.dropSounds();
            this.mediaPlayer.mute(true);
        }
        if (--this.soundRetry > 0) return;
        this.soundRetry = 20;
        if (!this.mediaPlayer.audio()) return;

        final Options options = Minecraft.getInstance().options;
        if (this.mediaPlayer.audioSource() == MediaPlayer.NO_SOURCE
                || options.getSoundSourceVolume(SoundSource.MASTER) <= 0
                || options.getSoundSourceVolume(SoundSource.RECORDS) <= 0) return;

        // THE PLAYER STAYS FLAGGED MUTED FOR ITS WHOLE LIFE: WATERMEDIA NEVER TOUCHES THE GAIN AGAIN AND THE
        // ENGINE BECOMES ITS ONLY WRITER; A LIVE TOGGLE INTO INTEGRATION ARRIVES HERE WITH THE GAIN STILL DIRECT
        this.mediaPlayer.mute(true);

        // STEREO: THE PRIMARY SINK TAKES THE LEFT CHANNEL AND A SECOND ONE THE RIGHT, EACH REGISTERED AS ITS OWN
        // GAME SOUND AT ITS EDGE OF THE PICTURE; MONO MEDIA PLAYS THE SAME SIGNAL ON BOTH. WITHOUT SINKS (OR WHEN
        // THE ENGINE REFUSES ONE) THE SINGLE SOURCE PLAYS CENTRED
        List<ALEngine.Sink> sinks = this.mediaPlayer.sinks();
        try {
            if (!sinks.isEmpty() && sinks.size() < 2 && this.mediaPlayer.addSink(ALEngine.Mix.RIGHT) != null) sinks = this.mediaPlayer.sinks();
            if (sinks.size() >= 2) sinks.get(0).mix(ALEngine.Mix.LEFT);
        } catch (final IllegalStateException e) {
            if (!this.sinkWarned) WaterFrames.LOGGER.warn(IT, "Stereo sinks unavailable for {}, playing centred", this.tile.getBlockPos(), e);
            this.sinkWarned = true;
            sinks = List.of();
        }

        final DisplaySound[] created = sinks.size() >= 2
                ? new DisplaySound[] {
                        DisplaySound.create(this, this.tile, sinks.get(0).source(), ALEngine.Mix.LEFT),
                        DisplaySound.create(this, this.tile, sinks.get(1).source(), ALEngine.Mix.RIGHT) }
                : new DisplaySound[] { DisplaySound.create(this, this.tile, this.mediaPlayer.audioSource(), ALEngine.Mix.MONO) };
        for (final DisplaySound sound: created) {
            if (sound != null) continue;
            // THE ENGINE REFUSED A SIDE: NEITHER PLAYS ALONE, THE NEXT RETRY REGISTERS THE PAIR AGAIN
            for (final DisplaySound other: created) if (other != null) other.drop();
            return;
        }
        this.sounds = created;
    }

    /**
     * World position of one side of the picture's audio, in blocks. The base is the block (plus the projector's
     * audio offset along its facing); a side moves to that edge of the picture as the viewer sees it, which is
     * the mirror image for projectors since they are watched from behind. Vertical frames have no left or right.
     */
    public Vec3 soundPos(final ALEngine.Mix side) {
        final Direction direction = this.tile.getDirection();
        final BlockPos base = this.tile.getBlockPos().relative(direction, (int) this.tile.data.audioOffset);
        final Vec3 center = Vec3.atCenterOf(base);
        if (side == ALEngine.Mix.MONO || direction.getAxis().isVertical()) return center;

        final Direction right = this.tile.caps.projects() ? direction.getClockWise() : direction.getCounterClockWise();
        final Axis across = Axis.of(right.getAxis());
        final AlignedBox box = this.tile.getRenderBox();
        // THE PICTURE MAY BE ANCHORED OFF THE BLOCK CENTRE, SO THE EDGE IS READ FROM THE BOX ITSELF
        final double edge = side == ALEngine.Mix.RIGHT
                ? (right.getAxisDirection().getStep() > 0 ? box.max(across) : box.min(across))
                : (right.getAxisDirection().getStep() > 0 ? box.min(across) : box.max(across));
        final double along = this.tile.getBlockPos().get(right.getAxis()) + edge;
        return switch (right.getAxis()) {
            case X -> new Vec3(along, center.y, center.z);
            case Z -> new Vec3(center.x, center.y, along);
            case Y -> center;
        };
    }

    private void dropSounds() {
        if (this.sounds == null) return;
        for (final DisplaySound sound: this.sounds) sound.drop();
        this.sounds = null;
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
        return this.mediaPlayer == null;
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
        return this.mediaPlayer != null && this.mediaPlayer.live();
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

    public void setMuteMode(boolean mute) {
        // UNDER ENGINE INTEGRATION THE PACKET'S DATA CHANGE ALREADY DRIVES THE INSTANCE GAIN
        if (this.mediaPlayer == null || WaterFramesConfig.soundIntegration()) return;
        this.mediaPlayer.mute(mute);
    }

    // =========================================================================
    // LIFECYCLE
    // =========================================================================

    public void release() {
        if (this.isReleased()) return;
        this.released = true;
        this.dropSounds();

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

        WaterFramesClient.unregisterTexture(this.location);
        DisplayList.remove(this);
    }

    // =========================================================================
    // AUDIO UTILITIES
    // =========================================================================

    public int rangedVol(int volume, int min, int max) {
        volume = this.falloffVol(volume, min, max);

        if (WaterFramesConfig.useMasterVolume()) {
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

}
