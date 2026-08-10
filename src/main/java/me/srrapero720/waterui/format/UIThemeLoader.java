package me.srrapero720.waterui.format;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.srrapero720.waterui.WaterUI;
import me.srrapero720.waterui.core.Spacing;
import me.srrapero720.waterui.theme.Color;
import me.srrapero720.waterui.theme.Drawable;
import me.srrapero720.waterui.theme.ElementTheme;
import me.srrapero720.waterui.theme.ProgressTheme;
import me.srrapero720.waterui.theme.ScrollTheme;
import me.srrapero720.waterui.theme.SliderTheme;
import me.srrapero720.waterui.theme.Sprite;
import me.srrapero720.waterui.theme.SwitchTheme;
import me.srrapero720.waterui.theme.Theme;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parses a {@code .ui.json} theme file into a {@link Theme} (UI-SPEC.md §14). A <b>Drawable</b> is an
 * object with exactly one discriminator key (first-defined wins, §4): {@code "color"} (hex string),
 * {@code "icon"} ({@code "<atlas>, <cx>, <cy>[, w, h]"} in the §5.7 extensionless atlas form) or
 * {@code "sprite"} (nine-slice GUI sprite path). Absent keys default silently by design (§14.5); only
 * an unconditionally-dereferenced slot, a whole missing role/sub-theme block, and an unknown (typo'd)
 * key warn.
 */
public final class UIThemeLoader {
    private static final Marker IT = MarkerManager.getMarker(UIThemeLoader.class.getSimpleName());

    private UIThemeLoader() {}

    // KNOWN SLOTS PER OBJECT: A PRESENT KEY OUTSIDE ITS SET IS A TYPO AND WARNS (§14.5)
    private static final Set<String> ROOT_KEYS = Set.of("text", "textDisabled", "accent", "accentBorder", "disabledOverlay",
            "field", "fieldHover", "selection", "danger", "dangerBorder", "panel", "clickable", "nested", "bar",
            "toggle", "slider", "progress", "scroll");
    private static final Set<String> ROLE_KEYS = Set.of("border", "padding", "margin", "shadow", "outline", "face", "hover");
    private static final Set<String> TOGGLE_KEYS = Set.of("trackWidth", "trackHeight", "border", "gap", "slideMs",
            "thumbWidth", "thumbHeight", "thumbMargin", "outline", "off", "on", "thumb", "thumbHover");
    private static final Set<String> SLIDER_KEYS = Set.of("height", "knobWidth", "track", "knob", "knobHover");
    private static final Set<String> PROGRESS_KEYS = Set.of("height", "fill");
    private static final Set<String> SCROLL_KEYS = Set.of("width", "track", "thumb");
    private static final Set<String> DRAWABLE_KEYS = Set.of("color", "icon", "sprite");

    /** Parses {@code reader} as a theme; malformed JSON surfaces as {@link UIFormatException}, never a crash. */
    public static Theme load(ResourceLocation file, Reader reader) {
        String path = file.toString();
        String ns = file.getNamespace();
        try {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) throw new UIFormatException(path, 0, "theme root must be a JSON object");
            JsonObject root = parsed.getAsJsonObject();
            unknownKeys(path, "root", root, ROOT_KEYS);

            int text = hexInt(root, path, "text", 0xFFFFFFFF);
            int textDisabled = hexInt(root, path, "textDisabled", 0xFF888888);

            Drawable accent = drawable(path, root, "accent", ns);
            Drawable accentBorder = drawable(path, root, "accentBorder", ns);
            // UNCONDITIONALLY DEREFERENCED BY Element.paint()/Switch.draw() -> NEVER null
            Drawable disabledOverlay = drawable(path, root, "disabledOverlay", ns);
            if (disabledOverlay == null) { warn(path, "disabledOverlay"); disabledOverlay = new Color(0); }
            Drawable field = drawable(path, root, "field", ns);
            Drawable fieldHover = drawable(path, root, "fieldHover", ns);
            Drawable selection = drawable(path, root, "selection", ns);
            Drawable danger = drawable(path, root, "danger", ns);
            Drawable dangerBorder = drawable(path, root, "dangerBorder", ns);

            ElementTheme panel = elementTheme(root, path, ns, "panel");
            ElementTheme clickable = elementTheme(root, path, ns, "clickable");
            ElementTheme nested = elementTheme(root, path, ns, "nested");
            ElementTheme bar = elementTheme(root, path, ns, "bar");

            JsonObject toggleObj = childObject(root, "toggle");
            SwitchTheme toggle;
            if (toggleObj == null) {
                warn(path, "toggle");
                toggle = Theme.ERROR.toggle();
            } else {
                unknownKeys(path, "toggle", toggleObj, TOGGLE_KEYS);
                // Switch.draw() DRAWS style.thumb(...) UNCONDITIONALLY -> thumb NEVER null
                Drawable thumb = drawable(path, toggleObj, "thumb", ns);
                if (thumb == null) { warn(path, "toggle.thumb"); thumb = new Color(0); }
                toggle = new SwitchTheme(
                        intOr(toggleObj, "trackWidth", 0), intOr(toggleObj, "trackHeight", 0),
                        intOr(toggleObj, "border", 0), intOr(toggleObj, "gap", 0), intOr(toggleObj, "slideMs", 0),
                        intOr(toggleObj, "thumbWidth", 0), intOr(toggleObj, "thumbHeight", 0),
                        spacing(toggleObj.get("thumbMargin")),
                        drawable(path, toggleObj, "outline", ns), drawable(path, toggleObj, "off", ns), drawable(path, toggleObj, "on", ns),
                        thumb, drawable(path, toggleObj, "thumbHover", ns));
            }

            JsonObject sliderObj = childObject(root, "slider");
            SliderTheme slider;
            if (sliderObj == null) {
                warn(path, "slider");
                slider = Theme.ERROR.slider();
            } else {
                unknownKeys(path, "slider", sliderObj, SLIDER_KEYS);
                // Slider.draw() DRAWS style.knob(...) UNCONDITIONALLY -> knob NEVER null
                Drawable knob = drawable(path, sliderObj, "knob", ns);
                if (knob == null) { warn(path, "slider.knob"); knob = new Color(0); }
                slider = new SliderTheme(intOr(sliderObj, "height", 0), intOr(sliderObj, "knobWidth", 0),
                        drawable(path, sliderObj, "track", ns), knob, drawable(path, sliderObj, "knobHover", ns));
            }

            JsonObject progressObj = childObject(root, "progress");
            ProgressTheme progress;
            if (progressObj == null) {
                warn(path, "progress");
                progress = Theme.ERROR.progress();
            } else {
                unknownKeys(path, "progress", progressObj, PROGRESS_KEYS);
                // ProgressBar.draw() DRAWS .fill() UNCONDITIONALLY -> fill NEVER null
                Drawable fill = drawable(path, progressObj, "fill", ns);
                if (fill == null) { warn(path, "progress.fill"); fill = new Color(0); }
                progress = new ProgressTheme(intOr(progressObj, "height", 0), fill);
            }

            JsonObject scrollObj = childObject(root, "scroll");
            ScrollTheme scroll;
            if (scrollObj == null) {
                warn(path, "scroll");
                scroll = Theme.ERROR.scroll();
            } else {
                unknownKeys(path, "scroll", scrollObj, SCROLL_KEYS);
                // ParentLinear DRAWS THE SCROLLBAR track/thumb UNCONDITIONALLY -> NEVER null
                Drawable track = drawable(path, scrollObj, "track", ns);
                if (track == null) { warn(path, "scroll.track"); track = new Color(0); }
                Drawable thumb = drawable(path, scrollObj, "thumb", ns);
                if (thumb == null) { warn(path, "scroll.thumb"); thumb = new Color(0); }
                scroll = new ScrollTheme(intOr(scrollObj, "width", 0), track, thumb);
            }

            return new Theme(text, textDisabled, accent, accentBorder, disabledOverlay, field, fieldHover, selection,
                    danger, dangerBorder, panel, clickable, nested, bar, toggle, slider, progress, scroll);
        } catch (UIFormatException e) {
            throw e.file != null ? e : new UIFormatException(path, 0, e.getMessage());
        } catch (RuntimeException e) {
            throw new UIFormatException(path, 0, "malformed theme JSON: " + e.getMessage());
        }
    }

    // FIRST RECOGNIZED DISCRIMINATOR KEY WINS BY JsonObject INSERTION ORDER (§4); ABSENT/UNRECOGNIZED -> null
    private static Drawable drawable(String file, JsonObject holder, String key, String ns) {
        JsonElement el = holder.get(key);
        if (el == null || !el.isJsonObject()) return null;
        JsonObject obj = el.getAsJsonObject();
        unknownKeys(file, key, obj, DRAWABLE_KEYS);
        for (Map.Entry<String, JsonElement> entry: obj.entrySet()) {
            switch (entry.getKey()) {
                case "color": return new Color(UIValues.color(entry.getValue().getAsString()));
                case "icon": return icon(entry.getValue().getAsString(), ns);
                case "sprite": return Sprite.of(entry.getValue().getAsString());
                default: break;
            }
        }
        return null;
    }

    // "<atlas>, <cx>, <cy>[, w, h]" IN THE §5.7 EXTENSIONLESS ATLAS FORM, RESOLVED THROUGH THE RESOURCE MANAGER
    private static Drawable icon(String spec, String ns) {
        String[] parts = spec.split(",");
        if (parts.length != 3 && parts.length != 5) throw new UIFormatException(null, -1, "theme icon needs '<atlas>, x, y' or '<atlas>, x, y, w, h'");
        UIAtlases.Handle handle = UIAtlases.load(parts[0].trim(), ns);
        int cx = intAtom(parts[1]);
        int cy = intAtom(parts[2]);
        int cw = parts.length == 5 ? intAtom(parts[3]) : 1;
        int ch = parts.length == 5 ? intAtom(parts[4]) : 1;
        return handle.icon(cx, cy, cw, ch);
    }

    private static int intAtom(String atom) {
        try {
            return Integer.parseInt(atom.trim());
        } catch (NumberFormatException e) {
            throw new UIFormatException(null, -1, "invalid number '" + atom + "'");
        }
    }

    // BARE NUMBER = ALL EDGES, [h,v] OR [t,r,b,l] ARRAYS (§8.3)
    private static Spacing spacing(JsonElement el) {
        if (el == null) return Spacing.ZERO;
        if (el.isJsonArray()) {
            List<UIValue> items = new ArrayList<>();
            for (JsonElement e: el.getAsJsonArray()) items.add(new UIValue.Num(e.getAsDouble(), e.getAsString()));
            return UIValues.spacing(new UIValue.Tuple(items));
        }
        return UIValues.spacing(new UIValue.Num(el.getAsDouble(), el.getAsString()));
    }

    // A ROLE BLOCK MISSING ENTIRELY FALLS BACK TO ElementTheme.NONE (NO CODE PRESET TO INHERIT)
    private static ElementTheme elementTheme(JsonObject root, String file, String ns, String key) {
        JsonObject obj = childObject(root, key);
        if (obj == null) { warn(file, key); return ElementTheme.NONE; }
        unknownKeys(file, key, obj, ROLE_KEYS);
        return new ElementTheme(intOr(obj, "border", 0), spacing(obj.get("padding")), spacing(obj.get("margin")),
                !obj.has("shadow") || obj.get("shadow").getAsBoolean(),
                drawable(file, obj, "outline", ns), drawable(file, obj, "face", ns), drawable(file, obj, "hover", ns));
    }

    private static JsonObject childObject(JsonObject root, String key) {
        JsonElement el = root.get(key);
        return el != null && el.isJsonObject() ? el.getAsJsonObject() : null;
    }

    private static int intOr(JsonObject obj, String key, int fallback) {
        return obj.has(key) ? obj.get(key).getAsInt() : fallback;
    }

    private static int hexInt(JsonObject root, String file, String key, int fallback) {
        if (!root.has(key)) { warn(file, key); return fallback; }
        return UIValues.color(root.get(key).getAsString());
    }

    // A DEFAULTED REQUIRED SLOT ALWAYS WARNS ONCE SO A THEME AUTHOR SEES WHAT WAS FILLED IN
    private static void warn(String file, String key) {
        WaterUI.LOGGER.warn(IT, "{} missing theme key '{}', using default", file, key);
    }

    // §14.5 TYPO CATCHER: A PRESENT KEY THAT MATCHES NO SLOT OF THIS OBJECT WARNS; OMITTED KEYS STAY SILENT
    private static void unknownKeys(String file, String where, JsonObject obj, Set<String> known) {
        for (String key: obj.keySet()) {
            if (!known.contains(key)) WaterUI.LOGGER.warn(IT, "{} unknown theme key '{}' in {}", file, key, where);
        }
    }
}
