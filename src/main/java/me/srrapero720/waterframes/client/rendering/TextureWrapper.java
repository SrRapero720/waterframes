package me.srrapero720.waterframes.client.rendering;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.renderer.texture.AbstractTexture;

public class TextureWrapper extends AbstractTexture {
    public TextureWrapper(final int id, final int width, final int height) {
        this.texture = new GlTexture("texturewrapper_" + id, TextureFormat.RGBA8, width, height, 1, id, false) {
            @Override public void close() { super.close(); }
        };
    }
}