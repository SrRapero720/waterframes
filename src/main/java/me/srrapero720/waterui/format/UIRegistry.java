package me.srrapero720.waterui.format;

import me.srrapero720.waterui.WaterUI;
import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.layout.FrameLayout;
import me.srrapero720.waterui.layout.Grid;
import me.srrapero720.waterui.layout.ParentLinear;
import me.srrapero720.waterui.layout.ParentList;
import me.srrapero720.waterui.theme.Icon;
import me.srrapero720.waterui.widget.Blank;
import me.srrapero720.waterui.widget.Button;
import me.srrapero720.waterui.widget.ComboButton;
import me.srrapero720.waterui.widget.InputText;
import me.srrapero720.waterui.widget.ItemIcon;
import me.srrapero720.waterui.widget.ParentTab;
import me.srrapero720.waterui.widget.PlayerHead;
import me.srrapero720.waterui.widget.ProgressBar;
import me.srrapero720.waterui.widget.SeekBar;
import me.srrapero720.waterui.widget.Slider;
import me.srrapero720.waterui.widget.Spacer;
import me.srrapero720.waterui.widget.State;
import me.srrapero720.waterui.widget.Stepper;
import me.srrapero720.waterui.widget.Switch;
import me.srrapero720.waterui.widget.Tab;
import me.srrapero720.waterui.widget.Text;
import me.srrapero720.waterui.widget.Thumbnail;
import me.srrapero720.waterui.widget.Toggle;
import net.minecraft.locale.Language;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Extensibility point of the {@code .ui} format (UI-SPEC.md §7.3, §11): maps a tag to an element
 * factory and its typed property appliers, event binders and live-binding targets. New tags register
 * here without touching the parser or the builder. Generic property names (§8.2) are reserved: a tag
 * cannot register one, so a widget can never shadow the shared layout vocabulary.
 */
public final class UIRegistry {
    private static final Marker IT = MarkerManager.getMarker(UIRegistry.class.getSimpleName());

    private UIRegistry() {}

    /** Builds a fresh, unconfigured instance of the tag's element type. */
    public interface Factory { Element create(); }

    /** Applies one baked property value to an element, resolving keywords and baked variables through the contract. */
    public interface Property { void apply(Element e, UIValue value, UIContract ctx); }

    /** Adapts a widget's own callback shape into firing a handler, carrying its runtime payload. */
    public interface EventBinder { void bind(Element e, UIEvents.Handler handler); }

    /** Attaches a live variable to a property, wiring its host supplier onto {@code e} through the context. */
    public interface Binding { void bind(Element e, String var, DataContext data); }

    // GENERIC PROPERTY NAMES (§8.2): THE BUILDER OWNS THEM, SO NO TAG MAY REGISTER ONE
    private static final Set<String> RESERVED = Set.of(
            "id", "width", "height", "weight", "margin", "padding", "border", "align", "justify",
            "anchor", "outsideAnchor", "shadow", "enabled", "visible", "hidden", "elevation", "scale",
            "tooltip", "face", "outline", "scrollX", "scrollY");

    private static final Map<String, Factory> FACTORIES = new HashMap<>();
    private static final Map<String, Boolean> CONTAINERS = new HashMap<>();
    // TAG -> PARENT TAG: A LOOKUP FALLS THROUGH TO THE PARENT SO A SUBCLASS TAG INHERITS ITS PROPS/EVENTS (§7.3)
    private static final Map<String, String> PARENTS = new HashMap<>();
    private static final Map<String, Property> PROPERTIES = new HashMap<>();
    private static final Map<String, EventBinder> EVENTS = new HashMap<>();
    private static final Map<String, EventBinder> GENERIC_EVENTS = new HashMap<>();
    private static final Map<String, Binding> BINDINGS = new HashMap<>();
    private static final Map<String, Binding> GENERIC_BINDINGS = new HashMap<>();

    private static volatile boolean ready;

    // COMPOUND KEY FOR THE PER-TAG MAPS, AVOIDS A NESTED MAP PER TAG
    private static String key(String tag, String name) {
        return tag + " " + name;
    }

    /** Registers an element type under {@code tag}; {@code container} says whether it may hold children. */
    public static void register(String tag, Factory factory, boolean container) {
        register(tag, factory, container, null);
    }

    /** Same, but {@code tag} inherits the properties, events and bindings of {@code parentTag} (§7.3). */
    public static void register(String tag, Factory factory, boolean container, String parentTag) {
        FACTORIES.put(tag, factory);
        CONTAINERS.put(tag, container);
        if (parentTag != null) PARENTS.put(tag, parentTag);
    }

    /** Registers {@code name} as a property of {@code tag}; rejects a name that shadows a generic (§8.2). */
    public static void property(String tag, String name, Property applier) {
        if (RESERVED.contains(name)) throw new IllegalArgumentException("tag '" + tag + "' cannot register the reserved generic property '" + name + "'");
        PROPERTIES.put(key(tag, name), applier);
    }

    public static void event(String tag, String name, EventBinder binder) {
        EVENTS.put(key(tag, name), binder);
    }

    /** Registers {@code name} as a generic pointer event any element may declare (§12.4). */
    public static void event(String name, EventBinder binder) {
        GENERIC_EVENTS.put(name, binder);
    }

    public static void binding(String tag, String name, Binding binder) {
        BINDINGS.put(key(tag, name), binder);
    }

    /** Registers a live-binding target on the generic property {@code name} (tooltip, enabled, visible). */
    public static void binding(String name, Binding binder) {
        GENERIC_BINDINGS.put(name, binder);
    }

    public static boolean reserved(String name) { return RESERVED.contains(name); }

    public static Factory factory(String tag) { return FACTORIES.get(tag); }

    public static boolean container(String tag) { return Boolean.TRUE.equals(CONTAINERS.get(tag)); }

    /** Property of {@code tag} or any ancestor tag, else null (§7.3). */
    public static Property tagProperty(String tag, String name) {
        for (String t = tag; t != null; t = PARENTS.get(t)) {
            Property p = PROPERTIES.get(key(t, name));
            if (p != null) return p;
        }
        return null;
    }

    /** Event binder of {@code tag} or any ancestor tag, else the generic pointer one, else null. */
    public static EventBinder eventBinder(String tag, String name) {
        for (String t = tag; t != null; t = PARENTS.get(t)) {
            EventBinder binder = EVENTS.get(key(t, name));
            if (binder != null) return binder;
        }
        return GENERIC_EVENTS.get(name);
    }

    /** Binding of {@code tag} or any ancestor tag, else the generic one, else null. */
    public static Binding binding(String tag, String name) {
        for (String t = tag; t != null; t = PARENTS.get(t)) {
            Binding b = BINDINGS.get(key(t, name));
            if (b != null) return b;
        }
        return GENERIC_BINDINGS.get(name);
    }

    /** Registers the built-in element catalog with its properties, events and bindings. Idempotent. */
    public static void bootstrap() {
        if (ready) return;

        // THE GLFW BUTTON DECIDES: "LEFT CLICK ONLY" IS ENFORCED HERE, NOT INSIDE THE WIDGETS
        EventBinder leftClick = (e, handler) -> ((Button) e).onClick(button -> { if (button == 0) handler.invoke(); });

        // ---- CONTAINERS (§10) ----------------------------------------------------------
        register("Parent", ParentLinear::new, true);
        property("Parent", "orientation", (e, v, c) -> ((ParentLinear) e).orientation(UIValues.orientation(v)));
        property("Parent", "spacing", (e, v, c) -> ((ParentLinear) e).spacing(c.intValue(v)));

        register("Grid", Grid::new, true);
        property("Grid", "columns", (e, v, c) -> ((Grid) e).columns(c.intValue(v)));
        property("Grid", "rows", (e, v, c) -> ((Grid) e).rows(c.intValue(v)));
        property("Grid", "spacing", (e, v, c) -> ((Grid) e).spacing(c.intValue(v)));

        // OVERLAY CONTAINER (§10.5): CHILDREN STACK IN THE SAME RECT, EACH PLACED BY ITS anchor IN FREE SPACE
        register("Frame", FrameLayout::new, true);

        // TEMPLATED LIST (§10): container=false FORBIDS INLINE CHILDREN; ROWS COME ONLY FROM THE entry TEMPLATE,
        // WHOSE PATH IS LAZY (NOT AN IMPORT) SO ITS PER-ROW vars NEVER AGGREGATE INTO THE SCREEN CONTRACT (§7)
        register("ParentList", ParentList::new, false);
        property("ParentList", "entry", (e, v, c) -> ((ParentList) e).entry(c.docPath(v)));
        property("ParentList", "spacing", (e, v, c) -> ((ParentList) e).spacing(c.intValue(v)));
        // INTEGRATED DRAG (§10.6): BLOCK TOKEN SOURCE, DROP SEMANTICS AND THE DRAG-OUT DELETE GATE
        property("ParentList", "groupBy", (e, v, c) -> ((ParentList) e).groupBy(c.string(v)));
        property("ParentList", "reorder", (e, v, c) -> {
            String kw = v instanceof UIValue.Id id ? id.name() : null;
            if ("MOVE".equals(kw)) ((ParentList) e).reorder(ParentList.Reorder.MOVE);
            else if ("SWAP".equals(kw)) ((ParentList) e).reorder(ParentList.Reorder.SWAP);
            else throw new UIFormatException(null, -1, "reorder expects MOVE or SWAP (§10.6)");
        });
        property("ParentList", "dragOut", (e, v, c) -> ((ParentList) e).dragOut(c.bool(v)));
        event("ParentList", "onListMove", (e, handler) -> ((ParentList) e).onListMove((origin, target) -> handler.invoke(String.valueOf(origin), String.valueOf(target))));
        event("ParentList", "onListSwap", (e, handler) -> ((ParentList) e).onListSwap((a, b) -> handler.invoke(String.valueOf(a), String.valueOf(b))));
        event("ParentList", "onListDelete", (e, handler) -> ((ParentList) e).onListDelete(index -> handler.invoke(String.valueOf(index))));

        // ParentTab AND ITS tabs LIST ARE BUILT BY UIBuilder.buildTabs; ONLY ITS SWITCH EVENT RESOLVES HERE
        register("ParentTab", ParentTab::new, true);
        event("ParentTab", "onTabChange", (e, handler) -> ((ParentTab) e).onChange(index -> handler.invoke(String.valueOf(index))));

        register("Blank", Blank::new, false);
        register("Spacer", Spacer::new, false);

        // FUSED BUTTONS (§10.4): A CONTAINER WHOSE ONLY LEGAL CHILDREN ARE BUTTON TAGS (VALIDATED IN THE BUILDER)
        register("ComboButton", ComboButton::new, true);
        property("ComboButton", "orientation", (e, v, c) -> ((ComboButton) e).orientation(UIValues.orientation(v)));

        // ---- TEXT & BUTTONS (§11) ------------------------------------------------------
        register("Text", Text::new, false);
        property("Text", "component", (e, v, c) -> ((Text) e).text(c.component(v)));
        property("Text", "color", (e, v, c) -> ((Text) e).color = c.color(v));
        property("Text", "fontScale", (e, v, c) -> ((Text) e).fontScale(c.floatValue(v)));
        property("Text", "ellipsize", (e, v, c) -> { if (c.bool(v)) ((Text) e).ellipsize(); });
        property("Text", "lines", (e, v, c) -> ((Text) e).lines(c.intValue(v)));
        // LIVE component: A TICKED STRING SUPPLIER, WRAPPED TO A LITERAL AND PUSHED THROUGH Text.text ONLY ON CHANGE (§5.5)
        binding("Text", "component", (e, name, data) -> { data.require(name, String.class, CharSequence.class); data.component(e, name); });

        // component/icon MUTUAL EXCLUSION, FIRST-DEFINED WINS (§4): ONLY THE component SIDE CAN CHECK THE OTHER SLOT
        register("Button", Button::new, false);
        property("Button", "component", (e, v, c) -> { if (((Button) e).icon() == null) ((Button) e).text(c.component(v)); });
        property("Button", "icon", (e, v, c) -> ((Button) e).icon(c.icon(v)));
        // {delay, interval} TICKS; A SINGLE NUMBER SETS delay == interval (§11)
        property("Button", "repeat", (e, v, c) -> {
            int delay;
            int interval;
            if (v instanceof UIValue.Tuple t) {
                if (t.items().size() != 2) throw new UIFormatException(null, -1, "repeat needs a {delay, interval} tuple");
                delay = c.intValue(t.items().get(0));
                interval = c.intValue(t.items().get(1));
            } else {
                delay = interval = c.intValue(v);
            }
            // FROM A DOCUMENT A DEAD OR NEGATIVE CADENCE IS ALWAYS A MISTAKE: SURFACE IT (§13.4)
            if (delay < 0 || interval <= 0) throw new UIFormatException(null, -1, "repeat needs delay >= 0 and interval > 0");
            ((Button) e).repeat(delay, interval);
        });
        event("Button", "onClick", leftClick);
        binding("Button", "icon", (e, name, data) -> { data.require(name, Icon.class); ((Button) e).source(data.iconOr(name)); });

        register("ToggleButton", Toggle::new, false);
        property("ToggleButton", "iconOn", (e, v, c) -> ((Toggle) e).iconOn(c.icon(v)));
        property("ToggleButton", "iconOff", (e, v, c) -> ((Toggle) e).iconOff(c.icon(v)));
        // A BAKED value= WRAPS THE CONSTANT IN A SUPPLIER, SO A LITERAL WORKS WITHOUT A @UIVar (§5.5, H7)
        property("ToggleButton", "value", (e, v, c) -> { boolean b = c.bool(v); ((Toggle) e).value(() -> b); });
        event("ToggleButton", "onClick", leftClick);
        binding("ToggleButton", "value", (e, name, data) -> { data.require(name, boolean.class); ((Toggle) e).value(data.boolOr(name)); });

        // state MUST FOLLOW states SO IT CLAMPS TO THE REAL LENGTH; THE CYCLE FIRES onChange(int), NOT onClick
        register("StateButton", State::new, false);
        property("StateButton", "states", (e, v, c) -> ((State) e).states(c.icons(v)));
        property("StateButton", "state", (e, v, c) -> ((State) e).state(c.intValue(v)));
        event("StateButton", "onClick", (e, handler) -> ((State) e).onChange(state -> handler.invoke(String.valueOf(state))));

        // A TAB STRIP BUTTON USABLE ON ITS OWN; ParentTab BUILDS ITS OWN FROM EACH TAB DOC'S tabIcon/tabTooltip
        register("TabButton", Tab::new, false);
        property("TabButton", "component", (e, v, c) -> ((Tab) e).title(c.component(v)));
        property("TabButton", "icon", (e, v, c) -> ((Tab) e).icon(c.icon(v)));
        event("TabButton", "onClick", leftClick);

        // ---- ICONS & EMBLEMS (§11) -----------------------------------------------------
        register("Icon", me.srrapero720.waterui.widget.Icon::new, false);
        property("Icon", "icon", (e, v, c) -> ((me.srrapero720.waterui.widget.Icon) e).icon(c.icon(v)));
        binding("Icon", "icon", (e, name, data) -> { data.require(name, Icon.class); ((me.srrapero720.waterui.widget.Icon) e).source(data.iconOr(name)); });

        register("ItemIcon", ItemIcon::new, false);
        property("ItemIcon", "item", (e, v, c) -> ((ItemIcon) e).stack(c.item(v)));
        property("ItemIcon", "flipX", (e, v, c) -> { if (c.bool(v)) ((ItemIcon) e).flipX(); });

        register("PlayerHead", PlayerHead::new, false);
        property("PlayerHead", "uuid", (e, v, c) -> ((PlayerHead) e).uuid(uuid(c.string(v))));

        // ---- STATEFUL CONTROLS (§11) ---------------------------------------------------
        register("Switch", Switch::new, false);
        property("Switch", "component", (e, v, c) -> ((Switch) e).text(c.component(v)));
        property("Switch", "value", (e, v, c) -> ((Switch) e).value(c.bool(v)));
        property("Switch", "textScale", (e, v, c) -> ((Switch) e).textScale(c.floatValue(v)));
        event("Switch", "onToggle", (e, handler) -> ((Switch) e).onChange(v -> handler.invoke(String.valueOf(v))));
        binding("Switch", "value", (e, name, data) -> { data.require(name, boolean.class); ((Switch) e).source(data.boolOr(name)); });

        register("Slider", Slider::new, false);
        property("Slider", "min", (e, v, c) -> ((Slider) e).range(c.doubleValue(v), ((Slider) e).max));
        property("Slider", "max", (e, v, c) -> ((Slider) e).range(((Slider) e).min, c.doubleValue(v)));
        property("Slider", "value", (e, v, c) -> ((Slider) e).value(c.doubleValue(v)));
        property("Slider", "stepped", (e, v, c) -> ((Slider) e).stepped(c.bool(v)));
        property("Slider", "icon", (e, v, c) -> ((Slider) e).icon(c.icon(v)));
        property("Slider", "format", (e, v, c) -> ((Slider) e).format(UIValues.format(v)));
        // BAKED KEY FOR PLACEHOLDER SLIDERS; MEMOIZED PER Language INSTANCE, WHICH SWAPS ON A LANGUAGE RELOAD
        property("Slider", "formatKey", (e, v, c) -> {
            String key = c.string(v);
            Language[] lang = { null };
            String[] text = { null };
            ((Slider) e).format((value, max) -> {
                Language current = Language.getInstance();
                if (current != lang[0]) {
                    lang[0] = current;
                    text[0] = Element.translate(key);
                }
                return text[0];
            });
        });
        property("Slider", "textScale", (e, v, c) -> ((Slider) e).textScale(c.floatValue(v)));
        event("Slider", "onValueChange", (e, handler) -> ((Slider) e).onChange(x -> handler.invoke(UIValues.number(x))));
        event("Slider", "onValueCommit", (e, handler) -> ((Slider) e).onCommit(x -> handler.invoke(UIValues.number(x))));

        // min/max/step BEFORE value SO IT LANDS INSIDE THE FINAL RANGE
        register("Stepper", Stepper::new, false);
        property("Stepper", "min", (e, v, c) -> ((Stepper) e).min(c.doubleValue(v)));
        property("Stepper", "max", (e, v, c) -> ((Stepper) e).max(c.doubleValue(v)));
        property("Stepper", "step", (e, v, c) -> ((Stepper) e).step(c.doubleValue(v)));
        property("Stepper", "value", (e, v, c) -> ((Stepper) e).value(c.doubleValue(v)));
        event("Stepper", "onValueChange", (e, handler) -> ((Stepper) e).onChange(x -> handler.invoke(UIValues.number(x))));
        event("Stepper", "onValueCommit", (e, handler) -> ((Stepper) e).onCommit(x -> handler.invoke(UIValues.number(x))));

        // ---- BARS (§11): value/max/time/duration ARE LIVE NUMBERS READ EVERY FRAME (§5.5) ---
        register("ProgressBar", ProgressBar::new, false);
        property("ProgressBar", "format", (e, v, c) -> ((ProgressBar) e).format(UIValues.format(v)));
        property("ProgressBar", "textScale", (e, v, c) -> ((ProgressBar) e).textScale(c.floatValue(v)));
        // BAKED value/max WRAP THE CONSTANT IN A SUPPLIER SO A LITERAL WORKS WITHOUT A @UIVar (§5.5, H7)
        property("ProgressBar", "value", (e, v, c) -> { long n = c.longValue(v); ((ProgressBar) e).value(() -> n); });
        property("ProgressBar", "max", (e, v, c) -> { long n = c.longValue(v); ((ProgressBar) e).max(() -> n); });
        binding("ProgressBar", "value", (e, name, data) -> { data.require(name, long.class, int.class); ((ProgressBar) e).value(data.numberOr(name)); });
        binding("ProgressBar", "max", (e, name, data) -> { data.require(name, long.class, int.class); ((ProgressBar) e).max(data.numberOr(name)); });

        register("Seekbar", SeekBar::new, false);
        property("Seekbar", "format", (e, v, c) -> ((SeekBar) e).format(UIValues.format(v)));
        property("Seekbar", "textScale", (e, v, c) -> ((SeekBar) e).textScale(c.floatValue(v)));
        property("Seekbar", "time", (e, v, c) -> { long n = c.longValue(v); ((SeekBar) e).time(() -> n); });
        property("Seekbar", "duration", (e, v, c) -> { long n = c.longValue(v); ((SeekBar) e).duration(() -> n); });
        binding("Seekbar", "time", (e, name, data) -> { data.require(name, long.class, int.class); ((SeekBar) e).time(data.numberOr(name)); });
        binding("Seekbar", "duration", (e, name, data) -> { data.require(name, long.class, int.class); ((SeekBar) e).duration(data.numberOr(name)); });
        event("Seekbar", "onSeekStart", (e, handler) -> ((SeekBar) e).onSeekStart(x -> handler.invoke(UIValues.number(x))));
        event("Seekbar", "onSeek", (e, handler) -> ((SeekBar) e).onSeek(x -> handler.invoke(UIValues.number(x))));
        event("Seekbar", "onSeekEnd", (e, handler) -> ((SeekBar) e).onSeekEnd(x -> handler.invoke(UIValues.number(x))));
        event("Seekbar", "onScroll", (e, handler) -> ((SeekBar) e).onScroll(x -> handler.invoke(UIValues.number(x))));

        // ---- INPUT (§11): value IS BAKED ONLY, LIVE TEXT WOULD FIGHT THE USER'S TYPING -----
        register("InputText", InputText::new, false);
        property("InputText", "value", (e, v, c) -> ((InputText) e).value(c.string(v)));
        // suggestion GOES THROUGH COMPONENT RESOLUTION SO A translatable: KEY RESOLVES BEFORE IT REACHES THE FIELD (§4)
        property("InputText", "suggestion", (e, v, c) -> ((InputText) e).suggestion(c.component(v).getString()));
        property("InputText", "maxLength", (e, v, c) -> ((InputText) e).maxLength(c.intValue(v)));
        event("InputText", "onTextChange", (e, handler) -> ((InputText) e).onChange(handler::invoke));
        event("InputText", "onSubmit", (e, handler) -> ((InputText) e).onSubmit(handler::invoke));
        event("InputText", "onValueCommit", (e, handler) -> ((InputText) e).onCommit(handler::invoke));

        // ---- THUMBNAIL (§11): source MAY BE A BAKED STRING OR A LIVE STRING SUPPLIER ----
        register("Thumbnail", Thumbnail::new, false);
        property("Thumbnail", "source", (e, v, c) -> ((Thumbnail) e).source(c.string(v)));
        property("Thumbnail", "fallback", (e, v, c) -> ((Thumbnail) e).fallback(c.icon(v)));
        event("Thumbnail", "onLoad", (e, handler) -> ((Thumbnail) e).onLoad(handler::invoke));
        event("Thumbnail", "onError", (e, handler) -> ((Thumbnail) e).onError(handler::invoke));
        binding("Thumbnail", "source", (e, name, data) -> { data.require(name, String.class, CharSequence.class); ((Thumbnail) e).source(data.stringOr(name)); });

        // ---- GENERIC POINTER EVENTS (§12.4): A TAG'S OWN SAME-NAME EVENT OUTRANKS THESE ----
        event("onClick", (e, handler) -> e.onClick(handler::invoke));
        event("onRightClick", (e, handler) -> e.onRightClick(handler::invoke));
        event("onDoubleClick", (e, handler) -> e.onDoubleClick(handler::invoke));
        event("onLongPress", (e, handler) -> e.onLongPress(handler::invoke));
        event("onPressStart", (e, handler) -> e.onPressStart(handler::invoke));
        event("onPressEnd", (e, handler) -> e.onPressEnd(handler::invoke));
        event("onHover", (e, handler) -> e.onHover(inside -> handler.invoke(String.valueOf(inside))));
        event("onEnter", (e, handler) -> e.onEnter(handler::invoke));
        event("onExit", (e, handler) -> e.onExit(handler::invoke));
        event("onDrag", (e, handler) -> e.onDrag((x, y) -> handler.invoke(UIValues.number(x), UIValues.number(y))));
        event("onScroll", (e, handler) -> e.onScroll(amount -> handler.invoke(UIValues.number(amount))));
        event("onFocus", (e, handler) -> e.onFocus(handler::invoke));
        event("onBlur", (e, handler) -> e.onBlur(handler::invoke));
        event("onFileDrop", (e, handler) -> e.onFileDrop(files -> {
            String[] paths = new String[files.size()];
            for (int i = 0; i < files.size(); i++) paths[i] = files.get(i).toString();
            handler.invoke(paths);
        }));

        // ---- GENERIC LIVE BINDINGS (§5.5): tooltip/enabled/visible EVALUATED AT THE 20 Hz TICK ----
        binding("tooltip", (e, name, data) -> { data.require(name, List.class); data.tooltip(e, name); });
        binding("enabled", (e, name, data) -> { data.require(name, boolean.class); data.enabled(e, name); });
        binding("visible", (e, name, data) -> { data.require(name, boolean.class); data.visible(e, name); });

        WaterUI.LOGGER.info(IT, "registry bootstrap complete: {} element tag(s)", FACTORIES.size());
        // PUBLISH ONLY ONCE EVERY CORE TAG IS IN THE MAPS, SO A RE-ENTRANT CALLER NEVER SEES A HALF-BUILT REGISTRY
        ready = true;
    }

    private static UUID uuid(String raw) {
        try {
            return UUID.fromString(raw.trim());
        } catch (RuntimeException e) {
            throw new UIFormatException(null, -1, "invalid UUID '" + raw + "'");
        }
    }
}
