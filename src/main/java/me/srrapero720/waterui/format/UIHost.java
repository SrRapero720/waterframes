package me.srrapero720.waterui.format;

import me.srrapero720.waterui.core.Element;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static me.srrapero720.waterui.WaterUI.LOGGER;

/**
 * A host carrying programmatic registrations in three registries (UI-SPEC.md §5.6, §12.2): DYNAMIC
 * suppliers for live bindings, INFLATABLE constants for the contract's baked map, and events —
 * named handlers plus element callbacks bound by id after the inflate. Screens and dialogs
 * implement it.
 */
public interface UIHost {

    /** The host's registration store; never null. */
    Store uiStore();

    /** Registers an inflatable constant fed to the contract on every inflate; register in the constructor (§5.6). */
    default void registerVar(String key, boolean value) {
        this.uiStore().inflatable(key, value);
    }

    /** Registers an inflatable numeric constant; {@code int} widens here (§5.6). */
    default void registerVar(String key, long value) {
        this.uiStore().inflatable(key, value);
    }

    /** Registers an inflatable numeric constant; {@code float} widens here (§5.6). */
    default void registerVar(String key, double value) {
        this.uiStore().inflatable(key, value);
    }

    /** Registers an inflatable String/Icon/ItemStack constant (§5.6). */
    default void registerVar(String key, Object value) {
        this.uiStore().inflatable(key, value);
    }

    /** Registers a dynamic variable read live like a {@code @UIVar} method; a {@code final var} snapshots it per inflate (§5.6). */
    default void registerVar(String key, Supplier<?> value) {
        this.uiStore().dynamic(key, value);
    }

    /** Registers a named handler for the document's {@code on<Event>=key(...)} calls, receiving id + baked args + payload (§12.2). */
    default void registerUIEvent(String key, UIEvents.Handler handler) {
        this.uiStore().event(key, handler);
    }

    /** Registers a payload-blind named handler (§12.2). */
    default void registerUIEvent(String key, Runnable handler) {
        this.uiStore().event(key, payload -> handler.run());
    }

    /** Binds {@code callback} straight to the element's click by id; the namespace is the inflated document's (§12.2). */
    default void registerEvent(String elementId, Consumer<Element> callback) {
        this.uiStore().element(elementId, callback);
    }

    /** Same as {@link #registerEvent(String, Consumer)} with an explicit {@code ns:path} id, for foreign elements. */
    default void registerEvent(ResourceLocation elementId, Consumer<Element> callback) {
        this.uiStore().element(elementId.toString(), callback);
    }

    /**
     * The three registries of one host, filled in its constructor and living as long as the instance:
     * suppliers must capture the host or its domain, never tree elements, since the tree rebuilds under
     * them. The map accessors expose live internals for the owning inflate — never mutate them outside.
     */
    final class Store {
        private static final Marker IT = MarkerManager.getMarker(Store.class.getSimpleName());

        private final Map<String, Supplier<?>> dynamic = new HashMap<>();
        private final Map<String, Object> inflatable = new HashMap<>();
        private final Map<String, UIEvents.Handler> events = new HashMap<>();
        // KEYED BY THE RAW REGISTRATION: "path" DEFAULTS TO THE INFLATED DOCUMENT'S NAMESPACE, "ns:path" IS EXPLICIT
        private final Map<String, Consumer<Element>> elements = new HashMap<>();
        // LATCHED BY THE OWNING inflate: A BIND-TIME REGISTRY WRITTEN AFTER IT CAN NO LONGER BIND (§5.6)
        private boolean inflated;

        public void dynamic(String key, Supplier<?> value) {
            if (inflated) LOGGER.warn(IT, "dynamic var '{}' registered after the inflate; it binds nothing until a re-init", key);
            this.dynamic.put(key, value);
        }

        public Supplier<?> dynamic(String name) {
            return dynamic.get(name);
        }

        public void inflatable(String key, Object value) {
            if (inflated) LOGGER.warn(IT, "inflatable var '{}' registered after the inflate; it feeds nothing until a re-init", key);
            this.inflatable.put(key, value);
        }

        /**
         * Baked view for one inflate (§5.6): a fresh snapshot of every dynamic supplier, overlaid by
         * the inflatable constants — so a {@code final var} re-reads mutable state on every re-inflate.
         */
        public Map<String, Object> baked() {
            Map<String, Object> view = new HashMap<>();
            for (Map.Entry<String, Supplier<?>> entry: dynamic.entrySet()) {
                try {
                    Object value = entry.getValue().get();
                    if (value != null) view.put(entry.getKey(), value);
                } catch (Throwable ignored) {
                    // A SUPPLIER NOT READY AT INFLATE SIMPLY CONTRIBUTES NO BAKED VALUE
                }
            }
            view.putAll(inflatable);
            return view;
        }

        public void event(String key, UIEvents.Handler handler) {
            this.events.put(key, handler);
        }

        public UIEvents.Handler event(String name) {
            return events.get(name);
        }

        public void element(String key, Consumer<Element> callback) {
            if (inflated) LOGGER.warn(IT, "element event '{}' registered after the inflate; it wires nothing until a re-init", key);
            this.elements.put(key, callback);
        }

        /** The element-event registrations the internal inflate wires by id after the tree builds (§12.2). */
        public Map<String, Consumer<Element>> elements() {
            return elements;
        }

        /** Marks the bind window closed; the owning inflate calls it so late writes warn instead of vanishing. */
        public void inflated() {
            this.inflated = true;
        }
    }
}
