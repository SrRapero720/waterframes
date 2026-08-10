package me.srrapero720.waterui.screen;

import me.srrapero720.waterui.WaterUI;
import me.srrapero720.waterui.core.AbstractParent;
import me.srrapero720.waterui.core.Anchor;
import me.srrapero720.waterui.core.Cursors;
import me.srrapero720.waterui.core.DebugOverlay;
import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.core.Spacing;
import me.srrapero720.waterui.format.DataContext;
import me.srrapero720.waterui.format.UIBuilder;
import me.srrapero720.waterui.format.UIEvents;
import me.srrapero720.waterui.format.UIHost;
import me.srrapero720.waterui.theme.Palette;
import me.srrapero720.waterui.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Vanilla screen that owns a {@link Panel} root and hosts a stack of {@link Dialog}s over it:
 * measures, lays out, draws and routes input. Subclasses inflate their content into the root in
 * {@link #build()}; the dialogs pop over it as stacked layers.
 */
public abstract class WaterScreen extends Screen implements UIHost {
    private static final Marker IT = MarkerManager.getMarker(WaterScreen.class.getSimpleName());
    // DEPTH BETWEEN STACKED PANELS; VANILLA DRAWS TOOLTIPS AT 400, WELL CLEAR OF THESE
    private static final int LAYER_DEPTH = 100;
    // VEIL OVER EVERYTHING UNDER THE TOP DIALOG, SO AN OPEN POPUP OWNS THE SCREEN
    private static final int DIM_COLOR = Palette.argb(0, 0, 0, 0.5f);

    protected final Panel root;
    protected final ResourceLocation id;
    private final List<Dialog> dialogs = new ArrayList<>();
    private final ScreenStack stack = new ScreenStack(this);
    private final List<Element> outside = new ArrayList<>();
    // LIVE-BINDING CONTEXT: RESET ON EVERY RE-INFLATE, DRAINED EACH TICK
    private final DataContext context = new DataContext();
    // PROGRAMMATIC REGISTRATIONS (§5.6, §12.2): CLEARED ON EVERY RE-INIT LIKE THE CONTEXT
    private final UIHost.Store store = new UIHost.Store();
    private Element focused;
    // LAYER THAT CONSUMED THE LAST PRESS (OUTSIDE ELEMENT OR TOP PANEL) AND THE GLFW BUTTON THAT
    // OWNS IT (§12.3): DRAGS AND THE OWNING RELEASE ROUTE TO IT ALONE, OTHER BUTTONS ARE SWALLOWED
    private Element pointerTarget;
    private int pointerButton = -1;
    private boolean reflowQueued = true;
    // LAST KNOWN CURSOR: GLFW GIVES NO CURSOR TRACKING DURING AN OS DRAG, SO FILE DROP ROUTING
    // USES THE POSITION BEFORE THE DRAG ENTERED THE WINDOW
    private double lastMouseX, lastMouseY;

    // ELEMENT FLOATING AT THE CURSOR OVER EVERY LAYER; THE GRAB POINT KEEPS IT UNDER THE HAND
    private Element dragged;
    private int dragGrabX, dragGrabY;

    /** Root of the theme chain: the root and every dialog without one of its own paints with this. */
    public Theme theme = Theme.ERROR;

    protected WaterScreen(ResourceLocation id, int width, int height) {
        super(Component.literal(id.getPath()));
        // THE ROOT IS A PLAIN Panel, SO NO SUBCLASS FIELD IS TOUCHED BY THIS CONSTRUCTOR
        this.id = id;
        this.root = new Panel(this.id.getPath(), width, height);
        this.root.screen = this;
        this.stack.add(root);
    }

    public Theme theme() {
        return this.theme;
    }

    /** The whole tree re-inits because its chrome resolves theme colours while it is being built. */
    public WaterScreen theme(Theme theme) {
        this.theme = theme;
        this.init();
        return this;
    }

    public Panel root() {
        return this.root;
    }

    /** The live-binding context the {@link me.srrapero720.waterui.format.UIBuilder} populates during inflate. */
    public DataContext context() {
        return this.context;
    }

    /** The three registries behind {@link #registerVar}/{@link #registerUIEvent}/{@link #registerEvent} (§5.6, §12.2). */
    @Override
    public UIHost.Store uiStore() {
        return this.store;
    }

    /** Registers an inflatable constant fed to the contract on every inflate; register in the constructor (§5.6). */
    protected final void registerVar(String key, boolean value) {
        this.store.inflatable(key, value);
    }

    /** Registers an inflatable numeric constant; {@code int} widens here (§5.6). */
    protected final void registerVar(String key, long value) {
        this.store.inflatable(key, value);
    }

    /** Registers an inflatable numeric constant; {@code float} widens here (§5.6). */
    protected final void registerVar(String key, double value) {
        this.store.inflatable(key, value);
    }

    /** Registers an inflatable String/Icon/ItemStack constant (§5.6). */
    protected final void registerVar(String key, Object value) {
        this.store.inflatable(key, value);
    }

    /** Registers a dynamic variable read live like a {@code @UIVar} method; a {@code final var} snapshots it per inflate (§5.6). */
    protected final void registerVar(String key, Supplier<?> value) {
        this.store.dynamic(key, value);
    }

    /** Registers a named handler for the document's {@code on<Event>=key(...)} calls, receiving id + baked args + payload (§12.2). */
    protected final void registerUIEvent(String key, UIEvents.Handler handler) {
        this.store.event(key, handler);
    }

    /** Registers a payload-blind named handler (§12.2). */
    protected final void registerUIEvent(String key, Runnable handler) {
        this.store.event(key, payload -> handler.run());
    }

    /** Binds {@code callback} straight to the element's click by id; the namespace is the inflated document's (§12.2). */
    protected final void registerEvent(String elementId, Consumer<Element> callback) {
        this.store.element(elementId, callback);
    }

    /** Same as {@link #registerEvent(String, Consumer)} with an explicit {@code ns:path} id, for foreign elements. */
    protected final void registerEvent(ResourceLocation elementId, Consumer<Element> callback) {
        this.store.element(elementId.toString(), callback);
    }

    // INFLATES THE SCREEN'S OWN DOCUMENT INTO THE ROOT, FEEDING THE BAKED VIEW AND WIRING THE
    // ELEMENT EVENTS (§5.6, §12.2); init() DRIVES IT, NEVER THE SUBCLASS
    private void inflate() {
        UIBuilder.inflate(id, root, this, store.baked());
        root.wireElements(store, id.getNamespace());
        this.store.inflated();
    }

    /** Element by id in the root tree or among the outside elements (§9.5), so both stay dressable. */
    public Element get(ResourceLocation id) {
        Element found = root.get(id);
        if (found != null) return found;
        for (Element element: outside) {
            if (id.equals(element.id())) return element;
            if (element instanceof AbstractParent group) {
                Element deeper = group.get(id);
                if (deeper != null) return deeper;
            }
        }
        return null;
    }

    /** Where every {@link Element#dirty()} of the tree lands; the reflow runs before the next frame. */
    public void reflowQueued() {
        this.reflowQueued = true;
    }

    /**
     * Registers an outside element placed against the root panel's outer edge per its
     * {@link Element#outsideEdge()} and {@link Element#outsideAlign()}. The box sits fully
     * outside the named edge, growing away from the panel.
     */
    public void outside(Element element) {
        this.outside.add(element);
        // PARENTED UNDER THE STACK SO theme() INHERITS THE SCREEN THEME AND dirty() QUEUES REFLOW
        this.stack.add(element);
        this.reflowQueued();
    }

    /** Drops every outside element. */
    public void clearOutside() {
        for (Element element: outside) this.stack.remove(element);
        this.outside.clear();
    }

    // ONLY THE ROOT OWNS THE SCREEN-WIDE THEME AND THE OUTSIDE ELEMENTS (§9.5); A DIALOG THEMES ITSELF
    public boolean base(Panel panel) {
        return panel == root;
    }

    public void openDialog(Dialog dialog) {
        WaterUI.LOGGER.debug(IT, "dialog {} opened", dialog.name);
        dialog.screen = this;
        dialog.context = new DataContext();
        this.dialogs.add(dialog);
        this.stack.add(dialog);
        // THE OWNER DRIVES THE DIALOG'S INFLATE, THEN init() DRESSES THE FRESH TREE (§5.6)
        dialog.clear();
        dialog.inflate();
        dialog.init();
        this.focus(null);
        this.reflowQueued();
    }

    public void closeDialog(Dialog dialog) {
        WaterUI.LOGGER.debug(IT, "dialog {} closed", dialog.name);
        this.dialogs.remove(dialog);
        this.stack.remove(dialog);
        dialog.closed();
        dialog.disposeInflated();
        dialog.dispose();
        // DROP THE DIALOG'S BINDING CONTEXT SO ITS EVALUATORS STOP; THE STORE DIES WITH THE INSTANCE
        if (dialog.context != null) { dialog.context.reset(); dialog.context = null; }
        dialog.screen = null;
        this.focus(null);
        if (pointerTarget == dialog) { this.pointerTarget = null; this.pointerButton = -1; }
        this.reflowQueued();
    }

    public Dialog topDialog() {
        return dialogs.isEmpty() ? null : dialogs.get(dialogs.size() - 1);
    }

    /** Closes the whole screen, e.g. when the tile a subclass edits is gone. */
    public void close() {
        this.onClose();
    }

    /**
     * Floats {@code element} at the cursor, drawn over every layer and outside any scissor, until
     * cleared with {@code null}. The offsets are the grab point inside the element; its hidden
     * self stays in its layout keeping the gap, this only paints the travelling copy.
     */
    public void drag(Element element, int grabX, int grabY) {
        this.dragged = element;
        this.dragGrabX = grabX;
        this.dragGrabY = grabY;
    }

    /** Dresses the freshly inflated tree: resolves widgets, couplings and whatever code-side wiring remains. */
    protected abstract void build();

    /** Screen-level cleanup when the screen is removed. */
    protected void closed() {}

    /** Extra pass after the reflow, for anything a subclass draws outside the root flow. */
    protected void afterReflow() {}

    /** Extra pass after every layer is painted, for anything a subclass draws outside the root flow. */
    protected void afterRender(GuiGraphics graphics, int mouseX, int mouseY, float partial) {}

    /** Element outside the root flow under the cursor, so its tooltip can still show. */
    protected Element pickExtra(double mouseX, double mouseY) {
        return null;
    }

    @Override
    protected void init() {
        WaterUI.LOGGER.debug(IT, "init screen {} ({} dialog(s))", root.name, dialogs.size());
        this.root.clear();
        // init() OWNS THE OUTSIDE LIFECYCLE: CLEARS THE LIST SO SUBCLASSES CANNOT FORGET
        this.clearOutside();
        // init() RE-RUNS ON RESIZE/THEME: THE OLD TREE IS GONE, SO A PRESS ON IT CANNOT END
        this.pointerTarget = null;
        this.pointerButton = -1;
        this.dragged = null;
        // DROP STALE TICKED EVALUATORS BEFORE THE RE-INFLATE; THE STORE LIVES WITH THE INSTANCE (§5.6)
        this.context.reset();
        this.inflate();
        this.build();
        // A COPY BECAUSE build() ENDS IN A TICK THAT MAY CLOSE THE SCREEN OVER A REMOVED TILE
        for (Dialog dialog: List.copyOf(dialogs)) {
            dialog.clear();
            // RESET THE DIALOG'S OWN CONTEXT SO STALE EVALUATORS FROM THE OLD TREE ARE DROPPED
            if (dialog.context != null) dialog.context.reset();
            dialog.inflate();
            dialog.init();
        }
        this.reflowQueued();
    }

    private void reflow() {
        this.reflowQueued = false;
        // THE ROOT AND EVERY DIALOG CENTER VIA Anchor.CENTER
        this.stack.layout(0, 0, width, height);
        // OUTSIDE ELEMENTS PLACE AFTER THE ROOT: THE ROOT RECT IS KNOWN BY NOW
        this.placeOutside();
        this.afterReflow();
    }

    // OUTSIDE ELEMENTS SIT AGAINST THE ROOT PANEL'S OUTER EDGE, GROWING AWAY FROM IT.
    // THE EDGE PICKS THE SIDE, THE ALIGN PLACES ALONG IT, THE PANEL IS THE SOLE REFERENCE
    private void placeOutside() {
        for (Element element: outside) {
            int edge = element.outsideEdge();
            if (edge == 0) continue;
            int align = element.outsideAlign();
            Spacing margin = element.margin();
            int ew = element.layoutWidth(root.bounds.width);
            int eh = element.layoutHeight(ew, root.bounds.height);
            boolean horizontal = (edge & (Anchor.START | Anchor.END)) != 0;

            int x, y;
            if (horizontal) {
                // EDGE AXIS: LEFT OR RIGHT SIDE OF THE PANEL
                x = (edge & Anchor.END) != 0
                        ? root.bounds.right() + margin.left()
                        : root.bounds.x - ew - margin.right();
                // ALONG-EDGE AXIS: VERTICAL PLACEMENT AGAINST THE PANEL
                if ((align & Anchor.BOTTOM) != 0)      y = root.bounds.bottom() - eh - margin.bottom();
                else if ((align & Anchor.TOP) != 0)     y = root.bounds.y + margin.top();
                else                                    y = root.bounds.y + (root.bounds.height - eh) / 2;
            } else {
                // EDGE AXIS: TOP OR BOTTOM SIDE OF THE PANEL
                y = (edge & Anchor.BOTTOM) != 0
                        ? root.bounds.bottom() + margin.top()
                        : root.bounds.y - eh - margin.bottom();
                // ALONG-EDGE AXIS: HORIZONTAL PLACEMENT AGAINST THE PANEL
                if ((align & Anchor.END) != 0)          x = root.bounds.right() - ew - margin.right();
                else if ((align & Anchor.START) != 0)   x = root.bounds.x + margin.left();
                else                                    x = root.bounds.x + (root.bounds.width - ew) / 2;
            }

            element.layout(x, y, ew, eh);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        if (reflowQueued) this.reflow();
        boolean rootIsTop = dialogs.isEmpty();

        // THE ROOT IS THE BASE LAYER AT DEPTH ZERO; ONLY THE TOP LAYER SEES THE REAL CURSOR
        root.elevation = 0;
        root.render(graphics, rootIsTop ? mouseX : -10000, rootIsTop ? mouseY : -10000, partial);
        graphics.flush();

        // OUTSIDE ELEMENTS FLOAT OVER THE ROOT BUT UNDER ANY DIALOG, WHICH DIMS THEM ALONGSIDE IT
        for (Element element: outside) {
            element.elevation = LAYER_DEPTH / 2;
            element.render(graphics, rootIsTop ? mouseX : -10000, rootIsTop ? mouseY : -10000, partial);
        }
        graphics.flush();

        // EACH DIALOG GETS ITS OWN DEPTH, NOT JUST ITS TURN: DRAW ORDER ALONE DOES NOT SETTLE WHAT
        // COVERS WHAT WHEN THE TEXT AND THE RECTANGLES OF A FRAME TRAVEL IN SEPARATE BATCHES
        for (int i = 0; i < dialogs.size(); i++) {
            Dialog dialog = dialogs.get(i);
            boolean top = i == dialogs.size() - 1;
            dialog.elevation = (i + 1) * LAYER_DEPTH;
            // AN OPEN POPUP OWNS THE SCREEN: EVERYTHING UNDER IT DIMS, RIDING A DEPTH BETWEEN
            // BOTH LAYERS SO THE VEIL COVERS THE LOWER ONES WITHOUT TOUCHING THE TOP
            if (top) {
                graphics.pose().pushPose();
                graphics.pose().translate(0f, 0f, dialog.elevation - LAYER_DEPTH / 2f);
                graphics.fill(0, 0, width, height, DIM_COLOR);
                graphics.pose().popPose();
            }
            dialog.render(graphics, top ? mouseX : -10000, top ? mouseY : -10000, partial);
            graphics.flush();
        }

        this.afterRender(graphics, mouseX, mouseY, partial);

        // THE DRAGGED ELEMENT ESCAPES ITS LIST AND ITS SCISSOR: PAINTED LAST, LIFTED OVER EVERY
        // LAYER AND GLUED TO THE CURSOR, WHILE ITS HIDDEN SELF KEEPS THE GAP BACK IN THE FLOW
        if (dragged != null) {
            graphics.flush();
            graphics.pose().pushPose();
            graphics.pose().translate(mouseX - dragGrabX - dragged.bounds.x, mouseY - dragGrabY - dragged.bounds.y,
                    LAYER_DEPTH * (dialogs.size() + 2));
            boolean hidden = dragged.hidden;
            dragged.hidden = false;
            dragged.render(graphics, -10000, -10000, partial);
            dragged.hidden = hidden;
            graphics.flush();
            graphics.pose().popPose();
        }
        // CURSOR SWEEP AFTER EVERY PAINT CLAIMED ITS SHAPE; NOTHING CLAIMED MEANS THE ARROW
        Cursors.frame();

        Panel top = topDialog() != null ? topDialog() : root;
        // THE INSPECTOR REPLACES TOOLTIPS: ITS PANEL ALREADY DESCRIBES WHAT SITS UNDER THE CURSOR
        if (DebugOverlay.active()) {
            DebugOverlay.render(graphics, top, mouseX, mouseY, width, height);
            return;
        }
        // NO DIALOG OWNS THE SCREEN: OUTSIDE, THEN THE ROOT'S OUT-OF-FLOW EXTRAS, WIN BEFORE THE ROOT
        Element hovered = null;
        if (topDialog() == null) {
            hovered = pickOutside(mouseX, mouseY);
            if (hovered == null) hovered = pickExtra(mouseX, mouseY);
        }
        if (hovered == null) hovered = top.pick(mouseX, mouseY);
        if (hovered == null) return;
        List<Component> tooltip = hovered.tooltip();
        if (tooltip != null && !tooltip.isEmpty()) {
            graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void tick() {
        // THE PHYSICAL BUTTON IS THE AUTHORITY (§12.3): A CAPTURE WHOSE RELEASE NEVER ARRIVED
        // (TOUCHSCREEN MODE, FOCUS LOSS) ENDS HERE, DELIVERING THE RELEASE AT THE LAST KNOWN CURSOR
        if (pointerTarget != null
                && GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), pointerButton) != GLFW.GLFW_PRESS) {
            Element stale = pointerTarget;
            int button = pointerButton;
            this.pointerTarget = null;
            this.pointerButton = -1;
            stale.release(stale.toLocalX(lastMouseX), stale.toLocalY(lastMouseY), button);
        }
        this.root.tick();
        for (Element element: outside) element.tick();
        // A COPY BECAUSE A TICKING WIDGET MAY CLOSE ITS OWN DIALOG
        for (Dialog dialog: List.copyOf(dialogs)) dialog.tick();
        // DRAIN THE LIVE-BINDING EVALUATORS (tooltip/enabled/visible) AT THE 20 Hz TICK RATE
        this.context.tick();
        for (Dialog dialog: List.copyOf(dialogs)) {
            DataContext dc = dialog.context;
            if (dc != null) dc.tick();
        }
    }

    private void focus(Element element) {
        if (focused == element) return;
        if (focused != null) focused.focusChange(false);
        this.focused = element;
        if (focused != null) focused.focusChange(true);
    }

    // DEEPEST OUTSIDE ELEMENT UNDER THE CURSOR, MIRRORING HOW A LAYER PICKS ITS HOVERED CHILD
    private Element pickOutside(double mouseX, double mouseY) {
        for (int i = outside.size() - 1; i >= 0; i--) {
            Element element = outside.get(i);
            if (!element.hovered(mouseX, mouseY)) continue;
            if (element instanceof AbstractParent group) {
                Element deeper = group.pick(element.toLocalX(mouseX), element.toLocalY(mouseY));
                if (deeper != null) return deeper;
            }
            return element;
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // A LIVE CAPTURE OWNS THE POINTER (§12.3): ANOTHER BUTTON'S PRESS IS SWALLOWED MID-GESTURE.
        // THE OWNING BUTTON PRESSING AGAIN MEANS ITS RELEASE WAS LOST: END THE STALE HOLD FIRST
        if (pointerTarget != null) {
            if (button != pointerButton) return true;
            Element stale = pointerTarget;
            this.pointerTarget = null;
            this.pointerButton = -1;
            stale.release(stale.toLocalX(mouseX), stale.toLocalY(mouseY), button);
        }

        Panel top = topDialog() != null ? topDialog() : root;

        // A DIALOG OWNS THE SCREEN: OUTSIDE ELEMENTS NEVER SEE A CLICK WHILE ONE IS OPEN
        if (dialogs.isEmpty()) {
            for (int i = outside.size() - 1; i >= 0; i--) {
                Element element = outside.get(i);
                if (element.hovered(mouseX, mouseY) && element.press(element.toLocalX(mouseX), element.toLocalY(mouseY), button)) {
                    this.focus(element.focusable() ? element : null);
                    this.pointerTarget = element;
                    this.pointerButton = button;
                    WaterUI.LOGGER.trace(IT, "pointer captured by outside {}", element.id());
                    return true;
                }
            }
        }

        Element picked = top.pick(mouseX, mouseY);
        this.focus(picked != null && picked.focusable() ? picked : null);
        if (top.press(mouseX, mouseY, button)) {
            // A PRESS THAT CLOSED ITS OWN DIALOG LEAVES NOTHING TO CAPTURE: THAT LAYER IS ALREADY DISPOSED
            if (top == root || dialogs.contains(top)) {
                this.pointerTarget = top;
                this.pointerButton = button;
                WaterUI.LOGGER.trace(IT, "pointer captured by {}", top.name);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // ONLY THE OWNING BUTTON'S RELEASE ENDS THE CAPTURE AND REACHES ITS CHAIN (§12.3); THE REST DROP
        if (pointerTarget == null || button != pointerButton) return true;
        Element target = pointerTarget;
        this.pointerTarget = null;
        this.pointerButton = -1;
        target.release(target.toLocalX(mouseX), target.toLocalY(mouseY), button);
        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        // A LIVE PRESS OWNS THE MOVES (DRAG); WITHOUT ONE THEY BROADCAST AS PLAIN HOVER TRAVEL
        if (pointerTarget != null) {
            pointerTarget.move(pointerTarget.toLocalX(mouseX), pointerTarget.toLocalY(mouseY));
            return;
        }
        if (dialogs.isEmpty()) {
            for (Element element: outside) element.move(element.toLocalX(mouseX), element.toLocalY(mouseY));
        }
        Panel top = topDialog() != null ? topDialog() : root;
        top.move(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // THE SAME MOTION ALREADY ARRIVED THROUGH mouseMoved; A SECOND move() PER FRAME IS WASTE
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (dialogs.isEmpty()) {
            for (int i = outside.size() - 1; i >= 0; i--) {
                Element element = outside.get(i);
                if (element.hovered(mouseX, mouseY) && element.wheel(element.toLocalX(mouseX), element.toLocalY(mouseY), scrollY)) return true;
            }
        }
        Panel top = topDialog() != null ? topDialog() : root;
        return top.wheel(mouseX, mouseY, scrollY);
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifiers) {
        if (DebugOverlay.chord(key)) return true;
        if (focused != null && focused.keyDown(key, scan, modifiers)) return true;
        Panel top = topDialog() != null ? topDialog() : root;
        if (top.keyDown(key, scan, modifiers)) return true;
        return super.keyPressed(key, scan, modifiers);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        // A HELD F3 MAKES EVERY KEY A CHORD, SO ITS STROKE MUST NOT LEAK INTO A FOCUSED INPUT
        if (DebugOverlay.chordHeld()) return true;
        if (focused != null && focused.charTyped(character, modifiers)) return true;
        return super.charTyped(character, modifiers);
    }

    /** A dialog busy with something it cannot abandon keeps the escape key from closing it. */
    @Override
    public boolean shouldCloseOnEsc() {
        return topDialog() == null || topDialog().closeable();
    }

    @Override
    public void onFilesDrop(List<java.nio.file.Path> files) {
        Panel top = topDialog() != null ? topDialog() : root;
        // ROUTE TO THE DEEPEST ELEMENT UNDER THE LAST KNOWN CURSOR THAT LISTENS, FALLING BACK
        // TO THE PANEL'S OWN filesDropped SO UploadScreen AND ANYTHING ELSE THAT OVERRIDES IT KEEPS WORKING
        Element target = top.pickForFileDrop(lastMouseX, lastMouseY);
        if (target != null && target.fireFileDrop(files)) return;
        top.filesDropped(files);
    }

    @Override
    public void removed() {
        WaterUI.LOGGER.trace(IT, "disposing screen {}: {} dialog(s), {} outside", root.name, dialogs.size(), outside.size());
        Cursors.reset();
        DebugOverlay.reset();
        for (Dialog dialog: List.copyOf(dialogs)) {
            dialog.closed();
            dialog.disposeInflated();
            dialog.dispose();
        }
        this.closed();
        root.disposeInflated();
        root.dispose();
        for (Element element: outside) element.dispose();
        super.removed();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    /** Displays keep playing while their settings are open, so this never pauses the game. */
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected static Component translatable(String key, Object... args) {
        return Component.translatable(key, args);
    }

    protected static String translate(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }
}
