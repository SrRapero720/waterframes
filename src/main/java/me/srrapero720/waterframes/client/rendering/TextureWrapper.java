package me.srrapero720.waterframes.client.rendering;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextureWrapper extends AbstractTexture {
    public TextureWrapper(int id) {
        this.id = id;
    }

    @Override public int getId() {
        return this.id;
    }

    @Override public void load(ResourceManager manager) { /* NO OP */ }
    @Override public void releaseId() { /* NO OP */ }
    @Override public void close() { /* NO OP */}
}
