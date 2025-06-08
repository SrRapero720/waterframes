package me.srrapero720.waterframes.client.rendering;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.util.TriState;
import org.watermedia.api.image.ImageRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextureWrapper extends AbstractTexture {
    private final int id;

    public TextureWrapper(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setFilter(TriState _param1, boolean _param2) {
        this.texture = ((GlDevice) RenderSystem.getDevice()).createExternalTexture("texturewrapper_" + this.getId(), this.getId());

        super.setFilter(_param1, _param2);
    }

    @Override public void close() { /* NO OP */}

    @OnlyIn(Dist.CLIENT)
    public static class Renderer extends TextureWrapper {
        private final ImageRenderer renderer;

        public Renderer(ImageRenderer imageRenderer) {
            super(-1);
            this.renderer = imageRenderer;
        }

        public int getId() {
            return renderer.texture(WaterFrames.getTicks(), WaterFrames.deltaFrames(), true);
        }
    }
}
