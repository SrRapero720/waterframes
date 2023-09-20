package me.srrapero720.waterframes.client.displays;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.api.DataBlock;
import me.srrapero720.waterframes.core.tools.TimerTool;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageCache;
import me.srrapero720.watermedia.api.image.ImageRenderer;

public class ImageDisplay extends IDisplay {
    public static final IDisplay LOADING_GIF = new ImageDisplay(ImageAPI.loadingGif(WaterFrames.ID));
    public static final IDisplay VLC_FAILED = new ImageDisplay(ImageAPI.failedVLC());
    
    public final ImageRenderer picture;
    public final ImageCache cache;
    private int textureId;
    
    public ImageDisplay(ImageRenderer picture) {
        this.picture = picture;
        this.cache = null;
    }

    public ImageDisplay(ImageCache cache) {
        this.cache = cache;
        this.picture = cache.getRenderer();
    }
    
    @Override
    public void prepare(DataBlock data) {
        long time = (data.tick * 50L) + (data.playing ? (long) (TimerTool.deltaFrames() * 50) : 0);
        long duration = picture.duration;
        if (duration > 0 && time > duration && data.loop) time %= duration;
        textureId = picture.texture(time);
    }

    @Override
    public int texture() { return textureId; }
    
    @Override
    public void release() { if (cache != null) cache.deuse(); }
    
    @Override
    public int width() { return picture.width; }
    
    @Override
    public int height() { return picture.height; }

    @Override
    public boolean canTick() { return true; }
}