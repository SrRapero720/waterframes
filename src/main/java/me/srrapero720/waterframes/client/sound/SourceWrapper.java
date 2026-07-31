package me.srrapero720.waterframes.client.sound;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.sounds.AudioStream;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.SOFTSourceSpatialize;

/**
 * A {@link Channel} over an OpenAL source owned by WaterMedia, the audio twin of
 * {@code TextureWrapper}: the game and audio mods drive gain, pitch, position and filters on the
 * real source, while every ownership operation is a no-op so the engine can never stop, starve
 * or delete a source it did not create.
 */
public class SourceWrapper extends Channel {
    // WITHOUT SPATIALIZE SUPPORT STEREO MEDIA IGNORES 3D ATTENUATION ENTIRELY; CALLERS MUST FALL
    // BACK TO EMULATED GAIN. THE CLASS ONLY LOADS ONCE AN AL CONTEXT EXISTS, THE READ IS SAFE
    static final boolean SPATIALIZE = AL.getCapabilities().AL_SOFT_source_spatialize;

    private final int source;
    private final DisplaySound owner;
    private volatile boolean dropped;

    public SourceWrapper(int source, DisplaySound owner) {
        super(source);
        this.source = source;
        this.owner = owner;
    }

    /**
     * Hands the display's range to the engine: full gain until {@code min} blocks, fading
     * linearly to silence at {@code max}, measured by OpenAL per audio frame. Spatialize forces
     * multichannel media to obey the model too, downmixed onto the block it plays from.
     */
    public void linearRange(int min, int max) {
        // AL_DISTANCE_MODEL (0xD000) IS THE PER-SOURCE PROPERTY; EXTSourceDistanceModel's
        // CONSTANT (0x200) IS THE alEnable CAPABILITY TOKEN AND RAISES AL_INVALID_ENUM HERE
        AL10.alSourcei(this.source, AL11.AL_DISTANCE_MODEL, AL11.AL_LINEAR_DISTANCE_CLAMPED);
        AL10.alSourcef(this.source, AL10.AL_REFERENCE_DISTANCE, min);
        AL10.alSourcef(this.source, AL10.AL_MAX_DISTANCE, max);
        AL10.alSourcef(this.source, AL10.AL_ROLLOFF_FACTOR, 1.0f);
        if (SPATIALIZE) AL10.alSourcei(this.source, SOFTSourceSpatialize.AL_SOURCE_SPATIALIZE_SOFT, AL10.AL_TRUE);
    }

    /**
     * Old-school emulated mode: the source rides the listener's head, distance lives in the
     * gain the owner computes. For distances the engine cannot measure (Sable/VS projections).
     */
    public void headLocked() {
        this.setRelative(true);
        this.disableAttenuation();
        if (SPATIALIZE) AL10.alSourcei(this.source, SOFTSourceSpatialize.AL_SOURCE_SPATIALIZE_SOFT, SOFTSourceSpatialize.AL_AUTO_SOFT);
    }

    // A STOP FROM MINECRAFT (/stopsound, CATEGORY SLIDER AT ZERO) MEANS "DROP THE SOUND",
    // NOT "HALT THE SOURCE": WATERMEDIA WOULD JUST RESTART IT ON THE NEXT UPLOAD
    @Override public void stop() {
        this.dropped = true;
        this.owner.drop();
    }

    @Override public boolean stopped() {
        return this.dropped;
    }

    // WATERMEDIA OWNS PLAY STATE AND THE BUFFER QUEUE; MINECRAFT MUST NEVER START,
    // FEED OR DELETE THE SOURCE
    @Override public void play() { /* NO OP */ }
    @Override public void destroy() { /* NO OP */ }
    @Override public void updateStream() { /* NO OP */ }
    @Override public void attachStaticBuffer(SoundBuffer buffer) { /* NO OP */ }
    @Override public void attachBufferStream(AudioStream stream) { /* NO OP */ }
}
