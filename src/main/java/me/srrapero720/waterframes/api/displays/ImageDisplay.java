package me.srrapero720.waterframes.api.displays;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.api.data.BasicData;
import me.srrapero720.waterframes.core.WaterNet;
import me.srrapero720.waterframes.core.tools.TimerTool;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageCache;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import net.minecraft.core.BlockPos;

public class ImageDisplay extends BaseDisplay {
    public static final BaseDisplay LOADING_GIF = new ImageDisplay(ImageAPI.loadingGif(WaterFrames.ID));
    public static final BaseDisplay VLC_FAILED = new ImageDisplay(ImageAPI.failedVLC());
    
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
    public void prepare(BasicData data) {
        long time = WaterMediaAPI.math_ticksToMillis(data.tick) + (data.playing ? (long) (TimerTool.deltaFrames() * 50) : 0);
        long duration = picture.duration;
        if (duration > 0 && time > duration && data.loop) time %= duration;
        textureId = picture.texture(time);
    }

    @Override
    public void tick(BlockPos pos, BasicData data) {
        if (syncTick) return;
        WaterNet.syncMaxTickTime(pos, maxTick());
        syncTick = true;
    }

    @Override
    public int maxTick() {
        return WaterMediaAPI.math_millisToTicks(picture.duration);
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