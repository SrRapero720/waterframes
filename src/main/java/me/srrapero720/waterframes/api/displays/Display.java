package me.srrapero720.waterframes.api.displays;

import team.creative.creativecore.common.util.math.vec.Vec3d;

public abstract class Display {
    protected int width;
    protected int height;
    protected boolean ready = false;
    public enum Type { VIDEO, IMAGE, GIF; }

    public static Display create() {
        return null;
    }

    public abstract int getWidth();
    public abstract int getHeight();
    public abstract long getDuration();
    public int getFrameCount() { return 0; }
    public abstract void prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);
    public void tick(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}
    public int maxTick() { return 0; }
    public void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}
    public void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}

    public abstract Type getType();
    public abstract int getTexID();
    public abstract void release();
    
}
