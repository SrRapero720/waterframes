package me.srrapero720.waterframes.client.sound;

import me.srrapero720.waterframes.DisplaysConfig;
import me.srrapero720.waterframes.DisplaysRegistry;
import me.srrapero720.waterframes.client.display.Display;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.compat.sable.SableCompat;
import me.srrapero720.waterframes.common.compat.valkyrienskies.VSCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * Minecraft face of a display's audio. The OpenAL source WaterMedia streams into is adopted by
 * the sound engine as a regular streaming sound, so category sliders, subtitles, sound events
 * and audio mods all act on the real source — the same road {@code TextureWrapper} walks for
 * the GL texture.
 */
public class DisplaySound extends AbstractTickableSoundInstance {
    // MINECRAFT NEVER PULLS PCM FROM THIS: BUFFERS BELONG TO WATERMEDIA. IT ONLY EXISTS SO THE
    // ENGINE WALKS ITS STREAMING PATH AND FIRES PlayStreamingSourceEvent ON THE REAL SOURCE
    private static final AudioStream EMPTY_STREAM = new AudioStream() {
        private final AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        @Override public AudioFormat getFormat() { return this.format; }
        @Override public ByteBuffer read(int size) { return null; }
        @Override public void close() { /* NO OP */ }
    };

    private final Display display;
    private final DisplayTile tile;
    private final SourceWrapper channel;
    private final CompletableFuture<AudioStream> stream = new CompletableFuture<>();
    private ChannelAccess.ChannelHandle handle;
    private volatile boolean dropped;
    private int rangeMin = Integer.MIN_VALUE;
    private int rangeMax = Integer.MIN_VALUE;

    private DisplaySound(Display display, DisplayTile tile, int source) {
        super(DisplaysRegistry.DISPLAY_SOUND.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.display = display;
        this.tile = tile;
        this.channel = new SourceWrapper(source, this);
        // DISTANCE FALLOFF IS THE DISPLAY'S OWN MIN/MAX RANGE; THE ENGINE ONLY PANS THE POSITION
        this.attenuation = Attenuation.NONE;
        this.refresh();
    }

    /**
     * Plays a sound through the vanilla pipeline and adopts WaterMedia's source as its channel.
     *
     * @return the registered sound, or {@code null} when the engine refused it (event cancelled
     *         or replaced, master volume at zero, channel pool exhausted); callers may retry.
     */
    public static DisplaySound create(Display display, DisplayTile tile, int source) {
        Minecraft mc = Minecraft.getInstance();
        SoundEngine engine = mc.getSoundManager().soundEngine;
        DisplaySound sound = new DisplaySound(display, tile, source);
        mc.getSoundManager().play(sound);

        ChannelAccess.ChannelHandle handle = engine.instanceToChannel.get(sound);
        if (handle == null) return null;

        sound.handle = handle;
        handle.execute(pooled -> {
            // THE POOLED CHANNEL GOES BACK UNUSED AND THE HANDLE DRIVES THE REAL SOURCE FROM NOW
            // ON. OUT OF THE ACCESS SET, THE REAPER NEVER READS AN UNDERRUN AS "FINISHED" NOR
            // HANDS THE FOREIGN CHANNEL TO THE POOLS (WHICH WOULD THROW AND DELETE THE SOURCE).
            // ATTENUATION AND RELATIVE MODE ARE NOT REPLAYED HERE: range() ALREADY OWNS THEM
            handle.channel = sound.channel;
            engine.channelAccess.channels.remove(handle);
            engine.library.releaseChannel(pooled);
            // THE ENGINE'S SETUP PASS RAN ON THE POOLED CHANNEL; REPLAY IT ON THE REAL SOURCE
            sound.channel.setSelfPosition(new Vec3(sound.x, sound.y, sound.z));
            sound.channel.setPitch(Mth.clamp(sound.getPitch(), 0.5f, 2.0f));
            sound.channel.setVolume(Mth.clamp(sound.getVolume() * mc.options.getSoundSourceVolume(SoundSource.RECORDS), 0.0f, 1.0f));
        });
        // COMPLETING THE STREAM FIRES ATTACH+PLAY: PlayStreamingSourceEvent REACHES AUDIO MODS
        // (SOUND PHYSICS AND ALIKE) CARRYING THE WRAPPER AROUND THE REAL SOURCE
        sound.stream.complete(EMPTY_STREAM);
        return sound;
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary buffers, Sound sound, boolean looping) {
        return this.stream;
    }

    // VOLUME ZERO AT REGISTRATION (MUTED, OUT OF RANGE) MUST NOT SKIP THE PLAY
    @Override
    public boolean canStartSilent() {
        return true;
    }

    /** Whether Minecraft let go of this sound; the display re-registers a fresh one. */
    public boolean dropped() {
        return this.dropped;
    }

    /** Ends the sound for the engine and the display; idempotent and safe from any thread. */
    public void drop() {
        if (this.dropped) return;
        this.dropped = true;
        this.stop();
        // THE STOPPED HANDLE IS WHAT MAKES THE ENGINE CLEAN ITS MAPS ON THE NEXT TICK
        if (this.handle != null) this.handle.stopped = true;
    }

    @Override
    public void tick() {
        if (this.display.isReleased()) {
            this.drop();
            return;
        }
        this.refresh();
    }

    // GAIN, PITCH AND POSITION ARE RE-READ BY THE ENGINE EVERY TICK; MUTE IS JUST GAIN ZERO.
    // THE RANGE BELONGS TO OPENAL: DISTANCE IS MEASURED PER AUDIO FRAME AGAINST THE REAL 3D
    // POSITION, THE PER-TICK VOLUME EMULATION ONLY SURVIVES WHERE THE ENGINE CANNOT MEASURE
    private void refresh() {
        if (emulated()) {
            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.volume = this.display.soundVol(true);
            this.range(-1, -1);
        } else {
            BlockPos pos = this.tile.getBlockPos().relative(this.tile.getDirection(), (int) this.tile.data.audioOffset);
            this.x = pos.getX() + 0.5d;
            this.y = pos.getY() + 0.5d;
            this.z = pos.getZ() + 0.5d;
            this.volume = this.display.soundVol(false);
            int min = Math.min(this.tile.data.minVolumeDistance, this.tile.data.maxVolumeDistance);
            int max = Math.max(this.tile.data.minVolumeDistance, this.tile.data.maxVolumeDistance);
            this.range(min, max + 1);
        }
        this.pitch = this.display.player().speed();
    }

    // ONLY FORWARDS CHANGES TO THE SOURCE; min BELOW ZERO MEANS THE EMULATED HEAD-LOCKED MODE
    private void range(int min, int max) {
        if (this.rangeMin == min && this.rangeMax == max) return;
        this.rangeMin = min;
        this.rangeMax = max;
        if (min < 0) this.channel.headLocked();
        else this.channel.linearRange(min, max);
    }

    // SABLE/VS DISTANCES CROSS SUB-LEVEL PROJECTIONS OPENAL CANNOT SEE, AND WITHOUT SPATIALIZE
    // SUPPORT STEREO MEDIA WOULD DODGE THE ENGINE'S ATTENUATION; BOTH STAY ON EMULATED GAIN
    private static boolean emulated() {
        return !SourceWrapper.SPATIALIZE
                || (SableCompat.installed() && DisplaysConfig.sableCompat())
                || (VSCompat.installed() && DisplaysConfig.vsEurekaCompat());
    }
}
