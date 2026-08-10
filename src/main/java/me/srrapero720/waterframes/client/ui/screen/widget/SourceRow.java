package me.srrapero720.waterframes.client.ui.screen.widget;

import me.srrapero720.waterframes.client.ui.Icons;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterui.format.UIVar;
import me.srrapero720.waterui.theme.Icon;
import me.srrapero720.waterui.widget.ValueFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.watermedia.api.media.MRL;
import org.watermedia.api.media.MediaAPI;
import org.watermedia.api.util.Metadata;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Model behind one templated playlist row: resolves the entry's media and feeds the row template's
 * live {@link UIVar} suppliers (title, byline, duration, thumbnail). No widgets here; the transport
 * badge shares {@link #statusIcon}/{@link #describe}.
 */
public final class SourceRow {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    public final DisplayData.Entry entry;
    private final DisplayTile tile;

    /** Resolved at construction; the fetch itself is asynchronous inside the media api. */
    private MRL mrl;

    // COMPOSED TEXT MEMO (M5): THE SUPPLIERS POLL PER TICK, SO THE STRINGS REBUILD ONLY WHEN THE
    // SOURCE OR METADATA REFERENCE CHANGES — watermedia HANDS OUT FRESH OBJECTS, NEVER MUTATES IN PLACE
    private MRL.Source composedSource;
    private Metadata composedFrom;
    private boolean composed;
    private String title;
    private String byline;
    private String duration;
    private String thumbnail;

    public SourceRow(DisplayTile tile, DisplayData.Entry entry) {
        this.tile = tile;
        this.entry = entry;
        this.resolve();
    }

    private void resolve() {
        try {
            this.mrl = MediaAPI.mrl(entry.url());
        } catch (Exception ignored) {
            // A LINE THAT IS NOT A URI AT ALL; THE ROW JUST STAYS IDLE
        }
    }

    // THE ACTIVE MEDIA MIRRORS THE REAL PIPELINE: THE MRL WHILE IT RESOLVES, THE PLAYER AFTERWARDS.
    // FOR A TICK AFTER A PICK THE TILE STILL POINTS AT THE PREVIOUS ONE, WHICH WOULD FLASH ITS TITLE HERE
    private MRL media() {
        if (this.playing() && tile.mrl != null && tile.mrl.uri.toString().equals(entry.url())) return tile.mrl;
        return this.mrl;
    }

    private boolean playing() {
        return tile.data.hasUrl() && entry.url().equals(tile.data.getUrl()) && entry.source() == tile.data.getSource();
    }

    /** How many sources the media turned out to hold, or 0 while nobody knows yet. */
    public int sourceCount() {
        MRL media = this.media();
        return media != null && media.status().loaded() ? media.sourceCount() : 0;
    }

    /** A stored entry whose source left the media: the row stays as the mark until a working one is picked. */
    public boolean missing() {
        int count = this.sourceCount();
        return count > 0 && entry.source() >= count;
    }

    // THE SOURCE OF THIS ENTRY ONCE THE MEDIA HAS LOADED, ELSE null; SHARED GUARD FOR EVERY LIVE READER
    private MRL.Source source() {
        MRL media = this.media();
        if (media == null || !media.status().loaded()) return null;
        return media.source(entry.source());
    }

    // ---- LIVE BINDINGS: POLLED PER TICK BY THE ROW TEMPLATE (§5.5), MEMOIZED PER METADATA (M5) ----

    private void compose() {
        MRL.Source source = this.source();
        Metadata data = source != null ? source.metadata() : null;
        if (composed && data == composedFrom && source == composedSource) return;
        this.composed = true;
        this.composedSource = source;
        this.composedFrom = data;

        this.title = data != null && data.title() != null && !data.title().isBlank() ? data.title() : shorten(entry.url());
        String author = data != null && data.author() != null && !data.author().isBlank() ? data.author() : unknown();
        String date = data != null && data.postedAt() != null ? DATE.format(data.postedAt()) : null;
        // COMPOSED "<AUTHOR> (<DATE>)"; THE DATE JOINS ONLY ONCE THE MEDIA REPORTS IT
        this.byline = date == null ? author : author + " (" + date + ")";
        this.duration = data != null && data.duration() > 0 ? ValueFormat.timestamp(data.duration()) : "??:??";
        this.thumbnail = source != null && source.thumbnail() != null ? source.thumbnail().toString() : "";
    }

    @UIVar("title")
    public String title() {
        this.compose();
        return title;
    }

    @UIVar("byline")
    public String byline() {
        this.compose();
        return byline;
    }

    @UIVar("duration")
    public String duration() {
        this.compose();
        return duration;
    }

    @UIVar("thumbnail")
    public String thumbnail() {
        this.compose();
        return thumbnail;
    }

    private static String unknown() {
        return translate("waterframes.gui.source.unknown");
    }

    private static String shorten(String url) {
        int scheme = url.indexOf("://");
        return scheme > 0 && scheme < url.length() - 3 ? url.substring(scheme + 3) : url;
    }

    // THE ACTIVE MEDIA MIRRORS THE REAL PIPELINE: THE MRL WHILE IT RESOLVES, THE PLAYER AFTERWARDS.
    // SHARED WITH THE TRANSPORT BAND, WHICH REPORTS THE TILE'S CURRENT SOURCE (§13.2)
    public static Icon statusIcon(MRL media, DisplayTile tile, boolean missing) {
        if (media == null) return Icons.STATUS_IDLE;
        if (media.status().failed()) return Icons.STATUS_ERROR;
        if (!media.status().loaded()) return Icons.STATUS_LOADING;
        if (missing) return Icons.STATUS_ERROR;
        if (tile.display == null) return Icons.STATUS_LOADING;
        if (tile.display.isNoEngine()) return Icons.STATUS_INTERNAL_ERROR;
        return switch (tile.display.status()) {
            case WAITING, LOADING -> Icons.STATUS_LOADING;
            case BUFFERING -> Icons.STATUS_BUFFERING;
            case PLAYING -> Icons.STATUS_OK;
            case PAUSED -> Icons.STATUS_IDLE;
            case STOPPED, ENDED -> Icons.STATUS_OFF;
            case ERROR -> Icons.STATUS_ERROR;
        };
    }

    public static List<Component> describe(MRL media, DisplayTile tile, boolean missing) {
        if (media == null) return List.of(state(ChatFormatting.AQUA, "waterframes.status.idle"));
        if (media.status().failed()) return List.of(state(ChatFormatting.RED, "waterframes.status.failed"));
        if (!media.status().loaded()) return List.of(state(ChatFormatting.YELLOW, "waterframes.status.loading"));
        if (missing) {
            return List.of(state(ChatFormatting.RED, "waterframes.status.missing"),
                    translatable("waterframes.status.missing.desc"));
        }
        if (tile.display == null) return List.of(state(ChatFormatting.YELLOW, "waterframes.status.loading"));
        if (tile.display.isNoEngine()) {
            return List.of(state(ChatFormatting.RED, "waterframes.status.no_engine"),
                    translatable("waterframes.status.no_engine.desc"));
        }
        return List.of(switch (tile.display.status()) {
            case WAITING, LOADING -> state(ChatFormatting.YELLOW, "waterframes.status.loading");
            case BUFFERING -> state(ChatFormatting.YELLOW, "waterframes.status.buffering");
            case PLAYING -> state(ChatFormatting.GREEN, "waterframes.status.operative");
            case PAUSED -> state(ChatFormatting.AQUA, "waterframes.common.paused");
            case STOPPED, ENDED -> state(ChatFormatting.GRAY, "waterframes.status.off");
            case ERROR -> state(ChatFormatting.RED, "waterframes.status.failed");
        });
    }

    private static Component state(ChatFormatting color, String key) {
        return translatable("waterframes.status", color + translate(key));
    }

    private static Component translatable(String key, Object... args) {
        return Component.translatable(key, args);
    }

    private static String translate(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }
}
