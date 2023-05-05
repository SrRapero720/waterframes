package me.srrapero720.waterframes.display;

import me.srrapero720.waterframes.display.texture.TextureCache;
import me.srrapero720.waterframes.watercore_supplier.WCoreUtil;
import team.creative.creativecore.client.CreativeCoreClient;

public class PictureDisplay implements IDisplay {
    
    public final TextureCache texture;
    private int textureId;
    
    public PictureDisplay(TextureCache texture) {
        this.texture = texture;
    }
    
    @Override
    public void prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        long time = tick * 50L + (playing ? (long) (WCoreUtil.toDeltaFrames() * 50) : 0);
        if (texture.getDuration() > 0 && time > texture.getDuration())
            if (loop)
                time %= texture.getDuration();
        textureId = texture.getTexture(time);
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
        texture.unuse();
    }
    
    @Override
    public int getWidth() {
        return texture.getWidth();
    }
    
    @Override
    public int getHeight() {
        return texture.getHeight();
    }
    
}
