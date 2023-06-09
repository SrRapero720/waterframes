package me.srrapero720.waterframes.displays;

import me.srrapero720.waterframes.api.IDisplay;
import me.srrapero720.waterframes.display.texture.TextureData;
import me.srrapero720.waterframes.watercore_supplier.WCoreUtil;

public class ImageDisplay extends IDisplay {
    private final TextureData picture;

    private int texID;
    public ImageDisplay(TextureData picture) {
        this.picture = picture;
    }
    
    @Override
    public void prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        long time = tick * 50L + (playing ? (long) (WCoreUtil.toDeltaFrames() * 50) : 0);
        if (picture.getDuration() > 0 && time > picture.getDuration())
            if (loop) time %= picture.getDuration();
        texID = picture.getTexture(time);
    }

    @Override public int getTexID() { return texID; }
    @Override public int getWidth() { return picture.getWidth(); }
    @Override public int getHeight() { return picture.getHeight(); }
    @Override public long getDuration() { return 0; }
    @Override public int getFrameCount() { return picture.textures.length; }
    @Override public void release() { picture.unuse(); }
    @Override public Type getType() { return Type.IMAGE; }
}
