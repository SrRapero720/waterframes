package me.srrapero720.waterui.format;

import me.srrapero720.waterui.WaterUI;
import me.srrapero720.waterui.core.AbstractParent;
import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.layout.ParentLinear;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Resolves an {@code on<Event>} handler (UI-SPEC.md §12) against the host by reflection, once at
 * build time. The builder passes the method name and the already-baked argument array (the element
 * id first, then the call arguments as strings); the returned {@link Handler} appends the firing
 * widget's runtime payload.
 */
public final class UIEvents {
    private static final Marker IT = MarkerManager.getMarker(UIEvents.class.getSimpleName());

    private UIEvents() {}

    /**
     * Baked trigger. The element id and the call arguments are already bound; {@code payload} is
     * whatever the firing widget carries at that moment, appended after them. A handler declaring
     * fewer parameters simply never sees the tail.
     */
    public interface Handler { void invoke(String... payload); }

    private static final Handler NOOP = payload -> {};

    /**
     * Resolves a public method {@code name} on {@code host}'s class taking Strings and returns a
     * {@link Handler} invoking it with {@code baked} then the firing payload. A missing handler logs
     * ERROR and returns a no-op — the screen still opens (§12.2, §13.4).
     */
    public static Handler resolve(Object host, String name, String[] baked, ResourceLocation file, int line) {
        // A REGISTERED HANDLER (§12.2) OUTRANKS REFLECTION AND SEES id + BAKED + PAYLOAD LIKE AN ALL-VARARGS
        // METHOD. LOOKED UP AT INVOKE TIME: A RE-ADOPTED TEMPLATED ROW FIRES THE CURRENT REGISTRATION,
        // NEVER A PRE-CLEAR CAPTURE, AND THE TRY/CATCH MATCHES THE REFLECTIVE PATH'S BELT
        if (host instanceof UIHost provider && provider.uiStore().event(name) != null) {
            if (UIBindings.event(host.getClass(), name) != null)
                WaterUI.LOGGER.warn(IT, "registered event '{}' shadows @UIEvent on {}", name, host.getClass().getSimpleName());
            WaterUI.LOGGER.debug(IT, "resolved handler '{}' -> registered on {}", name, host.getClass().getSimpleName());
            return payload -> {
                Handler direct = provider.uiStore().event(name);
                if (direct == null) {
                    WaterUI.LOGGER.error(IT, "registered handler '{}' on {} is gone", name, host.getClass().getSimpleName());
                    return;
                }
                try {
                    direct.invoke(concat(baked, payload));
                } catch (Throwable t) {
                    WaterUI.LOGGER.error(IT, "registered handler '{}' on {} threw", name, host.getClass().getName(), t);
                }
            };
        }
        Class<?> hostClass = host.getClass();
        Method method = findMethod(hostClass, name, baked.length);
        if (method == null) {
            WaterUI.LOGGER.error(IT, "{}:{} no handler '{}' taking {}+ String args on {}", file, line, name, baked.length, hostClass.getName());
            return NOOP;
        }
        method.setAccessible(true);
        WaterUI.LOGGER.debug(IT, "resolved handler '{}' -> {} on {}", name, method.getName(), hostClass.getSimpleName());
        return payload -> invoke(method, host, baked, payload, hostClass, name);
    }

    // RESOLUTION ORDER (§12.2): @UIEvent WINS AND ADAPTS TO ANY ALL-String SHAPE; THEN THE EXACT BAKED
    // ARITY; THEN THE CLOSEST WIDER ARITY OR A VARARGS TAIL, WHICH IS HOW A HANDLER OPTS INTO THE PAYLOAD
    private static Method findMethod(Class<?> host, String name, int arity) {
        Method annotated = UIBindings.event(host, name);
        if (annotated != null) return annotated;

        Class<?>[] exact = new Class<?>[arity];
        Arrays.fill(exact, String.class);
        try {
            Method m = host.getMethod(name, exact);
            if (reachable(m)) return m;
        } catch (NoSuchMethodException ignored) {}

        Method best = null;
        for (Method candidate: host.getMethods()) {
            if (!candidate.getName().equals(name) || !reachable(candidate)) continue;
            Class<?>[] params = candidate.getParameterTypes();
            if (candidate.isVarArgs()) {
                int fixed = params.length - 1;
                if (fixed > arity || params[fixed].getComponentType() != String.class) continue;
                if (!allExactlyString(params, fixed)) continue;
                if (best == null) best = candidate;
            } else if (params.length > arity && allExactlyString(params, params.length)) {
                if (best == null || best.isVarArgs() || params.length < best.getParameterCount()) best = candidate;
            }
        }
        return best;
    }

    // SECURITY (§12.2): ELEMENT-BASE DECLARATIONS (tooltip/component AND KIN) ARE NEVER PACK-REACHABLE
    // BY NAME; SCREEN-LAYER API LIKE Dialog.close STAYS BECAUSE ITS OWN CLASS DECLARES IT
    private static boolean reachable(Method m) {
        Class<?> owner = m.getDeclaringClass();
        return owner != Element.class && owner != AbstractParent.class && owner != ParentLinear.class;
    }

    // SECURITY (§12.2, L1): ONLY EXACT String PARAMETERS ARE REACHABLE FROM A RESOURCE-PACK .ui,
    // NEVER A PUBLIC METHOD TYPED Object/CharSequence/Comparable THAT String HAPPENS TO SATISFY
    private static boolean allExactlyString(Class<?>[] params, int count) {
        for (int i = 0; i < count; i++) if (params[i] != String.class) return false;
        return true;
    }

    // BAKED PLUS PAYLOAD IN ONE ARRAY; THE BAKED ARRAY ITSELF WHEN THERE IS NO PAYLOAD
    private static String[] concat(String[] baked, String[] payload) {
        if (payload == null || payload.length == 0) return baked;
        String[] full = Arrays.copyOf(baked, baked.length + payload.length);
        System.arraycopy(payload, 0, full, baked.length, payload.length);
        return full;
    }

    // THE BAKED ARGS PLUS THE PAYLOAD ADAPT TO THE METHOD'S SHAPE: A VARARGS TAIL TAKES THE REST PACKED,
    // A NARROWER FIXED ARITY DROPS THE TAIL, A WIDER ONE IS AN AUTHORING ERROR
    private static void invoke(Method method, Object host, String[] baked, String[] payload, Class<?> hostClass, String name) {
        String[] full = concat(baked, payload);
        try {
            if (method.isVarArgs()) {
                int fixed = method.getParameterCount() - 1;
                if (fixed > full.length) {
                    WaterUI.LOGGER.error(IT, "handler '{}' on {} wants {} fixed args, event carries {}", name, hostClass.getName(), fixed, full.length);
                    return;
                }
                Object[] packed = new Object[fixed + 1];
                System.arraycopy(full, 0, packed, 0, fixed);
                String[] rest = new String[full.length - fixed];
                System.arraycopy(full, fixed, rest, 0, rest.length);
                packed[fixed] = rest;
                method.invoke(host, packed);
            } else {
                int params = method.getParameterCount();
                if (params > full.length) {
                    WaterUI.LOGGER.error(IT, "handler '{}' on {} wants {} args, event carries {}", name, hostClass.getName(), params, full.length);
                    return;
                }
                method.invoke(host, (Object[]) (params == full.length ? full : Arrays.copyOf(full, params)));
            }
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            WaterUI.LOGGER.error(IT, "handler '{}' on {} threw", name, hostClass.getName(), e);
        }
    }
}
