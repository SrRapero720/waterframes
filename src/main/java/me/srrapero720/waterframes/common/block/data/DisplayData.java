package me.srrapero720.waterframes.common.block.data;

import me.srrapero720.waterframes.DisplaysConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.block.data.types.AudioPosition;
import me.srrapero720.waterframes.common.block.data.types.PositionHorizontal;
import me.srrapero720.waterframes.common.block.data.types.PositionVertical;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screens.DisplayScreen;
import me.srrapero720.waterframes.common.screens.PlayListScreen;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
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
    public static final String MUTED = "muted";
    public static final String TICK = "tick";
    public static final String TIME  = "time_ms";
    public static final String TICK_MAX = "tick_max";
    public static final String LIT = "lit";
    public static final String DATA_V = "data_v";

    // FRAME KEYS
    public static final String RENDER_BOTH_SIDES = "render_both";

    // PROJECTOR
    public static final String PROJECTION_DISTANCE = "projection_distance";
    public static final String AUDIO_OFFSET = "audio_offset";

    public static final short V = 2;

    // Media URLs - now stored as plain strings for direct MRL compatibility
    public String url = null;
    public List<String> urls = new ArrayList<>();
    public int urlIndex = 0;

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
    public boolean muted = false;
    public boolean lit = true;
    public int tick = 0;
    public int tickMax = -1;

    public boolean renderBothSides = false;

    // PROJECTOR VALUES
    public float projectionDistance = DisplaysConfig.maxProjDis(8f);
    public float audioOffset = 0;

    /**
     * Advances to the next URL in the playlist.
     * Resets tick/tickMax for the new media.
     * @return true if advanced, false if playlist is empty
     */
    public boolean nextUrl() {
        if (this.urls.isEmpty()) return false;
        this.urlIndex++;
        if (this.urlIndex >= this.urls.size()) {
            this.urlIndex = 0;
        }
        this.url = this.urls.get(this.urlIndex);
        this.tick = 0;
        this.tickMax = -1;
        return true;
    }

    /**
     * Goes back to the previous URL in the playlist.
     * Resets tick/tickMax for the new media.
     * @return true if rewound, false if playlist is empty
     */
    public boolean prevUrl() {
        if (this.urls.isEmpty()) return false;
        this.urlIndex--;
        if (this.urlIndex < 0) {
            this.urlIndex = this.urls.size() - 1;
        }
        this.url = this.urls.get(this.urlIndex);
        this.tick = 0;
        this.tickMax = -1;
        return true;
    }

    /**
     * Checks if any URL is configured (single or playlist).
     */
    public boolean hasUrl() {
        return (this.url != null && !this.url.isEmpty()) || !this.urls.isEmpty();
    }

    /**
     * Gets the current active URL.
     * If using playlist, returns the URL at current index.
     * @return the current URL string, or null if none
     */
    public String getUrl() {
        if (!this.urls.isEmpty()) {
            return this.urls.get(this.urlIndex);
        }
        return this.url;
    }

    /**
     * Sets the current URL (clears playlist mode).
     * @param url the URL string to set
     */
    public void setUrl(String url) {
        this.url = (url != null && !url.isEmpty()) ? url : null;
        this.urls.clear();
        this.urlIndex = 0;
    }

    /**
     * Sets the playlist URLs.
     * @param urls list of URL strings
     */
    public void setUrls(List<String> urls) {
        this.urls.clear();
        if (urls != null) {
            this.urls.addAll(urls);
        }
        this.urlIndex = 0;
        this.url = this.urls.isEmpty() ? null : this.urls.get(0);
    }

    /**
     * Checks if currently in playlist mode.
     */
    public boolean isPlaylist() {
        return !this.urls.isEmpty();
    }

    public PositionHorizontal getPosX() { return this.min.x == 0 ? PositionHorizontal.LEFT : this.max.x == 1 ? PositionHorizontal.RIGHT : PositionHorizontal.CENTER; }
    public PositionVertical getPosY() { return this.min.y == 0 ? PositionVertical.TOP : this.max.y == 1 ? PositionVertical.BOTTOM : PositionVertical.CENTER; }
    public float getWidth() { return this.max.x - this.min.x; }
    public float getHeight() { return this.max.y - this.min.y; }

    public void save(CompoundTag nbt, DisplayTile tile) {
        // Save URL as string directly
        nbt.putString(URL, this.url != null ? this.url : "");

        // Playlist: save as newline-delimited string
        nbt.putString(URL_LIST, WaterFrames.composeUrlList(this.urls));
        nbt.putInt(URL_INDEX, urlIndex);

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
        nbt.putBoolean(MUTED, muted);
        nbt.putBoolean(LIT, lit);
        nbt.putLong(TICK, tick);
        nbt.putLong(TICK_MAX, tickMax);
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
        this.urls = WaterFrames.decomposeUrlList(nbt.getString(URL_LIST));
        this.urlIndex = nbt.getInt(URL_INDEX);

        // Ensure index is valid
        if (!this.urls.isEmpty() && this.urlIndex >= this.urls.size()) {
            this.urlIndex = 0;
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
        this.brightness = nbt.contains(BRIGHTNESS) ? nbt.getInt(BRIGHTNESS) : this.alpha;
        this.volume = nbt.contains(VOLUME) ? DisplaysConfig.maxVol(nbt.getInt(VOLUME)) : this.volume;
        this.maxVolumeDistance = nbt.contains(VOL_RANGE_MAX) ? DisplaysConfig.maxVolDis(nbt.getInt(VOL_RANGE_MAX)) : this.maxVolumeDistance;
        this.minVolumeDistance = nbt.contains(VOL_RANGE_MIN) ? Math.min(nbt.getInt(VOL_RANGE_MIN), this.maxVolumeDistance) : this.minVolumeDistance;
        this.paused = nbt.getBoolean(PAUSED);
        this.muted = nbt.getBoolean(MUTED);
        this.lit = !nbt.contains(LIT) || nbt.getBoolean(LIT);
        this.tick = nbt.getInt(TICK);
        this.tickMax = nbt.contains(TICK_MAX) ? nbt.getInt(TICK_MAX) : this.tickMax;
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

    public DisplayData setProjectionDistance(float projectionDistance) {
        this.projectionDistance = projectionDistance;
        return this;
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

    public static CompoundTag build(PlayListScreen screen, DisplayTile tile) {
        CompoundTag nbt = new CompoundTag();

        nbt.putString(URL_LIST, WaterFrames.composeUrlList(screen.getUrls()));
        nbt.putInt(URL_INDEX, 0);

        return nbt;
    }

    public static CompoundTag build(DisplayScreen screen, DisplayTile tile) {
        CompoundTag nbt = new CompoundTag();

        nbt.putString(URL, screen.url.getText());
        nbt.putBoolean(ACTIVE, true);

        if (tile.caps.resizes()) {
            nbt.putFloat("width", Math.max(0.1F, (float) screen.widthField.getValue()));
            nbt.putFloat("height", Math.max(0.1F, (float) screen.heightField.getValue()));
            nbt.putInt("pos_x",  screen.pos_view.getX().ordinal());
            nbt.putInt("pos_y", screen.pos_view.getY().ordinal());
            nbt.putFloat(ROTATION, (float) screen.rotation.getValue());
        }

        nbt.putBoolean(FLIP_X, screen.flip_x.value);
        nbt.putBoolean(FLIP_Y, screen.flip_y.value);

        nbt.putInt(ALPHA, screen.alpha.getIntValue());
        nbt.putInt(BRIGHTNESS, screen.brightness.getIntValue());
        nbt.putInt(RENDER_DISTANCE, screen.render_distance.getIntValue());

        nbt.putInt(VOLUME, screen.volume.getIntValue());
        nbt.putInt(VOL_RANGE_MIN, screen.volume_min.getIntValue());
        nbt.putInt(VOL_RANGE_MAX, screen.volume_max.getIntValue());

        if (tile.getBlockState().hasProperty(DisplayBlock.VISIBLE)) {
            nbt.putBoolean("visible", screen.show_model.value);
        }

        nbt.putBoolean(LIT, screen.lit.value);

        if (tile.caps.renderBehind()) {
            nbt.putBoolean(RENDER_BOTH_SIDES, screen.mirror.value);
        }

        if (tile.caps.projects()) {
            nbt.putFloat(PROJECTION_DISTANCE, (float) screen.projection_distance.getValue());
            nbt.putInt(AUDIO_OFFSET, screen.audio_offset.getState());
        }

        return nbt;
    }

    public static void syncList(DisplayTile tile, Player player, CompoundTag tag) {
        tile.data.urls = WaterFrames.decomposeUrlList(tag.getString(URL_LIST));
        tile.data.urlIndex = tag.getInt(URL_INDEX);
        if (tile.data.urls.isEmpty()) {
            tile.data.url = null;
        } else {
            tile.data.url = tile.data.urls.get(tile.data.urlIndex);
        }
        tile.setDirty();
    }

    public static void sync(DisplayTile tile, Player player, CompoundTag nbt) {
        String url = nbt.getString(URL);
        if (DisplaysConfig.canSave(player, url)) {
            boolean urlChanged = !url.equals(tile.data.url);
            if (urlChanged) {
                tile.data.tick = 0;
                tile.data.tickMax = -1;
            }
            tile.data.url = url.isEmpty() ? null : url;
            tile.data.uuid = tile.data.hasUrl() ? player.getUUID() : Util.NIL_UUID;
            tile.data.active = nbt.getBoolean(ACTIVE);

            if (tile.caps.resizes()) {
                float width = DisplaysConfig.maxWidth(nbt.getFloat("width"));
                float height = DisplaysConfig.maxHeight(nbt.getFloat("height"));
                int posX = nbt.getInt("pos_x");
                int posY = nbt.getInt("pos_y");

                tile.data.setWidth(PositionHorizontal.VALUES[posX], width);
                tile.data.setHeight(PositionVertical.VALUES[posY], height);
                tile.data.rotation = nbt.getFloat(ROTATION);
            }

            tile.data.flipX = nbt.getBoolean(FLIP_X);
            tile.data.flipY = nbt.getBoolean(FLIP_Y);
            tile.data.alpha = nbt.getInt(ALPHA);
            tile.data.brightness = nbt.getInt(BRIGHTNESS);
            tile.data.renderDistance = DisplaysConfig.maxRenDis(nbt.getInt(RENDER_DISTANCE));
            tile.data.volume = DisplaysConfig.maxVol(nbt.getInt(VOLUME));
            tile.data.maxVolumeDistance = DisplaysConfig.maxVolDis(nbt.getInt(VOL_RANGE_MAX));
            tile.data.minVolumeDistance = Math.min(nbt.getInt(VOL_RANGE_MIN), tile.data.maxVolumeDistance);
            if (tile.data.minVolumeDistance > tile.data.maxVolumeDistance)
                tile.data.maxVolumeDistance = tile.data.minVolumeDistance;

            if (tile.canHideModel()) {
                tile.setVisibility(nbt.getBoolean("visible"));
            }

            tile.data.lit = nbt.getBoolean(LIT);

            if (tile.caps.renderBehind()) {
                tile.data.renderBothSides = nbt.getBoolean(RENDER_BOTH_SIDES);
            }

            if (tile.caps.projects()) {
                int mode = nbt.getInt(AUDIO_OFFSET);

                tile.data.projectionDistance = DisplaysConfig.maxProjDis(nbt.getFloat(PROJECTION_DISTANCE));
                tile.data.setAudioPosition(AudioPosition.VALUES[mode]);
            }
        }

        tile.setDirty();
    }
}
