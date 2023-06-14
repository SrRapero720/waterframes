package me.srrapero720.waterframes.displays;

import me.srrapero720.waterframes.FramesUtil;
import me.srrapero720.waterframes.display.texture.TextureData;
import me.srrapero720.waterframes.watercore_supplier.WCoreUtil;
import me.srrapero720.watermedia.api.util.GifDecoder;

import java.awt.image.BufferedImage;

public class ImageDisplay extends IDisplay {
    private long duration;
    private long[] delay;
    private int[] textures;
    private GifDecoder decoder;
    private int remaining;
    private int texID;

    public ImageDisplay(String url) {


    }
    public ImageDisplay ready(BufferedImage image) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.textures = new int[] { FramesUtil.preRender(image, width, height) };
        this.delay = new long[] { 0 };
        this.duration = 0;
        this.ready = true;
        return this;
    }

    public ImageDisplay ready(GifDecoder image) {
        this.decoder = image;
        var frame = decoder.getFrameSize();
        this.width = frame.width;
        this.height = frame.height;
        this.textures = new int[decoder.getFrameCount()];
        this.remaining = decoder.getFrameCount();
        long time = 0;
        for (int i = 0; i < decoder.getFrameCount(); i++) {
            textures[i] = -1;
            delay[i] = time;
            time += decoder.getDelay(i);
        }
        duration = time;
        ready = true;
        return this;
    }

    public ImageDisplay(TextureData picture) {

    }
    
    @Override
    public void prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (!ready) return;
        long time = tick * 50L + (playing ? (long) (WCoreUtil.toDeltaFrames() * 50) : 0);
        if (duration > 0 && time > duration)
            if (loop) time %= duration;
        texID = getTexture(time);
    }

    @Override public int getTexID() { return texID; }
    @Override public int getWidth() { return picture.getWidth(); }
    @Override public int getHeight() { return picture.getHeight(); }
    @Override public long getDuration() { return 0; }
    @Override public int getFrameCount() { return picture.textures.length; }
    @Override public void release() { picture.unuse(); }
    @Override public Type getType() { return Type.IMAGE; }

    private int getTexture(int index) {
        if (textures[index] == -1 && decoder != null) {
            textures[index] = FramesUtil.preRender(decoder.getFrame(index), width, height);
            remaining--;
            if (remaining <= 0)
                decoder = null;
        }
        return textures[index];
    }

    public int getTexture(long time) {
        if (textures == null) return -1;
        if (textures.length == 1) return getTexture(0);
        int last = getTexture(0);
        for (int i = 1; i < delay.length; i++) {
            if (delay[i] > time) break;
            last = getTexture(i);
        }
        return last;
    }
}
