package me.srrapero720.waterframes.rendering.media;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.api.IDisplay;
import me.srrapero720.waterframes.api.IMediaData;
import me.srrapero720.waterframes.display.MediaDisplay;
import me.srrapero720.waterframes.display.PictureDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import team.creative.creativecore.common.util.math.vec.Vec3d;

public class PictureData implements IMediaData {
    public final String url;
    public int[] tex;

    public PictureData(String url) {
        this.url = url;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void tick() {

    }

    @Override
    public void renderTick() {

    }

    @Override
    public void unload() {

    }

    @Override
    public void reload() {

    }

    @Override
    public void process() {

    }

    @Override
    public IDisplay getDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop, boolean noVideo) {
        volume *= Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);

        if (tex == null && !noVideo && !WFConfig.isDisabledVLC()) return MediaDisplay.createVideoDisplay(pos, url, volume, minDistance, maxDistance, loop);
        return new PictureDisplay(this);
    }

    @Override
    public int getTextureID() {

        return 0;
    }

    @Override
    public byte[] getTexture() {

        return new byte[0];
    }
}
