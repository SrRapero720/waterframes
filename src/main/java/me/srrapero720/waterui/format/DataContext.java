package me.srrapero720.waterui.format;

import me.srrapero720.waterui.WaterUI;
import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.theme.Icon;
import me.srrapero720.waterui.widget.Text;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Per-screen live-binding context. Resolves a host's registered {@link UIHost} entries and
 * {@link UIVar} methods to typed suppliers the widgets read, and holds the TICKED evaluators the
 * screen drains at 20 Hz. Reset before every re-inflate so a stale evaluator never runs against a
 * detached element.
 * <p>
 * Two binding modes: DIRECT suppliers (boolean/long) are handed straight to the widget setter and
 * read each frame; TICKED bindings (tooltip lists, enabled/visible fields) are evaluated on tick
 * and cached, since they allocate or mutate layout.
 */
public final class DataContext {
    private static final Marker IT = MarkerManager.getMarker(DataContext.class.getSimpleName());
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private Object host;
    private Class<?> hostClass;
    private final List<Runnable> ticked = new ArrayList<>();

    /** Binds the {@link UIVar} lookups to a new host; already-attached bindings keep their captured suppliers. */
    public void host(Object host) {
        if (this.host == host) return;
        this.host = host;
        this.hostClass = host != null ? host.getClass() : null;
    }

    /** Drops the host and every TICKED evaluator; called before an inflate rebuilds the tree. */
    public void reset() {
        this.host = null;
        this.hostClass = null;
        this.ticked.clear();
    }

    public void ticked(Runnable evaluator) {
        this.ticked.add(evaluator);
    }

    /** Runs every TICKED evaluator once; the screen calls this each tick. */
    public void tick() {
        for (int i = 0; i < ticked.size(); i++) ticked.get(i).run();
    }

    // ---- DIRECT SUPPLIERS (READ EVERY FRAME) -------------------------------------------

    /** Live boolean for {@code name}, or a constant {@code false} when missing/mismatched. */
    public BooleanSupplier boolOr(String name) {
        Supplier<?> reg = registered(name);
        if (reg != null) {
            probe(name, reg, Boolean.class);
            return () -> { try { return reg.get() instanceof Boolean b && b; } catch (Throwable t) { return false; } };
        }
        Method m = resolve(name);
        if (m == null) return () -> false;
        if (m.getReturnType() != boolean.class) return mismatch(name, m, "boolean", () -> false);
        MethodHandle h = handle(m);
        if (h == null) return () -> false;
        return () -> { try { return (boolean) h.invokeExact(); } catch (Throwable t) { return false; } };
    }

    /** Live long for {@code name} ({@code int} widened), or a constant {@code 0} when missing/mismatched. */
    public LongSupplier numberOr(String name) {
        Supplier<?> reg = registered(name);
        if (reg != null) {
            probe(name, reg, Number.class);
            return () -> { try { return reg.get() instanceof Number n ? n.longValue() : 0L; } catch (Throwable t) { return 0L; } };
        }
        Method m = resolve(name);
        if (m == null) return () -> 0L;
        Class<?> ret = m.getReturnType();
        if (ret != long.class && ret != int.class) return mismatch(name, m, "long/int", () -> 0L);
        MethodHandle h = handle(m);
        if (h == null) return () -> 0L;
        if (ret == long.class) return () -> { try { return (long) h.invokeExact(); } catch (Throwable t) { return 0L; } };
        return () -> { try { return (int) h.invokeExact(); } catch (Throwable t) { return 0L; } };
    }

    /** Live double for {@code name} ({@code float} widened), or a constant {@code 0} when missing/mismatched. */
    public DoubleSupplier doubleOr(String name) {
        Supplier<?> reg = registered(name);
        if (reg != null) {
            probe(name, reg, Number.class);
            return () -> { try { return reg.get() instanceof Number n ? n.doubleValue() : 0d; } catch (Throwable t) { return 0d; } };
        }
        Method m = resolve(name);
        if (m == null) return () -> 0d;
        Class<?> ret = m.getReturnType();
        if (ret != double.class && ret != float.class) return mismatch(name, m, "double/float", () -> 0d);
        MethodHandle h = handle(m);
        if (h == null) return () -> 0d;
        if (ret == double.class) return () -> { try { return (double) h.invokeExact(); } catch (Throwable t) { return 0d; } };
        return () -> { try { return (float) h.invokeExact(); } catch (Throwable t) { return 0d; } };
    }

    /** Live String for {@code name}, or a constant {@code ""} when missing/mismatched. */
    public Supplier<String> stringOr(String name) {
        Supplier<?> reg = registered(name);
        if (reg != null) {
            probe(name, reg, CharSequence.class);
            return () -> { try { Object o = reg.get(); return o instanceof CharSequence c ? c.toString() : ""; } catch (Throwable t) { return ""; } };
        }
        Method m = resolve(name);
        if (m == null) return () -> "";
        if (!CharSequence.class.isAssignableFrom(m.getReturnType())) return mismatch(name, m, "String", () -> "");
        MethodHandle h = handle(m);
        if (h == null) return () -> "";
        return () -> { try { Object o = h.invoke(); return o != null ? o.toString() : ""; } catch (Throwable t) { return ""; } };
    }

    /** Live {@link ItemStack} for {@code name}, or {@link ItemStack#EMPTY} when missing/mismatched. */
    public Supplier<ItemStack> itemOr(String name) {
        Supplier<?> reg = registered(name);
        if (reg != null) {
            probe(name, reg, ItemStack.class);
            return () -> { try { return reg.get() instanceof ItemStack stack ? stack : ItemStack.EMPTY; } catch (Throwable t) { return ItemStack.EMPTY; } };
        }
        Method m = resolve(name);
        if (m == null) return () -> ItemStack.EMPTY;
        if (!ItemStack.class.isAssignableFrom(m.getReturnType())) return mismatch(name, m, "ItemStack", () -> ItemStack.EMPTY);
        MethodHandle h = handle(m);
        if (h == null) return () -> ItemStack.EMPTY;
        return () -> { try { return (ItemStack) h.invoke(); } catch (Throwable t) { return ItemStack.EMPTY; } };
    }

    /** Live {@link Icon} for {@code name}, or a constant {@code null} when missing/mismatched. */
    public Supplier<Icon> iconOr(String name) {
        Supplier<?> reg = registered(name);
        if (reg != null) {
            probe(name, reg, Icon.class);
            return () -> { try { return reg.get() instanceof Icon icon ? icon : null; } catch (Throwable t) { return null; } };
        }
        Method m = resolve(name);
        if (m == null) return () -> null;
        if (!Icon.class.isAssignableFrom(m.getReturnType())) return mismatch(name, m, "Icon", () -> null);
        MethodHandle h = handle(m);
        if (h == null) return () -> null;
        return () -> { try { return (Icon) h.invoke(); } catch (Throwable t) { return null; } };
    }

    /** Live tooltip: read for the single hovered element only, so the supplier goes straight in. */
    public void tooltip(Element e, String name) {
        e.tooltip(listOr(name));
    }

    // ---- TICKED BINDINGS (EVALUATED AT 20 Hz, CACHED) ----------------------------------
    // EACH EVALUATOR RUNS ONCE AT REGISTRATION SO THE FIRST FRAME PAINTS THE REAL VALUE, NOT A DEFAULT

    /** Live enabled: writes the field each tick, gated so a redundant write is skipped like visible. */
    public void enabled(Element e, String name) {
        BooleanSupplier src = boolOr(name);
        Runnable eval = () -> { boolean v = src.getAsBoolean(); if (e.enabled != v) e.enabled = v; };
        eval.run();
        this.ticked(eval);
    }

    /** Live visible: toggled only on a real change, since {@code visible(bool)} triggers a reflow. */
    public void visible(Element e, String name) {
        BooleanSupplier src = boolOr(name);
        Runnable eval = () -> { boolean v = src.getAsBoolean(); if (e.visible != v) e.visible(v); };
        eval.run();
        this.ticked(eval);
    }

    /** Live text component: the string is wrapped only on change, so no {@link Component} is allocated per idle tick. */
    public void component(Element e, String name) {
        Supplier<String> src = stringOr(name);
        Text text = (Text) e;
        // RAW-STRING GUARD: Text.text ALREADY SKIPS EQUAL COMPONENTS, THIS AVOIDS THE PER-TICK Component ALLOCATION
        String[] shown = { null };
        Runnable eval = () -> {
            String s = src.get();
            if (!s.equals(shown[0])) {
                shown[0] = s;
                text.component(Component.literal(s));
            }
        };
        eval.run();
        this.ticked(eval);
    }

    // ---- RESOLUTION --------------------------------------------------------------------

    private Supplier<List<Component>> listOr(String name) {
        Supplier<?> reg = registered(name);
        if (reg != null) {
            probe(name, reg, List.class);
            // THE ELEMENT TYPE IS DOUBLY ERASED: PEEK THE FIRST ENTRY SO A List<String> SURFACES AT BIND
            try {
                if (reg.get() instanceof List<?> l && !l.isEmpty() && !(l.get(0) instanceof Component))
                    WaterUI.LOGGER.error(IT, "registered var '{}' supplies a List of {}, expected Component", name, l.get(0).getClass().getSimpleName());
            } catch (Throwable ignored) {}
            return () -> {
                try {
                    Object o = reg.get();
                    if (o instanceof List<?> l) {
                        @SuppressWarnings("unchecked")
                        List<Component> list = (List<Component>) l;
                        return list;
                    }
                    return List.of();
                } catch (Throwable t) {
                    return List.of();
                }
            };
        }
        Method m = resolve(name);
        if (m == null) return List::of;
        if (!List.class.isAssignableFrom(m.getReturnType())) return mismatch(name, m, "List<Component>", List::of);
        MethodHandle h = handle(m);
        if (h == null) return List::of;
        return () -> {
            try {
                @SuppressWarnings("unchecked")
                List<Component> value = (List<Component>) h.invoke();
                return value != null ? value : List.of();
            } catch (Throwable t) {
                return List.of();
            }
        };
    }

    /**
     * Asserts a live provider named {@code name} exists on the host with a return type among
     * {@code accepted}; a missing or mismatched provider is a load error (§5.2, §13.4). The suppliers
     * still fall back safely, but the builder calls this first so the failure surfaces as a screen.
     */
    public void require(String name, Class<?>... accepted) {
        if (hostClass == null)
            throw new UIFormatException(null, -1, "live variable '" + name + "' has no host to resolve against");
        // A DYNAMIC REGISTRATION SATISFIES THE CONTRACT (§5.6); ITS TYPE IS ERASED, THE BIND PROBE COVERS IT
        if (stored(name) != null) return;
        Method m = UIBindings.var(hostClass, name);
        if (m == null) throw new UIFormatException(null, -1, "no @UIVar or registered var '" + name + "' on " + hostClass.getName());
        Class<?> ret = m.getReturnType();
        for (Class<?> t: accepted) if (t == ret || t.isAssignableFrom(ret)) return;
        throw new UIFormatException(null, -1, "@UIVar '" + name + "' returns " + ret.getSimpleName() + ", incompatible with this property");
    }

    // RAW DYNAMIC-REGISTRY LOOKUP (§5.6); NO LOGGING, SHARED BY require() AND THE BIND PATH
    private Supplier<?> stored(String name) {
        return name != null && host instanceof UIHost provider ? provider.uiStore().dynamic(name) : null;
    }

    // BIND-TIME LOOKUP: A REGISTRATION OUTRANKS THE @UIVar, SO SHADOWING ONE IS WORTH A WARN
    private Supplier<?> registered(String name) {
        Supplier<?> found = stored(name);
        if (found == null) return null;
        Method annotated = hostClass != null ? UIBindings.var(hostClass, name) : null;
        if (annotated != null) WaterUI.LOGGER.warn(IT, "registered var '{}' shadows @UIVar on {}", name, annotated.getDeclaringClass().getSimpleName());
        return found;
    }

    // A BIND-TIME PROBE (ONE PER BINDING SITE) SO A WRONGLY-TYPED REGISTERED SUPPLIER SURFACES NOW,
    // NOT AS SILENT RUNTIME FALLBACKS; A SUPPLIER NOT READY AT INFLATE ONLY LOGS AT DEBUG
    private static void probe(String name, Supplier<?> s, Class<?> expected) {
        try {
            Object o = s.get();
            if (o != null && !expected.isInstance(o))
                WaterUI.LOGGER.error(IT, "registered var '{}' supplies {}, expected {}", name, o.getClass().getSimpleName(), expected.getSimpleName());
        } catch (Throwable t) {
            WaterUI.LOGGER.debug(IT, "registered var '{}' probe threw: {}", name, t.toString());
        }
    }

    // A MISSING @UIVar IS AN AUTHORING ERROR, NOT A CRASH: LOG ONCE AT BIND TIME AND FALL BACK TO A SAFE DEFAULT
    private Method resolve(String name) {
        if (name == null || hostClass == null) {
            WaterUI.LOGGER.error(IT, "live binding '{}' has no host to resolve against", name);
            return null;
        }
        Method found = UIBindings.var(hostClass, name);
        if (found == null) {
            WaterUI.LOGGER.error(IT, "no @UIVar or registered var '{}' on {}", name, hostClass.getName());
            return null;
        }
        WaterUI.LOGGER.debug(IT, "bound live var '{}' -> {} on {}", name, found.getName(), hostClass.getSimpleName());
        return found;
    }

    private <T> T mismatch(String name, Method m, String expected, T fallback) {
        WaterUI.LOGGER.error(IT, "@UIVar '{}' on {} returns {}, expected {}", name, hostClass.getName(), m.getReturnType().getSimpleName(), expected);
        return fallback;
    }

    private MethodHandle handle(Method m) {
        try {
            return LOOKUP.unreflect(m).bindTo(host);
        } catch (IllegalAccessException e) {
            WaterUI.LOGGER.error(IT, "@UIVar '{}' on {} is not accessible", m.getName(), hostClass.getName(), e);
            return null;
        }
    }

}
