package me.srrapero720.waterframes.rendering.images;

import me.srrapero720.waterframes.display.texture.TextureCache;
import me.srrapero720.waterframes.watercore_supplier.GifDecoder;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class PictureCache {
    private static final Map<String, PictureCache> cache = new LinkedHashMap<>();


    public static void tick() {
//        for (var iterator = cache.values().iterator(); iterator.hasNext();) {
//            var type = iterator.next();
//            if (!type.isUsed()) {
//                type.remove();
//                iterator.remove();
//            }
//        }
    }

//    public static void reloadAll() { for (var cache : cache.values()) cache.reload(); }

    public static void unload(Level event) {
//        for (TextureCache cache : cache.values()) cache.remove();
//        cache.clear();
    }

    public static TextureCache get(String url) {
//        TextureCache cache = PictureCache.cache.get(url);
//        if (cache != null) {
//            cache.use();
//            return cache;
//        }
//        cache = new TextureCache(url);
//        PictureCache.cache.put(url, cache);
//        return cache;
        return null;
    }

    public final String url;
    private int[] textures;
    private int width;
    private int height;
    private long[] delay;
    private long duration;
    private boolean isVideo;

    private PictureManager seeker;
    private boolean ready = false;
    private String error;

    private int usage = 0;

    private GifDecoder decoder;
    private int remaining;

    public PictureCache(String url) {
        this.url = url;
//        use();
//        trySeek();
    }


    public void processVideo() {
        this.textures = null;
        this.error = null;
        this.isVideo = true;
        this.ready = true;
        this.seeker = null;
    }

    public void processFailed(String error) {
        this.textures = null;
        this.error = error;
        this.ready = true;
        this.seeker = null;
    }

    public void process(BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();
//        textures = new int[] { uploadFrame(image, width, height) };
        delay = new long[] { 0 };
        duration = 0;
        seeker = null;
        ready = true;
    }

    public void process(GifDecoder decoder) {
        Dimension frameSize = decoder.getFrameSize();
        width = (int) frameSize.getWidth();
        height = (int) frameSize.getHeight();
        textures = new int[decoder.getFrameCount()];
        delay = new long[decoder.getFrameCount()];

        this.decoder = decoder;
        this.remaining = decoder.getFrameCount();
        long time = 0;
        for (int i = 0; i < decoder.getFrameCount(); i++) {
            textures[i] = -1;
            delay[i] = time;
            time += decoder.getDelay(i);
        }
        duration = time;
        seeker = null;
        ready = true;
    }
}
