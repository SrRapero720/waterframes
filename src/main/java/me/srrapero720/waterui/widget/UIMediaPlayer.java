package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.core.Element;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.watermedia.api.player.GFXEngine;
import org.watermedia.api.player.MRL;
import org.watermedia.api.player.MediaPlayer;
import org.watermedia.api.player.PlayerAPI;
import org.watermedia.api.player.SFXEngine;

import java.net.URI;

import static me.srrapero720.waterui.WaterUI.LOGGER;

/**
 * Renders a media player's video frame, letterboxed to its content box. Two creation modes:
 * wrapping (around an existing player it does not own) and owning (from a URI and source index,
 * creating its own player with exclusive GFX and SFX engines). WATERMedia is an optional
 * dependency — without it the element loads but draws nothing.
 */
public class UIMediaPlayer extends Element {
    private static final Marker IT = MarkerManager.getMarker(UIMediaPlayer.class.getSimpleName());
    private static final int DEFAULT_WIDTH = 48;
    private static final int DEFAULT_HEIGHT = 27;

    // CLASSLOAD PROBE: EVERY WATERMEDIA REFERENCE LIVES INSIDE Media, SO THIS CLASS ALWAYS LOADS
    private static final boolean READY = probe();

    private Media media;

    /**
     * Wraps an existing media player. The element renders its texture but never releases it;
     * dispose only drops the texture registration. The argument must be a
     * {@code org.watermedia.api.player.MediaPlayer} passed as Object for isolation.
     */
    public UIMediaPlayer wrap(Object player) {
        this.close();
        if (player == null) return this;
        if (!READY) {
            LOGGER.warn(IT, "WATERMedia is not present; UIMediaPlayer cannot wrap a player");
            return this;
        }
        try {
            this.media = Media.wrap(player);
            LOGGER.debug(IT, "media player wrapped");
        } catch (Throwable t) {
            LOGGER.error(IT, "UIMediaPlayer wrap failed", t);
            this.media = null;
        }
        return this;
    }

    /** Creates an owning player from a URI and source index. Releases any previous session. */
    public UIMediaPlayer source(URI uri, int sourceIndex) {
        this.close();
        if (uri == null) return this;
        if (!READY) {
            LOGGER.warn(IT, "WATERMedia is not present; UIMediaPlayer cannot create a player");
            return this;
        }
        try {
            this.media = Media.own(uri, sourceIndex);
            LOGGER.debug(IT, "media session created for {} (source {})", uri, sourceIndex);
        } catch (Throwable t) {
            LOGGER.error(IT, "UIMediaPlayer creation failed for {}", uri, t);
            this.media = null;
        }
        return this;
    }

    /** String form of {@link #source(URI, int)}; a malformed URL is silently ignored. */
    public UIMediaPlayer source(String url, int sourceIndex) {
        if (url == null || url.isBlank()) return this.source((URI) null, sourceIndex);
        try {
            return this.source(URI.create(url.trim()), sourceIndex);
        } catch (IllegalArgumentException e) {
            LOGGER.warn(IT, "UIMediaPlayer invalid URL: {}", url);
            return this;
        }
    }

    /** Drops the current session; wrapping mode frees its texture, owning mode releases the player. */
    public void release() {
        this.close();
    }

    @Override
    public void dispose() {
        this.release();
    }

    @Override
    public boolean ownsResources() {
        return true;
    }

    private void close() {
        if (media != null) {
            try {
                media.release();
            } catch (Throwable t) {
                LOGGER.error(IT, "UIMediaPlayer release failed", t);
            }
            this.media = null;
        }
    }

    @Override
    public void tick() {
        if (media == null) return;
        try {
            media.box(contentWidth(), contentHeight());
            media.tick();
        } catch (Throwable t) {
            LOGGER.error(IT, "UIMediaPlayer tick failed", t);
            this.close();
        }
    }

    @Override
    protected int prefContentWidth(int available) {
        return DEFAULT_WIDTH;
    }

    @Override
    protected int prefContentHeight(int width, int available) {
        return DEFAULT_HEIGHT;
    }

    @Override
    protected int minContentWidth(int available) {
        return 12;
    }

    @Override
    protected int minContentHeight(int width, int available) {
        return 12;
    }

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        ResourceLocation texture;
        try {
            texture = media != null ? media.texture() : null;
        } catch (Throwable t) {
            LOGGER.error(IT, "UIMediaPlayer texture failed", t);
            this.close();
            texture = null;
        }
        if (texture == null) return;

        MediaTexture.blit(graphics, texture, contentX(), contentY(), contentWidth(), contentHeight(),
                media.width(), media.height());
    }

    private static boolean probe() {
        try {
            Class.forName("org.watermedia.api.player.PlayerAPI");
            return true;
        } catch (Throwable t) {
            LOGGER.warn(IT, "WATERMedia is not present; UIMediaPlayer will draw nothing");
            return false;
        }
    }

    // MEDIA SESSION: WRAPPING OR OWNING. ONLY THIS CLASS NAMES WATERMEDIA TYPES SO IT NEVER
    // CLASSLOADS WHEN THE PROBE FAILED. WRAPPING DOES NOT OWN THE PLAYER; OWNING RELEASES IT
    private static final class Media {
        private MediaPlayer player;
        private MRL mrl;
        private int sourceIndex;
        private boolean owned;
        private GFXEngine gfx;
        private SFXEngine sfx;
        // GL TEXTURE REGISTRATION SHARED WITH Thumbnail: SWAP-AWARE, FREES ONLY ITS OWN
        private final MediaTexture managed = new MediaTexture("media_");
        private boolean released;

        // WIDGET CONTENT BOX IN GUI PIXELS, PUSHED BY THE OWNER EACH TICK; DRIVES THE UPLOAD CAP
        private int boxW, boxH;
        // LAST APPLIED scale SO REDUNDANT CALLS ARE SKIPPED
        private int cropW, cropH;

        static Media wrap(Object wrapped) {
            Media m = new Media();
            m.player = (MediaPlayer) wrapped;
            m.owned = false;
            return m;
        }

        static Media own(URI uri, int sourceIndex) {
            Media m = new Media();
            m.mrl = PlayerAPI.mrl(uri);
            m.sourceIndex = sourceIndex;
            m.owned = true;
            return m;
        }

        void box(int width, int height) {
            this.boxW = width;
            this.boxH = height;
        }

        // OPENS THE PLAYER WHEN THE MRL RESOLVES; NO-OP FOR WRAPPING MODE
        void tick() {
            if (released || !owned) return;
            if (player != null) {
                // FF RESOLVES ITS UPLOAD TARGET PER FRAME, SO A BOX/GUI-SCALE CHANGE STILL APPLIES LIVE
                this.applyCap();
                return;
            }
            if (mrl == null || mrl.status().failed()) return;
            if (!mrl.status().loaded()) return;
            // WAIT FOR THE FIRST LAYOUT: THE CAP MUST BE KNOWN BEFORE start() SO THE FIRST FRAME IS
            // ALREADY DECODED AT THE WIDGET'S SIZE INSTEAD OF AT FULL SIZE
            if (boxW <= 0 || boxH <= 0) return;
            // PER-INSTANCE GFX AND SFX ENGINES; THE PLAYER OWNS THEM FROM BIRTH
            Minecraft client = Minecraft.getInstance();
            this.player = PlayerAPI.player(mrl, sourceIndex,
                    () -> this.gfx = PlayerAPI.glEngine(client.gameThread, client),
                    () -> this.sfx = PlayerAPI.alEngine());
            if (this.player == null) {
                this.gfx = null;
                this.sfx = null;
                return;
            }
            this.applyCap();
            this.player.start();
            LOGGER.debug(IT, "media player started (source {}) capped at {}x{}", sourceIndex, cropW, cropH);
        }

        // TEXTURE TO BLIT, OR NULL WHILE LOADING OR UNAVAILABLE
        ResourceLocation texture() {
            if (released || player == null || !player.canPlay()) return null;
            int id = (int) player.texture();
            if (id <= 0) return null;
            return managed.locate(id);
        }

        int width() {
            return player != null ? player.width() : 0;
        }

        int height() {
            return player != null ? player.height() : 0;
        }

        // CAPS THE UPLOAD TO THE WIDGET'S OWN BOX AT FRAMEBUFFER RESOLUTION: THE PER-AXIS CAPS
        // FORCE THE BOX ASPECT WITHOUT KNOWING THE SOURCE. A WRAPPED PLAYER IS NEVER CAPPED
        private void applyCap() {
            if (player == null || !owned || boxW <= 0 || boxH <= 0) return;
            double scale = Minecraft.getInstance().getWindow().getGuiScale();
            int cw = Math.max(1, (int) Math.ceil(boxW * scale));
            int ch = Math.max(1, (int) Math.ceil(boxH * scale));
            if (cw == cropW && ch == cropH) return;
            this.cropW = cw;
            this.cropH = ch;
            player.scale(cw, ch);
        }

        void release() {
            if (released) return;
            this.released = true;
            LOGGER.debug(IT, "media session released ({})", owned ? "owned" : "wrapped");
            this.mrl = null;
            if (owned && player != null) {
                try {
                    // THE PLAYER OWNS THE ENGINES: THIS RELEASE FREES BOTH
                    player.release();
                } catch (Throwable t) {
                    LOGGER.error(IT, "media player failed to release", t);
                }
            }
            this.player = null;
            this.gfx = null;
            this.sfx = null;
            this.managed.free();
        }
    }
}
