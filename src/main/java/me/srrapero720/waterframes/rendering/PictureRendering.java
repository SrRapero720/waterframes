package me.srrapero720.waterframes.rendering;

import me.srrapero720.waterframes.api.RenderDisplay;
import me.srrapero720.waterframes.display.texture.TextureData;
import me.srrapero720.waterframes.watercore_supplier.WCoreUtil;

public class PictureRendering extends RenderDisplay {
    private final TextureData texData;
    private int texID;
    public PictureRendering(TextureData texData) { this.texData = texData; }
    
    @Override
    public void prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        long time = tick * 50L + (playing ? (long) (WCoreUtil.toDeltaFrames() * 50) : 0);
        if (texData.getDuration() > 0 && time > texData.getDuration())
            if (loop) time %= texData.getDuration();
        texID = texData.getTexture(time);
    }

    @Override public int getTexID() { return texID; }
    @Override public int getWidth() { return texData.getWidth(); }
    @Override public int getHeight() { return texData.getHeight(); }
    @Override public void release() { texData.unuse(); }
}
