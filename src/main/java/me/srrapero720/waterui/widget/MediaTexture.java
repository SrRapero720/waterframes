package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.WaterUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * GL texture a media player owns, exposed to the texture manager under a stable handle. The
 * registration follows the player's texture swaps and frees exactly what it registered, so a
 * swapped texture never leaks and never frees another session's id. Names no WATERMedia type,
 * so it loads even when the media probe failed.
 */
final class MediaTexture {
    private final String prefix;
    private ResourceLocation location;
    private int glId;

    MediaTexture(String prefix) {
        this.prefix = prefix;
    }

    // THE PLAYER MAY SWAP ITS GL TEXTURE OVER ITS LIFE; RE-REGISTER AND DROP THE STALE ONE WHEN IT DOES
    ResourceLocation locate(int id) {
        if (location == null || glId != id) {
            this.free();
            this.glId = id;
            this.location = WaterUI.id(prefix + id);
            Minecraft.getInstance().getTextureManager().register(location, new GlTexture(id));
        }
        return location;
    }

    // DROPS EXACTLY THE TEXTURE THIS SESSION REGISTERED, NEVER ANOTHER'S THAT SHARES A GL ID
    void free() {
        if (location == null) return;
        Minecraft.getInstance().getTextureManager().release(location);
        this.location = null;
        this.glId = 0;
    }

    /** Letterboxed blit: the frame keeps its aspect inside the box instead of being stretched into it. */
    static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int boxWidth, int boxHeight,
                     int mediaWidth, int mediaHeight) {
        mediaWidth = Math.max(1, mediaWidth);
        mediaHeight = Math.max(1, mediaHeight);
        float scale = Math.min(boxWidth / (float) mediaWidth, boxHeight / (float) mediaHeight);
        int width = Math.max(1, Math.round(mediaWidth * scale));
        int height = Math.max(1, Math.round(mediaHeight * scale));
        graphics.blit(texture, x + (boxWidth - width) / 2, y + (boxHeight - height) / 2,
                0, 0, width, height, width, height);
    }

    // A GL TEXTURE ID THE MEDIA PLAYER ALREADY OWNS, WRAPPED SO THE TEXTURE MANAGER CAN HOLD IT
    // WITHOUT LOADING OR RELEASING ANYTHING: THE PLAYER STAYS THE SOLE OWNER OF THE GL RESOURCE
    private static final class GlTexture extends AbstractTexture {
        GlTexture(int id) {
            this.id = id;
        }

        @Override public int getId() {
            return this.id;
        }

        @Override public void load(ResourceManager manager) { /* NO OP */ }
        @Override public void releaseId() { /* NO OP */ }
        @Override public void close() { /* NO OP */ }
    }
}
