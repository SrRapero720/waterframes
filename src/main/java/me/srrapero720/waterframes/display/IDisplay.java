package me.srrapero720.waterframes.display;

public interface IDisplay {
    
    int getWidth();
    
    int getHeight();
    
    void prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);
    
    void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);

    default int maxTick() {
        return 0;
    }
    
    void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);
    
    void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);
    
    int texture();
    
    void release();
    
}
