package me.srrapero720.waterframes.client.rendering;

import me.srrapero720.waterframes.WaterFrames;
import org.watermedia.api.image.ImageRenderer;
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

    @OnlyIn(Dist.CLIENT)
    public static class Renderer extends AbstractTexture {
        private final ImageRenderer renderer;

        public Renderer(ImageRenderer imageRenderer) {
            this.renderer = imageRenderer;
        }

        @Override
        public int getId() {
            return renderer.texture(WaterFrames.getTicks(), WaterFrames.deltaFrames(), true);
        }

        @Override
        public void load(ResourceManager resourceManager) {}
    }
}
