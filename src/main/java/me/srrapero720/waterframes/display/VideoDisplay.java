package me.srrapero720.waterframes.display;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import me.lib720.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import me.lib720.caprica.vlcj.player.embedded.videosurface.callback.UnAllocBufferFormatCallback;
import me.srrapero720.waterframes.display.texture.TextureCache;
import me.srrapero720.waterframes.watercore_supplier.ThreadUtil;
import me.srrapero720.waterframes.watercore_supplier.WCoreUtil;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.video.VideoLANPlayer;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class VideoDisplay implements IDisplay {
    private static final String VLC_FAILED = "https://i.imgur.com/XCcN2uX.png";
    private static final int ACCEPTABLE_SYNC_TIME = 1000;
    
    private static final List<VideoDisplay> OPEN_DISPLAYS = new ArrayList<>();
    
    public static void tick() {
        synchronized (OPEN_DISPLAYS) {
            for (var display: OPEN_DISPLAYS) {
                if (Minecraft.getInstance().isPaused()) {
                    var media = display.player;
                    if (display.stream && media.isPlaying()) media.setPauseMode(true);
                    else if (media.getDuration() > 0 && media.isPlaying()) media.setPauseMode(true);
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
    
    public static IDisplay createVideoDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop) {
        return ThreadUtil.tryAndReturn((defaultVar) -> {
            var display = new VideoDisplay(pos, url, volume, minDistance, maxDistance, loop);
            if (display.player.getRaw() == null) throw new IllegalStateException("MediaDisplay uses a broken player");
            OPEN_DISPLAYS.add(display);
            return display;

        }, ((Supplier<IDisplay>) () -> {
            var cache = TextureCache.find(VLC_FAILED);
            if (cache.ready()) return cache.createDisplay(pos, VLC_FAILED, volume, minDistance, maxDistance, loop, true);
            return null;
        }).get());
    }
    
    public volatile int width = 1;
    public volatile int height = 1;
    
    public VideoLANPlayer player;
    
    private final Vec3d pos;
    private volatile IntBuffer buffer;
    public int texture;
    private boolean stream = false;
    private float lastSetVolume;
    private volatile boolean needsUpdate = false;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile boolean first = true;
    private long lastCorrectedTime = Long.MIN_VALUE;
    
    public VideoDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop) {
        super();
        this.pos = pos;
        this.texture = GlStateManager._genTexture();
        this.player = new VideoLANPlayer(null, (mediaPlayer, nativeBuffers, bufferFormat) -> {
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
                    VideoDisplay.this.width = sourceWidth;
                    VideoDisplay.this.height = sourceHeight;
                    VideoDisplay.this.first = true;
                    buffer = MemoryTracker.create(sourceWidth * sourceHeight * 4).asIntBuffer();
                    needsUpdate = true;
                } finally {
                    lock.unlock();
                }
                return new BufferFormat("RGBA", sourceWidth, sourceHeight, new int[] { sourceWidth * 4 }, new int[] { sourceHeight });
            }
        });

        lastSetVolume = getVolume(volume, minDistance, maxDistance);
        player.setVolume((int) lastSetVolume);
        player.setRepeatMode(loop);
        player.start(url);
    }
    
    public int getVolume(float volume, float minDistance, float maxDistance) {
        if (player == null) return 0;
        assert Minecraft.getInstance().player != null;
        float distance = (float) pos.distance(Minecraft.getInstance().player.getPosition(WCoreUtil.toDeltaFrames()));
        if (minDistance > maxDistance) {
            float temp = maxDistance;
            maxDistance = minDistance;
            minDistance = temp;
        }
        
        if (distance > minDistance)
            if (distance > maxDistance)
                volume = 0;
            else
                volume *= 1 - ((distance - minDistance) / (maxDistance - minDistance));
        return (int) (volume * 100F);
    }

    @Override
    public int maxTick() {
        if (player != null) return WaterMediaAPI.msToGameTicks(player.getDuration());
        return 0;
    }

    @Override
    public void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null) return;
        
        volume = getVolume(volume, minDistance, maxDistance);
        if (volume != lastSetVolume) {
            // ENSURE PLAYER GET MUTED ON LONG DISTANCES
            if (volume < 0.1) player.getRaw().mediaPlayer().audio().setMute(true);
            if (volume >= 0.1) player.getRaw().mediaPlayer().audio().setMute(false);

            player.setVolume((int) volume);
            lastSetVolume = volume;
        }
        
        if (player.isValid()) {
            boolean realPlaying = playing && !Minecraft.getInstance().isPaused();
            
            if (player.getRepeatMode() != loop)
                player.setRepeatMode(loop);
            long tickTime = 50;
            long newDuration = player.getDuration();

            if (!stream && newDuration != -1 && newDuration != 0 && player.getMediaInfoDuration() == 0) stream = true;
            if (!stream && player.isStream()) stream = true;

            if (stream) {
                if (player.isPlaying() != realPlaying) player.setPauseMode(!realPlaying);
            } else {
                if (player.getDuration() > 0) {
                    if (player.isPlaying() != realPlaying) player.setPauseMode(!realPlaying);
                    
                    if (player.isSeekable()) {
                        long time = tick * tickTime + (realPlaying ? (long) (WCoreUtil.toDeltaFrames() * tickTime) : 0);
                        if (time > player.getTime() && loop)
                            time %= player.getDuration();
                        if (Math.abs(time - player.getTime()) > ACCEPTABLE_SYNC_TIME && Math.abs(time - lastCorrectedTime) > ACCEPTABLE_SYNC_TIME) {
                            lastCorrectedTime = time;
                            player.seekTo(time);
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
                GlStateManager._pixelStore(3314, 0);
                GlStateManager._pixelStore(3316, 0);
                GlStateManager._pixelStore(3315, 0);
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
        if (player != null) player.release();
        player = null;
        if (texture != -1) {
            GlStateManager._deleteTexture(texture);
            texture = -1;
        }

    }
    
    @Override
    public void release() {
        free();
        synchronized (OPEN_DISPLAYS) {
            OPEN_DISPLAYS.remove(this);
        }
    }
    
    @Override
    public int texture() {
        return texture;
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
    
    @Override
    public int getWidth() {
        return width;
    }
    
    @Override
    public int getHeight() {
        return height;
    }
    
}
