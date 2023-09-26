package me.srrapero720.waterframes.api.display;

import me.srrapero720.waterframes.api.data.BasicData;
import net.minecraft.core.BlockPos;

public class TextureDisplay extends IDisplay {
    static final TextureDisplay VIDEO_FAILED = new TextureDisplay();

    @Override
    public int width() {
        return 0;
    }

    @Override
    public int height() {
        return 0;
    }

    @Override
    public int texture() {
        return 0;
    }

    @Override
    public void preRender() {

    }

    @Override
    public int durationInTicks() {
        return 0;
    }

    @Override
    public long duration() {
        return 0;
    }

    @Override
    public boolean canTick() {
        return false;
    }

    @Override
    public void tick(BlockPos pos) {

    }

    @Override
    public boolean isBuffering() {
        return false;
    }

    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void release() {

    }
}