package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.WaterUI;
import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.theme.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.watermedia.api.media.MRL;
import org.watermedia.api.media.MediaAPI;
import org.watermedia.api.media.engines.GFXEngine;
import org.watermedia.api.media.players.MediaPlayer;

import java.net.URI;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Preview picture of a media, falling back to a plain icon while there is nothing to show.
 * The media session lives inside the element: every instance owns its player and its GFX
 * engine exclusively, and carries no sound engine at all. WATERMedia is an optional
 * dependency of the toolkit — without it the widget stays on its fallback and reports once.
 */
public class Thumbnail extends Element {
    private static final Marker IT = MarkerManager.getMarker(Thumbnail.class.getSimpleName());
    /** 16:9, the shape almost every thumbnail actually arrives in. */
    private static final int DEFAULT_WIDTH = 48;
    private static final int DEFAULT_HEIGHT = 27;

    // CLASSLOAD PROBE: EVERY WATERMEDIA REFERENCE LIVES INSIDE Media, SO THIS CLASS ALWAYS LOADS
    private static final boolean READY = probe();

    private Media media;
    private URI source;
    private Supplier<String> liveSource;
    private String liveShown;
    private Icon fallback;
    private Runnable loadListener;
    private Consumer<String> errorListener;
    private boolean errorFired;

    /** Placeholder drawn while there is no picture; none is drawn when null. */
    public Thumbnail fallback(Icon fallback) {
        this.fallback = fallback;
        return this;
    }

    /** Replaces the picture, dropping the player and the engine behind the old one. */
    public Thumbnail source(URI uri) {
        if (Objects.equals(uri, source)) return this;
        // WEB SCHEMES ONLY (L6): SOURCES ARRIVE FROM REMOTE METADATA AND DOCUMENTS, SO A file:
        // VALUE MUST NEVER OPEN A LOCAL PATH; EVERY REAL THUMBNAIL IS AN http(s) PLATFORM URI
        if (uri != null && !"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
            WaterUI.LOGGER.warn(IT, "thumbnail scheme rejected: {}", uri);
            uri = null;
        }
        if (Objects.equals(uri, source)) return this;
        this.close();
        this.errorFired = false;
        this.source = uri;
        if (uri == null) return this;
        if (!READY) {
            this.error("WATERMedia is not installed");
            return this;
        }
        // THE PROBE PASSED, YET THE BACKEND CAN STILL REFUSE THE URI OR DIE LOADING: NEVER CRASH THE SCREEN
        try {
            this.media = new Media(uri);
            this.media.onLoaded = () -> { if (loadListener != null) loadListener.run(); };
            this.media.onError = this::error;
            WaterUI.LOGGER.debug(IT, "thumbnail session opened for {}", uri);
        } catch (Throwable t) {
            WaterUI.LOGGER.error(IT, "thumbnail media session failed for {}", uri, t);
            this.media = null;
            this.error("Media session failed: " + t.getMessage());
        }
        return this;
    }

    /** String form of {@link #source(URI)}; a malformed url reports through onError. */
    public Thumbnail source(String url) {
        if (url == null || url.isBlank()) return this.source((URI) null);
        try {
            return this.source(URI.create(url.trim()));
        } catch (IllegalArgumentException e) {
            this.errorFired = false;
            this.error("Invalid URL: " + url);
            return this;
        }
    }

    /** Live form: re-read every tick, the picture follows the value. */
    public Thumbnail source(Supplier<String> url) {
        this.liveSource = url;
        this.liveShown = null;
        return this;
    }

    /** Fires once when the preview texture becomes available for the first time. */
    public Thumbnail onLoad(Runnable onLoad) {
        this.loadListener = onLoad;
        return this;
    }

    /** Fires once when the thumbnail fails to resolve, with an error string. */
    public Thumbnail onError(Consumer<String> onError) {
        this.errorListener = onError;
        return this;
    }

    /** Drops the current picture and its media session; listeners and fallback stay. */
    public void release() {
        this.close();
        this.liveSource = null;
        this.liveShown = null;
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
            media.release();
            this.media = null;
        }
        this.source = null;
    }

    private void error(String message) {
        if (errorFired) return;
        this.errorFired = true;
        if (errorListener != null) errorListener.accept(message);
    }

    @Override
    public void tick() {
        if (liveSource != null) {
            String url = liveSource.get();
            if (!Objects.equals(url, liveShown)) {
                this.liveShown = url;
                this.source(url);
            }
        }
        // THE BACKEND MUST NEVER TAKE THE GAME TICK DOWN: A DYING SESSION BECOMES AN ERROR + FALLBACK
        if (media != null) {
            try {
                media.box(contentWidth(), contentHeight());
                media.tick();
            } catch (Throwable t) {
                WaterUI.LOGGER.error(IT, "thumbnail media tick failed", t);
                this.close();
                this.error("Media backend failed: " + t.getMessage());
            }
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

    // THE PICTURE IS LETTERBOXED, SO IT SHRINKS TO WHATEVER BOX IT IS GIVEN
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
        // SAME GUARD AS tick(): A BACKEND THROW ON THE RENDER PATH DEGRADES TO THE FALLBACK ICON
        ResourceLocation texture;
        try {
            texture = media != null ? media.texture() : null;
        } catch (Throwable t) {
            WaterUI.LOGGER.error(IT, "thumbnail texture failed", t);
            this.close();
            this.error("Media backend failed: " + t.getMessage());
            texture = null;
        }
        if (texture == null) {
            // FLAT ON PURPOSE: A SHADOW UNDER THE PLACEHOLDER READS AS DIRT ON THE EMPTY SLOT
            if (fallback != null) {
                fallback.drawSquared(graphics, contentX(), contentY(), contentWidth(), contentHeight(), false);
            }
            return;
        }

        // LETTERBOXED, LIKE EVERY OTHER SQUARED DRAW HERE: A 16:9 PREVIEW IN A SQUARE BOX KEEPS
        // ITS SHAPE INSTEAD OF BEING STRETCHED INTO ONE
        int mediaWidth = Math.max(1, media.width());
        int mediaHeight = Math.max(1, media.height());
        float scale = Math.min(contentWidth() / (float) mediaWidth, contentHeight() / (float) mediaHeight);
        int width = Math.max(1, Math.round(mediaWidth * scale));
        int height = Math.max(1, Math.round(mediaHeight * scale));
        graphics.blit(texture, contentX() + (contentWidth() - width) / 2, contentY() + (contentHeight() - height) / 2,
                0, 0, width, height, width, height);
    }

    private static boolean probe() {
        try {
            Class.forName("org.watermedia.api.media.MediaAPI");
            return true;
        } catch (Throwable t) {
            WaterUI.LOGGER.warn(IT, "WATERMedia is not present; thumbnails will only show their fallback");
            return false;
        }
    }

    // MEDIA SESSION OF ONE THUMBNAIL; ONLY THIS CLASS NAMES WATERMEDIA TYPES SO IT NEVER
    // CLASSLOADS WHEN THE PROBE FAILED. THE PLAYER OWNS THE ENGINE FROM BIRTH
    private static final class Media {
        private MRL mrl;
        private MediaPlayer player;
        private GFXEngine gfx;
        // THE RL THIS SESSION REGISTERED ITS CURRENT GL TEXTURE UNDER, AND THE ID BEHIND IT, SO A
        // SWAPPED TEXTURE RE-REGISTERS AND FREES THE STALE ONE INSTEAD OF LEAKING OR FREEING ANOTHER'S
        private ResourceLocation location;
        private int glId;
        private boolean released;

        // COMPLETION CALLBACKS: FIRED ONCE ON THE RENDER/TICK THREAD, NEVER ON THE WORKER
        private Runnable onLoaded;
        private Consumer<String> onError;
        private boolean loadFired;
        private boolean errorFired;

        // WIDGET CONTENT BOX IN GUI PIXELS, PUSHED BY THE OWNER EACH TICK; DRIVES THE UPLOAD CAP
        private int boxW, boxH;
        // LAST APPLIED maxSize SO REDUNDANT CALLS ARE SKIPPED
        private int cropW, cropH;

        Media(URI uri) {
            this.mrl = MediaAPI.mrl(uri);
        }

        void box(int width, int height) {
            this.boxW = width;
            this.boxH = height;
        }

        // OPENS THE PLAYER AS SOON AS THE URL RESOLVES; CALLED ONCE PER TICK BY THE OWNER
        void tick() {
            if (released || errorFired) return;
            if (player != null) {
                // FF RESOLVES ITS UPLOAD TARGET PER FRAME, SO A BOX/GUI-SCALE CHANGE STILL APPLIES LIVE
                this.applyCap();
                return;
            }
            if (loadFired) return;
            if (mrl == null) {
                this.errorFired = true;
                if (onError != null) onError.accept("Invalid URI");
                return;
            }
            if (mrl.status().failed()) {
                this.errorFired = true;
                if (onError != null) onError.accept("MRL resolution failed");
                return;
            }
            if (!mrl.status().loaded()) return;
            // WAIT FOR THE FIRST LAYOUT: TxMediaPlayer RESOLVES maxSize ONLY AT PREPARE, SO A STILL
            // IMAGE MUST KNOW ITS CAP BEFORE start() OR IT UPLOADS FULL-SIZE FOREVER
            if (boxW <= 0 || boxH <= 0) return;
            // ONE EXCLUSIVE GFX ENGINE PER ELEMENT, NO SFX: PREVIEWS ARE PICTURE-ONLY
            this.player = MediaAPI.createPlayer(mrl,
                    () -> this.gfx = MediaAPI.glEngine(Minecraft.getInstance().gameThread, Minecraft.getInstance()),
                    () -> null);
            if (this.player == null) {
                // A FAILED CONSTRUCTION ALREADY RELEASED WHATEVER ENGINE IT OBTAINED (MediaAPI CLEANUP)
                this.gfx = null;
                this.errorFired = true;
                if (onError != null) onError.accept("Player creation failed");
                return;
            }
            this.player.repeat(true);
            this.applyCap();
            this.player.start();
            WaterUI.LOGGER.debug(IT, "thumbnail player started for {} capped at {}x{}", mrl, cropW, cropH);
        }

        // CAPS THE UPLOAD TO THE WIDGET'S OWN BOX AT FRAMEBUFFER RESOLUTION: THE PER-AXIS CAPS
        // FORCE THE BOX ASPECT WITHOUT KNOWING THE SOURCE, AND A PREVIEW NEVER DECODES PAST ITS SIZE
        private void applyCap() {
            if (player == null || boxW <= 0 || boxH <= 0) return;
            double scale = Minecraft.getInstance().getWindow().getGuiScale();
            int cw = Math.max(1, (int) Math.ceil(boxW * scale));
            int ch = Math.max(1, (int) Math.ceil(boxH * scale));
            if (cw == cropW && ch == cropH) return;
            this.cropW = cw;
            this.cropH = ch;
            player.maxSize(cw, ch);
        }

        /** Texture to blit, or null while it is still loading or if it never will. */
        ResourceLocation texture() {
            if (released || player == null || !player.canPlay()) return null;
            // GL TEXTURE 0 IS "NONE"; DRAWING IT WOULD SHARE ONE DEAD ID BETWEEN EVERY LOADING PLAYER
            int id = (int) player.texture();
            if (id <= 0) return null;
            // FIRST SUCCESSFUL TEXTURE FRAME: THE PREVIEW IS READY TO SHOW
            if (!loadFired) {
                this.loadFired = true;
                if (onLoaded != null) onLoaded.run();
            }
            // THE PLAYER MAY SWAP ITS GL TEXTURE OVER ITS LIFE; RE-REGISTER AND DROP THE STALE ONE WHEN IT DOES
            if (location == null || glId != id) {
                this.free();
                this.glId = id;
                this.location = WaterUI.id("thumbnail_" + id);
                Minecraft.getInstance().getTextureManager().register(location, new GlTexture(id));
            }
            return location;
        }

        int width() {
            return player != null ? player.width() : 0;
        }

        int height() {
            return player != null ? player.height() : 0;
        }

        void release() {
            if (released) return;
            this.released = true;
            WaterUI.LOGGER.debug(IT, "thumbnail session released");
            this.mrl = null;
            this.gfx = null;
            if (this.player != null) {
                try {
                    // THE PLAYER OWNS THE ENGINE: THIS RELEASE FREES BOTH
                    this.player.release();
                } catch (Throwable t) {
                    // A BACKEND THAT DIES ON ITS WAY OUT MUST NOT TAKE THE SCREEN WITH IT, AND MUST NOT
                    // LEAVE THE ROW HALF RELEASED EITHER: THE TEXTURE STILL FOLLOWS
                    WaterUI.LOGGER.error(IT, "thumbnail player failed to release", t);
                }
                this.player = null;
            }
            this.free();
        }

        // DROPS EXACTLY THE TEXTURE THIS SESSION REGISTERED, NEVER ANOTHER ROW'S THAT SHARES A GL ID
        private void free() {
            if (location == null) return;
            Minecraft.getInstance().getTextureManager().release(location);
            this.location = null;
            this.glId = 0;
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
}
