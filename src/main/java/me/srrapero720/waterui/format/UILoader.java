package me.srrapero720.waterui.format;

import me.srrapero720.waterui.WaterUI;
import me.srrapero720.waterui.screen.WaterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Discovers every {@code .ui} layout and {@code .ui.json} theme under {@code assets/<modid>/ui} on
 * each resource reload (UI-SPEC.md §2) and keeps the parsed {@link UIDocument}s for {@code UIBuilder}
 * to inflate. A layout is keyed by its path relative to {@code ui/} minus the {@code .ui} extension
 * ({@code ui/dialogs/search.ui} → {@code <modid>:dialogs/search}), so subfolders are identity; a
 * theme lives under {@code ui/themes} and is keyed by its bare stem.
 */
public class UILoader implements ResourceManagerReloadListener {
    private static final Marker IT = MarkerManager.getMarker(UILoader.class.getSimpleName());
    // CLIENT-THREAD ONLY: RESOURCE RELOADS RUN ON THE RENDER THREAD, NO CONCURRENT ACCESS TO GUARD
    private static final Map<ResourceLocation, UIDocument> DOCUMENTS = new HashMap<>();

    /** Parsed document for {@code <modid>:<path>}, or null when absent or it failed to load. */
    public static UIDocument document(ResourceLocation key) {
        return DOCUMENTS.get(key);
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        UIAtlases.invalidate();
        UIThemes.clear();
        DOCUMENTS.clear();

        int themes = 0;
        for (Map.Entry<ResourceLocation, Resource> entry: manager.listResources("ui/themes", loc -> loc.getPath().endsWith(".ui.json")).entrySet()) {
            ResourceLocation loc = entry.getKey();
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), themeStem(loc.getPath()));
            // RECORD THE FILE EXISTED SO A PARSE FAILURE SURFACES AS Theme.ERROR, NOT THE SILENT DEFAULT (§2, §14.5)
            UIThemes.attempted(id);
            try (Reader reader = entry.getValue().openAsReader()) {
                UIThemes.put(id, UIThemeLoader.load(loc, reader));
                themes++;
                WaterUI.LOGGER.debug(IT, "parsed theme {} from {}", id, loc);
            } catch (Exception e) {
                WaterUI.LOGGER.error(IT, "failed to load theme '{}': {}", loc, e.getMessage());
            }
        }

        int uis = 0;
        for (Map.Entry<ResourceLocation, Resource> entry: manager.listResources("ui", loc -> loc.getPath().endsWith(".ui") && !loc.getPath().contains("/themes/")).entrySet()) {
            ResourceLocation loc = entry.getKey();
            try {
                ResourceLocation source = ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), docKey(loc.getPath()));
                byte[] bytes;
                try (InputStream in = entry.getValue().open()) { bytes = in.readAllBytes(); }
                String text = new String(bytes, StandardCharsets.UTF_8);
                DOCUMENTS.put(source, UIParser.parse(UILexer.tokenize(text, source.toString()), source));
                uis++;
                WaterUI.LOGGER.debug(IT, "parsed document {} from {}", source, loc);
            } catch (Exception e) {
                WaterUI.LOGGER.error(IT, "failed to load ui '{}': {}", loc, e.getMessage());
            }
        }

        WaterUI.LOGGER.info(IT, "loaded {} ui document(s), {} theme(s)", uis, themes);

        // F3+T REBUILDS AN OPEN SCREEN THROUGH ITS theme() SETTER; A THROWING build() MUST NOT FAIL THE WHOLE RELOAD
        if (Minecraft.getInstance().screen instanceof WaterScreen w) {
            WaterUI.LOGGER.debug(IT, "reload rebuilding open screen {}", w.getClass().getSimpleName());
            try {
                w.theme(w.theme);
            } catch (Exception e) {
                WaterUI.LOGGER.error(IT, "failed to rebuild the open screen after reload: {}", e.getMessage());
            }
        }
    }

    // "ui/dialogs/search.ui" -> "dialogs/search"; SUBFOLDERS ARE IDENTITY, NO DOUBLE-SUFFIX STRIPPING (§2)
    private static String docKey(String path) {
        String stripped = path.substring("ui/".length());
        return stripped.substring(0, stripped.length() - ".ui".length());
    }

    // "ui/themes/waterui.ui.json" -> "waterui" (BARE STEM, KEYED UNDER THE FILE'S NAMESPACE)
    private static String themeStem(String path) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        return name.substring(0, name.length() - ".ui.json".length());
    }
}
