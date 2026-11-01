package me.srrapero720.waterui.screen;

import me.srrapero720.waterui.format.DataContext;
import me.srrapero720.waterui.format.UIBuilder;
import me.srrapero720.waterui.format.UIHost;
import net.minecraft.resources.ResourceLocation;

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

    @Override
    public UIHost.Store uiStore() {
        return this.store;
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
