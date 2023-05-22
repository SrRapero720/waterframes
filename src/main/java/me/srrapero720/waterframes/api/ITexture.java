package me.srrapero720.waterframes.api;

public interface ITexture {
    String VLC_FAILED = "https://i.imgur.com/UAXbZeM.jpg";

    int width();
    int height();
    int posTick();
    int durationTick();
    int tex();
    void prepare();
    default void tick() {}
    default void play() {}
    default void pause() {}
    default void stop() {}
    void release();


    default ITexture sync(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        return this;
    }
}
