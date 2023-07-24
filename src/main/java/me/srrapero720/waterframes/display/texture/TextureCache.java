package me.srrapero720.waterframes.display.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.display.IDisplay;
import me.srrapero720.waterframes.display.VideoDisplay;
import me.srrapero720.waterframes.display.ImageDisplay;
import me.srrapero720.watermedia.api.images.PictureFetcher;
import me.srrapero720.watermedia.api.images.RenderablePicture;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import java.util.LinkedHashMap;
import java.util.Map;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class TextureCache {
    private static final Map<String, TextureCache> CACHE = new LinkedHashMap<>();

    public static void clientTick() {
        for (var it = CACHE.values().iterator(); it.hasNext();) {
            var type = it.next();
            if (!type.isUsed()) {
                type.remove();
                it.remove();
            }
        }
    }

    public static void renderTick() {
        VideoDisplay.tick();
    }

    public static void reloadAll() {
        for (var cache : CACHE.values())
            cache.reload();
    }

    public static void unload() {
        for (TextureCache cache : CACHE.values()) cache.remove();
        CACHE.clear();
        VideoDisplay.unload();
    }
    
    public static TextureCache get(String url) {
        TextureCache cache = CACHE.get(url);
        if (cache != null) {
            cache.use();
            return cache;
        }
        cache = new TextureCache(url);
        CACHE.put(url, cache);
        return cache;
    }
    
    public final String url;
    private boolean isVideo;
    
    private PictureFetcher seeker;
    private RenderablePicture picture;
    private boolean ready = false;
    private String error;
    
    private int usage = 0;

    public TextureCache(String url) {
        this.url = url;
        use();
        trySeek();
    }
    
    private void trySeek() {
        if (seeker != null) return;
        if (PictureFetcher.canSeek() && !this.url.isEmpty()) {
            this.seeker = new PictureFetcher(url) {
                @Override
                public void onFailed(Exception e) {
                    if (e instanceof VideoContentException) {
                        if (WFConfig.isDisabledVLC()) processFailed("No image found");
                        else processVideo();
                        return;
                    }
                    LOGGER.error("An exception occurred while loading Waterframes image", e);

                    if (!isVideo) {
                        if (e == null) processFailed("download.exception.gif");
                        else if (e.getMessage().startsWith("Server returned HTTP response code: 403")) processFailed("download.exception.forbidden");
                        else if (e.getMessage().startsWith("Server returned HTTP response code: 404")) processFailed("download.exception.notfound");
                        else processFailed("download.exception.invalid");
                    }
                }

                @Override
                public void onSuccess(RenderablePicture renderablePicture) {
                    Minecraft.getInstance().executeBlocking(() -> process(renderablePicture));
                }
            };
        }
    }

    public IDisplay createDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop) {
        return createDisplay(pos, url, volume, minDistance, maxDistance, loop, false);
    }
    
    public IDisplay createDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop, boolean noVideo) {
        volume *= Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
        if (picture == null && !noVideo && !WFConfig.isDisabledVLC()) return VideoDisplay.createVideoDisplay(pos, url, volume, minDistance, maxDistance, loop);
        return new ImageDisplay(picture) {
            @Override
            public void release() {
                unuse();
            }
        };
    }
    
    public String getError() {
        return error;
    }

    public void process(RenderablePicture picture) {
        this.picture = picture;
        this.ready = true;
        this.error = null;
        this.isVideo = false;
    }
    
    public void processVideo() {
        this.error = null;
        this.isVideo = true;
        this.ready = true;
        this.seeker = null;
    }
    
    public void processFailed(String error) {
        this.error = error;
        this.ready = true;
        this.seeker = null;
    }
    
    public boolean ready() {
        if (ready) return true;
        trySeek();
        return false;
    }
    
    public boolean isVideo() {
        return isVideo;
    }
    
    public void reload() {
        remove();
        error = null;
        trySeek();
    }
    
    public void use() {
        usage++;
    }
    
    public void unuse() {
        usage--;
    }
    
    public boolean isUsed() {
        return usage > 0;
    }
    
    public void remove() {
        ready = false;
        if (picture != null) {
            if (picture.textures != null) for (int texture: picture.textures) GlStateManager._deleteTexture(texture);
            if (picture.image != null) picture.image.flush();
            if (picture.decoder != null) {
                for (int i = 0; i < picture.decoder.getFrameCount(); i++) {
                    picture.decoder.getFrame(i).flush();
                }
            }
            picture = null;
        }
    }
}
