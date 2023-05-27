package me.srrapero720.waterframes.display.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import me.srrapero720.waterframes.DisplayUtil;
import me.srrapero720.waterframes.watercore_supplier.GifDecoder;
import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.api.RenderDisplay;
import me.srrapero720.waterframes.rendering.VLCRendering;
import me.srrapero720.waterframes.rendering.PictureRendering;
import me.srrapero720.waterframes.watercore_supplier.ThreadUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

public class TextureData {
    private static final Map<String, TextureData> cached = new LinkedHashMap<>();

    public static void tick() {
        for (var iterator = cached.values().iterator(); iterator.hasNext();) {
            var type = iterator.next();
            if (!type.isUsed()) {
                type.remove();
                iterator.remove();
            }
        }
    }

    public static void reloadAll() {
        for (var cache : cached.values()) cache.reload();
    }

    public static void unload(Level event) {
        for (TextureData cache : cached.values()) cache.remove();
        cached.clear();
    }
    
    public static TextureData get(String url) {
        TextureData cache = cached.get(url);
        if (cache != null) {
            cache.use();
            return cache;
        }
        cache = new TextureData(url);
        cached.put(url, cache);
        return cache;
    }
    
    public final String url;
    private int[] textures;
    private int width;
    private int height;
    private long[] delay;
    private long duration;
    private boolean isVideo;
    
    private PictureFetch seeker;
    private boolean ready = false;
    private String error;
    
    private int usage = 0;
    
    private GifDecoder decoder;
    private int remaining;
    
    public TextureData(String url) {
        this.url = url;
        usage++;
        if (PictureFetch.canBeSeeked() && url != null && !url.isEmpty()) this.seeker = new PictureFetch(this);
    }
    
    private void trySeek() {
        if (seeker != null) return;
        if (PictureFetch.canBeSeeked()) this.seeker = new PictureFetch(this);
    }
    
    private int getTexture(int index) {
        if (textures[index] == -1 && decoder != null) {
            textures[index] = DisplayUtil.preRender(decoder.getFrame(index), width, height);
            remaining--;
            if (remaining <= 0)
                decoder = null;
        }
        return textures[index];
    }
    
    public int getTexture(long time) {
        if (textures == null)
            return -1;
        if (textures.length == 1)
            return getTexture(0);
        int last = getTexture(0);
        for (int i = 1; i < delay.length; i++) {
            if (delay[i] > time)
                break;
            last = getTexture(i);
        }
        return last;
    }

    public RenderDisplay createDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop, boolean noVideo) {
        var mcVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
        var vol = volume * (mcVolume * mcVolume);
        var cache = TextureData.get(RenderDisplay.VLC_FAILED);

        if (textures == null && !noVideo && !DisplayConfig.isDisabledVLC())
            return ThreadUtil.tryAndReturn(defaultVar ->
                    new VLCRendering(pos, url, vol, minDistance, maxDistance, loop),
                    cache.ready() ? cache.createDisplay(pos, url, volume, minDistance, maxDistance, loop, noVideo) : null);

        return new PictureRendering(this);
    }
    
    public String getError() {
        return error;
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
        textures = new int[] { DisplayUtil.preRender(image, width, height) };
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
    
    public boolean ready() {
        if (ready)
            return true;
        trySeek();
        return false;
    }

    @Deprecated
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
        if (textures != null) for (int texture: textures) GlStateManager._deleteTexture(texture);
    }
    
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public long getDuration() { return duration; }
}
