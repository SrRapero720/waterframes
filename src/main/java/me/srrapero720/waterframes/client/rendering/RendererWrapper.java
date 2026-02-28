package me.srrapero720.waterframes.client.rendering;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;
import org.watermedia.api.image.ImageRenderer;
import org.watermedia.api.math.MathAPI;

@Environment(EnvType.CLIENT)
public class RendererWrapper extends AbstractTexture {
    private final ImageRenderer renderer;
    private final GlTexture[] glTextures;
    private final GpuTextureView[] glTextureViews;

    public RendererWrapper(final ImageRenderer imageRenderer) {
        super();
        this.renderer = imageRenderer;
        this.glTextures = new GlTexture[this.renderer.textures.length];
        this.glTextureViews = new GpuTextureView[this.renderer.textures.length];
        var device = RenderSystem.getDevice();
        for (int i = 0; i < this.glTextures.length; i++) {
            this.glTextures[i] = new GlTexture(GpuTexture.USAGE_TEXTURE_BINDING, "texturewrapper_" + this.renderer.texture(i), TextureFormat.RGBA8, this.renderer.width, this.renderer.height, 1, 1, this.renderer.texture(i)) {
                @Override public void close() { super.close(); }
            };
            this.glTextures[i].setTextureFilter(FilterMode.NEAREST, false);
            this.glTextureViews[i] = device.createTextureView(this.glTextures[i]);
            GlStateManager._bindTexture(0);
        }
        this.texture = this.glTextures[0];
        this.textureView = this.glTextureViews[0];
    }

    @Override
    public @NotNull GpuTexture getTexture() {
        int idx = getCurrentFrameIndex();
        return this.texture = this.glTextures[idx];
    }

    @Override
    public @NotNull GpuTextureView getTextureView() {
        int idx = getCurrentFrameIndex();
        return this.textureView = this.glTextureViews[idx];
    }

    private int getCurrentFrameIndex() {
        final int id = this.renderer.texture(WaterFrames.getTicks(), MathAPI.tickToMs(WaterFrames.deltaFrames()), true);
        for (int i = 0; i < this.renderer.textures.length; i++) {
            if (this.glTextures[i].glId() == id) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void close() {
        // NO OP
    }
}