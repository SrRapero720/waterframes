package me.srrapero720.waterframes.custom.displayers;

public abstract class DisplayerApi {
    
    public abstract int getWidth();
    
    public abstract int getHeight();
    
    public abstract void prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);
    
    public abstract void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);
    
    public abstract void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);
    
    public abstract void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);
    
    public abstract int texture();
    
    public abstract void release();
    
}
