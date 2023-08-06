package me.srrapero720.waterframes.display;

import me.srrapero720.waterframes.watercore_supplier.WCoreUtil;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.images.ImageCache;
import me.srrapero720.watermedia.api.images.ImageRenderer;
import me.srrapero720.watermedia.util.WaterOs;

public class ImageDisplay implements IDisplay {
    public static final IDisplay LOADING_GIF = new ImageDisplay(WaterMediaAPI.LOADING_GIF);
    public static final IDisplay VLC_FAILED = new ImageDisplay(WaterOs.getArch().wrapped ? WaterMediaAPI.VLC_FAILED : WaterMediaAPI.VLC_FAILED_INSTALL);
    
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
    public void prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        long time = tick * 50L + (playing ? (long) (WCoreUtil.toDeltaFrames() * 50) : 0);
        long duration = picture.duration;
        if (duration > 0 && time > duration && loop) time %= duration;
        textureId = picture.textureId(time);
    }
    
    @Override
    public void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}
    
    @Override
    public void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}
    
    @Override
    public void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}
    
    @Override
    public int texture() {
        return textureId;
    }
    
    @Override
    public void release() {
        if (cache != null) cache.deuse();
    }
    
    @Override
    public int getWidth() { return picture.width; }
    
    @Override
    public int getHeight() { return picture.height; }
    
}
