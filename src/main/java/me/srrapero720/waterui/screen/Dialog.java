package me.srrapero720.waterui.screen;

import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.format.DataContext;
import me.srrapero720.waterui.format.UIBuilder;
import me.srrapero720.waterui.format.UIEvents;
import me.srrapero720.waterui.format.UIHost;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A {@link Panel} that pops OVER a screen as a stacked layer, the way the playlist opens on top of
 * the display settings without losing them. Several can be stacked; the topmost owns the screen and
 * dims everything under it. Its title and action bands are authored in its own {@code .ui} document.
 * Each dialog owns its own {@link DataContext} so live bindings resolve against the dialog's host
 * and its TICKED evaluators die with it instead of leaking into the screen's shared context.
 */
public abstract class Dialog extends Panel implements UIHost {

    // OWNED BY WaterScreen: CREATED ON OPEN, RESET ON RE-INIT, DROPPED ON CLOSE
    DataContext context;
    // THE DIALOG'S OWN DOCUMENT; THE OWNING SCREEN INFLATES IT ON OPEN AND ON EVERY RE-INIT
    final ResourceLocation id;
    // PROGRAMMATIC REGISTRATIONS (§5.6, §12.2), FILLED IN THE SUBCLASS CONSTRUCTOR FOR THE INSTANCE'S LIFE
    private final UIHost.Store store = new UIHost.Store();

    /** The dialog's own live-binding context, or {@code null} before it is opened. */
    public DataContext context() { return context; }

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

    // INFLATES THE DIALOG'S OWN DOCUMENT INTO ITSELF, FEEDING THE BAKED VIEW AND WIRING THE
    // ELEMENT EVENTS (§5.6, §12.2); THE OWNING SCREEN DRIVES IT, NEVER THE SUBCLASS
    final void inflate() {
        UIBuilder.inflate(id, this, this, store.baked());
        this.wireElements(store, id.getNamespace());
        this.store.inflated();
    }

    /** Takes its look from the screen it is opened on. */
    protected Dialog(ResourceLocation id, int width, int height) {
        super(id.getPath(), width, height);
        this.id = id;
    }

    /**
     * Dresses the freshly inflated content: resolves widgets and regains whatever state the
     * previous tree carried. Called on open and again on every resize, always after the inflate.
     */
    public abstract void init();

    /** Last call before the layer goes away, for whatever still has to be written out. */
    public void closed() {}

    /** Whether the escape key may dismiss this layer. */
    public boolean closeable() {
        return true;
    }

    public void close() {
        screen.closeDialog(this);
    }

    // .ui onClick=close TARGET: UIEvents PASSES THE ELEMENT id AS THE LONE ARG, WHICH THIS DROPS
    public void close(String id) {
        this.close();
    }
}
