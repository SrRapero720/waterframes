package me.srrapero720.waterui.format;

import me.srrapero720.waterui.WaterUI;
import me.srrapero720.waterui.theme.Theme;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Registry of loaded {@link Theme}s keyed by resource id, plus the §2 assignment fallback chain. */
public final class UIThemes {
    private static final Marker IT = MarkerManager.getMarker(UIThemes.class.getSimpleName());
    private static final Map<ResourceLocation, Theme> THEMES = new HashMap<>();
    // THEME FILES THAT WERE DISCOVERED (PARSED OR NOT): TELLS A BROKEN PAIR FROM "NO THEME AT ALL"
    private static final Set<ResourceLocation> ATTEMPTED = new HashSet<>();
    // DEDUPES THE MISSING-THEME ERROR SO A BROKEN REFERENCE LOGS ONCE, NOT EVERY FRAME
    private static final Set<ResourceLocation> MISSING_LOGGED = new HashSet<>();

    private UIThemes() {}

    /** Records that a theme file for {@code id} was found, even if it later failed to parse (§2). */
    public static void attempted(ResourceLocation id) {
        ATTEMPTED.add(id);
    }

    public static void put(ResourceLocation id, Theme theme) {
        THEMES.put(id, theme);
    }

    public static void clear() {
        THEMES.clear();
        ATTEMPTED.clear();
        MISSING_LOGGED.clear();
    }

    /**
     * Resolves a document's theme (§2): a declared {@code theme "ref"} wins, else a theme whose stem
     * matches the document's own stem, else the silent {@link Theme#WATERUI} default. {@link Theme#ERROR}
     * is reserved for a theme that was declared or paired but is missing or failed to parse — that case
     * logs; the silent default never does (§14.5).
     */
    public static Theme resolve(ResourceLocation declared, ResourceLocation docSource) {
        if (declared != null) {
            Theme theme = THEMES.get(declared);
            if (theme != null) {
                WaterUI.LOGGER.debug(IT, "theme resolved: declared '{}'", declared);
                return theme;
            }
            if (MISSING_LOGGED.add(declared)) WaterUI.LOGGER.error(IT, "theme '{}' is declared but missing or broken, using Theme.ERROR", declared);
            return Theme.ERROR;
        }
        ResourceLocation pair = ResourceLocation.fromNamespaceAndPath(docSource.getNamespace(), stem(docSource.getPath()));
        Theme paired = THEMES.get(pair);
        if (paired != null) {
            WaterUI.LOGGER.debug(IT, "theme resolved: paired '{}'", pair);
            return paired;
        }
        if (ATTEMPTED.contains(pair)) {
            if (MISSING_LOGGED.add(pair)) WaterUI.LOGGER.error(IT, "paired theme '{}' failed to parse, using Theme.ERROR", pair);
            return Theme.ERROR;
        }
        WaterUI.LOGGER.debug(IT, "theme resolved: default WATERUI for {}", docSource);
        return Theme.WATERUI;
    }

    // THE DOCUMENT KEY ALREADY DROPPED THE ui/ PREFIX AND THE EXTENSION, SO ITS STEM IS THE LAST PATH SEGMENT
    private static String stem(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }
}
