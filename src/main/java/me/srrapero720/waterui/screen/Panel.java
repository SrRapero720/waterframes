package me.srrapero720.waterui.screen;

import me.srrapero720.waterui.WaterUI;
import me.srrapero720.waterui.core.Anchor;
import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.format.UIHost;
import me.srrapero720.waterui.layout.ParentLinear;
import me.srrapero720.waterui.theme.Drawable;
import me.srrapero720.waterui.theme.Face;
import me.srrapero720.waterui.theme.Theme;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Themed content slab: a bordered, centred panel. It is the root a {@link WaterScreen} owns, and
 * the base of every {@link Dialog} overlay. The design size is the outer box; the thick border
 * and the panel padding inset the content inside it.
 */
public class Panel extends ParentLinear {
    private static final Marker IT = MarkerManager.getMarker(Panel.class.getSimpleName());

    public final String name;

    WaterScreen screen;
    // SCREEN HOSTING THIS PANEL, OR NULL BEFORE IT IS ATTACHED
    public WaterScreen screen() { return screen; }
    private boolean edged;

    /** Takes its look from the screen it is opened on. */
    public Panel(String name, int width, int height) {
        super(Orientation.VERTICAL);
        this.face = Face.PANEL;
        this.name = name;
        this.size(width, height);
        this.alignChildren(Anchor.STRETCH);
        this.anchor(Anchor.CENTER);
    }

    /**
     * Drops the thick panel frame for a single pixel edge in the panel colour, so the chrome
     * bands reaching the border still sit inside a sliver of the panel itself.
     */
    public void edgeFrame() {
        this.border(1);
        this.edged = true;
    }

    // EDGE TRADES THE THICK FRAME FOR A 1px SLAB IN THE PANEL COLOUR, BUT ONLY WHEN THE
    // THEME'S OWN BORDER IS THICKER THAN 1 — A THEME THAT IS ALREADY THIN HAS NOTHING TO TRADE
    @Override
    protected Drawable borderDisplay() {
        if (edged && theme().panel().border() > 1) return theme().panel().face();
        return super.borderDisplay();
    }

    // BUILDER-OWNED DISPOSABLES: THE FORMAT BUILDER REGISTERS ELEMENTS IT CREATED, SO THE
    // PANEL CAN RELEASE THEIR RESOURCES ON SCREEN CLOSE WITHOUT TOUCHING HOST-CACHED ONES
    private List<Element> disposables;

    /** Registers a builder-created element whose resources this panel will release on dispose. */
    public void disposable(Element element) {
        if (disposables == null) disposables = new ArrayList<>();
        this.disposables.add(element);
    }

    /** Disposes every registered element and clears the registry. */
    public void disposeInflated() {
        if (disposables == null) return;
        for (Element element: disposables) element.dispose();
        this.disposables.clear();
    }

    // ATTACHES THE STORE'S ELEMENT EVENTS BY id AFTER AN INFLATE (§12.2): SAME SEMANTICS AS A
    // DECLARED onClick (LEFT-ONLY, enabled-GATED, CONSUMING); A MISSING id LOGS AND THE SCREEN OPENS
    void wireElements(UIHost.Store store, String namespace) {
        for (Map.Entry<String, Consumer<Element>> entry: store.elements().entrySet()) {
            String key = entry.getKey();
            ResourceLocation id;
            try {
                int colon = key.indexOf(':');
                id = colon >= 0
                        ? ResourceLocation.fromNamespaceAndPath(key.substring(0, colon), key.substring(colon + 1))
                        : ResourceLocation.fromNamespaceAndPath(namespace, key);
            } catch (RuntimeException e) {
                WaterUI.LOGGER.error(IT, "[{}] registered element event has an invalid id '{}'", name, key);
                continue;
            }
            // THE SCREEN FALLBACK REACHES THE OUTSIDE ELEMENTS (§9.5), WHICH ARE LIFTED OUT OF THE ROOT
            Element element = this.get(id);
            if (element == null && screen != null && screen.base(this)) element = screen.get(id);
            if (element == null) {
                WaterUI.LOGGER.error(IT, "[{}] registered element event '{}' resolves to nothing", name, id);
                continue;
            }
            if (element.hasClick()) WaterUI.LOGGER.warn(IT, "[{}] element event '{}' replaces a declared onClick", name, id);
            Element target = element;
            Consumer<Element> callback = entry.getValue();
            element.onClick(() -> callback.accept(target));
        }
    }

    /** Files dragged onto the window while this panel is on top. */
    public void filesDropped(List<Path> files) {}

    // ROOT OF THE TREE: WITHOUT A PARENT VIEW THE THEME COMES FROM THE SCREEN HOSTING IT
    @Override
    protected Theme inheritedTheme() {
        return screen != null ? screen.theme : Theme.ERROR;
    }

    // AND THE REFLOW REQUEST OF THE WHOLE TREE LANDS ON THAT SAME SCREEN
    @Override
    protected void queueReflow() {
        if (screen != null) screen.reflowQueued();
    }

    // DRAG-OVERLAY REQUESTS BUBBLING FROM THE TREE LAND ON THE SCREEN, WHICH PAINTS THE FLOATING COPY
    @Override
    protected void floatDragged(Element element, int grabX, int grabY) {
        if (screen != null) screen.drag(element, grabX, grabY);
    }
}
