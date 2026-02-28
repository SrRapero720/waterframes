package me.srrapero720.waterframes.client.rendering;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.renderer.texture.AbstractTexture;

public class TextureWrapper extends AbstractTexture {
    public TextureWrapper(final int id, final int width, final int height) {
        this.texture = new GlTexture(GpuTexture.USAGE_TEXTURE_BINDING, "texturewrapper_" + id, TextureFormat.RGBA8, width, height, 1, 1, id) {
            @Override public void close() { super.close(); }
        };
        this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
    }
}