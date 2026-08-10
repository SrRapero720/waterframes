package me.srrapero720.waterui.format;

import me.srrapero720.waterui.WaterUI;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.lang.annotation.ElementType;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Static index of {@link UIVar} / {@link UIEvent} handler methods, discovered once by scanning
 * every mod file's {@link ModFileScanData}. Lookups walk a host's class and its superclasses, so
 * a handler declared on a base screen is found for any subclass instance. Methods are resolved
 * reflectively and cached the first time a concrete class is queried.
 */
public final class UIBindings {
    private static final Marker IT = MarkerManager.getMarker(UIBindings.class.getSimpleName());

    private UIBindings() {}

    // SCANNED: DECLARING CLASS NAME -> (ANNOTATION VALUE -> METHOD MEMBER "name+descriptor")
    private static final Map<String, Map<String, String>> RAW_VAR = new HashMap<>();
    private static final Map<String, Map<String, String>> RAW_EVENT = new HashMap<>();

    // RESOLVED CACHE PER CONCRETE CLASS: ANNOTATION VALUE -> Method, BUILT LAZILY ON FIRST LOOKUP
    private static final Map<Class<?>, Map<String, Method>> VAR_CACHE = new HashMap<>();
    private static final Map<Class<?>, Map<String, Method>> EVENT_CACHE = new HashMap<>();

    private static volatile boolean scanned;

    /** Warms the annotation scan up front; safe to call more than once. */
    public static void bootstrap() {
        if (!scanned) scan();
    }

    /** The {@link UIVar} method for {@code name} on {@code host} or a superclass; null when none. */
    public static Method var(Class<?> host, String name) {
        return lookup(RAW_VAR, VAR_CACHE, host, name);
    }

    /** The {@link UIEvent} method for {@code name} on {@code host} or a superclass; null when none. */
    public static Method event(Class<?> host, String name) {
        return lookup(RAW_EVENT, EVENT_CACHE, host, name);
    }

    private static Method lookup(Map<String, Map<String, String>> raw, Map<Class<?>, Map<String, Method>> cache, Class<?> host, String name) {
        if (!scanned) scan();
        // A HANDLER ON A BASE SCREEN MUST RESOLVE FOR A SUBCLASS INSTANCE, SO WALK UP TO Object
        for (Class<?> c = host; c != null && c != Object.class; c = c.getSuperclass()) {
            Method m = resolved(raw, cache, c).get(name);
            if (m != null) return m;
        }
        return null;
    }

    // RESOLVES EVERY SCANNED MEMBER DECLARED ON EXACTLY THIS CLASS BY MATCHING name+descriptor, THEN CACHES IT
    private static synchronized Map<String, Method> resolved(Map<String, Map<String, String>> raw, Map<Class<?>, Map<String, Method>> cache, Class<?> clazz) {
        Map<String, Method> hit = cache.get(clazz);
        if (hit != null) return hit;

        Map<String, String> members = raw.get(clazz.getName());
        Map<String, Method> out = new HashMap<>();
        if (members != null && !members.isEmpty()) {
            Map<String, Method> bySig = new HashMap<>();
            for (Method m: clazz.getDeclaredMethods()) {
                bySig.put(m.getName() + MethodType.methodType(m.getReturnType(), m.getParameterTypes()).toMethodDescriptorString(), m);
            }
            for (Map.Entry<String, String> e: members.entrySet()) {
                Method m = bySig.get(e.getValue());
                if (m == null) {
                    WaterUI.LOGGER.error(IT, "handler '{}' -> no method '{}' on {}", e.getKey(), e.getValue(), clazz.getName());
                    continue;
                }
                m.setAccessible(true);
                out.put(e.getKey(), m);
            }
        }
        cache.put(clazz, out);
        return out;
    }

    // ONE-TIME PASS OVER EVERY MOD FILE'S ASM SCAN DATA, FILTERED TO OUR TWO METHOD ANNOTATIONS (§5.6, §12.2)
    private static synchronized void scan() {
        if (scanned) return;
        String varType = UIVar.class.getName();
        String eventType = UIEvent.class.getName();
        for (IModFileInfo info: ModList.get().getModFiles()) {
            ModFileScanData data;
            try {
                data = info.getFile().getScanResult();
            } catch (Throwable t) {
                continue; // SOME SYNTHETIC MOD FILES CARRY NO SCAN DATA
            }
            if (data == null) continue;
            for (ModFileScanData.AnnotationData ad: data.getAnnotations()) {
                if (ad.targetType() != ElementType.METHOD) continue;
                String type = ad.annotationType().getClassName();
                Map<String, Map<String, String>> raw = varType.equals(type) ? RAW_VAR : eventType.equals(type) ? RAW_EVENT : null;
                if (raw == null) continue;
                Object value = ad.annotationData().get("value");
                if (!(value instanceof String key)) continue;
                // memberName IS "name+descriptor" (e.g. loop()Z), MATCHED AGAINST A REFLECTED METHOD LATER
                raw.computeIfAbsent(ad.clazz().getClassName(), k -> new HashMap<>()).put(key, ad.memberName());
            }
        }
        WaterUI.LOGGER.debug(IT, "scanned {} @UIVar and {} @UIEvent host class(es)", RAW_VAR.size(), RAW_EVENT.size());
        scanned = true;
    }
}
