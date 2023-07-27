package me.srrapero720.waterframes.display.texture;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.display.IDisplay;
import me.srrapero720.waterframes.display.ImageDisplay;
import me.srrapero720.waterframes.display.VideoDisplay;
import me.srrapero720.watermedia.Util;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.images.PictureFetcher;
import me.srrapero720.watermedia.api.images.RenderablePicture;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class TextureCache {
    private static final Map<String, TextureCache> CACHE = new HashMap<>();
    public static final TextureCache DEF_VLC_FAILED = new TextureCache((Util.ARCH.wrapped) ? WaterMediaAPI.VLC_FAILED_INSTALL : WaterMediaAPI.VLC_FAILED) {
        @Override
        public void remove() {} // DISABLE IT
    };

    public final String url;
    private volatile PictureFetcher seeker;
    private volatile RenderablePicture picture;
    private volatile String error;
    private volatile boolean ready = false;
    private volatile boolean isVideo = false;
    private final AtomicInteger usage = new AtomicInteger();

    public TextureCache(String url) {
        this.url = url;
        use();
        attemptToLoad();
    }

    public TextureCache(RenderablePicture picture) {
        url = null;
        process(picture);
        use();
    }
    
    private synchronized void attemptToLoad() {
        if (this.seeker != null) return;
        if (PictureFetcher.canSeek() && !this.url.isEmpty()) {
            this.seeker = new FramePictureFetcher(this, url);
        }
    }

    public IDisplay createDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop, boolean playing) {
        return createDisplay(pos, url, volume, minDistance, maxDistance, loop, playing, false);
    }
    
    public IDisplay createDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop, boolean playing, boolean noVideo) {
        volume *= Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
        if (picture == null && !noVideo && !WFConfig.isDisabledVLC()) return VideoDisplay.createVideoDisplay(pos, url, volume, minDistance, maxDistance, loop, playing);

        return new ImageDisplay(picture) {
            @Override
            public void release() { deuse(); }
        };
    }

    public void process(RenderablePicture picture) {
        if (ready) return;
        this.ready = true;
        this.seeker = null;
        this.picture = picture;
        this.error = null;
        this.isVideo = false;
    }
    
    public void processVideo() {
        this.ready = true;
        this.seeker = null;
        this.picture = null;
        this.error = null;
        this.isVideo = true;
    }
    
    public void processFailed(String error) {
        this.error = error;
        this.seeker = null;
        this.picture = null;
        this.ready = true;
        this.isVideo = false;
    }
    
    public synchronized boolean ready() {
        if (ready || seeker == null) return true;
        attemptToLoad();
        return false;
    }
    
    public void reload() {
        remove();
        error = null;
        attemptToLoad();
    }

    public String getError() { return error; }
    public boolean isVideo() { return isVideo; }
    public void use() { usage.incrementAndGet(); }
    public void deuse() { usage.decrementAndGet(); }
    public boolean isUsed() { return usage.get() > 0; }
    
    public void remove() {
        ready = false;
        if (picture != null) {
            picture.release();
            Arrays.fill(picture.textures, -1);
        }
        picture = null;
        seeker = null;
    }

    public static void clientTick() { CACHE.values().removeIf(o -> !o.isUsed()); }
    public static void renderTick() { VideoDisplay.tick(); }
    public static void reloadAll() { for (var cache : CACHE.values()) cache.reload(); }
    public static void unload() { for (TextureCache cache : CACHE.values()) cache.remove(); CACHE.clear(); }

    public static TextureCache find(String url) {
        TextureCache cache = CACHE.get(url);
        if (cache != null) {
            cache.use();
            CACHE.put(url, cache);
            return cache;
        }
        cache = new TextureCache(url);
        CACHE.put(url, cache);
        return cache;
    }

    private static final class FramePictureFetcher extends PictureFetcher {
        private final TextureCache cache;
        public FramePictureFetcher(TextureCache cache, String originalURL) {
            super(originalURL);
            this.cache = cache;
        }

        @Override
        public void onSuccess(RenderablePicture renderablePicture) {
            Minecraft.getInstance().executeBlocking(() -> cache.process(renderablePicture));
        }

        @Override
        public void onFailed(Exception e) {
            Minecraft.getInstance().executeBlocking(() -> {
                if (e instanceof VideoContentException) {
                    if (WFConfig.isDisabledVLC()) cache.processFailed("No image found");
                    else cache.processVideo();
                    return;
                }

                if (!cache.isVideo()) {
                    if (e == null) cache.processFailed("download.exception.gif");
                    else if (e.getMessage().startsWith("Server returned HTTP response code: 403")) cache.processFailed("download.exception.forbidden");
                    else if (e.getMessage().startsWith("Server returned HTTP response code: 404")) cache.processFailed("download.exception.notfound");
                    else cache.processFailed("download.exception.invalid");
                }

                LOGGER.error("An exception occurred while loading image", e);
            });
        }
    }
}
