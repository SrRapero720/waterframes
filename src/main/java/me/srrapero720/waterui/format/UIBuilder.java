package me.srrapero720.waterui.format;

import me.srrapero720.waterui.WaterUI;
import me.srrapero720.waterui.core.AbstractParent;
import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.layout.FrameLayout;
import me.srrapero720.waterui.layout.Grid;
import me.srrapero720.waterui.layout.ParentLinear;
import me.srrapero720.waterui.layout.ParentList;
import me.srrapero720.waterui.screen.Dialog;
import me.srrapero720.waterui.screen.Panel;
import me.srrapero720.waterui.theme.Color;
import me.srrapero720.waterui.theme.Drawable;
import me.srrapero720.waterui.theme.Theme;
import me.srrapero720.waterui.theme.ThemeRole;
import me.srrapero720.waterui.widget.ParentTab;
import me.srrapero720.waterui.widget.Tab;
import me.srrapero720.waterui.widget.Text;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Turns a parsed {@link UIDocument} into a live {@link Element} tree under a {@link Panel} root
 * (UI-SPEC.md §6, §8-§13), depth-first. It resolves the variable contract, the theme fallback chain
 * and the placement context (flow vs free space). Any error raised on the way is caught at the
 * boundary and replaced with one red {@link Text} inside the root — the screen always opens (§13.4).
 */
public final class UIBuilder {
    private static final Marker IT = MarkerManager.getMarker(UIBuilder.class.getSimpleName());

    private UIBuilder() {}

    // PLACEMENT CONTEXT OF A CHILD: THE FLOW OF A LINEAR CONTAINER, OR FREE 2D SPACE (GRID CELL, SCREEN)
    private enum Place { FLOW_H, FLOW_V, FREE }

    // CONTEXTUAL ROOT PREFERENCES (§6.2): CONSUMED ONLY WHEN THE DOCUMENT IS USED A CERTAIN WAY
    // (TAB HOOKS, ROW TEMPLATE THUMB), INERT EVERYWHERE ELSE INSTEAD OF AN UNKNOWN-PROPERTY ERROR
    private static final List<String> CONTEXTUAL = List.of("tabIcon", "tabTooltip", "onActivate", "onDeactivate", "dragThumbId");

    /** Loads {@code ui}, wires it against {@code host} and populates {@code root} (§6, §13). */
    public static void inflate(ResourceLocation ui, Panel root, Object host, Map<String, Object> vars) {
        UIRegistry.bootstrap();
        WaterUI.LOGGER.debug(IT, "inflating {} into {}", ui, root.name);
        // DIALOGS OWN THEIR CONTEXT; THE ROOT PANEL USES THE SCREEN'S SHARED ONE
        DataContext data = root instanceof Dialog d && d.context() != null ? d.context() : (root.screen() != null ? root.screen().context() : null);
        if (data != null) data.host(host);
        boolean base = root.screen() != null && root.screen().base(root);

        UIDocument doc = null;
        try {
            // RELEASE THE PREVIOUS INFLATE'S BUILDER-OWNED RESOURCES BEFORE THIS ONE REGISTERS ITS OWN (§13.3)
            root.disposeInflated();

            doc = UILoader.document(ui);
            if (doc == null) { error(root, ui.toString(), 0, "UI not found: " + ui); return; }

            UIContract contract = UIContract.build(doc, vars);

            Theme theme = UIThemes.resolve(doc.themeId(), doc.source());
            root.theme(theme);
            if (base) root.screen().theme = theme;

            Place rootFlow = applyRoot(root, doc, contract, host);

            ArrayDeque<ResourceLocation> ancestry = new ArrayDeque<>();
            ancestry.push(doc.source());

            for (UINode node: doc.tree()) {
                // AN outsideAnchor AT THE LOADED DOCUMENT'S OWN TOP LEVEL LIFTS THE ELEMENT OUT OF THE FLOW (§9.5)
                boolean outside = base && node.props().containsKey("outsideAnchor");
                Element element = build(node, contract, data, host, root::disposable, ancestry, outside ? Place.FREE : rootFlow);
                if (outside) {
                    if (element.width() == Element.FILL || element.height() == Element.FILL) {
                        throw new UIFormatException(node.source().toString(), node.line(), "an outside element cannot be FILL (§9.5)");
                    }
                    root.screen().outside(element);
                } else {
                    root.add(element);
                }
            }
            WaterUI.LOGGER.debug(IT, "inflated {} ({} root node(s))", doc.source(), doc.tree().size());
        } catch (UIFormatException ex) {
            String file = ex.file != null ? ex.file : (doc != null ? doc.source().toString() : ui.toString());
            error(root, file, ex.line, rawMessage(ex));
        } catch (RuntimeException ex) {
            // A DECODER OR APPLIER THAT THREW A PLAIN EXCEPTION (e.g. a bad enum name) STILL OPENS THE SCREEN (§13.4)
            error(root, doc != null ? doc.source().toString() : ui.toString(), 0, ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    /**
     * Inflates {@code doc}'s top-level tree into an already-generated {@code row} container, no {@link Panel}
     * involved (§10). The importer-generates/root-configures flow of {@link #buildTabs}: the template's root
     * preferences shape the row, its elements flow inside. Live bindings resolve against {@code data} (the row
     * model), events against {@code eventHost} (the screen level, §12); resource owners register into
     * {@code disposables} (the row's own sink, §13.3). A throw propagates so the list drops that one row (§13.4).
     * Returns the element the root's {@code dragThumbId} names, resolved inside this row, or null (§10.6).
     */
    public static Element inflateRow(UIDocument doc, ParentLinear row, UIContract contract, DataContext data, Object eventHost, Consumer<Element> disposables) {
        // THE TEMPLATE ROOT PREFS CONFIGURE THE GENERATED ROW (§6.1); frame AND THE TAB-ONLY HOOKS ARE INERT HERE.
        // ROOT EVENTS BIND TOO, SO A TEMPLATE'S onClick=select(key) LIVES ON THE ROW ITSELF (§12)
        List<Map.Entry<String, UIValue>> events = new ArrayList<>();
        UIValue thumbRef = null;
        for (Map.Entry<String, UIValue> rp: doc.rootProps().entrySet()) {
            String key = rp.getKey();
            // dragThumbId IS THE ROW-CONTEXT PREFERENCE (§6.2); THE REST OF THE CONTEXTUAL SET IS INERT HERE
            if ("dragThumbId".equals(key)) { thumbRef = rp.getValue(); continue; }
            if (CONTEXTUAL.contains(key) || "frame".equals(key)) continue;
            if (UIParser.isEvent(key)) { events.add(rp); continue; }
            applyOne(row, "Parent", key, rp.getValue(), contract, Place.FREE, doc.source(), 0);
        }
        bindEvents(row, "Parent", events, contract, eventHost, doc.source(), 0);
        ArrayDeque<ResourceLocation> ancestry = new ArrayDeque<>();
        ancestry.push(doc.source());
        Place flow = childPlace("Parent", row);
        for (UINode node: doc.tree()) row.add(build(node, contract, data, eventHost, disposables, ancestry, flow));
        return thumbRef != null ? dragThumb(thumbRef, row, doc.source()) : null;
    }

    // RESOLVES THE ROOT'S dragThumbId WITHIN THE ROW'S OWN SUBTREE (§10.6): TEMPLATE IDS REPEAT
    // ACROSS ROWS, SO THE LOOKUP IS ROW-SCOPED AND THE LIST KEEPS THEM ENCAPSULATED.
    // A TEMPLATE-AUTHORED SLIP MUST NOT KILL EVERY ROW (§13.4): THE ROWS BUILD WITHOUT A DRAG HANDLE
    private static Element dragThumb(UIValue ref, ParentLinear row, ResourceLocation file) {
        ResourceLocation base = resolveId(ref, file);
        Element thumb = row.get(base);
        if (thumb == null) WaterUI.LOGGER.error(IT, "{} dragThumbId '{}' resolves to nothing in the template (§10.6)", file, base.getPath());
        return thumb;
    }

    // APPLIES THE root[...] BAG TO THE PANEL WHEN LOADED FIRST (§6.1); RETURNS THE FLOW ITS CHILDREN SIT IN
    private static Place applyRoot(Panel root, UIDocument doc, UIContract contract, Object host) {
        List<Map.Entry<String, UIValue>> events = new ArrayList<>();
        for (Map.Entry<String, UIValue> entry: doc.rootProps().entrySet()) {
            String name = entry.getKey();
            if (CONTEXTUAL.contains(name)) continue;
            if ("frame".equals(name)) {
                // frame IS root-ONLY CHROME (§6.1): EDGE TRADES THE THICK BORDER FOR A ONE-PIXEL PANEL EDGE
                String frame = keyword(entry.getValue());
                if ("EDGE".equals(frame)) root.edgeFrame();
                else if (!"FULL".equals(frame)) throw new UIFormatException(doc.source().toString(), 0, "unknown frame '" + frame + "' (FULL or EDGE)");
                continue;
            }
            if (UIParser.isEvent(name)) { events.add(entry); continue; }
            applyOne(root, "Parent", name, entry.getValue(), contract, Place.FREE, doc.source(), 0);
        }
        bindEvents(root, "Parent", events, contract, host, doc.source(), 0);
        return root.orientation == ParentLinear.Orientation.VERTICAL ? Place.FLOW_V : Place.FLOW_H;
    }

    // DEPTH-FIRST CONSTRUCTION OF ONE NODE (§8); AN IMPORT ALIAS INSERTS A DOCUMENT, ParentTab BUILDS TABS.
    // disposables SINKS EVERY RESOURCE-OWNING ELEMENT: THE PANEL'S REGISTRY FOR A SCREEN, THE ROW'S OWN FOR A TEMPLATED LIST (§10, §13.3)
    private static Element build(UINode node, UIContract contract, DataContext data, Object host, Consumer<Element> disposables, ArrayDeque<ResourceLocation> ancestry, Place place) {
        String tag = node.tag();
        UIImport imp = contract.alias(tag);
        if (imp != null) {
            // A JAVA IMPORT WAS ALREADY VALIDATED EAGERLY AT CONTRACT BUILD (§7.3); THE ALIAS IS ITS REGISTERED TAG
            if (imp.kind() == UIImport.Kind.JAVA) tag = imp.alias();
            else return buildImport(node, imp, contract, data, host, disposables, ancestry, place);
        }
        if ("ParentTab".equals(tag)) return buildTabs(node, contract, data, host, disposables, ancestry);

        UIRegistry.Factory factory = UIRegistry.factory(tag);
        if (factory == null) throw new UIFormatException(node.source().toString(), node.line(), "unknown element '" + node.tag() + "'");
        Element element = factory.create();
        // A RESOURCE-OWNING ELEMENT (PLAYER/TEXTURE) IS RELEASED BY ITS OWNER ON THE NEXT INFLATE OR ON CLOSE (§13.3)
        if (element.ownsResources()) disposables.accept(element);
        // A ParentList's ROWS RESOLVE THEIR EVENTS AGAINST THE SCREEN-LEVEL HOST BY DEFAULT (§12); eventHost(...) MAY OVERRIDE
        if (element instanceof ParentList list) list.eventHost(host);

        applyProps(element, node, tag, contract, data, host, place);

        boolean container = UIRegistry.container(tag);
        if (!node.children().isEmpty() && !container) throw new UIFormatException(node.source().toString(), node.line(), "'" + node.tag() + "' cannot have children");

        // §10.1: A CHILD SET TO FILL ON AN AXIS ITS PARENT SCROLLS HAS UNBOUNDED LEFTOVER THERE -> COMPILE ERROR
        boolean scrollX = false, scrollY = false;
        if (element instanceof ParentLinear scroller) { scrollX = scroller.scrollsX(); scrollY = scroller.scrollsY(); }
        // §10.4: A ComboButton FUSES BUTTONS ONLY; ANYTHING ELSE NESTED IS A VALIDATION ERROR
        boolean combo = "ComboButton".equals(tag);
        Place childPlace = childPlace(tag, element);
        for (UINode childNode: node.children()) {
            if (combo && !isButtonTag(childNode.tag())) throw new UIFormatException(childNode.source().toString(), childNode.line(), "ComboButton nests only Button/ToggleButton/StateButton, found '" + childNode.tag() + "' (§10.4)");
            Element child = build(childNode, contract, data, host, disposables, ancestry, childPlace);
            if (scrollX && child.width() == Element.FILL) throw new UIFormatException(childNode.source().toString(), childNode.line(), "a child cannot be FILL on the scrolled X axis (§10.1)");
            if (scrollY && child.height() == Element.FILL) throw new UIFormatException(childNode.source().toString(), childNode.line(), "a child cannot be FILL on the scrolled Y axis (§10.1)");
            ((AbstractParent) element).add(child);
        }
        return element;
    }

    private static boolean isButtonTag(String tag) {
        return "Button".equals(tag) || "ToggleButton".equals(tag) || "StateButton".equals(tag);
    }

    // A GRID CELL AND A Frame OVERLAY ARE FREE SPACE (§9.3, §10.2); A LINEAR CONTAINER FLOWS ALONG ITS ORIENTATION (§9.1)
    private static Place childPlace(String tag, Element element) {
        if ("Grid".equals(tag) || element instanceof FrameLayout) return Place.FREE;
        if (element instanceof ParentLinear pl) return pl.orientation == ParentLinear.Orientation.VERTICAL ? Place.FLOW_V : Place.FLOW_H;
        return Place.FLOW_V;
    }

    // PASS 1: id FIRST (SO EVENTS SEE IT), THEN GENERICS + TAG PROPS + LIVE BINDINGS; PASS 2: EVENTS
    private static void applyProps(Element element, UINode node, String tag, UIContract contract, DataContext data, Object host, Place place) {
        UIValue idv = node.props().get("id");
        if (idv != null) element.id(resolveId(idv, node.source()));

        List<Map.Entry<String, UIValue>> events = new ArrayList<>();
        for (Map.Entry<String, UIValue> entry: node.props().entrySet()) {
            String name = entry.getKey();
            UIValue value = entry.getValue();
            if ("id".equals(name)) continue;
            if (UIParser.isEvent(name)) { events.add(entry); continue; }
            // A LIVE VARIABLE ON A BINDABLE PROPERTY ATTACHES A HOST SUPPLIER (§5.5); OTHERWISE IT IS A BAKED VALUE
            if (value instanceof UIValue.Id id && contract.live(id.name())) {
                UIRegistry.Binding binding = UIRegistry.binding(tag, name);
                if (binding == null) throw new UIFormatException(node.source().toString(), node.line(), "live variable '" + id.name() + "' used on non-bindable property '" + name + "'");
                binding.bind(element, id.name(), data);
                continue;
            }
            applyOne(element, tag, name, value, contract, place, node.source(), node.line());
        }
        bindEvents(element, tag, events, contract, host, node.source(), node.line());
    }

    // ONE VALUE ONTO e: GENERIC PROPS FIRST (§8.2), THEN THE TAG'S OWN (§11)
    private static void applyOne(Element e, String tag, String name, UIValue value, UIContract contract, Place place, ResourceLocation file, int line) {
        if (applyGeneric(e, tag, name, value, contract, place, file, line)) return;
        UIRegistry.Property property = UIRegistry.tagProperty(tag, name);
        if (property == null) throw new UIFormatException(file.toString(), line, "unknown property '" + name + "' on '" + tag + "'");
        try {
            property.apply(e, value, contract);
        } catch (UIFormatException ex) {
            throw locate(ex, file, line);
        }
    }

    // THE SHARED §8.2 VOCABULARY; RETURNS false WHEN name IS NOT A GENERIC PROPERTY
    private static boolean applyGeneric(Element e, String tag, String name, UIValue value, UIContract contract, Place place, ResourceLocation file, int line) {
        try {
            switch (name) {
                case "width" -> e.width(contract.size(value));
                case "height" -> e.height(contract.size(value));
                case "weight" -> e.weight(contract.floatValue(value));
                case "margin" -> e.margin(contract.spacing(value));
                case "padding" -> e.padding(contract.spacing(value));
                case "border" -> e.border(contract.intValue(value));
                case "shadow" -> e.shadow(contract.bool(value));
                case "enabled" -> e.enabled(contract.bool(value));
                case "visible" -> e.visible(contract.bool(value));
                case "hidden" -> e.hidden(contract.bool(value));
                case "elevation" -> e.elevation(contract.intValue(value));
                case "scale" -> e.scale(contract.floatValue(value));
                case "tooltip" -> e.tooltip(List.of(contract.component(value)));
                case "face" -> e.face(surface(value));
                case "outline" -> e.outline(surface(value));
                case "align" -> applyAlign(e, tag, value, place);
                case "anchor" -> applyAnchor(e, value, place);
                case "outsideAnchor" -> { int[] o = UIValues.outsideAnchor(value); e.outsideAnchor(o[0], o[1]); }
                case "justify" -> applyJustify(e, value);
                case "scrollX" -> scroll(e, value, contract, true);
                case "scrollY" -> scroll(e, value, contract, false);
                default -> { return false; }
            }
        } catch (UIFormatException ex) {
            throw locate(ex, file, line);
        }
        return true;
    }

    // align IS THE CONTAINER DEFAULT ON A Parent (VALIDATED VS ITS OWN FLOW), A CHILD OVERRIDE ELSEWHERE (§9.2)
    private static void applyAlign(Element e, String tag, UIValue value, Place place) {
        if ("Parent".equals(tag) && e instanceof ParentLinear pl) {
            pl.alignChildren(UIValues.alignMask(value, pl.orientation == ParentLinear.Orientation.VERTICAL));
            return;
        }
        if (place == Place.FREE) throw new UIFormatException(null, -1, "align is invalid in free space (use anchor) (§9.3)");
        e.align(UIValues.alignMask(value, place == Place.FLOW_V));
    }

    // anchor ATTACHES A CHILD IN FREE SPACE ONLY; ON A FLOW CHILD IT IS A COMPILE ERROR (§9.3)
    private static void applyAnchor(Element e, UIValue value, Place place) {
        if (place != Place.FREE) throw new UIFormatException(null, -1, "anchor is invalid on a flow child (use align) (§9.3)");
        e.anchor(UIValues.anchorMask(value));
    }

    private static void applyJustify(Element e, UIValue value) {
        if (e instanceof Grid grid) grid.justify(UIValues.justify(value));
        else if (e instanceof ParentLinear pl) pl.justify(UIValues.justify(value));
        else throw new UIFormatException(null, -1, "justify is valid only on Parent and Grid (§9.4)");
    }

    private static void scroll(Element e, UIValue value, UIContract contract, boolean x) {
        if (!(e instanceof ParentLinear pl)) throw new UIFormatException(null, -1, "scroll is valid only on a linear container (§10.1)");
        if (x) pl.scrollX(contract.bool(value));
        else pl.scrollY(contract.bool(value));
    }

    private static void bindEvents(Element element, String tag, List<Map.Entry<String, UIValue>> events, UIContract contract, Object host, ResourceLocation file, int line) {
        for (Map.Entry<String, UIValue> entry: events) {
            UIRegistry.EventBinder binder = UIRegistry.eventBinder(tag, entry.getKey());
            if (binder == null) {
                // AN UNKNOWN EVENT IS A RECOVERABLE SLIP (§13.4): LOG AND NO-OP, THE SCREEN STILL OPENS
                WaterUI.LOGGER.error(IT, "{}:{} unknown event '{}' on '{}'", file, line, entry.getKey(), tag);
                continue;
            }
            UIValue.Call call = (UIValue.Call) entry.getValue();
            UIEvents.Handler handler = UIEvents.resolve(host, call.name(), bakeArgs(call, contract, element.id()), file, line);
            binder.bind(element, handler);
        }
    }

    // BAKES THE ELEMENT id AS THE FIRST ARGUMENT, THEN EACH CALL ARGUMENT AS A STRING (§12.1)
    private static String[] bakeArgs(UIValue.Call call, UIContract contract, ResourceLocation id) {
        String[] baked = new String[call.args().size() + 1];
        // AN id-LESS ELEMENT BAKES AN EMPTY FIRST ARG, NEVER THE LITERAL "null"
        baked[0] = id != null ? id.toString() : "";
        for (int i = 0; i < call.args().size(); i++) baked[i + 1] = contract.eventArg(call.args().get(i));
        return baked;
    }

    // sources[width=FILL] INSERTS AN IMPORTED DOCUMENT'S ROOT CONTENT; THE PLACING BAG SHAPES THE CONTAINER (§7.1)
    private static Element buildImport(UINode node, UIImport imp, UIContract contract, DataContext data, Object host, Consumer<Element> disposables, ArrayDeque<ResourceLocation> ancestry, Place place) {
        ResourceLocation ref = contract.aliasDoc(imp.alias());
        UIDocument imported = UILoader.document(ref);
        if (imported == null) throw new UIFormatException(node.source().toString(), node.line(), "import '" + imp.alias() + "' references unknown/unloaded document '" + ref + "'");
        if (ancestry.contains(ref)) throw new UIFormatException(node.source().toString(), node.line(), "recursive import '" + ref + "'");
        WaterUI.LOGGER.debug(IT, "inserting import {} as '{}'", ref, imp.alias());

        ParentLinear container = new ParentLinear(ParentLinear.Orientation.VERTICAL);
        // IMPORTED ROOT PREFS SHAPE THE CONTAINER FOR EVERY KEY THE ALIAS BAG DID NOT SET; THE BAG WINS (§6.1, §7.2)
        for (Map.Entry<String, UIValue> rp: imported.rootProps().entrySet()) {
            String key = rp.getKey();
            if (CONTEXTUAL.contains(key) || "frame".equals(key) || node.props().containsKey(key)) continue;
            applyOne(container, "Parent", key, rp.getValue(), contract, place, imported.source(), 0);
        }
        applyProps(container, node, "Parent", contract, data, host, place);
        if (container.id() == null) container.id(ref);

        ancestry.push(ref);
        try {
            Place childPlace = childPlace("Parent", container);
            for (UINode child: imported.tree()) container.add(build(child, contract, data, host, disposables, ancestry, childPlace));
        } finally {
            ancestry.pop();
        }
        return container;
    }

    // ParentTab[tabs={a, b}]: EACH ALIAS IS ONE TAB; THE STRIP BUTTON COMES FROM THE TAB DOC'S tabIcon/tabTooltip (§10.3)
    private static Element buildTabs(UINode node, UIContract contract, DataContext data, Object host, Consumer<Element> disposables, ArrayDeque<ResourceLocation> ancestry) {
        UIValue tabsValue = node.props().get("tabs");
        if (!(tabsValue instanceof UIValue.Tuple tabs)) throw new UIFormatException(node.source().toString(), node.line(), "ParentTab needs a 'tabs' tuple of import aliases");
        WaterUI.LOGGER.debug(IT, "building tab strip with {} tab(s)", tabs.items().size());

        ParentTab tabbed = new ParentTab();
        UIValue idv = node.props().get("id");
        if (idv != null) tabbed.id(resolveId(idv, node.source()));

        List<Map.Entry<String, UIValue>> events = new ArrayList<>();
        for (Map.Entry<String, UIValue> entry: node.props().entrySet()) {
            String name = entry.getKey();
            if ("tabs".equals(name) || "id".equals(name)) continue;
            if (UIParser.isEvent(name)) { events.add(entry); continue; }
            applyOne(tabbed, "ParentTab", name, entry.getValue(), contract, Place.FLOW_V, node.source(), node.line());
        }
        bindEvents(tabbed, "ParentTab", events, contract, host, node.source(), node.line());

        for (UIValue item: tabs.items()) {
            if (!(item instanceof UIValue.Id ref)) throw new UIFormatException(node.source().toString(), node.line(), "a tab entry must be an import alias");
            ResourceLocation stem = contract.aliasDoc(ref.name());
            if (stem == null) throw new UIFormatException(node.source().toString(), node.line(), "'" + ref.name() + "' is not an imported document");
            UIDocument tab = UILoader.document(stem);
            if (tab == null) throw new UIFormatException(node.source().toString(), node.line(), "tab references unknown/unloaded document '" + stem + "'");
            if (ancestry.contains(stem)) throw new UIFormatException(node.source().toString(), node.line(), "recursive tab '" + stem + "'");

            // THE STRIP BUTTON TAKES tabIcon/tabTooltip AND THE LIFECYCLE HOOKS; EVERY OTHER ROOT PROP SHAPES
            // THE AUTO-GENERATED BODY (§6.1, §10.3), WHICH IS BARE, SO THEY APPLY WHOLE — NO WRAPPER ELEMENT
            Tab button = new Tab();
            Runnable[] lifecycle = new Runnable[2];
            ParentLinear content = new ParentLinear(ParentLinear.Orientation.VERTICAL);
            content.id(stem);
            for (Map.Entry<String, UIValue> rp: tab.rootProps().entrySet()) {
                String key = rp.getKey();
                switch (key) {
                    case "tabIcon" -> button.icon(contract.icon(rp.getValue()));
                    case "tabTooltip" -> button.tooltip(List.of(contract.component(rp.getValue())));
                    case "onActivate" -> lifecycle[0] = tabHook(host, rp.getValue(), stem, contract);
                    case "onDeactivate" -> lifecycle[1] = tabHook(host, rp.getValue(), stem, contract);
                    case "frame" -> { /* PANEL-ONLY CHROME, NOT A BODY PROPERTY */ }
                    default -> applyOne(content, "Parent", key, rp.getValue(), contract, Place.FREE, stem, 0);
                }
            }

            Place tabPlace = childPlace("Parent", content);
            ancestry.push(stem);
            try {
                for (UINode child: tab.tree()) content.add(build(child, contract, data, host, disposables, ancestry, tabPlace));
            } finally {
                ancestry.pop();
            }
            tabbed.tab(content, button, lifecycle[0], lifecycle[1]);
        }
        return tabbed;
    }

    // A TAB LIFECYCLE HOOK RESOLVES AGAINST THE HOST WITH THE TAB DOCUMENT id AS THE ELEMENT id (§12.6)
    private static Runnable tabHook(Object host, UIValue value, ResourceLocation stem, UIContract contract) {
        if (!(value instanceof UIValue.Call call)) throw new UIFormatException(stem.toString(), -1, "onActivate/onDeactivate need parentheses");
        UIEvents.Handler handler = UIEvents.resolve(host, call.name(), bakeArgs(call, contract, stem), stem, 0);
        return handler::invoke;
    }

    // #hex PINS A FLAT COLOUR; A BARE ROLE NAME FOLLOWS THE LIVE THEME (§8.4)
    private static Drawable surface(UIValue value) {
        if (value instanceof UIValue.Hex h) return new Color(h.argb());
        if (value instanceof UIValue.Id id) return themeRole(id.name());
        throw new UIFormatException(null, -1, "face/outline expects a #color or a theme role name");
    }

    private static ThemeRole themeRole(String name) {
        Function<Theme, Drawable> selector = switch (name) {
            case "field" -> Theme::field;
            case "fieldHover" -> Theme::fieldHover;
            case "accent" -> Theme::accent;
            case "accentBorder" -> Theme::accentBorder;
            case "selection" -> Theme::selection;
            case "danger" -> Theme::danger;
            case "dangerBorder" -> Theme::dangerBorder;
            case "disabledOverlay" -> Theme::disabledOverlay;
            case "panel" -> t -> t.panel().face();
            case "clickable" -> t -> t.clickable().face();
            case "nested" -> t -> t.nested().face();
            case "bar" -> t -> t.bar().face();
            default -> throw new UIFormatException(null, -1, "unknown theme role '" + name + "'");
        };
        return new ThemeRole(selector);
    }

    // NAMESPACE IS THE OWNING DOCUMENT'S OWN (§8.2); AN EXPLICIT FOREIGN one:name IS ALLOWED BUT WARNS
    private static ResourceLocation resolveId(UIValue value, ResourceLocation file) {
        String docNs = file.getNamespace();
        String raw = value instanceof UIValue.Id id ? id.name() : value instanceof UIValue.Str s ? s.text() : null;
        if (raw == null) throw new UIFormatException(file.toString(), -1, "id must be an identifier or a string");
        int colon = raw.indexOf(':');
        try {
            if (colon >= 0) {
                String otherNs = raw.substring(0, colon);
                if (!otherNs.equals(docNs)) WaterUI.LOGGER.warn(IT, "id '{}' uses a foreign namespace (document is '{}')", raw, docNs);
                return ResourceLocation.fromNamespaceAndPath(otherNs, raw.substring(colon + 1));
            }
            return ResourceLocation.fromNamespaceAndPath(docNs, raw);
        } catch (RuntimeException e) {
            throw new UIFormatException(file.toString(), -1, "invalid id '" + raw + "'");
        }
    }

    private static String keyword(UIValue value) {
        if (value instanceof UIValue.Id id) return id.name();
        throw new UIFormatException(null, -1, "expected a keyword");
    }

    // A DECODER THREW WITHOUT SOURCE CONTEXT (file null): STAMP THE OWNING DOCUMENT AND LINE ON IT NOW
    private static UIFormatException locate(UIFormatException ex, ResourceLocation file, int line) {
        if (ex.file != null) return ex;
        return new UIFormatException(file.toString(), line, ex.getMessage());
    }

    // UIFormatException BAKES "file:line " INTO getMessage() ONLY WHEN file WAS KNOWN AT THE THROW SITE
    private static String rawMessage(UIFormatException ex) {
        if (ex.file == null) return ex.getMessage();
        String prefix = ex.file + ":" + ex.line + " ";
        return ex.getMessage().startsWith(prefix) ? ex.getMessage().substring(prefix.length()) : ex.getMessage();
    }

    // THE §13.4 SURFACE: ONE RED LABEL INSIDE THE ROOT PLUS AN ERROR LOG LINE, SO A BROKEN DOCUMENT STILL OPENS
    private static void error(Panel root, String file, int line, String message) {
        WaterUI.LOGGER.error(IT, "{}:{} {}", file, line, message);
        Text fallback = new Text().text(Component.literal("UI ERROR: " + file + ":" + line + " " + message));
        fallback.color = 0xFFFF5555;
        root.add(fallback);
    }
}
