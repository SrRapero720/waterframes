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
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.joml.Vector2f;

import java.net.URI;
import java.util.LinkedList;
import java.util.UUID;

public class DisplayData {
    public static final String URL = "url";
    public static final String URI_LIST = "uri_list";
    public static final String URI_INDEX = "uri_index";
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

    public URI uri = null;
    public LinkedList<URI> uris = new LinkedList<>();
    public int uri_index;
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
    public int maxVolumeDistance = 20;
    public int minVolumeDistance = 5;

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

    public boolean nextUri() {
        if (this.uris.isEmpty()) return false;
        this.uri_index++;
        if (this.uri_index >= this.uris.size()) {
            this.uri_index = 0;
        }
        this.uri = this.uris.get(this.uri_index);
        this.tick = 0;
        this.tickMax = -1;
        return true;
    }
    public boolean prevUri() {
        if (this.uris.isEmpty()) return false;
        this.uri_index--;
        if (this.uri_index < 0) {
            this.uri_index = this.uris.size() - 1;
        }
        this.uri = this.uris.get(this.uri_index);
        this.tick = 0;
        this.tickMax = -1;
        return true;
    }
    public boolean hasUri() { return this.uri != null || !this.uris.isEmpty(); }
    public URI getUri() { return this.uris.isEmpty() ? this.uri : this.uris.get(this.uri_index); }
    public PositionHorizontal getPosX() { return this.min.x == 0 ? PositionHorizontal.LEFT : this.max.x == 1 ? PositionHorizontal.RIGHT : PositionHorizontal.CENTER; }
    public PositionVertical getPosY() { return this.min.y == 0 ? PositionVertical.TOP : this.max.y == 1 ? PositionVertical.BOTTOM : PositionVertical.CENTER; }
    public float getWidth() { return this.max.x - this.min.x; }
    public float getHeight() { return this.max.y - this.min.y; }

    public void save(ValueOutput nbt, DisplayTile tile) {
        nbt.putString(URL, !hasUri() ? "" : this.getUri().toString());
        // EXPERIMENTAL: LISTING
        nbt.putString(URI_LIST, WaterFrames.composeURIString(this.uris));
        nbt.putInt(URI_INDEX, uri_index);
        // HERE ENDS
        nbt.store(PLAYER_UUID, UUIDUtil.CODEC, uuid);
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

    public void load(ValueInput nbt, DisplayTile tile) {
        String url = nbt.getStringOr(URL, "");
        this.uri = url.isEmpty() ? null : WaterFrames.createURI(url);
        // EXPERIMENTAL: LISTING
        this.uris = WaterFrames.decomposeURIString(nbt.getStringOr(URI_LIST, ""));
        this.uri_index = nbt.getIntOr(URI_INDEX, 0);
        // EXPERIMENTAL ENDS

        this.uuid = nbt.read(PLAYER_UUID, UUIDUtil.CODEC).orElse(this.uuid);
        this.active = nbt.getBooleanOr(ACTIVE, active);
        if (tile.caps.resizes()) {
            this.min.x = nbt.getFloatOr(MIN_X, 0F);
            this.min.y = nbt.getFloatOr(MIN_Y, 0F);
            this.max.x = nbt.getFloatOr(MAX_X, 1F);
            this.max.y = nbt.getFloatOr(MAX_Y, 1F);
            this.rotation = nbt.getFloatOr(ROTATION, 0F);
        }
        this.renderDistance = DisplaysConfig.maxRenDis(nbt.getIntOr(RENDER_DISTANCE, this.renderDistance));
        this.flipX = nbt.getBooleanOr(FLIP_X, this.flipX);
        this.flipY = nbt.getBooleanOr(FLIP_Y, this.flipY);
        this.alpha = nbt.getIntOr(ALPHA, this.alpha);
        this.brightness = nbt.getIntOr(BRIGHTNESS, this.brightness);
        this.volume = DisplaysConfig.maxVol(nbt.getIntOr(VOLUME, this.volume));
        this.maxVolumeDistance = nbt.getIntOr(VOL_RANGE_MAX, this.maxVolumeDistance);
        this.minVolumeDistance = Math.min(nbt.getIntOr(VOL_RANGE_MIN, this.minVolumeDistance), this.maxVolumeDistance);
        this.paused = nbt.getBooleanOr(PAUSED, this.paused);
        this.muted = nbt.getBooleanOr(MUTED, this.muted);
        this.lit = nbt.getBooleanOr(LIT, this.lit);
        this.tick = nbt.getIntOr(TICK, 0);
        this.tickMax = nbt.getIntOr(TICK_MAX, this.tickMax);
        this.loop = nbt.getBooleanOr(LOOP, this.loop);

        if (tile.caps.renderBehind()) {
            this.renderBothSides = nbt.getBooleanOr(RENDER_BOTH_SIDES, this.renderBothSides);
        }

        if (tile.caps.projects()) {
            this.projectionDistance = DisplaysConfig.maxProjDis(nbt.getFloatOr(PROJECTION_DISTANCE, this.projectionDistance));
            this.audioOffset = nbt.getFloatOr(AUDIO_OFFSET, this.audioOffset);
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

        nbt.putString(URI_LIST, WaterFrames.composeURIString(screen.getUris()));
        nbt.putInt(URI_INDEX, 0);

        return nbt;
    }

    public static CompoundTag build(DisplayScreen screen, DisplayTile tile) {
        CompoundTag nbt = new CompoundTag();

        nbt.putString(URL, screen.url.getText());
        nbt.putBoolean(ACTIVE, true); // reset

        if (tile.caps.resizes()) {
            nbt.putFloat("width", Math.max(0.1F, (float) screen.widthField.getValue()));
            nbt.putFloat("height", Math.max(0.1F, (float) screen.heightField.getValue()));
            nbt.putInt("pos_x",  screen.pos_view.getX().ordinal());
            nbt.putInt("pos_y", screen.pos_view.getY().ordinal());
            nbt.putFloat(ROTATION, (float) screen.rotation.getValue());
        }

        nbt.putBoolean(FLIP_X, screen.flip_x.get());
        nbt.putBoolean(FLIP_Y, screen.flip_y.get());

        nbt.putInt(ALPHA, screen.alpha.getIntValue());
        nbt.putInt(BRIGHTNESS, screen.brightness.getIntValue());
        nbt.putInt(RENDER_DISTANCE, screen.render_distance.getIntValue());

        nbt.putInt(VOLUME, screen.volume.getIntValue());
        nbt.putInt(VOL_RANGE_MIN, (int) screen.volume_min.getValue());
        nbt.putInt(VOL_RANGE_MAX, (int) screen.volume_max.getValue());

        if (tile.getBlockState().hasProperty(DisplayBlock.VISIBLE)) {
            nbt.putBoolean("visible", screen.show_model.get());
        }

        nbt.putBoolean(LIT, screen.lit.get());

        if (tile.caps.renderBehind()) {
            nbt.putBoolean(RENDER_BOTH_SIDES, screen.mirror.getState());
        }

        if (tile.caps.projects()) {
            nbt.putFloat(PROJECTION_DISTANCE, (float) screen.projection_distance.getValue());
            nbt.putInt(AUDIO_OFFSET, screen.audio_offset.getState());
        }

        return nbt;
    }

    public static void syncList(DisplayTile tile, Player player, CompoundTag tag) {
        tile.data.uris = WaterFrames.decomposeURIString(tag.getStringOr(URI_LIST, ""));
        tile.data.uri_index = tag.getIntOr(URI_INDEX, 0);
        if (tile.data.uris.isEmpty()) {
            tile.data.uri = null;
        }
        tile.setDirty();
    }

    public static void sync(DisplayTile tile, Player player, CompoundTag nbt) {
        String url = nbt.getStringOr(URL, "");
        if (DisplaysConfig.canSave(player, url)) {
            final URI uri = WaterFrames.createURI(url);
            if (!tile.data.hasUri() || !tile.data.getUri().equals(uri)) {
                tile.data.tick = 0;
                tile.data.tickMax = -1;
            }
            tile.data.uri = uri;
            tile.data.uuid = tile.data.hasUri() ? player.getUUID() : Util.NIL_UUID;
            tile.data.active = nbt.getBooleanOr(ACTIVE, true);

            if (tile.caps.resizes()) {
                float width = DisplaysConfig.maxWidth(nbt.getFloatOr("width", 1F));
                float height = DisplaysConfig.maxHeight(nbt.getFloatOr("height", 1F));
                int posX = nbt.getIntOr("pos_x", PositionHorizontal.LEFT.ordinal());
                int posY = nbt.getIntOr("pos_y", PositionVertical.TOP.ordinal());

                tile.data.setWidth(PositionHorizontal.VALUES[posX], width);
                tile.data.setHeight(PositionVertical.VALUES[posY], height);
                tile.data.rotation = nbt.getFloatOr(ROTATION, 0);
            }

            tile.data.flipX = nbt.getBooleanOr(FLIP_X, false);
            tile.data.flipY = nbt.getBooleanOr(FLIP_Y, false);
            tile.data.alpha = nbt.getIntOr(ALPHA, 255);
            tile.data.brightness = nbt.getIntOr(BRIGHTNESS, 255);
            tile.data.renderDistance = DisplaysConfig.maxRenDis(nbt.getIntOr(RENDER_DISTANCE, 4));
            tile.data.volume = DisplaysConfig.maxVol(nbt.getIntOr(VOLUME, 100));
            tile.data.maxVolumeDistance = nbt.getIntOr(VOL_RANGE_MAX, 20);
            tile.data.minVolumeDistance = Math.min(nbt.getIntOr(VOL_RANGE_MIN, 4), tile.data.maxVolumeDistance);
            if (tile.data.minVolumeDistance > tile.data.maxVolumeDistance)
                tile.data.maxVolumeDistance = tile.data.minVolumeDistance;

            if (tile.canHideModel()) {
                tile.setVisibility(nbt.getBooleanOr("visible", true));
            }

            tile.data.lit = nbt.getBooleanOr(LIT, true);

            if (tile.caps.renderBehind()) {
                tile.data.renderBothSides = nbt.getBooleanOr(RENDER_BOTH_SIDES, false);
            }

            if (tile.caps.projects()) {
                int mode = nbt.getIntOr(AUDIO_OFFSET, AudioPosition.BLOCK.ordinal());

                tile.data.projectionDistance = DisplaysConfig.maxProjDis(nbt.getFloatOr(PROJECTION_DISTANCE, 8f));
                tile.data.setAudioPosition(AudioPosition.VALUES[mode]);
            }
        }

        tile.setDirty();
    }
}