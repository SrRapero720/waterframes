package me.srrapero720.waterui.core;

import me.srrapero720.waterui.theme.Drawable;
import me.srrapero720.waterui.theme.ElementTheme;
import me.srrapero720.waterui.theme.Face;
import me.srrapero720.waterui.theme.Theme;
import me.srrapero720.waterui.theme.ThemeRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

/**
 * Base of the widget tree. One value per axis describes the size: an exact outer size in
 * pixels, {@link #FILL} to take the leftover space of the parent, or {@link #CONTAIN} to wrap
 * whatever the content measures. Minimums are never declared, they are computed from the
 * content itself: text width, icon floor, children plus spacing, border and padding.
 * <p>
 * An explicit size and {@link #FILL} both describe the <b>outer</b> box: border and padding
 * never grow the element, they only inset the content. Only {@link #CONTAIN} adds them on
 * top of what the content measures, so the content always fits.
 */
public abstract class Element {
    /** Axis sentinels; any value >= 0 is an exact outer size in GUI pixels. */
    public static final int FILL = -1;
    public static final int CONTAIN = -2;

    public boolean enabled = true;
    /** An invisible element leaves the layout entirely; its room is handed to its siblings. */
    public boolean visible = true;
    /** A hidden element skips painting and input but keeps its room, so nothing reflows. */
    public boolean hidden;

    /** Visual role. Every widget fixes its own; the theme decides how it is painted. */
    protected Face face = Face.NONE;

    // LAYOUT REQUEST: ONE VALUE PER AXIS PLUS PLACEMENT, ALL READ BY THE PARENT
    private int width = CONTAIN;
    private int height = CONTAIN;
    private float weight = 1f;
    private int anchor;
    private int alignMask;
    private int outsideEdge;
    private int outsideAlign;
    private Spacing margin;

    private ResourceLocation id;
    // UNIFORM SCALE: MEASUREMENT/LAYOUT CONVERT AT THE BOUNDARY, EVERYTHING DOWNSTREAM STAYS LOCAL
    private float scale = 1f;

    // OVERRIDES OF THE THEME DEFAULTS: -1 AND null MEAN "WHATEVER THE THEME SAYS"
    private Theme theme;
    private int border = -1;
    private Spacing padding;
    private Boolean shadow;
    private Drawable borderOverride;
    private Drawable faceOverride;

    protected AbstractParent parent;
    /** The layout that owns this element, or null for root-level elements. */
    public final AbstractParent parent() { return parent; }
    protected boolean hovering;
    /**
     * Size the parent already settled on the cross axis, published before the main axis is
     * measured. Lets an element mirror one side onto the other, which is how the anchor grid
     * stays square whatever height the column above it leaves.
     */
    protected int crossHint;

    // MEASURE MEMO: SINGLE-ENTRY CACHE PER CALL, AVOIDS RE-RECURSION WITHIN A LAYOUT PASS
    private static final int MC_LW = 1, MC_LH = 2, MC_MW = 4, MC_MH = 8, MC_XW = 16, MC_XH = 32;
    private int mcFlags;
    private int lwAvail, lwSize;
    private int lhWidth, lhAvail, lhSize;
    private int minWAvail, minWSize;
    private int minHWidth, minHAvail, minHSize;
    private int maxWAvail, maxWSize;
    private int maxHWidth, maxHAvail, maxHSize;

    public final Rect bounds = new Rect();

    private Supplier<List<Component>> tooltip;

    // GENERIC POINTER LISTENERS (§13), LAZILY ALLOCATED: MOST ELEMENTS NEVER LISTEN
    private PointerEvents pointer;

    // DRAG GESTURE THIS ELEMENT CARRIES AS THE GRAB SURFACE OF ITS OWNER (§8.7)
    private DragThumb thumb;

    /**
     * Lifts this element over its siblings. Needed by anything that overlays: vanilla batches
     * text and only flushes it at the end of the frame, so a label drawn earlier would
     * otherwise land on top of whatever is painted after it.
     */
    public int elevation;

    // ---- CONFIGURATION ---------------------------------------------------------------

    /** Outer size per axis: pixels, {@link #FILL} or {@link #CONTAIN}. */
    public Element size(int width, int height) {
        this.width = width;
        this.height = height;
        this.mcFlags = 0;
        return this;
    }

    /** Same value on both axes, e.g. {@code size(Element.FILL)} or a square in pixels. */
    public Element size(int size) {
        return this.size(size, size);
    }

    public Element width(int width) {
        this.width = width;
        this.mcFlags = 0;
        return this;
    }

    public Element height(int height) {
        this.height = height;
        this.mcFlags = 0;
        return this;
    }

    /** Share of the leftover among the {@link #FILL} siblings; they all default to 1. */
    public Element weight(float weight) {
        this.weight = weight;
        return this;
    }

    public Element anchor(int anchor) {
        this.anchor = anchor;
        return this;
    }

    /** Cross-flow placement override; 0 = unset, falls back to the container default. */
    public final Element align(int mask) {
        this.alignMask = mask;
        return this;
    }

    /**
     * Lifts this element outside the root panel, placed against its outer edge.
     * @param edge the side to attach to (an Anchor side bit: START, END, TOP, BOTTOM); 0 = in-flow.
     * @param align placement along the edge (a perpendicular Anchor side bit, or 0 for CENTER).
     */
    public Element outsideAnchor(int edge, int align) {
        this.outsideEdge = edge;
        this.outsideAlign = align;
        return this;
    }

    public Element margin(Spacing margin) {
        this.margin = margin;
        // MARGIN IS CONSUMED BY THE PARENT: dirty() CLEARS THE ANCESTOR CACHES AND QUEUES THE REFLOW (L5)
        this.dirty();
        return this;
    }

    public Element margin(int all) {
        return this.margin(Spacing.all(all));
    }

    public Element margin(int horizontal, int vertical) {
        return this.margin(Spacing.hv(horizontal, vertical));
    }

    /** Overrides the frame thickness the theme gives to this role. */
    public Element border(int pixels) {
        this.border = pixels;
        this.mcFlags = 0;
        return this;
    }

    public Element padding(Spacing padding) {
        this.padding = padding;
        this.mcFlags = 0;
        return this;
    }

    public Element padding(int all) {
        return this.padding(Spacing.all(all));
    }

    public Element padding(int horizontal, int vertical) {
        return this.padding(Spacing.hv(horizontal, vertical));
    }

    /** Overrides the drop shadow the theme puts under icons and text. */
    public Element shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    /** Pins the frame to one drawable, whatever the theme says for this role. */
    public Element outline(Drawable outline) {
        this.borderOverride = outline;
        return this;
    }

    /** Pins the surface to one drawable, whatever the theme says for this role. */
    public Element face(Drawable face) {
        this.faceOverride = face;
        return this;
    }

    /** Applies a theme to this element and, by inheritance, to everything below it. */
    public Element theme(Theme theme) {
        this.theme = theme;
        // THEME DEFAULTS FEED MEASUREMENT (PADDING, CONTROL METRICS): THE WHOLE SUBTREE INVALIDATES (L4)
        this.measureDirty();
        this.dirty();
        return this;
    }

    public Element enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Element tooltip(Supplier<List<Component>> tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public Element tooltip(List<Component> tooltip) {
        return this.tooltip(() -> tooltip);
    }

    public Element tooltip(String translationKey) {
        return this.tooltip(List.of(Component.translatable(translationKey)));
    }

    public List<Component> tooltip() {
        return tooltip != null ? tooltip.get() : null;
    }

    /** Identity used by {@link AbstractParent#get(ResourceLocation)} and event dispatch. */
    public Element id(ResourceLocation id) {
        this.id = id;
        return this;
    }

    /** Null when never set. */
    public final ResourceLocation id() {
        return id;
    }

    /** Uniform scale over measurement, render and hit-testing; triggers a reflow. */
    public Element scale(float scale) {
        this.scale = scale;
        this.dirty();
        return this;
    }

    public final float scale() {
        return scale;
    }

    public Element visible(boolean visible) {
        this.visible = visible;
        this.dirty();
        return this;
    }

    /** A hidden element keeps its room, so no reflow is needed. */
    public Element hidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    public Element elevation(int elevation) {
        this.elevation = elevation;
        return this;
    }

    // ---- POINTER EVENTS (§13): GENERIC LISTENERS LAYERED OVER THE WIDGET'S OWN BEHAVIOR,
    // CONSUMING BY DEFAULT; A SAME-NAME TYPED EVENT OUTRANKS THESE IN THE REGISTRY

    private PointerEvents pointer() {
        if (pointer == null) pointer = new PointerEvents();
        return pointer;
    }

    /** Fires on every left press; click means press, there is no release-inside gate. */
    public Element onClick(Runnable onClick) {
        this.pointer().click = onClick;
        return this;
    }

    /** Runs the attached generic click listener, if any; how a drag thumb's tap fires its carrier's own onClick. */
    public boolean performClick() {
        if (pointer == null || pointer.click == null) return false;
        pointer.click.run();
        return true;
    }

    /** Whether a generic click listener is attached, declared or wired. */
    public boolean hasClick() {
        return pointer != null && pointer.click != null;
    }

    public Element onRightClick(Runnable onRightClick) {
        this.pointer().rightClick = onRightClick;
        return this;
    }

    /** Fires on the second left press landing within the double-click window. */
    public Element onDoubleClick(Runnable onDoubleClick) {
        this.pointer().doubleClick = onDoubleClick;
        return this;
    }

    /** Fires once when a held left press crosses the long-press threshold. */
    public Element onLongPress(Runnable onLongPress) {
        this.pointer().longPress = onLongPress;
        return this;
    }

    public Element onPressStart(Runnable onPressStart) {
        this.pointer().pressStart = onPressStart;
        return this;
    }

    /** Fires on the left release ending a press, wherever the cursor ended up. */
    public Element onPressEnd(Runnable onPressEnd) {
        this.pointer().pressEnd = onPressEnd;
        return this;
    }

    /** Fires with the local cursor position on every move while left-pressed. */
    public Element onDrag(PointerEvents.Drag onDrag) {
        this.pointer().drag = onDrag;
        return this;
    }

    /** Fires {@code true} when the cursor enters the element and {@code false} when it leaves. */
    public Element onHover(Consumer<Boolean> onHover) {
        this.pointer().hover = onHover;
        return this;
    }

    /** Fires once when the cursor enters the element bounds. */
    public Element onEnter(Runnable onEnter) {
        this.pointer().enter = onEnter;
        return this;
    }

    /** Fires once when the cursor leaves the element bounds. */
    public Element onExit(Runnable onExit) {
        this.pointer().exit = onExit;
        return this;
    }

    /** Fires with the wheel amount when the widget itself left the scroll unconsumed. */
    public Element onScroll(DoubleConsumer onScroll) {
        this.pointer().scroll = onScroll;
        return this;
    }

    /** Makes this element the drag thumb of its owner; the gesture outranks the widget's own input. */
    public Element thumb(DragThumb thumb) {
        this.thumb = thumb;
        return this;
    }

    // BUBBLES A DRAG-OVERLAY REQUEST UP THE TREE; THE PANEL HANDS IT TO THE SCREEN, WHICH PAINTS
    // THE FLOATING COPY AT THE CURSOR (null CLEARS IT)
    protected void floatDragged(Element element, int grabX, int grabY) {
        AbstractParent owner = this.parent();
        if (owner != null) owner.floatDragged(element, grabX, grabY);
    }

    /** Fires when this element gains keyboard focus. */
    public Element onFocus(Runnable onFocus) {
        this.pointer().focusIn = onFocus;
        return this;
    }

    /** Fires when this element loses keyboard focus. */
    public Element onBlur(Runnable onBlur) {
        this.pointer().focusOut = onBlur;
        return this;
    }

    /** Fires with one path per dropped file when the OS drag lands on this element. */
    public Element onFileDrop(Consumer<List<Path>> onFileDrop) {
        this.pointer().fileDrop = onFileDrop;
        return this;
    }

    /** Whether this element listens for OS file drops. */
    public final boolean acceptsFileDrop() {
        return pointer != null && pointer.fileDrop != null;
    }

    /** Delivers OS-dropped files to this element; returns true if a listener handled them. */
    public final boolean fireFileDrop(List<Path> files) {
        if (pointer == null || pointer.fileDrop == null) return false;
        pointer.fileDrop.accept(files);
        return true;
    }

    // ---- LAYOUT REQUEST --------------------------------------------------------------

    public final int width() {
        return width;
    }

    public final int height() {
        return height;
    }

    public final float weight() {
        return weight;
    }

    public final int anchor() {
        return anchor;
    }

    public final int alignMask() {
        return alignMask;
    }

    public final int outsideEdge() {
        return outsideEdge;
    }

    public final int outsideAlign() {
        return outsideAlign;
    }

    public final void crossHint(int crossHint) {
        if (this.crossHint != crossHint) {
            this.crossHint = crossHint;
            this.mcFlags = 0;
        }
    }

    // ---- THEME -------------------------------------------------------------------------

    /** Own theme when it has one, otherwise the nearest one up the tree. */
    public final Theme theme() {
        return theme != null ? theme : this.inheritedTheme();
    }

    /** Where an unset theme comes from. Screen layers take it from the screen instead. */
    protected Theme inheritedTheme() {
        return parent != null ? parent.theme() : Theme.ERROR;
    }

    /** Defaults the theme declares for this element's role. */
    public final ElementTheme style() {
        return theme().style(face);
    }

    public final int borderWidth() {
        return border != -1 ? border : style().border();
    }

    /** Overridable so a widget can bend its theme default, e.g. icon buttons staying square. */
    public Spacing padding() {
        return padding != null ? padding : style().padding();
    }

    /** Whether a caller pinned the padding explicitly, which outranks any widget default. */
    protected final boolean paddingSet() {
        return padding != null;
    }

    public final Spacing margin() {
        return margin != null ? margin : style().margin();
    }

    public final boolean shadow() {
        return shadow != null ? shadow : style().shadow();
    }

    // ---- SIZE ------------------------------------------------------------------------

    /** Border plus padding taken away from the width. */
    public final int insetX() {
        return borderWidth() * 2 + padding().horizontal();
    }

    public final int insetY() {
        return borderWidth() * 2 + padding().vertical();
    }

    /** Natural content size, and the size the layout starts the element at. */
    protected int prefContentWidth(int available) {
        return 0;
    }

    protected int prefContentHeight(int width, int available) {
        return 0;
    }

    /** Smallest the content can shrink to; only shrinkable content reports less than its preference. */
    protected int minContentWidth(int available) {
        return prefContentWidth(available);
    }

    protected int minContentHeight(int width, int available) {
        return prefContentHeight(width, available);
    }

    protected int maxContentWidth(int available) {
        return -1;
    }

    protected int maxContentHeight(int width, int available) {
        return -1;
    }

    // EXPLICIT SIZES ARE OUTER AND OUTRANK THE INTRINSIC MINIMUM: THE CALLER KNOWS BETTER.
    // available STAYS IN OUTER SPACE ON PURPOSE: CONTENT HOOKS TAKE IT AS A LOOSE CEILING
    public final int minWidth(int available) {
        if ((mcFlags & MC_MW) != 0 && minWAvail == available) return minWSize;
        int r;
        if (width >= 0) r = scale == 1f ? width : Math.round(width * scale);
        else if (scale == 1f) r = minContentWidth(available) + insetX();
        else r = Math.round((minContentWidth(Math.round(available / scale)) + insetX()) * scale);
        minWAvail = available; minWSize = r; mcFlags |= MC_MW;
        return r;
    }

    // CALLERS PASS THE OUTER WIDTH; CONTENT HOOKS EXPECT THE CONTENT WIDTH (INSETS SUBTRACTED)
    public final int minHeight(int width, int available) {
        if ((mcFlags & MC_MH) != 0 && minHWidth == width && minHAvail == available) return minHSize;
        int r;
        if (height >= 0) r = scale == 1f ? height : Math.round(height * scale);
        else if (scale == 1f) {
            int contentW = Math.max(0, width - insetX());
            r = minContentHeight(contentW, available) + insetY();
        } else {
            int contentW = Math.max(0, Math.round(width / scale) - insetX());
            r = Math.round((minContentHeight(contentW, Math.round(available / scale)) + insetY()) * scale);
        }
        minHWidth = width; minHAvail = available; minHSize = r; mcFlags |= MC_MH;
        return r;
    }

    // AN EXPLICIT SIZE OUTRANKS THE INTRINSIC MAXIMUM TOO: THE CAP ONLY BOUNDS CONTAIN SIZING
    public final int maxWidth(int available) {
        if ((mcFlags & MC_XW) != 0 && maxWAvail == available) return maxWSize;
        int r;
        if (width >= 0) r = -1;
        else if (scale == 1f) {
            int max = maxContentWidth(available);
            r = max == -1 ? -1 : max + insetX();
        } else {
            int max = maxContentWidth(Math.round(available / scale));
            r = max == -1 ? -1 : Math.round((max + insetX()) * scale);
        }
        maxWAvail = available; maxWSize = r; mcFlags |= MC_XW;
        return r;
    }

    public final int maxHeight(int width, int available) {
        if ((mcFlags & MC_XH) != 0 && maxHWidth == width && maxHAvail == available) return maxHSize;
        int r;
        if (height >= 0) r = -1;
        else if (scale == 1f) {
            int max = maxContentHeight(Math.max(0, width - insetX()), available);
            r = max == -1 ? -1 : max + insetY();
        } else {
            int max = maxContentHeight(Math.max(0, Math.round(width / scale) - insetX()), Math.round(available / scale));
            r = max == -1 ? -1 : Math.round((max + insetY()) * scale);
        }
        maxHWidth = width; maxHAvail = available; maxHSize = r; mcFlags |= MC_XH;
        return r;
    }

    /** Size the layout starts from: the exact one when it was given, the preference otherwise. */
    public final int layoutWidth(int available) {
        if ((mcFlags & MC_LW) != 0 && lwAvail == available) return lwSize;
        int r;
        if (width >= 0) r = clampWidth(scale == 1f ? width : Math.round(width * scale), available);
        else if (scale == 1f) r = clampWidth(prefContentWidth(available) + insetX(), available);
        else r = clampWidth(Math.round((prefContentWidth(Math.round(available / scale)) + insetX()) * scale), available);
        lwAvail = available; lwSize = r; mcFlags |= MC_LW;
        return r;
    }

    public final int layoutHeight(int width, int available) {
        if ((mcFlags & MC_LH) != 0 && lhWidth == width && lhAvail == available) return lhSize;
        int r;
        if (height >= 0) r = clampHeight(scale == 1f ? height : Math.round(height * scale), width, available);
        else if (scale == 1f) {
            int contentW = Math.max(0, width - insetX());
            r = clampHeight(prefContentHeight(contentW, available) + insetY(), width, available);
        } else {
            int contentW = Math.max(0, Math.round(width / scale) - insetX());
            r = clampHeight(Math.round((prefContentHeight(contentW, Math.round(available / scale)) + insetY()) * scale), width, available);
        }
        lhWidth = width; lhAvail = available; lhSize = r; mcFlags |= MC_LH;
        return r;
    }

    public final int clampWidth(int width, int available) {
        width = Math.max(width, minWidth(available));
        int max = maxWidth(available);
        if (max != -1) width = Math.min(width, max);
        return Math.max(0, width);
    }

    public final int clampHeight(int height, int width, int available) {
        height = Math.max(height, minHeight(width, available));
        int max = maxHeight(width, available);
        if (max != -1) height = Math.min(height, max);
        return Math.max(0, height);
    }

    public final void layout(int x, int y, int width, int height) {
        // ORIGIN STAYS PARENT-SPACE, SIZE BECOMES LOCAL SPACE: EVERYTHING DOWNSTREAM IS LOCAL FOR FREE
        if (scale == 1f) this.bounds.set(x, y, width, height);
        else this.bounds.set(x, y, Math.round(width / scale), Math.round(height / scale));
        this.arrange();
    }

    protected void arrange() {}

    /** Requests a whole-tree reflow; the screen hosting the root runs it before the next frame. */
    public final void dirty() {
        this.mcFlags = 0;
        if (parent != null) parent.dirty();
        else this.queueReflow();
    }

    // DOWNWARD MEASURE INVALIDATION: dirty() ONLY WALKS UP, A THEME SWAP MUST ALSO REACH DESCENDANTS
    void measureDirty() {
        this.mcFlags = 0;
    }

    /** Root hook: the layer at the top of the tree forwards the request to its screen. */
    protected void queueReflow() {}

    // ---- CONTENT BOX -------------------------------------------------------------------

    public final int contentX() {
        return bounds.x + borderWidth() + padding().left();
    }

    public final int contentY() {
        return bounds.y + borderWidth() + padding().top();
    }

    public final int contentWidth() {
        return Math.max(0, bounds.width - insetX());
    }

    public final int contentHeight() {
        return Math.max(0, bounds.height - insetY());
    }

    public final int right() {
        return bounds.right();
    }

    public final int bottom() {
        return bounds.bottom();
    }

    // ---- COORDINATE SPACE --------------------------------------------------------------

    /** Maps a parent-space coordinate onto this element's local (unscaled) bounds space. */
    public final double toLocalX(double x) {
        return scale == 1f ? x : bounds.x + (x - bounds.x) / scale;
    }

    public final double toLocalY(double y) {
        return scale == 1f ? y : bounds.y + (y - bounds.y) / scale;
    }

    // ---- RENDERING ---------------------------------------------------------------------

    public final void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        if (!visible || hidden) return;
        boolean lifted = elevation != 0;
        boolean scaled = scale != 1f;
        if (lifted) graphics.flush();
        if (lifted || scaled) graphics.pose().pushPose();
        if (lifted) graphics.pose().translate(0f, 0f, elevation);
        if (scaled) {
            // PIVOT ON bounds' OWN ORIGIN SO THE OUTER BOX GROWS FROM ITS CORNER, NOT THE SCREEN'S
            graphics.pose().translate(bounds.x, bounds.y, 0f);
            graphics.pose().scale(scale, scale, 1f);
            graphics.pose().translate(-bounds.x, -bounds.y, 0f);
        }
        this.paint(graphics, scaled ? (int) toLocalX(mouseX) : mouseX, scaled ? (int) toLocalY(mouseY) : mouseY, partial);
        if (lifted) graphics.flush();
        if (lifted || scaled) graphics.pose().popPose();
    }

    private void paint(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        // HOVER TRANSITIONS AND THE LONG-PRESS THRESHOLD RIDE THE FRAME: THE ONLY PULSE THAT
        // ALWAYS RUNS WITH THE LIVE CURSOR, AND A COVERED LAYER'S FAKE CURSOR EXITS IT FOR FREE
        boolean inside = bounds.contains(mouseX, mouseY);
        if (pointer != null) {
            if (inside != hovering) pointer.hover(inside);
            pointer.frame();
        }
        if (thumb != null) thumb.frame(inside && enabled);
        this.hovering = inside;
        int border = this.borderWidth();

        // BORDER COVERS THE WHOLE BOX AND THE FACE IS PAINTED INSIDE IT, WHICH IS WHAT LEAVES
        // THE VISIBLE FRAME WITHOUT DRAWING FOUR SEPARATE EDGES
        Drawable outline = this.borderDisplay();
        if (outline != null) outline.draw(graphics, bounds.x, bounds.y, bounds.width, bounds.height);

        Drawable surface = this.faceDisplay();
        if (surface != null) {
            surface.draw(graphics, bounds.x + border, bounds.y + border,
                    Math.max(0, bounds.width - border * 2), Math.max(0, bounds.height - border * 2));
        }

        this.draw(graphics, mouseX, mouseY, partial);

        // ONLY A VIEW WITH A SURFACE GETS THE OVERLAY; THE TRANSPARENT ONES DIM THEMSELVES
        if (!enabled && face != Face.NONE) {
            theme().disabledOverlay().draw(graphics, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    protected Drawable borderDisplay() {
        // A ThemeRole OVERRIDE FOLLOWS THE LIVE THEME; A PLAIN Drawable OVERRIDE IS PINNED
        if (borderOverride instanceof ThemeRole role) return role.resolve(theme());
        return borderOverride != null ? borderOverride : style().outline();
    }

    protected Drawable faceDisplay() {
        if (faceOverride instanceof ThemeRole role) return role.resolve(theme());
        return faceOverride != null ? faceOverride : style().face(enabled && hovering);
    }

    /** Paints the content; the content box is already inset by border and padding. */
    protected abstract void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial);

    protected final int textColor() {
        return enabled ? theme().text() : theme().textDisabled();
    }

    /** Glyphs occupy 8px of the 9px line box, so centring on lineHeight biases the text upwards. */
    public static final int GLYPH_HEIGHT = 8;

    /** Baseline offset that vertically centres a line inside a box of this height. */
    public static int centeredTextY(int boxHeight) {
        // ROUND TO CEILING SO THE EXTRA PIXEL GOES BELOW, NOT ABOVE
        return Math.max(0, (boxHeight - GLYPH_HEIGHT + 1) / 2);
    }

    /** Draws a string centred inside the content box, the way every control here labels itself. */
    protected final void drawCentered(GuiGraphics graphics, String text, int color) {
        graphics.drawString(font(), text,
                contentX() + Math.max(0, (contentWidth() - font().width(text) + 1) / 2),
                contentY() + centeredTextY(contentHeight()), color, shadow());
    }

    /** Draws a string centred inside the content box, scaled through the pose matrix. */
    protected final void drawCenteredScaled(GuiGraphics graphics, String text, int color, float textScale) {
        if (textScale == 1f) {
            this.drawCentered(graphics, text, color);
            return;
        }
        int scaledWidth = Math.round(font().width(text) * textScale);
        int scaledGlyph = Math.round(GLYPH_HEIGHT * textScale);
        int x = contentX() + Math.max(0, (contentWidth() - scaledWidth + 1) / 2);
        int y = contentY() + Math.max(0, (contentHeight() - scaledGlyph + 1) / 2);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(textScale, textScale, 1f);
        graphics.drawString(font(), text, 0, 0, color, shadow());
        graphics.pose().popPose();
    }

    // ---- INPUT: ROUTED ENTRY POINTS (FINAL) — THE DRAG THUMB OUTRANKS THE WIDGET, THEN THE
    // GENERIC POINTER LISTENERS; PARENTS AND THE SCREEN CALL THESE, WIDGETS OVERRIDE mouse*/scroll

    /** Press entry a parent routes here; true consumes and captures the pointer on this element. */
    public final boolean press(double mouseX, double mouseY, int button) {
        if (enabled && thumb != null && thumb.press(mouseX, mouseY, button)) return true;
        boolean consumed = this.mouseDown(mouseX, mouseY, button);
        if (enabled && pointer != null && pointer.press(button)) consumed = true;
        return consumed;
    }

    /** Release entry, delivered only to the element that consumed the press. */
    public final void release(double mouseX, double mouseY, int button) {
        if (thumb != null) thumb.release(button);
        this.mouseUp(mouseX, mouseY, button);
        if (pointer != null) pointer.release(button);
    }

    public final void move(double mouseX, double mouseY) {
        if (thumb != null) thumb.move(mouseX, mouseY);
        this.mouseMove(mouseX, mouseY);
        if (pointer != null) pointer.move(mouseX, mouseY);
    }

    public final boolean wheel(double mouseX, double mouseY, double amount) {
        if (this.scroll(mouseX, mouseY, amount)) return true;
        return enabled && pointer != null && pointer.wheel(amount);
    }

    public boolean hovered(double mouseX, double mouseY) {
        if (!visible || hidden) return false;
        if (scale == 1f) return bounds.contains(mouseX, mouseY);
        // HIT-TESTING USES THE OUTER (SCALED) BOX, NOT THE LOCAL bounds SIZE
        return mouseX >= bounds.x && mouseX < bounds.x + bounds.width * scale
                && mouseY >= bounds.y && mouseY < bounds.y + bounds.height * scale;
    }

    public boolean mouseDown(double mouseX, double mouseY, int button) {
        return false;
    }

    public void mouseUp(double mouseX, double mouseY, int button) {}

    public void mouseMove(double mouseX, double mouseY) {}

    public boolean scroll(double mouseX, double mouseY, double amount) {
        return false;
    }

    public boolean keyDown(int key, int scan, int modifiers) {
        return false;
    }

    public boolean charTyped(char character, int modifiers) {
        return false;
    }

    public boolean focusable() {
        return false;
    }

    /** Routed entry for focus changes; fires the widget hook then the bag listeners. */
    public final void focusChange(boolean focused) {
        this.focusChanged(focused);
        if (pointer != null) pointer.focus(focused);
    }

    /** Widget-level focus hook; override to react to gaining or losing keyboard focus. */
    public void focusChanged(boolean focused) {}

    /** Environment checks only: closing over a broken block, async results, enable gating. */
    public void tick() {}

    /** Releases resources this element owns (players, textures, engines). Default no-op. */
    public void dispose() {}

    /** True when this element holds native resources the builder must release on re-inflate or close (§13.3). */
    public boolean ownsResources() {
        return false;
    }

    // ---- HELPERS -----------------------------------------------------------------------

    protected static Font font() {
        return Minecraft.getInstance().font;
    }

    protected static void playSound(SoundEvent sound) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
    }

    protected static void playClick() {
        playSound(SoundEvents.UI_BUTTON_CLICK.value());
    }

    public static Component translatable(String key, Object... args) {
        return Component.translatable(key, args);
    }

    /** {@code .ui} text resolution (spec §4.2): a {@code translatable:} prefix selects i18n, anything else is literal. */
    public static Component component(String value) {
        return value.startsWith("translatable:")
                ? Component.translatable(value.substring("translatable:".length()))
                : Component.literal(value);
    }

    public static String translate(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }
}
