package me.srrapero720.waterframes.client.ui.screen;

import me.srrapero720.waterframes.DisplaysConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterui.format.UIEvent;
import me.srrapero720.waterui.format.UIVar;
import me.srrapero720.waterui.screen.WaterScreen;
import me.srrapero720.waterframes.client.ui.Icons;
import me.srrapero720.waterframes.client.ui.screen.widget.AnchorPicker;
import me.srrapero720.waterframes.client.ui.screen.widget.SourceRow;
import me.srrapero720.waterframes.client.ui.screen.widget.UrlField;
import me.srrapero720.waterframes.common.compat.watervision.WVCompat;
import me.srrapero720.waterui.core.Spacing;
import me.srrapero720.waterui.layout.ParentList;
import me.srrapero720.waterui.widget.ParentTab;
import me.srrapero720.waterui.widget.Slider;
import me.srrapero720.waterui.widget.StateButton;
import me.srrapero720.waterui.widget.Stepper;
import me.srrapero720.waterui.widget.Switch;
import me.srrapero720.waterui.widget.UIMediaPlayer;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.data.DisplayData.Entry;
import me.srrapero720.waterframes.common.block.data.types.AudioPosition;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import me.srrapero720.waterframes.common.network.packets.DataListSyncPacket;
import me.srrapero720.waterframes.common.network.packets.DataSyncPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.watermedia.api.player.MRL;
import org.watermedia.api.player.MediaPlayer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static me.srrapero720.waterui.core.Element.translatable;
import static me.srrapero720.waterui.core.Element.translate;

/**
 * Settings of a display, split into tabs. The strip on top picks the page, the player pane
 * on the right stays put whichever page is open, and the screen writes itself once the
 * player stops turning knobs, so there is no save button to forget. Saves travel as partial
 * tags: only what actually differs from the tile leaves the client. The tree lives in
 * {@code display_preview.ui} (plus one imported {@code .ui} per tab); this class registers the
 * variables and events, dresses the resolved widgets and owns the working playlist.
 */
public class DisplayScreen extends WaterScreen implements ParentList.RowBinder {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 227;

    // BAKED ROW GEOMETRY (3:2 THUMBS): A MEDIA HEAD IS FULL SIZE, AN EXPANDED SUB-SOURCE IS COMPACT AND LEFT-INSET
    private static final int THUMB_W = 42;
    private static final int THUMB_H = 28;
    private static final int THUMB_SUB_W = 24;
    private static final int THUMB_SUB_H = 16;
    private static final float TITLE = 0.75f;
    private static final float TITLE_SUB = 0.6f;
    private static final int SUB_INSET = 8;

    public final DisplayTile tile;

    // ---- WORKING PLAYLIST (THE TILE ONLY SEES IT WHEN THE PLAYER SAVES) ------------------

    private final List<Entry> playlist;
    /** Entry the player picked in the list, which becomes the one on air on the next write. */
    private Entry active;
    // SCREEN TICKS, AND THE ONE THE WORKING LIST LAST LEFT FOR THE SERVER: A LIST STILL DIFFERING
    // FROM THE TILE A SECOND LATER WAS REFUSED THERE (OR CHANGED BY SOMEONE ELSE) AND IS ADOPTED BACK
    private int ticks;
    private int listSent = Integer.MIN_VALUE / 2;
    /** Entry currently selected (accent border); null when the pick left the playlist. */
    private Entry selectedEntry;

    // STABLE PER-KEY MODEL: binder.model MUST RETURN THE SAME SourceRow FOR A KEY OVER ITS LIFETIME, SO ITS
    // LIVE SUPPLIERS KEEP FEEDING THE SAME ROW. THE TOKEN REVERSES A ROW'S BAKED key BACK TO ITS ENTRY (EVENTS)
    private final Map<Entry, SourceRow> models = new HashMap<>();
    private final Map<Entry, String> tokenByEntry = new HashMap<>();
    private final Map<String, Entry> entryByToken = new HashMap<>();
    private int tokenSeq;
    // MODEL OF THE URL ON AIR WHEN NO PLAYLIST ENTRY MATCHES IT; KEPT OUT OF models, WHICH refresh() PRUNES
    private SourceRow loose;

    // ROW CACHE OWNED ACROSS RE-INITS: A FRESH list RE-ADOPTS IT SO A PREVIEW PLAYER OUTLIVES A RESIZE (§10.6)
    private final ParentList.RowStore rowStore = new ParentList.RowStore();

    // CACHED ONCE PER TICK FOR THE LIVE BINDINGS; field.valid() TRIGGERS PlayerAPI.mrl(), SO IT RUNS ONCE NOT THREE TIMES
    private boolean cachedLink;
    private boolean cachedQuery;

    // ---- WIDGETS RESOLVED PER INFLATE (§13.2): THE DOCUMENT POSITIONS, THE SCREEN DRESSES.
    // A DOCUMENT A PACK BROKE RESOLVES NULL AND ITS SECTION CONTRIBUTES NOTHING (H4)

    /** The declarative tab container from display_preview.ui; owns the strip, the bodies and the switch. */
    private ParentTab parentTab;
    /** Which tab is open, kept across the re-inflate a resize triggers so the page does not reset. */
    private int activeTab;
    // INDEX OF THE SOURCES PAGE, FOUND BY CONTENT id SO A PACK REORDERING THE TABS CANNOT MISROUTE THE TICK
    private int sourcesTab = -1;

    private UrlField field;
    private ParentList list;
    private Stepper widthField;
    private Stepper heightField;
    private AnchorPicker position;
    private Slider rotation, alpha, brightness, renderDistance, projectionDistance;
    private Switch lit, mirror, showModel, flipX, flipY;
    private Slider volume, volumeMin, volumeMax;
    private StateButton audioOffset;
    /** Video surface mirroring the tile's own player; the document only places it (§13.2). */
    private UIMediaPlayer preview;
    // PLAYER THE PREVIEW IS WRAPPED AROUND, HELD AS Object THE WAY UIMediaPlayer TAKES IT
    private MediaPlayer previewOf;

    public DisplayScreen(DisplayTile tile) {
        super(WaterFrames.asResource("display"), WIDTH, HEIGHT);
        this.tile = tile;
        this.playlist = new ArrayList<>(tile.data.playlist);

        // ONE-LINE STATE READS AND TRANSPORT COMMANDS REGISTER STRAIGHT INTO THE HOST STORE (§5.6, §12.2);
        // THE HOT NUMERICS (time/duration) AND THE COMPOSED TOOLTIPS KEEP THEIR ANNOTATED METHODS
        this.registerVar("loop", () -> tile.data.loop);
        this.registerVar("paused", () -> tile.data.paused);
        this.registerVar("muted", () -> tile.data.muted);
        this.registerVar("reload_ready", () -> tile.mrl != null && tile.data.hasUrl());
        this.registerVar("watervision_ready", () -> tile.data.hasUrl() && tile.mrl != null && tile.mrl.status().loaded());
        this.registerVar("audio_tracks_ready", () -> tile.follower() != null && tile.follower().audioTracks().size() > 1);
        // THE PICK IS THIS VIEWER'S: AUDIO IS NEVER SESSION STATE, EVERY CLIENT HEARS ITS OWN TRACK
        this.registerUIEvent("nextAudioTrack", () -> {
            final MediaPlayer follower = tile.follower();
            if (follower == null) return;
            final List<MediaPlayer.AudioTrack> tracks = follower.audioTracks();
            if (tracks.size() < 2) return;
            follower.audioTrack(tracks.get((tracks.indexOf(follower.audioTrack()) + 1) % tracks.size()));
        });
        // THUMBNAIL URL OF THE MEDIA ON AIR; LIVE BECAUSE THE MRL RESOLVES ASYNC AFTER THE SCREEN OPENS
        this.registerVar("media_source", () -> {
            if (tile.mrl == null || !tile.mrl.status().loaded()) return "";
            MRL.Source source = tile.mrl.source(tile.data.getSource());
            return source != null && source.thumbnail() != null ? source.thumbnail().toString() : "";
        });
        // ACTIVE-SOURCE STATE FOR THE TRANSPORT BADGE: THE SAME MAPPING THE PLAYLIST ROWS USE, OVER tile.mrl/tile.display
        this.registerVar("status_icon", () -> SourceRow.statusIcon(tile.mrl, tile, this.missing()));
        this.registerVar("status_tooltip", () -> SourceRow.describe(tile.mrl, tile, this.missing()));
        this.registerVar("transport_ready", () -> tile.follower() != null);
        // NOW PLAYING TEXT OF THE PLAYER PANE: THE ON-AIR ROW MODEL, EMPTY WHEN NOTHING IS ON AIR
        this.registerVar("now_title", () -> {
            final SourceRow row = this.onAir();
            return row != null ? row.title() : "";
        });
        this.registerVar("now_byline", () -> {
            final SourceRow row = this.onAir();
            return row != null ? row.byline() : "";
        });
        this.registerVar("submit_enabled", () -> cachedLink || cachedQuery);
        this.registerVar("submit_icon", () -> cachedLink ? Icons.ADD : Icons.SEARCH);
        this.registerVar("delete_enabled", () -> selectedEntry != null && selectedEntry.source() == 0);
        this.registerVar("resize_x_tooltip", () -> this.ratioTooltip("waterframes.gui.resize.x", this.widthRatio()));
        this.registerVar("resize_y_tooltip", () -> this.ratioTooltip("waterframes.gui.resize.y", this.heightRatio()));

        this.registerUIEvent("toggleLoop", () -> tile.loop(true, !tile.data.loop));
        this.registerUIEvent("togglePlayback", () -> tile.setPause(true, !tile.data.paused));
        this.registerUIEvent("toggleMute", () -> tile.setMute(true, !tile.data.muted));
        this.registerUIEvent("stop", () -> tile.setStop(true));
        // SKIPPING IS A SESSION COMMAND: IT TRAVELS TO THE SERVER CLOCK AND COMES BACK TO EVERY VIEWER
        this.registerUIEvent("rewind", () -> tile.skip(true, -DisplayTile.SKIP_MS));
        this.registerUIEvent("fastForward", () -> tile.skip(true, DisplayTile.SKIP_MS));
        // REFETCH THE MRL AND DROP THE PLAYER SO THE NEXT TICK REBUILDS IT FROM THE FRESH SOURCES
        this.registerUIEvent("reload", tile::reload);
        // HANDS THE MEDIA TO WATERVISION'S OWN SCREEN AND PAUSES THE DISPLAY MEANWHILE
        this.registerUIEvent("openWatervision", () -> { WVCompat.openScreen(tile.data.getUrl(), tile.data.volume); tile.setPause(true, true); });
        // TOGGLE PAYLOADS AND ROW TOKENS ARRIVE AT args[1], AFTER THE ELEMENT id (§12.1, §12.2)
        this.registerUIEvent("mute", args -> tile.setMute(true, Boolean.parseBoolean(args[1])));
        this.registerUIEvent("shaderMode", args -> DisplaysConfig.shaderMode(Boolean.parseBoolean(args[1])));
        this.registerUIEvent("submit", this::submit);
        this.registerUIEvent("upload", () -> this.openDialog(new UploadScreen(this::append)));
        this.registerUIEvent("deleteSource", this::deleteSelected);
        this.registerUIEvent("select", args -> this.select(args[1]));
        this.registerUIEvent("remove", args -> this.remove(args[1]));
        // DRAG DROP FROM THE PLAYLIST (§10.6): THE LIST FIRES BLOCK-NORMALIZED ROW INDEXES
        this.registerUIEvent("moveEntry", args -> this.move(Integer.parseInt(args[1]), Integer.parseInt(args[2])));
        // SCRUBBING IS A SESSION COMMAND, NOT DISPLAY STATE: IT TRAVELS TO THE SERVER CLOCK AND COMES
        // BACK TO EVERY VIEWER FROM THERE
        this.registerUIEvent("seek", args -> tile.seek(true, (long) Double.parseDouble(args[1])));
        this.registerUIEvent("skip", args -> tile.skip(true, Double.parseDouble(args[1]) > 0 ? DisplayTile.SKIP_MS : -DisplayTile.SKIP_MS));

        // RATIO BUTTONS (§12.2 ELEMENT EVENTS): CLICK MATCHES THE OTHER AXIS TO THE MEDIA'S NATIVE ASPECT
        this.registerEvent("render/resize_y", el -> { double v = this.heightRatio(); if (v > 0 && heightField != null) heightField.value(v); });
        this.registerEvent("render/resize_x", el -> { double v = this.widthRatio(); if (v > 0 && widthField != null) widthField.value(v); });

        // IMMUTABLE TILE FACTS AND CONFIG CAPS AS INFLATABLE CONSTANTS (§5.6)
        this.registerVar("resizes", tile.caps.resizes());
        this.registerVar("projects", tile.caps.projects());
        this.registerVar("render_behind", tile.caps.renderBehind());
        this.registerVar("hide_model", tile.canHideModel());
        this.registerVar("watervision", WVCompat.installed());
        this.registerVar("max_width", DisplaysConfig.maxWidth());
        this.registerVar("max_height", DisplaysConfig.maxHeight());
        this.registerVar("max_render_distance", DisplaysConfig.maxRenDis());
        this.registerVar("max_projection_distance", DisplaysConfig.maxProjDis());
        this.registerVar("max_volume", DisplaysConfig.maxVol());
        this.registerVar("max_volume_distance", DisplaysConfig.maxVolDis());
        // THE EMBLEM IS AUTHORED IN display_preview.ui AS AN outsideAnchor ItemIcon; JAVA ONLY SUPPLIES THE STACK
        this.registerVar("emblem_item", new ItemStack(tile.getBlockState().getBlock()));
        this.registerVar("emblem_name", tile.getBlockState().getBlock().getName().getString());

        // MUTABLE TILE STATE AS SUPPLIERS: THE final vars OF THE DOCS SNAPSHOT THEM FRESH ON EVERY
        // RE-INFLATE (§5.6), SO A RESIZE RE-SEEDS THE KNOBS FROM THE CURRENT DATA, NOT THE OPEN-TIME ONE
        this.registerVar("volume_min_max", () -> Math.min(tile.data.maxVolumeDistance, DisplaysConfig.maxVolDis()));
        this.registerVar("width", tile.data::getWidth);
        this.registerVar("height", tile.data::getHeight);
        this.registerVar("rotation", () -> tile.data.rotation);
        this.registerVar("alpha", () -> tile.data.alpha);
        this.registerVar("brightness", () -> tile.data.brightness);
        this.registerVar("render_distance", () -> tile.data.renderDistance);
        this.registerVar("projection_distance", () -> tile.data.projectionDistance);
        this.registerVar("volume", () -> tile.data.volume);
        this.registerVar("volume_min", () -> tile.data.minVolumeDistance);
        this.registerVar("volume_max", () -> tile.data.maxVolumeDistance);
        this.registerVar("audio_offset", () -> tile.data.getAudioPosition().ordinal());
        this.registerVar("lit", () -> tile.data.lit);
        this.registerVar("mirror", () -> tile.data.renderBothSides);
        this.registerVar("show_model", () -> tile.canHideModel() && tile.isVisible());
        this.registerVar("flip_x", () -> tile.data.flipX);
        this.registerVar("flip_y", () -> tile.data.flipY);
        this.registerVar("shader_mode", DisplaysConfig::shaderMode);
        this.registerVar("pos_x", () -> tile.data.getPosX().name());
        this.registerVar("pos_y", () -> tile.data.getPosY().name());
    }

    // ---- LIVE BINDINGS (annotated: hot numerics and composed tooltips, §5.6) -------------

    @UIVar("time")
    public long time() {
        return tile.display != null ? tile.display.time() : 0;
    }

    @UIVar("duration")
    public long duration() {
        return tile.display != null ? tile.display.duration() : 0;
    }

    @UIVar("loop_tooltip")
    public List<Component> loopTooltip() {
        return List.of(translatable("waterframes.gui.loop",
                (tile.data.loop ? ChatFormatting.GREEN : ChatFormatting.RED) + translate("waterframes.common." + tile.data.loop)));
    }

    @UIVar("playback_tooltip")
    public List<Component> playbackTooltip() {
        return List.of(translatable("waterframes.gui.playback",
                ChatFormatting.AQUA + translate("waterframes.common." + (tile.data.paused ? "paused" : "playing"))));
    }

    @UIVar("mute_tooltip")
    public List<Component> muteTooltip() {
        return List.of(translatable("waterframes.gui.mute.state",
                (this.tile.data.muted ? ChatFormatting.RED : ChatFormatting.GREEN) + translate("waterframes.common." + this.tile.data.muted)));
    }

    @UIVar("submit_tooltip")
    public List<Component> submitTooltip() {
        String key = cachedLink ? "waterframes.gui.source.add"
                : cachedQuery ? "waterframes.gui.source.search" : "waterframes.gui.source.hint";
        return List.of(translatable(key));
    }

    @UIVar("audio_track_tooltip")
    public List<Component> audioTrackTooltip() {
        final MediaPlayer follower = tile.follower();
        final List<MediaPlayer.AudioTrack> tracks = follower != null ? follower.audioTracks() : List.of();
        if (tracks.size() < 2) return List.of(translatable("waterframes.gui.media.audio_track.none"));
        final MediaPlayer.AudioTrack current = follower.audioTrack();
        // A TRACK NAMES ITSELF, ELSE ITS LANGUAGE SPELLS IT OUT, ELSE ITS POSITION IN THE LIST
        final int index = Math.max(0, tracks.indexOf(current));
        final String name = current != null && current.name() != null ? current.name()
                : current != null && current.lang() != null ? Locale.forLanguageTag(current.lang()).getDisplayLanguage()
                : "#" + (index + 1);
        return List.of(translatable("waterframes.gui.media.audio_track.current", ChatFormatting.AQUA + name),
                translatable("waterframes.gui.media.audio_track.switch", index + 1, tracks.size()));
    }

    @UIVar("audio_pos_tooltip")
    public List<Component> audioPosTooltip() {
        int state = audioOffset != null ? audioOffset.state() : tile.data.getAudioPosition().ordinal();
        return List.of(translatable("waterframes.gui.audio_pos.1"),
                translatable("waterframes.gui.audio_pos.2",
                        ChatFormatting.AQUA + translate("waterframes.gui.audio_pos.states." + state)));
    }

    // THE ACTIVE SOURCE INDEX FELL OUT OF RANGE OF THE RESOLVED MEDIA: THE PLAYLIST OR THE MEDIA MOVED UNDER IT
    private boolean missing() {
        if (tile.mrl == null || !tile.mrl.status().loaded()) return false;
        int count = tile.mrl.sourceCount();
        return count > 0 && tile.data.getSource() >= count;
    }

    @Override
    protected void build() {
        // WIDGETS RESOLVE FIRST, ALWAYS: A BROKEN DOCUMENT NULLS EVERY FIELD SO flush() NEVER READS A
        // DISCONNECTED KNOB; A PACK DROPPING AN id LEAVES ITS PATCH SECTION CONTRIBUTING NOTHING (H4)
        this.widthField = this.get(WaterFrames.asResource("render/width")) instanceof Stepper s ? s : null;
        this.heightField = this.get(WaterFrames.asResource("render/height")) instanceof Stepper s ? s : null;
        this.position = this.get(WaterFrames.asResource("render/position")) instanceof AnchorPicker p ? p : null;
        this.rotation = this.slider("render/rotation");
        this.alpha = this.slider("render/alpha");
        this.brightness = this.slider("render/brightness");
        this.renderDistance = this.slider("render/render_distance");
        this.projectionDistance = this.slider("render/projection_distance");
        this.lit = this.toggle("render/lit");
        this.mirror = this.toggle("render/mirror");
        this.showModel = this.toggle("render/show_model");
        this.flipX = this.toggle("render/flip_x");
        this.flipY = this.toggle("render/flip_y");
        this.volume = this.slider("media/volume");
        this.volumeMin = this.slider("media/volume_min");
        this.volumeMax = this.slider("media/volume_max");
        this.audioOffset = this.get(WaterFrames.asResource("media/audio_offset")) instanceof StateButton s ? s : null;
        // A RE-INFLATE HANDS OUT A FRESH SURFACE: FORGET THE OLD WRAP SO THE NEXT TICK BINDS THIS ONE
        this.preview = this.get(WaterFrames.asResource("display/preview")) instanceof UIMediaPlayer p ? p : null;
        this.previewOf = null;
        // VOLUME RANGE COUPLING: THE MIN SLIDER'S CEILING FOLLOWS THE MAX SLIDER LIVE
        if (volumeMin != null && volumeMax != null) volumeMax.onChange(volumeMin::max);

        // PLAYLIST WIRING: RE-ADOPT THE SURVIVING ROWS AND ARM THE PER-ROW BINDER (§10.6)
        this.field = this.get(WaterFrames.asResource("sources/url")) instanceof UrlField url ? url : null;
        this.list = this.get(WaterFrames.asResource("sources/list")) instanceof ParentList pl ? pl : null;
        if (field != null && list != null) {
            list.rows(rowStore);
            list.binder(this);
            this.refresh();
        }

        // A TAB SWITCH KEEPS THE OPEN INDEX, PRIMES THE SOURCES PAGE WHEN IT APPEARS AND QUEUES A REFLOW
        this.parentTab = this.get(WaterFrames.asResource("display/tabs")) instanceof ParentTab tabs ? tabs : null;
        if (parentTab != null) {
            parentTab.onChange(index -> {
                this.activeTab = index;
                if (index == sourcesTab) this.sourcesTick();
                this.reflowQueued();
            });
            this.sourcesTab = parentTab.indexOf(WaterFrames.asResource("tabs/sources"));
            // A RESIZE REBUILDS THIS SCREEN AND RESETS THE FRESH STRIP TO TAB 0; RESTORE THE OPEN PAGE
            parentTab.active(activeTab);
        }

        this.tick();
    }

    private Slider slider(String id) {
        return this.get(WaterFrames.asResource(id)) instanceof Slider s ? s : null;
    }

    private Switch toggle(String id) {
        return this.get(WaterFrames.asResource(id)) instanceof Switch s ? s : null;
    }

    // ---- EVENT HANDLERS STILL WORTH A METHOD (§12.2) -------------------------------------

    /** The document reports a finished edit here; the patch ships on the spot, there is no debounce. */
    @UIEvent("commit")
    public void commit() {
        this.flush();
    }

    // THE ROW'S BAKED TOKEN ARRIVES AS key (§12.1); IT REVERSES BACK TO THE PLAYLIST ENTRY
    @UIEvent("openUri")
    void openUri(String id, String key) {
        String url = this.urlForToken(key);
        if (url == null) return;
        // A PLAYER-AUTHORED, SERVER-SYNCED URI IS UNTRUSTED: ONLY THE WEB SCHEMES EVER REACH THE OS,
        // AND NEVER WITHOUT THE VANILLA CONFIRM GATE (LOCAL PLAYLIST PATHS SIMPLY DO NOT OPEN)
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            return;
        }
        if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) return;
        Minecraft mc = Minecraft.getInstance();
        Screen previous = mc.screen;
        mc.setScreen(new ConfirmLinkScreen(confirmed -> {
            if (confirmed) {
                try {
                    Util.getPlatform().openUri(uri);
                } catch (Exception ignored) {
                    // A BROWSER MAY STILL BE MISSING; THE CLICK JUST DOES NOTHING
                }
            }
            mc.setScreen(previous);
        }, url, false));
    }

    // ---- RATIO MATH: MATCH ONE AXIS TO THE MEDIA'S NATIVE ASPECT ------------------------

    // NO SIZE YET MEANS NO RATIO: DIVIDING BY A ZERO SIDE YIELDS NaN AND POISONS THE FIELD
    private float heightRatio() {
        if (tile.display == null || tile.display.width() <= 0 || tile.display.height() <= 0 || widthField == null) return -1;
        return (float) (tile.display.height() / (tile.display.width() / widthField.value()));
    }

    private float widthRatio() {
        if (tile.display == null || tile.display.width() <= 0 || tile.display.height() <= 0 || heightField == null) return -1;
        return (float) (tile.display.width() / (tile.display.height() / heightField.value()));
    }

    private List<Component> ratioTooltip(String key, float ratio) {
        return List.of(translatable(key), ratio == -1
                ? translatable("waterframes.gui.resize.no_size")
                : translatable("waterframes.gui.resize.size", ChatFormatting.AQUA.toString() + ratio));
    }

    // ---- WORKING PLAYLIST OPERATIONS ----------------------------------------------------

    public void submit() {
        if (field == null) return;
        if (field.valid()) this.append();
        else this.openDialog(new SearchScreen(field.text(), this::append));
    }

    public void deleteSelected() {
        if (list == null || selectedEntry == null || selectedEntry.source() != 0) return;
        this.removeEntry(selectedEntry);
    }

    // SYNCS THE LIST WITH THE WORKING PLAYLIST: submit MOVES SURVIVING ROWS AND DISPOSES DROPPED ONES, SO A
    // REORDER OR RESIZE DOES NOT TEAR DOWN AND REBUILD THE PREVIEW PLAYER OF A ROW THAT ONLY MOVED (§10.6)
    private void refresh() {
        if (list == null) return;
        this.list.submit(new ArrayList<>(playlist));

        // PRUNE THE STABLE CACHES FOR ENTRIES THE LIST NO LONGER HOLDS; SURVIVORS KEEP THEIR MODEL AND TOKEN
        this.models.keySet().removeIf(e -> !playlist.contains(e));
        this.tokenByEntry.entrySet().removeIf(e -> {
            if (playlist.contains(e.getKey())) return false;
            this.entryByToken.remove(e.getValue());
            return true;
        });

        if (selectedEntry != null && !playlist.contains(selectedEntry)) this.selectedEntry = null;
        this.list.selected(selectedEntry);
        this.list.playing(this.playingEntry());
    }

    // THE ENTRY WHOSE URL AND SOURCE MATCH WHAT THE TILE PLAYS; ITS ROW WEARS THE selection FACE
    private Entry playingEntry() {
        if (!tile.data.hasUrl()) return null;
        String url = tile.data.getUrl();
        int source = tile.data.getSource();
        for (Entry e: playlist) {
            if (e.source() == source && e.url().equals(url)) return e;
        }
        return null;
    }

    // ROW MODEL OF WHAT THE TILE PLAYS: THE PLAYLIST ROW'S OWN, ELSE ONE BUILT ONCE PER LOOSE URL, ELSE null
    private SourceRow onAir() {
        if (!this.tile.data.hasUrl()) return null;
        final Entry entry = this.playingEntry();
        if (entry != null) return this.models.computeIfAbsent(entry, e -> new SourceRow(this.tile, e));
        final String url = this.tile.data.getUrl();
        final int source = this.tile.data.getSource();
        if (this.loose == null || this.loose.entry.source() != source || !this.loose.entry.url().equals(url)) {
            this.loose = new SourceRow(this.tile, new Entry(url, source, null));
        }
        return this.loose;
    }

    public void append(String url) {
        if (url == null || url.isBlank()) return;

        // WHOEVER TYPED IT OWNS IT, AND EVERY SOURCE THAT COMES OUT OF IT TOO
        var player = Minecraft.getInstance().player;
        Entry entry = new Entry(url, 0, player != null ? player.getUUID() : null);
        if (playlist.stream().anyMatch(entry::same)) return;

        this.playlist.add(entry);
        this.refresh();
        this.flush();
    }

    private void append() {
        if (field == null || !field.valid()) return;
        this.append(field.url());
        this.field.text("");
    }

    // PICKING AN ENTRY MARKS IT FOR THE ACTIONS BELOW AND PUTS IT ON AIR (RESOLVED FROM THE ROW'S BAKED TOKEN).
    // AN UNKNOWN TOKEN IS THE GUARD (M7): A ROW EVENT ARRIVING AFTER ITS ENTRY WAS PRUNED MUST NOT SELECT
    void select(String token) {
        if (list == null) return;
        Entry entry = entryByToken.get(token);
        if (entry == null) return;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
        this.selectedEntry = entry;
        this.list.selected(entry);
        if (!entry.equals(active)) this.play(entry);
    }

    // SWAPS THE MEDIA ON THE SPOT: THE TILE READS WHAT IT PLAYS FROM THE LIST INDEX, SO MOVING THE
    // INDEX DROPS THE RUNNING PLAYER AND HAS THE NEXT TICK OPEN ONE ON THE NEW SOURCE
    private void play(Entry target) {
        if (!playlist.contains(target)) return;
        this.active = target;

        // A GONE SOURCE WAS ONLY THE MARK OF WHAT WENT WRONG; PICKING A WORKING ONE CLEARS THEM ALL
        SourceRow model = models.get(target);
        boolean targetMissing = model != null && model.missing();
        if (!targetMissing && playlist.removeIf(entry -> {
            SourceRow other = models.get(entry);
            return other != null && other.missing();
        })) this.refresh();

        tile.data.entryIndex = playlist.indexOf(active);
        tile.data.url = active.url();
        tile.cleanDisplay();
        this.listSent = this.ticks;
        DisplayNetwork.sendServer(new DataListSyncPacket(tile.getBlockPos(), this.listNbt()));
        this.flush();
    }

    // A MEDIA GOES AWAY WHOLE: ITS EXPANDED SOURCES ARE PART OF IT, NOT ENTRIES OF THEIR OWN
    void remove(String token) {
        if (list == null) return;
        Entry entry = entryByToken.get(token);
        if (entry != null) this.removeEntry(entry);
    }

    private void removeEntry(Entry target) {
        this.playlist.removeIf(entry -> entry.sameMedia(target));
        if (selectedEntry != null && !playlist.contains(selectedEntry)) this.selectedEntry = null;
        if (active != null && !playlist.contains(active)) this.active = null;
        this.refresh();
        this.flush();
    }

    // THE RAW URL A ROW'S BAKED TOKEN POINTS AT, OR null; THE ConfirmLinkScreen GATE LIVES ON THE CALLER
    private String urlForToken(String token) {
        Entry entry = entryByToken.get(token);
        return entry != null ? entry.url() : null;
    }

    // DROP NOTIFICATION FROM THE LIST'S onListMove; THE LIST ALREADY MOVED THE BLOCKS, THE PLAYLIST MIRRORS
    void move(int origin, int target) {
        if (list == null) return;
        List<Entry> order = new ArrayList<>();
        for (Object key: list.order()) if (key instanceof Entry entry) order.add(entry);
        // DESYNC GUARD: MIRROR ONLY A TRUE PERMUTATION; OTHERWISE SNAP THE VIEW BACK TO THE DATA
        if (order.size() != playlist.size() || !order.containsAll(playlist)) {
            this.refresh();
            return;
        }
        this.playlist.clear();
        this.playlist.addAll(order);
        this.refresh();
        this.flush();
    }

    // A MEDIA RESOLVES INTO A GALLERY: EACH SOURCE BECOMES AN ENTRY BEHIND THE ONE THAT BROUGHT IT IN.
    // ONLY A MEDIA STILL ALONE ON ITS FIRST SOURCE IS EXPANDED; A SHAPED GROUP IS LEFT AS THE PLAYER SET IT
    private void expand() {
        // A MID-DRAG EXPANSION WOULD REBUILD THE ROWS AND BREAK THE CAPTURE CHAIN; IT WAITS THE DROP.
        // ONE PASS COUNTS EACH MEDIA'S ENTRIES SO THE PER-HEAD SCAN DOES NOT GO N^2 AT 20 Hz (M4)
        if (list.dragging()) return;
        Map<String, Integer> held = new HashMap<>();
        for (Entry entry: playlist) held.merge(entry.url(), 1, Integer::sum);
        boolean grown = false;
        for (int i = playlist.size() - 1; i >= 0; i--) {
            Entry entry = playlist.get(i);
            if (entry.source() != 0 || held.getOrDefault(entry.url(), 0) != 1) continue;

            SourceRow model = models.get(entry);
            int count = model == null ? 0 : model.sourceCount();
            for (int source = count - 1; source > 0; source--) {
                playlist.add(i + 1, new Entry(entry.url(), source, entry.owner()));
                grown = true;
            }
        }
        if (grown) {
            this.refresh();
            this.flush();
        }
    }

    // WHICH ENTRY STAYS ON AIR: IT FOLLOWS WHAT THE DISPLAY ALREADY PLAYS, SO REORDERING THE LIST
    // AROUND IT DOES NOT MAKE THE MEDIA JUMP TO ANOTHER ROW
    private int index() {
        if (playlist.isEmpty()) return 0;
        int picked = active != null ? playlist.indexOf(active) : -1;
        if (picked != -1) return picked;
        int playing = playlist.indexOf(tile.data.entry());
        return playing != -1 ? playing : Math.clamp(tile.data.entryIndex, 0, playlist.size() - 1);
    }

    private String url() {
        if (!playlist.isEmpty()) return playlist.get(this.index()).url();
        return tile.data.hasUrl() ? tile.data.getUrl() : "";
    }

    private boolean listChanged() {
        return !playlist.equals(tile.data.playlist) || this.index() != tile.data.entryIndex;
    }

    // THE TILE IS THE AUTHORITY: ITS LIST REPLACES THE WORKING COPY AND THE PICK THAT WAS NOT ACCEPTED IS DROPPED
    private void adopt() {
        this.playlist.clear();
        this.playlist.addAll(this.tile.data.playlist);
        this.active = null;
        this.listSent = this.ticks;
        this.refresh();
    }

    // THE PLAYLIST TRAVELS IN ITS OWN PACKET; THE SERVER REBUILDS THE LIST FROM THIS TAG
    private CompoundTag listNbt() {
        return DisplayData.listTag(playlist, this.index());
    }

    // ---- ROW BINDER (§10.6): PER-ROW BAKED VALUES AND THE STABLE MODEL -------------------

    @Override
    public Map<String, Object> baked(Object key) {
        Entry entry = (Entry) key;
        // OWNERSHIP AND EDITING BELONG TO THE MEDIA, NOT ITS SOURCES: ONLY THE FIRST SOURCE IS A DRAGGABLE HEAD
        boolean media = entry.source() == 0;
        Map<String, Object> row = new HashMap<>();
        row.put("key", this.token(entry));
        row.put("media", media);
        // DRAG-BLOCK TOKEN (§10.6): A MEDIA HEAD AND ITS EXPANDED SOURCES SHARE THE URL, SO THEY TRAVEL WHOLE
        row.put("group", entry.url());
        row.put("uuid", (entry.owner() != null ? entry.owner() : Util.NIL_UUID).toString());
        row.put("thumb_width", media ? THUMB_W : THUMB_SUB_W);
        row.put("thumb_height", media ? THUMB_H : THUMB_SUB_H);
        row.put("title_scale", media ? TITLE : TITLE_SUB);
        // A SUB-SOURCE IS INSET SO IT READS AS THE TAIL OF ITS MEDIA HEAD; A HEAD SITS FLUSH
        row.put("row_inset", media ? Spacing.ZERO : new Spacing(0, 0, 0, SUB_INSET));
        return row;
    }

    // STABLE PER-KEY MODEL: THE SAME INSTANCE FEEDS THE ROW'S LIVE SUPPLIERS FOR AS LONG AS THE KEY LIVES
    @Override
    public Object model(Object key) {
        return models.computeIfAbsent((Entry) key, e -> new SourceRow(tile, e));
    }

    // A COUNTER-BASED TOKEN, NEVER DERIVED FROM THE URL (DUPLICATE URLS EXIST); IT ROUTES A ROW EVENT BACK
    private String token(Entry entry) {
        return tokenByEntry.computeIfAbsent(entry, e -> {
            String token = "r" + (tokenSeq++);
            this.entryByToken.put(token, e);
            return token;
        });
    }

    // ---- LIFECYCLE -----------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        if (tile.isRemoved()) {
            this.close();
            return;
        }
        if (++this.ticks - this.listSent > 20 && this.listChanged() && (this.list == null || !this.list.dragging())) this.adopt();
        // THE PREVIEW MIRRORS THE BLOCK'S OWN PLAYER INSTEAD OF OPENING A SECOND SESSION, SO IT COSTS
        // NOTHING TO DECODE; A RELOAD OR A SOURCE SWAP BUILDS A NEW PLAYER AND THE WRAP FOLLOWS IT
        if (preview != null) {
            MediaPlayer player = tile.follower();
            if (player != previewOf) {
                this.previewOf = player;
                this.preview.wrap(player);
            }
        }
        // THE PLAYLIST PAGE IS THE ONLY ONE WITH TICK WORK; GATED SO ITS CACHES COST NOTHING ELSEWHERE
        if (parentTab != null && parentTab.active() == sourcesTab) this.sourcesTick();
    }

    // RUNS WHILE THE SOURCES PAGE SHOWS, AND ONCE WHEN IT APPEARS: URL CACHES FOR THE SUBMIT GATES,
    // THE GALLERY EXPANSION AND THE ON-AIR ROW FACE
    private void sourcesTick() {
        if (field == null || list == null) return;
        this.cachedLink = field.valid();
        String text = field.text().trim();
        this.cachedQuery = !text.isEmpty() && !text.contains("/");
        this.expand();
        this.list.playing(this.playingEntry());
    }

    // PLAYLIST FIRST (THE SERVER PICKS THE URL OUT OF IT), THEN THE DATA PATCH; ONLY KEYS DIFFERING
    // FROM THE TILE TRAVEL, AND A SECTION WHOSE WIDGETS RESOLVED NULL CONTRIBUTES NOTHING (H4)
    private void flush() {
        if (tile.isRemoved() || !DisplaysConfig.canSave(Minecraft.getInstance().player, this.url())) return;
        if (this.listChanged()) {
            this.listSent = this.ticks;
            DisplayNetwork.sendServer(new DataListSyncPacket(tile.getBlockPos(), this.listNbt()));
        }

        DisplayData.Patch patch = DisplayData.patch(tile);
        if (widthField != null && heightField != null && position != null) {
            patch.width((float) widthField.value(), position.x()).height((float) heightField.value(), position.y());
        }
        if (rotation != null) patch.rotation((float) rotation.value());
        if (flipX != null && flipY != null) patch.flipX(flipX.value()).flipY(flipY.value());
        if (alpha != null && brightness != null) patch.alpha(alpha.intValue()).brightness(brightness.intValue());
        if (renderDistance != null) patch.renderDistance(renderDistance.intValue());
        if (lit != null) patch.lit(lit.value());
        if (showModel != null) patch.visible(showModel.value());
        if (mirror != null) patch.bothSides(mirror.value());
        if (projectionDistance != null) patch.projection((float) projectionDistance.value());
        if (volume != null) patch.volume(volume.intValue());
        if (volumeMin != null && volumeMax != null) patch.volumeRange(volumeMin.intValue(), volumeMax.intValue());
        // CLAMPED: A PACK DECLARING EXTRA StateButton ICONS MUST NOT INDEX PAST THE REAL ENUM
        if (audioOffset != null) patch.audioPosition(AudioPosition.VALUES[Math.clamp(audioOffset.state(), 0, AudioPosition.VALUES.length - 1)]);
        // PUTTING NEW MEDIA ON AIR SWITCHES THE DISPLAY ON; A KNOB TURN ON A SWITCHED OFF ONE LEAVES IT OFF
        String url = this.url();
        String current = tile.data.getUrl();
        patch.url(url);
        if (!url.equals(current == null ? "" : current)) patch.active(true);
        if (!patch.empty()) DisplayNetwork.sendServer(new DataSyncPacket(tile.getBlockPos(), patch.nbt()));
    }

    @Override
    protected void closed() {
        // THE ROW PREVIEW PLAYERS ARE RELEASED BY THE TREE DISPOSE CASCADE (ParentList OWNS ITS ROW STORE, §10.6);
        // THE STORE BELT ONLY COVERS ONE ORPHANED BY A BROKEN RE-INIT, A SECOND DISPOSE IS A NO-OP
        this.flush();
        this.rowStore.dispose();
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifiers) {
        if (super.keyPressed(key, scan, modifiers)) return true;
        // ENTER COMMITS PENDING EDITS, BUT ONLY WHEN NO DIALOG OWNS THE SCREEN AND NOTHING ELSE TOOK IT
        if (topDialog() == null && (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)) {
            this.flush();
            return true;
        }
        return false;
    }
}
