package me.srrapero720.waterframes.common.block.data;

import me.srrapero720.waterframes.DisplaysConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.DisplayBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import me.srrapero720.waterframes.common.block.data.types.AudioPosition;
import me.srrapero720.waterframes.common.block.data.types.PositionHorizontal;
import me.srrapero720.waterframes.common.block.data.types.PositionVertical;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DisplayData {
    // NBT Keys - kept as uri_* for backwards compatibility (NBT always stored strings)
    public static final String URL = "url";
    public static final String URL_LIST = "uri_list";
    public static final String URL_INDEX = "uri_index";
    public static final String PLAYER_UUID = "player_uuid";
    public static final String ACTIVE = "active";
    public static final String MIN_X = "min_x";
    public static final String MIN_Y = "min_y";
    public static final String MAX_X = "max_x";
    public static final String MAX_Y = "max_y";

    // SCREEN SYNC KEYS: THE PATCH TRAVELS AS WIDTH/HEIGHT PLUS ANCHOR, NOT AS MIN/MAX COORDS
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String POS_X = "pos_x";
    public static final String POS_Y = "pos_y";
    public static final String VISIBLE = "visible";

    public static final String FLIP_X = "flip_x";
    public static final String FLIP_Y = "flip_y";

    public static final String ROTATION = "rotation";
    public static final String ALPHA = "alpha";
    public static final String BRIGHTNESS = "brightness";
    public static final String RENDER_DISTANCE = "render_distance";

    public static final String VOLUME = "volume";
    public static final String VOL_RANGE_MIN= "volume_min_range";
    public static final String VOL_RANGE_MAX = "volume_max_range";
    public static final String LOOP = "loop";
    public static final String PAUSED = "paused";
    public static final String REDSTONE_PAUSED = "redstone_paused";
    public static final String MUTED = "muted";
    public static final String LIT = "lit";
    public static final String DATA_V = "data_v";

    // FRAME KEYS
    public static final String RENDER_BOTH_SIDES = "render_both";

    // PROJECTOR
    public static final String PROJECTION_DISTANCE = "projection_distance";
    public static final String AUDIO_OFFSET = "audio_offset";

    public static final short V = 2;

    // A PLAYLIST FROM THE WIRE IS CAPPED BEFORE IT IS PARSED: A HUGE ONE WOULD STALL THE SERVER TICK ON WHITELIST CHECKS
    public static final int MAX_PLAYLIST = 128;
    public static final int MAX_PLAYLIST_BYTES = 64 * 1_024;

    /**
     * One playlist line. A media is a gallery of sources and every one of them stands on its
     * own here, so a url with three videos behind it fills three entries.
     *
     * @param url the media, as typed
     * @param source which source of that media, always 0 for media that hold a single one
     * @param owner who put it in the list, null when nobody knows
     */
    public record Entry(String url, int source, UUID owner) {
        /** Two entries are the same media when the url matches, whoever put them there. */
        public boolean sameMedia(Entry other) {
            return this.url.equals(other.url);
        }

        /** And the very same entry when they point at the same source of that media. */
        public boolean same(Entry other) {
            return this.source == other.source && this.sameMedia(other);
        }
    }

    // Media URLs - now stored as plain strings for direct MRL compatibility
    public String url = null;
    public List<Entry> playlist = new ArrayList<>();
    public int entryIndex = 0;

    public UUID uuid = Util.NIL_UUID;
    public boolean active = true;
    public Vector2f min = new Vector2f(0F, 0F);
    public Vector2f max = new Vector2f(1F, 1F);

    public boolean flipX = false;
    public boolean flipY = false;

    public float rotation = 0;
    public int alpha = 255;
    public int brightness = 255;
    public int renderDistance = DisplaysConfig.maxRenDis(32);

    public int volume = DisplaysConfig.maxVol();
    public int maxVolumeDistance = DisplaysConfig.maxVolDis(20);
    public int minVolumeDistance = Math.min(5, maxVolumeDistance);

    public boolean loop = true;
    public boolean paused = false;
    // WHETHER THE CURRENT PAUSE CAME FROM A REDSTONE SIGNAL, SO ITS FALLING EDGE KNOWS WHAT TO UNDO
    public boolean redstonePaused = false;
    public boolean muted = false;
    public boolean lit = true;

    public boolean renderBothSides = false;

    // PROJECTOR VALUES
    public float projectionDistance = DisplaysConfig.maxProjDis(8f);
    public float audioOffset = 0;

    /**
     * Advances to the next entry in the playlist.
     * @return true if advanced, false if playlist is empty
     */
    public boolean nextUrl() {
        if (this.playlist.isEmpty()) return false;
        this.entryIndex++;
        if (this.entryIndex >= this.playlist.size()) {
            this.entryIndex = 0;
        }
        this.url = this.entry().url();
        return true;
    }

    /**
     * Goes back to the previous entry in the playlist.
     * @return true if rewound, false if playlist is empty
     */
    public boolean prevUrl() {
        if (this.playlist.isEmpty()) return false;
        this.entryIndex--;
        if (this.entryIndex < 0) {
            this.entryIndex = this.playlist.size() - 1;
        }
        this.url = this.entry().url();
        return true;
    }

    /**
     * Checks if any URL is configured (single or playlist).
     */
    public boolean hasUrl() {
        return (this.url != null && !this.url.isEmpty()) || !this.playlist.isEmpty();
    }

    /** The entry on air, or null when there is no playlist and the single url is all there is. */
    public Entry entry() {
        if (this.playlist.isEmpty()) return null;
        return this.playlist.get(Math.clamp(this.entryIndex, 0, this.playlist.size() - 1));
    }

    /**
     * Gets the current active URL.
     * If using playlist, returns the URL of the current entry.
     * @return the current URL string, or null if none
     */
    public String getUrl() {
        Entry entry = this.entry();
        return entry != null ? entry.url() : this.url;
    }

    /** Which source of the media is on air; media that hold a single one always answer 0. */
    public int getSource() {
        Entry entry = this.entry();
        return entry != null ? entry.source() : 0;
    }

    /**
     * Sets the current URL (clears playlist mode).
     * @param url the URL string to set
     */
    public void setUrl(String url) {
        this.url = (url != null && !url.isEmpty()) ? url : null;
        this.playlist.clear();
        this.entryIndex = 0;
    }

    /**
     * Composes a playlist for NBT storage, one entry per line. A line is {@code url},
     * {@code url<TAB>owner} or {@code url<TAB>owner<TAB>source}, so a list written before
     * owners or sources existed still reads back the same way.
     */
    public static String composePlaylist(List<Entry> playlist) {
        StringBuilder out = new StringBuilder();
        for (Entry entry: playlist) {
            if (!out.isEmpty()) out.append('\n');
            out.append(entry.url());

            boolean owned = entry.owner() != null && !entry.owner().equals(Util.NIL_UUID);
            // THE OWNER COLUMN HOLDS THE PLACE OF THE SOURCE ONE, SO IT IS WRITTEN EVEN WHEN EMPTY
            if (owned || entry.source() != 0) out.append('\t').append(owned ? entry.owner() : "");
            if (entry.source() != 0) out.append('\t').append(entry.source());
        }
        return out.toString();
    }

    /** Reads back what {@link #composePlaylist} wrote, tolerating every older shape of a line. */
    public static List<Entry> decomposePlaylist(String str) {
        List<Entry> playlist = new ArrayList<>();
        if (str == null || str.isEmpty()) return playlist;

        Set<String> seen = new HashSet<>();
        for (String line: str.split("\n")) {
            String[] column = line.trim().split("\t", 3);
            String url = column[0].trim();
            if (url.isEmpty()) continue;

            UUID owner = null;
            int source = 0;
            try {
                if (column.length > 1 && !column[1].isBlank()) owner = UUID.fromString(column[1].trim());
                if (column.length > 2) source = Math.max(0, Integer.parseInt(column[2].trim()));
            } catch (IllegalArgumentException ignored) {
                // A LINE CARRYING SOMETHING ELSE IN ITS COLUMNS; THE URL STILL COUNTS
            }

            // AN ENTRY IS A SOURCE OF A MEDIA AND THE LIST INDEXES IT; THE SAME ONE TWICE IS A LIE
            if (seen.add(url + '#' + source)) playlist.add(new Entry(url, source, owner));
        }
        return playlist;
    }

    public PositionHorizontal getPosX() { return this.min.x == 0 ? PositionHorizontal.LEFT : this.max.x == 1 ? PositionHorizontal.RIGHT : PositionHorizontal.CENTER; }
    public PositionVertical getPosY() { return this.min.y == 0 ? PositionVertical.TOP : this.max.y == 1 ? PositionVertical.BOTTOM : PositionVertical.CENTER; }
    public float getWidth() { return this.max.x - this.min.x; }
    public float getHeight() { return this.max.y - this.min.y; }

    public void save(CompoundTag nbt, DisplayTile tile) {
        // Save URL as string directly
        nbt.putString(URL, this.url != null ? this.url : "");

        // Playlist: save as newline-delimited string
        nbt.putString(URL_LIST, composePlaylist(this.playlist));
        nbt.putInt(URL_INDEX, entryIndex);

        nbt.putUUID(PLAYER_UUID, uuid);
        nbt.putBoolean(ACTIVE, active);
        if (tile.caps.resizes()) {
            nbt.putFloat(MIN_X, min.x);
            nbt.putFloat(MIN_Y, min.y);
            nbt.putFloat(MAX_X, max.x);
            nbt.putFloat(MAX_Y, max.y);
            nbt.putFloat(ROTATION, rotation);
        }
        nbt.putInt(RENDER_DISTANCE, renderDistance);
        nbt.putBoolean(FLIP_X, flipX);
        nbt.putBoolean(FLIP_Y, flipY);
        nbt.putInt(ALPHA, alpha);
        nbt.putInt(BRIGHTNESS, brightness);
        nbt.putInt(VOLUME, volume);
        nbt.putInt(VOL_RANGE_MIN, minVolumeDistance);
        nbt.putInt(VOL_RANGE_MAX, maxVolumeDistance);
        nbt.putBoolean(PAUSED, paused);
        nbt.putBoolean(REDSTONE_PAUSED, redstonePaused);
        nbt.putBoolean(MUTED, muted);
        nbt.putBoolean(LIT, lit);
        nbt.putBoolean(LOOP, loop);

        if (tile.caps.renderBehind()) {
            nbt.putBoolean(RENDER_BOTH_SIDES, renderBothSides);
        }

        if (tile.caps.projects()) {
            nbt.putFloat(PROJECTION_DISTANCE, projectionDistance);
            nbt.putFloat(AUDIO_OFFSET, audioOffset);
        }

        nbt.putShort(DATA_V, V);
    }

    public void load(CompoundTag nbt, DisplayTile tile) {
        short version = nbt.getShort(DATA_V);

        // Load URL (NBT always stored as string)
        String loadedUrl = nbt.getString(URL);
        this.url = loadedUrl.isEmpty() ? null : loadedUrl;

        // Load playlist
        this.playlist = decomposePlaylist(nbt.getString(URL_LIST));
        this.entryIndex = nbt.getInt(URL_INDEX);

        // Ensure index is valid
        if (!this.playlist.isEmpty() && this.entryIndex >= this.playlist.size()) {
            this.entryIndex = 0;
        }

        this.uuid = nbt.contains(PLAYER_UUID) ? nbt.getUUID(PLAYER_UUID) : this.uuid;
        this.active = nbt.contains(ACTIVE) ? nbt.getBoolean(ACTIVE) : this.active;
        if (tile.caps.resizes()) {
            this.min.x = nbt.getFloat(MIN_X);
            this.min.y = nbt.getFloat(MIN_Y);
            this.max.x = nbt.getFloat(MAX_X);
            this.max.y = nbt.getFloat(MAX_Y);
            this.rotation = nbt.getFloat(ROTATION);
        }
        this.renderDistance = DisplaysConfig.maxRenDis(nbt.getInt(RENDER_DISTANCE));
        this.flipX = nbt.getBoolean(FLIP_X);
        this.flipY = nbt.getBoolean(FLIP_Y);
        this.alpha = nbt.contains(ALPHA) ? nbt.getInt(ALPHA) : this.alpha;
        this.brightness = nbt.contains(BRIGHTNESS) ? nbt.getInt(BRIGHTNESS) : this.brightness;
        this.volume = nbt.contains(VOLUME) ? DisplaysConfig.maxVol(nbt.getInt(VOLUME)) : this.volume;
        this.maxVolumeDistance = nbt.contains(VOL_RANGE_MAX) ? DisplaysConfig.maxVolDis(nbt.getInt(VOL_RANGE_MAX)) : this.maxVolumeDistance;
        this.minVolumeDistance = nbt.contains(VOL_RANGE_MIN) ? Math.min(nbt.getInt(VOL_RANGE_MIN), this.maxVolumeDistance) : this.minVolumeDistance;
        this.paused = nbt.getBoolean(PAUSED);
        this.redstonePaused = nbt.getBoolean(REDSTONE_PAUSED);
        this.muted = nbt.getBoolean(MUTED);
        this.lit = !nbt.contains(LIT) || nbt.getBoolean(LIT);
        this.loop = nbt.getBoolean(LOOP);

        if (tile.caps.renderBehind()) {
            this.renderBothSides = nbt.getBoolean(RENDER_BOTH_SIDES);
        }

        if (tile.caps.projects()) {
            this.projectionDistance = nbt.contains(PROJECTION_DISTANCE) ? DisplaysConfig.maxProjDis(nbt.getFloat(PROJECTION_DISTANCE)) : this.projectionDistance;
            this.audioOffset = nbt.contains(AUDIO_OFFSET) ? nbt.getFloat(AUDIO_OFFSET) : this.audioOffset;
        }

        // Handle legacy data versions
        switch (version) {
            case 1 -> {
                this.alpha = (int) (nbt.getFloat(ALPHA) * 255);
                this.brightness = (int) (nbt.getFloat(BRIGHTNESS) * 255);
            }
            case 0 -> { // Very old format
                if (!nbt.contains("maxx")) return;
                this.min.x = nbt.getFloat("minx");
                this.min.y = nbt.getFloat("miny");
                this.max.x = nbt.getFloat("maxx");
                this.max.y = nbt.getFloat("maxy");

                this.flipX = nbt.getBoolean("flipX");
                this.flipY = nbt.getBoolean("flipY");

                this.maxVolumeDistance = DisplaysConfig.maxVolDis((int) nbt.getFloat("max"));
                this.minVolumeDistance = Math.min((int) nbt.getFloat("min"), maxVolumeDistance);

                this.renderDistance = nbt.getInt("render");

                if (tile.canHideModel()) {
                    tile.setVisibility(nbt.getBoolean("visibleFrame"));
                }

                if (tile.caps.renderBehind()) {
                    this.renderBothSides = nbt.getBoolean("bothSides");
                }
            }
        }

        this.restrictWidth();
        this.restrictHeight();
    }

    public void setAudioPosition(AudioPosition position) {
        this.audioOffset = switch (position) {
            case BLOCK -> 0f;
            case PROJECTION -> projectionDistance;
            case CENTER -> projectionDistance / 2f;
        };
    }

    public AudioPosition getAudioPosition() {
        return audioOffset == 0 ? AudioPosition.BLOCK : audioOffset == projectionDistance ? AudioPosition.PROJECTION : AudioPosition.CENTER;
    }

    public void setWidth(final float width) { this.setWidth(this.getPosX(), width); }
    public void setWidth(final PositionHorizontal position, final float width) {
        switch (position) {
            case LEFT -> {
                this.min.x = 0;
                this.max.x = width;
            }
            case RIGHT -> {
                this.min.x = 1 - width;
                this.max.x = 1;
            }
            default -> {
                float middle = width / 2;
                this.min.x = 0.5F - middle;
                this.max.x = 0.5F + middle;
            }
        }
    }

    public void setHeight(final float height) { this.setHeight(this.getPosY(), height); }
    public void setHeight(final PositionVertical position, final float height) {
        switch (position) {
            case TOP -> {
                this.min.y = 0;
                this.max.y = height;
            }
            case BOTTOM -> {
                this.min.y = 1 - height;
                this.max.y = 1;
            }
            default -> {
                float middle = height / 2;
                this.min.y = 0.5F - middle;
                this.max.y = 0.5F + middle;
            }
        }
    }

    private void restrictWidth() {
        float maxWidth = DisplaysConfig.maxWidth();
        if (getWidth() > maxWidth) {
            switch (getPosX()) {
                case LEFT -> {
                    this.min.x = 0;
                    this.max.x = maxWidth;
                }
                case RIGHT -> {
                    this.min.x = 1 - maxWidth;
                    this.max.x = 1;
                }
                default -> {
                    float middle = maxWidth / 2f;
                    this.min.x = 0.5F - middle;
                    this.max.x = 0.5F + middle;
                }
            }
        }
    }

    private void restrictHeight() {
        float maxHeight = DisplaysConfig.maxHeight();
        if (getHeight() > maxHeight) {
            switch (getPosY()) {
                case TOP -> {
                    this.min.y = 0f;
                    this.max.y = maxHeight;
                }
                case BOTTOM -> {
                    this.min.y = 1f - maxHeight;
                    this.max.y = 1f;
                }
                default -> {
                    float middle = maxHeight / 2f;
                    this.min.y = 0.5F - middle;
                    this.max.y = 0.5F + middle;
                }
            }
        }
    }

    /**
     * Replaces the playlist whole. Every line must pass the same gate a single url does and the list must fit
     * {@link #MAX_PLAYLIST}, or nothing is written; owners are never taken from the wire, an entry keeps the owner
     * the display already knew for it and a new one is stamped with the sender.
     */
    public static void syncList(final DisplayTile tile, final Player player, final CompoundTag tag) {
        final String lines = tag.getString(URL_LIST);
        final List<Entry> received = lines.length() > MAX_PLAYLIST_BYTES ? null : decomposePlaylist(lines);
        if (received == null || received.size() > MAX_PLAYLIST) {
            reject(tile, player, "");
            return;
        }
        String rejected = received.isEmpty() && !DisplaysConfig.canSave(player, "") ? "" : null;
        for (final Entry entry: received) {
            if (rejected == null && !DisplaysConfig.canSave(player, entry.url())) rejected = entry.url();
        }
        if (rejected != null) {
            reject(tile, player, rejected);
            return;
        }

        final Map<String, UUID> known = new HashMap<>();
        for (final Entry entry: tile.data.playlist) known.put(entry.url() + '#' + entry.source(), entry.owner());
        final List<Entry> playlist = new ArrayList<>(received.size());
        for (final Entry entry: received) {
            final UUID owner = known.containsKey(entry.url() + '#' + entry.source()) ? known.get(entry.url() + '#' + entry.source()) : player.getUUID();
            playlist.add(new Entry(entry.url(), entry.source(), owner));
        }

        tile.data.playlist = playlist;
        tile.data.entryIndex = Math.clamp(tag.getInt(URL_INDEX), 0, Math.max(0, playlist.size() - 1));
        final Entry entry = tile.data.entry();
        tile.data.url = entry != null ? entry.url() : null;
        tile.data.uuid = entry == null ? Util.NIL_UUID : entry.owner() != null ? entry.owner() : player.getUUID();
        tile.setDirty();
    }

    /** Playlist tag for {@code DataListSyncPacket}; the list always travels whole. */
    public static CompoundTag listTag(List<Entry> playlist, int index) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString(URL_LIST, composePlaylist(playlist));
        nbt.putInt(URL_INDEX, index);
        return nbt;
    }

    /**
     * Applies a screen patch, touching only the keys the tag carries. Every value keeps its
     * config clamp, and fields gated by a capability the tile lacks are ignored outright.
     */
    public static void sync(DisplayTile tile, Player player, CompoundTag nbt) {
        DisplayData data = tile.data;
        // THE PERMISSION GATE CHECKS THE INCOMING URL, OR THE ONE ON AIR WHEN THE PATCH HAS NONE
        String gate = nbt.contains(URL) ? nbt.getString(URL) : data.getUrl();
        if (!DisplaysConfig.canSave(player, gate == null ? "" : gate)) {
            reject(tile, player, gate == null ? "" : gate);
            return;
        }

        if (nbt.contains(URL)) {
            String url = nbt.getString(URL);
            data.url = url.isEmpty() ? null : url;
            data.uuid = data.hasUrl() ? player.getUUID() : Util.NIL_UUID;
        }
        if (nbt.contains(ACTIVE)) data.active = nbt.getBoolean(ACTIVE);

        if (tile.caps.resizes()) {
            // SIZE AND ANCHOR TRAVEL APART; THE MISSING HALF IS READ BACK FROM THE DATA
            if (nbt.contains(WIDTH) || nbt.contains(POS_X)) {
                float width = nbt.contains(WIDTH) ? DisplaysConfig.maxWidth(nbt.getFloat(WIDTH)) : data.getWidth();
                data.setWidth(nbt.contains(POS_X) ? enumOf(PositionHorizontal.VALUES, nbt.getInt(POS_X)) : data.getPosX(), width);
            }
            if (nbt.contains(HEIGHT) || nbt.contains(POS_Y)) {
                float height = nbt.contains(HEIGHT) ? DisplaysConfig.maxHeight(nbt.getFloat(HEIGHT)) : data.getHeight();
                data.setHeight(nbt.contains(POS_Y) ? enumOf(PositionVertical.VALUES, nbt.getInt(POS_Y)) : data.getPosY(), height);
            }
            if (nbt.contains(ROTATION) && Float.isFinite(nbt.getFloat(ROTATION))) data.rotation = nbt.getFloat(ROTATION);
        }

        if (nbt.contains(FLIP_X)) data.flipX = nbt.getBoolean(FLIP_X);
        if (nbt.contains(FLIP_Y)) data.flipY = nbt.getBoolean(FLIP_Y);
        if (nbt.contains(ALPHA)) data.alpha = Math.clamp(nbt.getInt(ALPHA), 0, 255);
        if (nbt.contains(BRIGHTNESS)) data.brightness = Math.clamp(nbt.getInt(BRIGHTNESS), 0, 255);
        if (nbt.contains(RENDER_DISTANCE)) data.renderDistance = DisplaysConfig.maxRenDis(nbt.getInt(RENDER_DISTANCE));

        if (nbt.contains(VOLUME)) data.volume = DisplaysConfig.maxVol(nbt.getInt(VOLUME));
        if (nbt.contains(VOL_RANGE_MAX) || nbt.contains(VOL_RANGE_MIN)) {
            if (nbt.contains(VOL_RANGE_MAX)) data.maxVolumeDistance = DisplaysConfig.maxVolDis(nbt.getInt(VOL_RANGE_MAX));
            if (nbt.contains(VOL_RANGE_MIN)) data.minVolumeDistance = nbt.getInt(VOL_RANGE_MIN);
            data.minVolumeDistance = Math.clamp(data.minVolumeDistance, 0, Math.max(0, data.maxVolumeDistance));
        }

        if (nbt.contains(VISIBLE) && tile.canHideModel()) tile.setVisibility(nbt.getBoolean(VISIBLE));
        if (nbt.contains(LIT)) data.lit = nbt.getBoolean(LIT);
        if (nbt.contains(RENDER_BOTH_SIDES) && tile.caps.renderBehind()) data.renderBothSides = nbt.getBoolean(RENDER_BOTH_SIDES);

        if (tile.caps.projects()) {
            if (nbt.contains(PROJECTION_DISTANCE)) data.projectionDistance = DisplaysConfig.maxProjDis(nbt.getFloat(PROJECTION_DISTANCE));
            // AFTER PROJECTION: THE OFFSET IS DERIVED FROM WHATEVER DISTANCE ENDED UP IN FORCE
            if (nbt.contains(AUDIO_OFFSET)) data.setAudioPosition(enumOf(AudioPosition.VALUES, nbt.getInt(AUDIO_OFFSET)));
        }

        tile.setDirty();
    }

    // A REFUSED WRITE IS ANSWERED, NOT SWALLOWED: THE SENDER HEARS WHY AND GETS THE AUTHORITATIVE DATA
    // BACK, SO ITS SCREEN SNAPS OUT OF THE OPTIMISTIC STATE IT SHOWED WHILE THE PACKET TRAVELLED
    private static void reject(final DisplayTile tile, final Player player, final String url) {
        final String key = url.isEmpty() || WaterFrames.isValidUrl(url) ? "waterframes.gui.url.tooltip.not_whitelisted" : "waterframes.gui.url.tooltip.invalid_url";
        player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.RED), true);
        tile.setDirty();
    }

    // A HOSTILE PACKET CAN CARRY ANY ORDINAL, SO EVERY ENUM READ GOES THROUGH THIS CLAMP
    private static <T> T enumOf(T[] values, int ordinal) {
        return values[Math.clamp(ordinal, 0, values.length - 1)];
    }

    public static Patch patch(DisplayTile tile) {
        return new Patch(tile);
    }

    /**
     * Partial save builder for the settings screen. Every setter compares against what the
     * display already holds and only writes the keys that differ, so an idle screen produces
     * an empty patch and a knob turn produces a tag of one or two keys.
     * <p>
     * Setters gated by a capability the tile lacks are no-ops, mirroring {@code sync}.
     */
    public static final class Patch {
        // FLOATS COME BACK FROM SIZE MATH WITH DRIFT; ANYTHING CLOSER THAN THIS IS THE SAME VALUE
        private static final float EPSILON = 1e-4f;

        private final DisplayTile tile;
        private final CompoundTag nbt = new CompoundTag();

        private Patch(DisplayTile tile) {
            this.tile = tile;
        }

        public Patch url(String url) {
            String current = tile.data.getUrl();
            if (!url.equals(current == null ? "" : current)) nbt.putString(URL, url);
            return this;
        }

        public Patch active(boolean active) {
            if (active != tile.data.active) nbt.putBoolean(ACTIVE, active);
            return this;
        }

        public Patch width(float width, PositionHorizontal position) {
            if (!tile.caps.resizes()) return this;
            if (Math.abs(width - tile.data.getWidth()) > EPSILON) nbt.putFloat(WIDTH, width);
            if (position != tile.data.getPosX()) nbt.putInt(POS_X, position.ordinal());
            return this;
        }

        public Patch height(float height, PositionVertical position) {
            if (!tile.caps.resizes()) return this;
            if (Math.abs(height - tile.data.getHeight()) > EPSILON) nbt.putFloat(HEIGHT, height);
            if (position != tile.data.getPosY()) nbt.putInt(POS_Y, position.ordinal());
            return this;
        }

        public Patch rotation(float rotation) {
            if (tile.caps.resizes() && Math.abs(rotation - tile.data.rotation) > EPSILON) nbt.putFloat(ROTATION, rotation);
            return this;
        }

        public Patch flipX(boolean flip) {
            if (flip != tile.data.flipX) nbt.putBoolean(FLIP_X, flip);
            return this;
        }

        public Patch flipY(boolean flip) {
            if (flip != tile.data.flipY) nbt.putBoolean(FLIP_Y, flip);
            return this;
        }

        public Patch alpha(int alpha) {
            if (alpha != tile.data.alpha) nbt.putInt(ALPHA, alpha);
            return this;
        }

        public Patch brightness(int brightness) {
            if (brightness != tile.data.brightness) nbt.putInt(BRIGHTNESS, brightness);
            return this;
        }

        public Patch renderDistance(int distance) {
            if (distance != tile.data.renderDistance) nbt.putInt(RENDER_DISTANCE, distance);
            return this;
        }

        public Patch volume(int volume) {
            if (volume != tile.data.volume) nbt.putInt(VOLUME, volume);
            return this;
        }

        public Patch volumeRange(int min, int max) {
            if (min != tile.data.minVolumeDistance) nbt.putInt(VOL_RANGE_MIN, min);
            if (max != tile.data.maxVolumeDistance) nbt.putInt(VOL_RANGE_MAX, max);
            return this;
        }

        public Patch lit(boolean lit) {
            if (lit != tile.data.lit) nbt.putBoolean(LIT, lit);
            return this;
        }

        public Patch visible(boolean visible) {
            if (tile.canHideModel() && visible != tile.isVisible()) nbt.putBoolean(VISIBLE, visible);
            return this;
        }

        public Patch bothSides(boolean both) {
            if (tile.caps.renderBehind() && both != tile.data.renderBothSides) nbt.putBoolean(RENDER_BOTH_SIDES, both);
            return this;
        }

        public Patch projection(float distance) {
            if (tile.caps.projects() && Math.abs(distance - tile.data.projectionDistance) > EPSILON) nbt.putFloat(PROJECTION_DISTANCE, distance);
            return this;
        }

        public Patch audioPosition(AudioPosition position) {
            if (tile.caps.projects() && position != tile.data.getAudioPosition()) nbt.putInt(AUDIO_OFFSET, position.ordinal());
            return this;
        }

        public boolean empty() {
            return nbt.isEmpty();
        }

        public CompoundTag nbt() {
            return nbt;
        }
    }
}
