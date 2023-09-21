package me.srrapero720.waterframes.api.displays;

import me.lib720.watermod.safety.TryCore;
import me.srrapero720.waterframes.api.data.BasicData;
import me.srrapero720.watermedia.api.image.ImageCache;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.vec.Vec3d;

@OnlyIn(Dist.CLIENT)
public abstract class BaseDisplay {

    public static BaseDisplay create(ImageRenderer image) { return new ImageDisplay(image); }
    public static BaseDisplay create(ImageCache image) { return new ImageDisplay(image); }
    public static BaseDisplay create(Vec3d pos, BasicData data) { return create(pos, data, false); }
    public static BaseDisplay create(Vec3d pos, BasicData data, boolean noVideo) {
        return TryCore.withReturn(defaultVar -> {
            VideoDisplay display = new VideoDisplay(pos, data.url, data.volume, data.minVolumeDistance, data.maxVolumeDistance, data.loop, data.playing);
            if (display.videoPlayer.raw() != null) return display;
            display.release();
            return defaultVar;
        }, ImageDisplay.VLC_FAILED);
    }
    public abstract int width();
    public abstract int height();
    public abstract int texture();
    public int maxTick() { return 0; }
    public abstract boolean canTick();
    public abstract void prepare(BasicData data);
    public void pause(BasicData data) {}
    public void resume(BasicData data) {}
    public void tick(BasicData data) {}
    public abstract void release();
}