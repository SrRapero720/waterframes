package me.srrapero720.waterui.format;

import me.srrapero720.waterui.WaterUI;
import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.core.Spacing;
import me.srrapero720.waterui.theme.Icon;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The variable contract of a loaded document (UI-SPEC.md §5). It aggregates the declarations of the
 * whole import chain with the shadowing rules of §5.3 (a name closer to the root wins on the same
 * type, a type collision is a load error), then resolves every referenced value: a keyword the
 * property expects, a baked variable (host map outranks the inline default, §5.1) or a live variable
 * left for the {@link DataContext}. Baked resolution is memoized and cycle-guarded (§13.4).
 */
public final class UIContract {
    private static final Marker IT = MarkerManager.getMarker(UIContract.class.getSimpleName());

    private final ResourceLocation source;
    private final Map<String, UIDecl> decls;
    private final Map<String, UIImport> aliases;
    private final Map<String, Object> host;

    // MEMOIZED BAKED RESULTS AND THE IN-FLIGHT SET THAT CATCHES a=b=a CYCLES
    private final Map<String, Object> cache = new HashMap<>();
    private final Set<String> resolving = new HashSet<>();

    private UIContract(ResourceLocation source, Map<String, UIDecl> decls, Map<String, UIImport> aliases, Map<String, Object> host) {
        this.source = source;
        this.decls = decls;
        this.aliases = aliases;
        this.host = host;
    }

    /** Builds the aggregated contract for {@code root}, walking its import chain; throws on collisions and cycles. */
    public static UIContract build(UIDocument root, Map<String, Object> host) {
        Skeleton skeleton = skeleton(root);
        WaterUI.LOGGER.debug(IT, "contract for {} built: {} decl(s), {} import(s)", root.source(), skeleton.decls.size(), skeleton.aliases.size());
        return of(skeleton, root.source(), host);
    }

    /**
     * The aggregated declarations and import aliases of a document's whole import chain, walked ONCE
     * (§5.2-§5.3). A templated list bakes this a single time and spins a fresh {@link UIContract} per
     * row over it (§10), so N rows never re-walk the imports or re-validate Java classes and cycles.
     */
    public static final class Skeleton {
        private final Map<String, UIDecl> decls;
        private final Map<String, UIImport> aliases;

        private Skeleton(Map<String, UIDecl> decls, Map<String, UIImport> aliases) {
            this.decls = decls;
            this.aliases = aliases;
        }
    }

    /** Aggregates and validates {@code root}'s import chain ONCE into a reusable skeleton (§10); throws on collisions and cycles. */
    public static Skeleton skeleton(UIDocument root) {
        Map<String, UIDecl> decls = new HashMap<>();
        Map<String, UIImport> aliases = new HashMap<>();
        aggregate(root, decls, aliases, new ArrayDeque<>());
        return new Skeleton(decls, aliases);
    }

    // THE SHARED decls/aliases ARE READ-ONLY AFTER AGGREGATION; ONLY cache/resolving ARE PER-INSTANCE, SO ROWS SHARE SAFELY
    /** A per-row contract sharing {@code skeleton}'s aggregated decls/aliases, with its own {@code source} and baked host map (§10). */
    public static UIContract of(Skeleton skeleton, ResourceLocation source, Map<String, Object> host) {
        return new UIContract(source, skeleton.decls, skeleton.aliases, host != null ? host : Map.of());
    }

    // DEPTH-FIRST FROM THE ROOT: A NAME SEEN CLOSER TO THE ROOT WINS (§5.3); IMPORT ANCESTRY REJECTS CYCLES (§7.1)
    private static void aggregate(UIDocument doc, Map<String, UIDecl> decls, Map<String, UIImport> aliases, ArrayDeque<ResourceLocation> ancestry) {
        if (ancestry.contains(doc.source())) throw new UIFormatException(doc.source().toString(), -1, "recursive import '" + doc.source() + "'");
        ancestry.push(doc.source());
        try {
            // OWN DECLARATIONS FIRST, SO THE IMPORTER OUTRANKS ITS IMPORTS ON THE SAME NAME
            for (UIDecl d: doc.decls()) putDecl(decls, aliases, d, doc);
            for (UIImport imp: doc.imports()) {
                putAlias(decls, aliases, imp, doc);
                if (imp.kind() == UIImport.Kind.JAVA) {
                    // §7.3: AN UNLOADABLE, UNREGISTERED OR NON-Element CLASS FAILS COMPILATION EVEN IF THE TAG IS NEVER USED
                    validateJava(imp, doc.source());
                    continue;
                }
                ResourceLocation ref = docRef(imp, doc.source().getNamespace(), doc.source().toString());
                UIDocument imported = UILoader.document(ref);
                if (imported == null) throw new UIFormatException(doc.source().toString(), imp.line(), "import references unknown/unloaded document '" + ref + "'");
                aggregate(imported, decls, aliases, ancestry);
            }
        } finally {
            ancestry.pop();
        }
    }

    private static void putDecl(Map<String, UIDecl> decls, Map<String, UIImport> aliases, UIDecl decl, UIDocument doc) {
        // SYMMETRIC WITH putAlias: A VARIABLE COLLIDING WITH AN IMPORT ALIAS IS A LOAD ERROR (§7.1)
        if (aliases.containsKey(decl.name())) throw new UIFormatException(doc.source().toString(), decl.line(), "variable '" + decl.name() + "' collides with an import alias");
        UIDecl existing = decls.get(decl.name());
        if (existing == null) { decls.put(decl.name(), decl); return; }
        // DIFFERENT EXPLICIT TYPES NEVER MERGE (§5.3); AN INFERRED (null) TYPE MATCHES ANYTHING
        if (existing.type() != null && decl.type() != null && !existing.type().equals(decl.type())) {
            throw new UIFormatException(doc.source().toString(), decl.line(), "variable '" + decl.name() + "' declared with conflicting types '" + existing.type() + "' and '" + decl.type() + "'");
        }
        // KEEP THE CLOSER-TO-ROOT WINNER
    }

    // AN IMPORT IS AN IMPLICIT final var (§7.1); THE CLOSER-TO-ROOT ALIAS WINS, A CLASH WITH A REAL VAR IS AN ERROR
    private static void putAlias(Map<String, UIDecl> decls, Map<String, UIImport> aliases, UIImport imp, UIDocument doc) {
        if (aliases.containsKey(imp.alias())) return;
        if (decls.containsKey(imp.alias())) {
            throw new UIFormatException(doc.source().toString(), imp.line(), "import alias '" + imp.alias() + "' collides with a variable of a different type");
        }
        aliases.put(imp.alias(), imp);
    }

    // <docNs>:path FOR A LOCAL IMPORT, ns:path FOR A CROSS-MOD ONE (§7)
    private static ResourceLocation docRef(UIImport imp, String docNs, String errFile) {
        try {
            if (imp.kind() == UIImport.Kind.CROSSMOD) {
                int colon = imp.ref().indexOf(':');
                return ResourceLocation.fromNamespaceAndPath(imp.ref().substring(0, colon), imp.ref().substring(colon + 1));
            }
            return ResourceLocation.fromNamespaceAndPath(docNs, imp.ref());
        } catch (RuntimeException e) {
            throw new UIFormatException(errFile, imp.line(), "invalid import reference '" + imp.ref() + "'");
        }
    }

    // ---- QUERIES ----------------------------------------------------------------------

    public ResourceLocation source() { return source; }

    public boolean declared(String name) { return decls.containsKey(name) || aliases.containsKey(name); }

    public boolean live(String name) { UIDecl d = decls.get(name); return d != null && d.mode() == UIDecl.Mode.LIVE; }

    public boolean bakedVar(String name) { UIDecl d = decls.get(name); return d != null && d.mode() == UIDecl.Mode.BAKED; }

    public UIImport alias(String name) { return aliases.get(name); }

    /** The document reference of a local/cross-mod import alias, or null for a Java or unknown alias. */
    public ResourceLocation aliasDoc(String name) {
        UIImport imp = aliases.get(name);
        if (imp == null || imp.kind() == UIImport.Kind.JAVA) return null;
        return docRef(imp, source.getNamespace(), source.toString());
    }

    /**
     * A LAZY document path (§7) resolved like {@link UILoader}'s keying: {@code ns:path} keeps its
     * namespace, a bare path takes the document's own. Unlike an import this pulls nothing into the
     * contract — it is a reference resolved when the consumer loads it (a templated list's rows, §10).
     */
    public ResourceLocation docPath(UIValue v) {
        if (!(v instanceof UIValue.Str s)) throw fail("a document path must be a string");
        String raw = s.text();
        try {
            int colon = raw.indexOf(':');
            if (colon >= 0) return ResourceLocation.fromNamespaceAndPath(raw.substring(0, colon), raw.substring(colon + 1));
            return ResourceLocation.fromNamespaceAndPath(source.getNamespace(), raw);
        } catch (RuntimeException e) {
            throw fail("invalid document path '" + raw + "'");
        }
    }

    // ---- TYPED VALUE DECODING (KEYWORD -> BAKED VAR -> ERROR, §4) ----------------------

    /** {@code FILL}/{@code CONTAIN} sentinel, an exact pixel size, or a baked numeric variable (§8.1). */
    public int size(UIValue v) {
        if (v instanceof UIValue.Id id) {
            if ("FILL".equals(id.name())) return Element.FILL;
            if ("CONTAIN".equals(id.name())) return Element.CONTAIN;
            return (int) number(resolve(id.name()), id.name());
        }
        if (v instanceof UIValue.Num n) return (int) n.value();
        throw fail("expected a size (FILL, CONTAIN, a number or a variable)");
    }

    public int intValue(UIValue v) { return (int) doubleValue(v); }

    public long longValue(UIValue v) { return (long) doubleValue(v); }

    public float floatValue(UIValue v) { return (float) doubleValue(v); }

    public double doubleValue(UIValue v) {
        if (v instanceof UIValue.Num n) return n.value();
        if (v instanceof UIValue.Id id) return number(resolve(id.name()), id.name());
        throw fail("expected a number");
    }

    public boolean bool(UIValue v) {
        if (v instanceof UIValue.Id id) {
            if ("true".equals(id.name())) return true;
            if ("false".equals(id.name())) return false;
            Object o = resolve(id.name());
            if (o instanceof Boolean b) return b;
            throw fail("'" + id.name() + "' is not a boolean");
        }
        throw fail("expected true, false or a boolean variable");
    }

    public String string(UIValue v) {
        if (v instanceof UIValue.Str s) return interpolate(s.text());
        if (v instanceof UIValue.Num n) return UIValues.number(n.value());
        if (v instanceof UIValue.Id id) return text(resolve(id.name()));
        throw fail("expected a string");
    }

    public Component component(UIValue v) {
        return Element.component(string(v));
    }

    public Icon icon(UIValue v) {
        if (v instanceof UIValue.Call c) return atlasIcon(c.name(), c.args());
        if (v instanceof UIValue.Id id) {
            Object o = resolve(id.name());
            if (o instanceof Icon icon) return icon;
            throw fail("'" + id.name() + "' is not an icon");
        }
        throw fail("expected an icon variable or an atlas call");
    }

    public Icon[] icons(UIValue v) {
        if (v instanceof UIValue.Tuple t) {
            Icon[] out = new Icon[t.items().size()];
            for (int i = 0; i < out.length; i++) out[i] = icon(t.items().get(i));
            return out;
        }
        throw fail("states expects a tuple of icons");
    }

    public ItemStack item(UIValue v) {
        if (v instanceof UIValue.Id id) {
            Object o = resolve(id.name());
            if (o instanceof ItemStack stack) return stack;
            throw fail("'" + id.name() + "' is not an ItemStack");
        }
        throw fail("expected an ItemStack variable");
    }

    public Spacing spacing(UIValue v) {
        if (v instanceof UIValue.Id id) {
            Object o = resolve(id.name());
            // A tuple/num DEFAULT RESOLVES TO ITS STRUCTURAL FORM; A HOST-SUPPLIED NUMBER MEANS ALL EDGES
            if (o instanceof UIValue.Tuple || o instanceof UIValue.Num) return UIValues.spacing((UIValue) o);
            if (o instanceof Spacing sp) return sp;
            if (o instanceof Number num) return Spacing.all(num.intValue());
            throw fail("'" + id.name() + "' is not a spacing value");
        }
        return UIValues.spacing(v);
    }

    public int color(UIValue v) {
        if (v instanceof UIValue.Hex h) return h.argb();
        throw fail("expected a #color literal");
    }

    /** One event argument baked to a string (§12.1): a literal or a declared variable reference. */
    public String eventArg(UIValue v) {
        if (v instanceof UIValue.Str s) return interpolate(s.text());
        if (v instanceof UIValue.Num n) return UIValues.number(n.value());
        if (v instanceof UIValue.Hex h) return String.format("#%08X", h.argb());
        if (v instanceof UIValue.Id id) {
            if (!declared(id.name())) throw fail("event argument '" + id.name() + "' is not a declared variable");
            return text(resolve(id.name()));
        }
        throw fail("event arguments must be literals or variables");
    }

    // ---- BAKED RESOLUTION -------------------------------------------------------------

    // RESOLVES A BAKED VARIABLE TO A CONCRETE VALUE OR, FOR A tuple/flags DEFAULT, THE STRUCTURAL UIValue ITSELF
    Object resolve(String name) {
        if (cache.containsKey(name)) return cache.get(name);
        if (aliases.containsKey(name)) throw fail("'" + name + "' is an import, not a value");
        UIDecl decl = decls.get(name);
        if (decl == null) throw fail("unknown variable '" + name + "'");
        if (decl.mode() == UIDecl.Mode.LIVE) throw failAt(decl, "live variable '" + name + "' has no build-time value");

        if (host.containsKey(name)) {
            Object v = host.get(name);
            checkType(decl, v);
            cache.put(name, v);
            return v;
        }
        if (decl.inline() == null) throw failAt(decl, "no value provided for baked variable '" + name + "' (no host value, no inline default)");
        if (!resolving.add(name)) throw failAt(decl, "variable cycle at '" + name + "'");
        try {
            Object v = decodeInline(decl.inline());
            cache.put(name, v);
            return v;
        } finally {
            resolving.remove(name);
        }
    }

    private Object decodeInline(UIValue v) {
        if (v instanceof UIValue.Num n) return n.value();
        if (v instanceof UIValue.Hex h) return h.argb();
        if (v instanceof UIValue.Str s) return interpolate(s.text());
        if (v instanceof UIValue.Id id) {
            if ("true".equals(id.name())) return Boolean.TRUE;
            if ("false".equals(id.name())) return Boolean.FALSE;
            return resolve(id.name());
        }
        if (v instanceof UIValue.Call c) {
            // loadAtlas("path") YIELDS AN ATLAS HANDLE; EVERY OTHER CALL IS A HANDLE INVOCATION YIELDING AN ICON (§5.7)
            if ("loadAtlas".equals(c.name())) {
                if (c.args().size() != 1 || !(c.args().get(0) instanceof UIValue.Str path)) throw fail("loadAtlas expects one string path");
                return UIAtlases.load(path.text(), source.getNamespace());
            }
            return atlasIcon(c.name(), c.args());
        }
        // A tuple/flags DEFAULT STAYS STRUCTURAL; THE CONSUMING PROPERTY DECODES IT IN CONTEXT
        return v;
    }

    private Icon atlasIcon(String handleName, List<UIValue> args) {
        Object o = resolve(handleName);
        if (!(o instanceof UIAtlases.Handle handle)) throw fail("'" + handleName + "' is not an atlas handle");
        if (args.size() != 2 && args.size() != 4) throw fail("an atlas call needs (x, y) or (x, y, w, h)");
        int cx = (int) number(literal(args.get(0)), handleName);
        int cy = (int) number(literal(args.get(1)), handleName);
        int cw = args.size() == 4 ? (int) number(literal(args.get(2)), handleName) : 1;
        int ch = args.size() == 4 ? (int) number(literal(args.get(3)), handleName) : 1;
        return handle.icon(cx, cy, cw, ch);
    }

    // AN ATLAS CALL ARGUMENT IS A NUMBER LITERAL OR A NUMERIC VARIABLE
    private Object literal(UIValue v) {
        if (v instanceof UIValue.Num n) return n.value();
        if (v instanceof UIValue.Id id) return resolve(id.name());
        throw fail("atlas coordinates must be numbers");
    }

    /** Replaces every {@code {name}} bound to a baked variable with its text; unknown names pass through (§5.4). */
    public String interpolate(String raw) {
        if (raw.indexOf('{') < 0) return raw;
        StringBuilder out = new StringBuilder(raw.length());
        int i = 0;
        while (i < raw.length()) {
            char c = raw.charAt(i);
            if (c == '{') {
                int close = raw.indexOf('}', i + 1);
                String name = close >= 0 ? raw.substring(i + 1, close) : null;
                // ONLY A DECLARED BAKED VARIABLE SUBSTITUTES; A LIVE OR UNKNOWN NAME IS LEFT LITERAL
                if (name != null && varName(name) && bakedVar(name)) {
                    out.append(text(resolve(name)));
                    i = close + 1;
                    continue;
                }
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    private static boolean varName(String name) {
        if (name.isEmpty()) return false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') return false;
        }
        return true;
    }

    private double number(Object o, String name) {
        if (o instanceof Number n) return n.doubleValue();
        throw fail("'" + name + "' is not a number");
    }

    private static String text(Object o) {
        if (o instanceof Number n) return UIValues.number(n.doubleValue());
        return String.valueOf(o);
    }

    // HOST-SUPPLIED VALUES ARE CHECKED AGAINST AN EXPLICIT DECLARED TYPE; AN INFERRED TYPE ACCEPTS WHATEVER FITS AT USE
    private void checkType(UIDecl decl, Object value) {
        String type = decl.type();
        if (type == null || value == null) return;
        boolean ok = switch (type) {
            case "boolean" -> value instanceof Boolean;
            case "int", "long", "float", "double" -> value instanceof Number;
            case "String" -> value instanceof CharSequence;
            case "Icon" -> value instanceof Icon;
            case "ItemStack" -> value instanceof ItemStack;
            case "Atlas" -> value instanceof UIAtlases.Handle;
            default -> true;
        };
        if (!ok) throw failAt(decl, "host value for '" + decl.name() + "' is " + value.getClass().getSimpleName() + ", declared type is " + type);
    }

    private UIFormatException fail(String message) {
        return new UIFormatException(source.toString(), -1, message);
    }

    // STAMPS THE DECLARING DOCUMENT (§13.4) SO A VARIABLE ERROR NAMES THE FILE THAT DECLARED IT ACROSS IMPORTS
    private UIFormatException failAt(UIDecl decl, String message) {
        return new UIFormatException((decl != null ? decl.source() : source).toString(), -1, message);
    }

    // EAGER §7.3 VALIDATION: THE CLASS MUST BE REGISTERED, LOADABLE AND AN Element, WHETHER OR NOT THE TAG IS USED
    private static void validateJava(UIImport imp, ResourceLocation docSource) {
        if (UIRegistry.factory(imp.alias()) == null) throw new UIFormatException(docSource.toString(), imp.line(), "Java import '" + imp.ref() + "' is not a registered element");
        Class<?> clazz;
        try {
            clazz = Class.forName(imp.ref(), false, Element.class.getClassLoader());
        } catch (ClassNotFoundException | LinkageError e) {
            throw new UIFormatException(docSource.toString(), imp.line(), "Java import class not found '" + imp.ref() + "'");
        }
        if (!Element.class.isAssignableFrom(clazz)) throw new UIFormatException(docSource.toString(), imp.line(), "Java import '" + imp.ref() + "' is not an Element");
    }
}
