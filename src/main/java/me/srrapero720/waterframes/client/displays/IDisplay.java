package me.srrapero720.waterframes.client.displays;

import me.srrapero720.waterframes.api.DataBlock;
import me.srrapero720.watermedia.api.image.ImageCache;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import team.creative.creativecore.common.util.math.vec.Vec3d;

public abstract class IDisplay {


    public static IDisplay create(Vec3d pos, DataBlock data) {
        try {
            VideoDisplay display = new VideoDisplay(pos, data.url, data.volume, data.minVolumeDistance, data.maxVolumeDistance, data.loop, data.playing);
            if (display.videoPlayer.raw() != null) return display;
            display.release();
        } catch (Exception ignored) {}
        return ImageDisplay.VLC_FAILED;
    }

    public abstract int width();
    public abstract int height();
    public abstract int texture();
    public int maxTick() { return 0; }
    public abstract boolean canTick();
    public abstract void prepare(DataBlock data);
    public void pause(DataBlock data) {}
    public void resume(DataBlock data) {}
    public void tick(DataBlock data) {}
    public abstract void release();

    static IDisplay create(ImageRenderer image) {
        return new ImageDisplay(image);
    }

    static IDisplay create(ImageCache image) {
        return new ImageDisplay(image);
    }
}