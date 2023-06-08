package me.srrapero720.waterframes.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import me.lib720.caprica.vlcj4.factory.MediaPlayerFactory;
import me.lib720.caprica.vlcj4.player.embedded.videosurface.callback.BufferFormat;
import me.lib720.caprica.vlcj4.player.embedded.videosurface.callback.BufferFormatCallback;
import me.srrapero720.waterframes.api.RenderDisplay;
import me.srrapero720.waterframes.watercore_supplier.ThreadUtil;
import me.srrapero720.waterframes.watercore_supplier.WCoreUtil;
import me.srrapero720.watermedia.api.media.players.WaterVLCPlayer;
import me.srrapero720.watermedia.vlc.VLCManager;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import org.lwjgl.opengl.GL11;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class VLCRendering extends RenderDisplay {
    private static final int ACCEPTABLE_SYNC_TIME = 1000;
    private static final List<VLCRendering> OPEN_DISPLAYS = new ArrayList<>();
    
    public static void tick() {
        synchronized (OPEN_DISPLAYS) {
            for (var display: OPEN_DISPLAYS) {
                if (Minecraft.getInstance().isPaused()) {
                    var media = display.player;
                    if (display.stream && media.isPlaying()) media.setPauseMode(true);
                    else if (media.getMediaLength() > 0 && media.isPlaying()) media.setPauseMode(true);
                }
            }
        }
    }
    
    public static void unload() {
        synchronized (OPEN_DISPLAYS) {
            for (var display : OPEN_DISPLAYS) display.free();
            OPEN_DISPLAYS.clear();
        }
    }

    private static MediaPlayerFactory FACTORY;
    private static final String[] DEFAULT_VLC_ARGS = new String[] {"--aout", "directsound", "--file-caching", "6000", "--file-logging", "--logfile", "vlc-waveout.log", "--logmode", "text", "--verbose", "2", "--no-quiet"};
    
    public volatile int width = 1;
    public volatile int height = 1;
    
    public WaterVLCPlayer player;

    private final Vec3d pos;
    private volatile IntBuffer buffer;
    public int texture;
    private boolean stream = false;
    private float lastSetVolume;
    private volatile boolean needsUpdate = false;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile boolean first = true;
    private long lastCorrectedTime = Long.MIN_VALUE;
    
    public VLCRendering(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop) {
        super();
        if (FACTORY == null) FACTORY = VLCManager.createVLCPlayerFactory(DEFAULT_VLC_ARGS);

        this.player = new WaterVLCPlayer(url, FACTORY, (mediaPlayer, nativeBuffers, bufferFormat) -> {
            lock.lock();

            try {
                buffer.put(nativeBuffers[0].asIntBuffer());
                buffer.rewind();
                needsUpdate = true;
            } catch (Exception e) {
                LOGGER.error("Something is wrong processing buffers", e);
            }

            lock.unlock();
        }, new BufferFormatCallback() {
            @Override
            public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                lock.lock();

                ThreadUtil.trySimple(() -> {
                    setWidth(sourceWidth);
                    setHeight(sourceHeight);
                    setFirstMode(true);
                    setBuffer(MemoryTracker.create(sourceWidth * sourceHeight * 4).asIntBuffer());
                    setNeedsUpdateMode(true);
                });

                lock.unlock();

                return new BufferFormat("RGBA", sourceWidth, sourceHeight, new int[] { sourceWidth * 4 }, new int[] { sourceHeight });
            }

            @Override
            public void allocatedBuffers(ByteBuffer[] buffers) {}
        });

        // VARS
        this.pos = pos;
        this.texture = GlStateManager._genTexture();
        this.lastSetVolume = getVolume(volume, minDistance, maxDistance);

        // PLAYER
        player.setVolume((int) lastSetVolume); // 0 - 100
        player.setRepeatMode(loop);
        player.start();
    }

    // SETTERS
    public synchronized void setWidth(int width) { this.width = width; }
    public synchronized void setHeight(int height) { this.height = height; }
    public synchronized void setFirstMode(boolean mode) { this.first = mode; }
    public synchronized void setNeedsUpdateMode(boolean mode) { this.needsUpdate = mode; }
    public synchronized void setBuffer(IntBuffer buffer) { this.buffer = buffer; }


    public int getVolume(float volume, float minDistance, float maxDistance) {
        if (player == null) return 0;
        float distance = (float) pos.distance(Minecraft.getInstance().player.getPosition(WCoreUtil.toDeltaFrames()));

        if (minDistance > maxDistance) {
            float temp = maxDistance;
            maxDistance = minDistance;
            minDistance = temp;
        }
        
        if (distance > minDistance)
            if (distance > maxDistance) volume = 0;
            else volume *= 1 - ((distance - minDistance) / (maxDistance - minDistance));


        var gameVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);

        return (int) ((volume * 100) * gameVolume);
    }

    @Override
    public int maxTick() {
        if (player != null) return (int) player.getDuration();
        return 0;
    }

    @Override
    public void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null) return;
        
        volume = getVolume(volume, minDistance, maxDistance);
        if (volume != lastSetVolume) {
            player.setVolume((int) volume);
            lastSetVolume = volume;
        }
        
        if (player.isValid()) {
            boolean realPlaying = playing && !Minecraft.getInstance().isPaused();
            
            if (player.getRepeatMode() != loop) player.setRepeatMode(loop);
            long tickTime = 50;
            long newDuration = player.getMediaLength();

            if (!stream && newDuration != -1 && newDuration != 0 && player.getDuration() == 0)
                stream = true;
            if (stream) {
                if (player.isPlaying() != realPlaying) player.setPauseMode(!realPlaying);
            } else {
                if (player.getMediaLength() > 0) {
                    if (player.isPlaying() != realPlaying)
                        player.setPauseMode(!realPlaying);

                    // TODO: Check what is wrong here
                    if (player.isSeekable()) {
                        long time = tick * tickTime + (realPlaying ? (long) (WCoreUtil.toDeltaFrames() * tickTime) : 0);
                        if (time > player.getTime() && loop)
                            time %= player.getMediaLength();
                        if (Math.abs(time - player.getTime()) > ACCEPTABLE_SYNC_TIME && Math.abs(time - lastCorrectedTime) > ACCEPTABLE_SYNC_TIME) {
                            lastCorrectedTime = time;
                            player.seekFastTo(time);
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null) return;
        lock.lock();
        try {
            if (needsUpdate) {
                // fixes random crash, when values are too high it causes a jvm crash, caused weird behavior when game is paused
                GlStateManager._pixelStore(GL11.GL_UNPACK_ROW_LENGTH, GL11.GL_ZERO);
                GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_PIXELS, GL11.GL_ZERO);
                GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_ROWS, GL11.GL_ZERO);

                RenderSystem.bindTexture(texture);
                if (first) {
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                    first = false;
                } else
                    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                needsUpdate = false;
            }
        } finally {
            lock.unlock();
        }
        
    }
    
    public void free() {
        if (player != null) {
            player.stop();
            player.release();
        }
        if (texture != -1) {
            GlStateManager._deleteTexture(texture);
            texture = -1;
        }
        player = null;
    }
    
    @Override
    public void release() {
        free();
        synchronized (OPEN_DISPLAYS) {
            OPEN_DISPLAYS.remove(this);
        }
    }
    
    @Override
    public int getTexID() { return texture; }
    
    @Override
    public void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null) return;
        player.seekGameTicksTo(tick);
        player.setPauseMode(true);
//        player.pause();
    }
    
    @Override
    public void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null) return;
        player.seekGameTicksTo(tick);
        player.play();
    }
    
    @Override
    public int getWidth() { return width; }
    
    @Override
    public int getHeight() { return height; }
}
