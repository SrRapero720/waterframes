package me.srrapero720.waterframes.displays;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import me.lib720.caprica.vlcj.factory.MediaPlayerFactory;
import me.lib720.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import me.lib720.caprica.vlcj.player.embedded.videosurface.callback.UnAllocBufferFormatCallback;
import me.srrapero720.waterframes.FramesUtil;
import me.srrapero720.waterframes.watercore_supplier.WCoreUtil;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.video.players.VideoLanPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import org.lwjgl.opengl.GL11;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class VideoDisplay extends IDisplay {
    private static final MediaPlayerFactory FACTORY = WaterMediaAPI.newVLCPlayerFactory(FramesUtil.getJsonListFromRes("vlc_args.json").toArray(new String[0]));
    public static final String VLC_FAILED = "https://i.imgur.com/UAXbZeM.jpg";
    public static final int ACCEPTABLE_SYNC_TIME = 3000;
    public static final List<VideoDisplay> OPEN_DISPLAYS = new ArrayList<>();

    public static void tick() {
        synchronized (OPEN_DISPLAYS) {
            for (var display: OPEN_DISPLAYS) {
                if (Minecraft.getInstance().isPaused()) {
                    var player = display.player;
                    if ((player.getDuration() > 0 || display.stream) && player.isPlaying()) player.setPauseMode(true);
                }
            }
        }
    }

    public static void clearAll() {
        synchronized (OPEN_DISPLAYS) {
            for (var display : OPEN_DISPLAYS) display.clear();
            OPEN_DISPLAYS.clear();
        }
    }

    public VideoLanPlayer player;
    private final Vec3d pos;
    private volatile IntBuffer buffer;
    public volatile int width = 1;
    public volatile int height = 1;
    public int texture;
    private boolean stream = false;
    private volatile boolean first = true;
    private volatile boolean needsUpdate = false;

    private float lastSetVolume;
    private long lastCorrectedTime = Long.MIN_VALUE;
    private final ReentrantLock lock = new ReentrantLock();

    public VideoDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop) {
        super();
        this.player = new VideoLanPlayer(FACTORY, (mediaPlayer, nativeBuffers, bufferFormat) -> {
            lock.lock();

            try {
                buffer.put(nativeBuffers[0].asIntBuffer());
                buffer.rewind();
                needsUpdate = true;
            } finally {
                lock.unlock();
            }
        }, new UnAllocBufferFormatCallback() {
            @Override
            public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                lock.lock();

                try {
                    setWidth(sourceWidth);
                    setHeight(sourceHeight);
                    setFirstMode();
                    setBuffer(MemoryTracker.create(sourceWidth * sourceHeight * 4).asIntBuffer());
                    setNeedsUpdateMode(true);
                } finally {
                    lock.unlock();
                }

                return new BufferFormat("RGBA", sourceWidth, sourceHeight, new int[] { sourceWidth * 4 }, new int[] { sourceHeight });
            }
        });

        OPEN_DISPLAYS.add(this);

        // VARS
        this.pos = pos;
        this.texture = GlStateManager._genTexture();
        this.lastSetVolume = getVolume(volume, minDistance, maxDistance);

        // PLAYER
        player.setVolume((int) lastSetVolume); // 0 - 100
        player.setRepeatMode(loop);
        player.start(url);
    }

    // SETTERS
    private synchronized void setWidth(int width) { this.width = width; }
    private synchronized void setHeight(int height) { this.height = height; }
    private synchronized void setFirstMode() { this.first = true; }
    private synchronized void setNeedsUpdateMode(boolean mode) { this.needsUpdate = mode; }
    private synchronized void setBuffer(IntBuffer buffer) { this.buffer = buffer; }

    // GETTERS
    @Override public int getWidth() { return width; }
    @Override public int getHeight() { return height; }
    @Override public long getDuration() { return player.getDuration(); }
    @Override public int getTexID() { return texture; }
    @Override public int maxTick() { if (player != null) return (int) player.getGameTickDuration(); return 0; }
    @Override public Type getType() { return Type.VIDEO; }

    public int getGameTickTime() { return (int) player.getGameTickTime(); }
    public int getGameTickDuration() { return (int) player.getGameTickMediaLength(); }

    public void clear() {
        if (player != null) player.release();
        if (texture != -1) GlStateManager._deleteTexture(texture);
        texture = -1;
        player = null;
    }

    @Override
    public void release() {
        clear();
        synchronized (OPEN_DISPLAYS) { OPEN_DISPLAYS.remove(this); }
    }

    @Override
    public void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null) return;
        player.seekGameTicksTo(tick);
        player.pause();
    }

    @Override
    public void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null) return;
        player.seekGameTicksTo(tick);
        player.play();
    }

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

        return (int) ((volume * 100F) * Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER));
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
            long newDuration = player.getDuration();

            if (!stream && newDuration != -1 && newDuration != 0 && player.getStatusDuration() == 0)
                stream = true;
            if (stream) {
                if (player.isPlaying() != realPlaying) player.setPauseMode(!realPlaying);
            } else {
                if (player.getDuration() > 0) {
                    if (player.isPlaying() != realPlaying)
                        player.setPauseMode(!realPlaying);

                    // TODO: Check what is wrong here
                    if (player.isSeekable()) {
                        long time = tick * tickTime + (realPlaying ? (long) (WCoreUtil.toDeltaFrames() * tickTime) : 0);
                        if (time > player.getTime() && loop)
                            time %= player.getDuration();
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
                } else GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

                needsUpdate = false;
            }
        } finally {
            lock.unlock();
        }
    }
}
