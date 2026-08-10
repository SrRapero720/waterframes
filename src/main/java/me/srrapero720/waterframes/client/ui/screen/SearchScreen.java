package me.srrapero720.waterframes.client.ui.screen;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterui.layout.ParentList;
import me.srrapero720.waterui.screen.Dialog;
import me.srrapero720.waterui.widget.InputText;
import me.srrapero720.waterui.widget.Text;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.watermedia.api.platform.PlatformAPI;
import org.watermedia.api.platform.PlatformResult;
import org.watermedia.api.platform.PlatformSearch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Panel that queries every platform and lists what comes back. Picking a result hands its url
 * to whoever opened the panel and closes it.
 */
public class SearchScreen extends Dialog {
    private static final int WIDTH = 232;
    private static final int HEIGHT = 192;

    private final Consumer<String> onPick;
    private final String initialQuery;

    // TOKEN REGISTRY: EACH PlatformResult GETS A COUNTER-BASED STRING TOKEN AS ITS ParentList KEY.
    // LinkedHashMap PRESERVES INSERTION ORDER SO submit() ALWAYS REFLECTS ARRIVAL ORDER
    private final Map<String, PlatformResult> tokens = new LinkedHashMap<>();
    private int tokenCounter;

    // ROW CACHE SURVIVING RESIZES SO THUMBNAILS ARE NOT RE-FETCHED (§10.6 RowStore)
    private ParentList.RowStore rowStore;

    // BINDER: EACH ROW'S BAKED VARIABLES COME FROM THE TOKEN REGISTRY, NO LIVE VARS
    private final ParentList.RowBinder binder = new ParentList.RowBinder() {
        @Override
        public Map<String, Object> baked(Object key) {
            PlatformResult r = tokens.get((String) key);
            if (r == null) return Map.of("key", key, "title", "", "platform", "", "thumbnail", "");
            return Map.of(
                    "key", key,
                    "title", literal(r.title()),
                    "platform", literal(r.platform()),
                    "thumbnail", r.thumbnail() != null ? r.thumbnail().toString() : ""
            );
        }

        @Override
        public Object model(Object key) {
            return null;
        }
    };

    // A REMOTE STRING MUST NEVER SELECT THE i18n BRANCH OF THE COMPONENT FACTORY (M6):
    // A LEADING SPACE BREAKS THE translatable: PREFIX WHILE STAYING VISUALLY INERT
    private static String literal(String s) {
        if (s == null) return "";
        return s.startsWith("translatable:") ? " " + s : s;
    }

    // RESOLVED FROM dialogs/search.ui ON EVERY init(); THE FORMAT OWNS THE TREE, JAVA ONLY WIRES BEHAVIOUR
    private InputText query;
    private Text notice;
    private ParentList results;
    // TRUE ONLY AFTER init() RESOLVED EVERY WIDGET, SO A BROKEN DOCUMENT NEVER NPEs A LATER PATH
    private boolean wired;

    private PlatformSearch search;
    private int shown;
    private int ticks;
    private int dots;
    private boolean initialized;

    public SearchScreen(String query, Consumer<String> onPick) {
        super(WaterFrames.asResource("dialogs/search"), WIDTH, HEIGHT);
        this.onPick = onPick;
        this.initialQuery = query;
        // SEARCH BUTTON (onClick=search): RUNS THE CURRENT QUERY; pick KEEPS ITS METHOD, IT HAS REAL LOGIC
        this.registerUIEvent("search", () -> { if (wired) this.run(this.query.text()); });
    }

    @Override
    public void init() {
        this.wired = false;

        this.query = (InputText) this.get(WaterFrames.asResource("search/query"));
        this.notice = (Text) this.get(WaterFrames.asResource("search/notice"));
        this.results = (ParentList) this.get(WaterFrames.asResource("search/results"));

        // A BROKEN dialogs/search.ui LEAVES A LOOKUP NULL; NAME THE FIRST MISSING id AND SKIP THE WIRING
        String missing = query == null ? "search/query" : notice == null ? "search/notice" : results == null ? "search/results" : null;
        if (missing != null) {
            WaterFrames.LOGGER.error("[WaterFrames] search screen missing id '{}', skipping wiring", missing);
            return;
        }
        this.wired = true;

        // WIRE THE BINDER AND ADOPT THE ROW CACHE (NEW ON FIRST INIT, SURVIVING ON RESIZE)
        this.results.binder(binder);
        this.results.rows(rowStore);
        this.rowStore = results.rows();

        if (!initialized) {
            this.initialized = true;
            if (initialQuery != null && !initialQuery.isBlank()) this.query.text(initialQuery.trim());
            this.run(query.text());
        } else {
            // RESIZE: RE-SUBMIT CURRENT KEYS SO ROWS REAPPEAR WITH THEIR THUMBNAILS INTACT
            if (!tokens.isEmpty()) this.results.submit(new ArrayList<>(tokens.keySet()));
            this.notice.visible = tokens.isEmpty();
            this.dirty();
        }
    }

    // RESULT ROW CLICK HANDLER (onClick=pick(key)): PASSES THE URL AND CLOSES
    public void pick(String id, String key) {
        PlatformResult result = tokens.get(key);
        if (result == null) return;
        playClick();
        onPick.accept(result.url().toString());
        close();
    }

    private void run(String text) {
        if (text == null || text.isBlank()) return;
        String terms = text.trim();

        // CLEAR THE OLD SEARCH STATE AND DROP ALL ROWS
        this.tokens.clear();
        this.tokenCounter = 0;
        this.shown = 0;
        this.dots = 0;
        if (wired) {
            this.results.submit(List.of());
            this.notice.text(translatable("waterframes.gui.search.working"));
            this.notice.visible = true;
        }

        try {
            this.search = PlatformAPI.search(terms);
        } catch (Exception e) {
            this.search = null;
            if (wired) this.notice.text(translatable("waterframes.gui.search.failed"));
        }
        this.dirty();
    }

    @Override
    public void tick() {
        super.tick();
        if (!wired || search == null) return;

        // ANIMATED DOTS WHILE THE PLATFORMS ANSWER
        if (!search.done() && notice.visible) {
            int next = (++ticks / 8) % 4;
            if (next != dots) {
                this.dots = next;
                this.notice.text(Component.literal(translate("waterframes.gui.search.working") + ".".repeat(next)));
            }
        }

        // ASSIGN TOKENS TO NEW RESULTS AND SUBMIT THE UPDATED KEY LIST
        List<PlatformResult> found = search.results();
        if (found.size() > shown) {
            for (int i = shown; i < found.size(); i++) {
                String token = String.valueOf(tokenCounter++);
                tokens.put(token, found.get(i));
            }
            this.shown = found.size();
            this.results.submit(new ArrayList<>(tokens.keySet()));
            this.notice.visible = false;
            this.dirty();
        }

        if (search.done() && found.isEmpty()) this.notice.text(translatable("waterframes.gui.search.none"));
    }

    @Override
    public boolean keyDown(int key, int scan, int modifiers) {
        if (wired && (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)) {
            this.run(query.text());
            return true;
        }
        return super.keyDown(key, scan, modifiers);
    }

    @Override
    public void closed() {
        // BELT FOR A STORE ORPHANED BY A BROKEN RE-INIT; THE TREE CASCADE ALREADY DISPOSED LIVE ROWS
        if (rowStore != null) rowStore.dispose();
    }
}
