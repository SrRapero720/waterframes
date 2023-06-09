package me.srrapero720.waterframes.api;

public abstract class IDisplay {
    public enum Type { VIDEO, IMAGE, GIF; }

    public abstract int getWidth();
    public abstract int getHeight();
    public abstract long getDuration();
    public int getFrameCount() { return 0; }
    public abstract void prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);
    public void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}
    public int maxTick() {
        return 0;
    }
    public void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}
    public void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}

    public abstract Type getType();
    public abstract int getTexID();
    public abstract void release();
    
}
