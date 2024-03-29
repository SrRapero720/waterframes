package me.srrapero720.waterframes.common.block.data;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screens.DisplayScreen;
import me.srrapero720.waterframes.WFMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.util.math.vec.Vec2f;

public class DisplayData {
    public static final String URL = "url";
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
    public static final String TICK_MAX = "tick_max";
    public static final String DATA_V = "data_v";

    // FRAME KEYS
    public static final String VISIBLE_FRAME = "frame_visibility";
    public static final String RENDER_BOTH_SIDES = "render_both";

    // PROJECTOR
    public static final String PROJECTION_DISTANCE = "projection_distance";
    public static final String AUDIO_OFFSET = "audio_offset";

    public static final short V = 1;

    public String url = "";
    public boolean active = true;
    public final Vec2f min = new Vec2f(0f, 0f);
    public final Vec2f max = new Vec2f(1f, 1f);

    public boolean flipX = false;
    public boolean flipY = false;

    public float rotation = 0;
    public float alpha = 1;
    public float brightness = 1;
    public int renderDistance = DisplayConfig.maxRenderDistance(32);

    public int volume = DisplayConfig.maxVolume();
    public int maxVolumeDistance = Math.min(20, DisplayConfig.maxVolumeDistance());
    public int minVolumeDistance = Math.min(5, maxVolumeDistance);

    public boolean loop = true;
    public boolean paused = false;
    public boolean muted = false;
    public int tick = 0;
    public int tickMax = -1;

    // FRAME VALUES
    public boolean frameVisibility = true;
    public boolean renderBothSides = false;

    // PROJECTOR VALUES
    public int projectionDistance = DisplayConfig.maxProjectionDistance(8);
    public float audioOffset = 0;

    public PositionHorizontal getPosX() { return this.min.x == 0 ? PositionHorizontal.LEFT : this.max.x == 1 ? PositionHorizontal.RIGHT : PositionHorizontal.CENTER; }
    public PositionVertical getPosY() { return this.min.y == 0 ? PositionVertical.TOP : this.max.y == 1 ? PositionVertical.BOTTOM : PositionVertical.CENTER; }
    public float getWidth() { return this.max.x - this.min.x; }
    public float getHeight() { return this.max.y - this.min.y; }

    public void save(CompoundTag nbt, DisplayTile displayTile) {
        nbt.putString(URL, url);
        nbt.putBoolean(ACTIVE, active);
        if (displayTile.canResize()) {
            nbt.putFloat(MIN_X, min.x);
            nbt.putFloat(MIN_Y, min.y);
            nbt.putFloat(MAX_X, max.x);
            nbt.putFloat(MAX_Y, max.y);
        }
        nbt.putFloat(ROTATION, rotation);
        nbt.putInt(RENDER_DISTANCE, renderDistance);
        nbt.putBoolean(FLIP_X, flipX);
        nbt.putBoolean(FLIP_Y, flipY);
        nbt.putFloat(ALPHA, alpha);
        nbt.putFloat(BRIGHTNESS, brightness);
        nbt.putInt(VOLUME, volume);
        nbt.putInt(VOL_RANGE_MIN, minVolumeDistance);
        nbt.putInt(VOL_RANGE_MAX, maxVolumeDistance);
        nbt.putBoolean(PAUSED, paused);
        nbt.putBoolean(MUTED, muted);
        nbt.putInt(TICK, tick);
        nbt.putInt(TICK_MAX, tickMax);
        nbt.putBoolean(LOOP, loop);

        if (displayTile.canRenderBackside()) {
            nbt.putBoolean(RENDER_BOTH_SIDES, renderBothSides);
        }

        if (displayTile.canHideModel()) {
            nbt.putBoolean(VISIBLE_FRAME, frameVisibility);
        }

        if (displayTile.canProject()) {
            nbt.putInt(PROJECTION_DISTANCE, projectionDistance);
            nbt.putFloat(AUDIO_OFFSET, audioOffset);
        }

        nbt.putShort(DATA_V, V);
    }

    public void load(CompoundTag nbt, DisplayTile displayTile) {
        this.url = nbt.getString(URL);
        this.active = nbt.contains(ACTIVE) ? nbt.getBoolean(ACTIVE) : this.active;
        if (displayTile.canResize()) {
            this.min.x = nbt.getFloat(MIN_X);
            this.min.y = nbt.getFloat(MIN_Y);
            this.max.x = nbt.getFloat(MAX_X);
            this.max.y = nbt.getFloat(MAX_Y);
        }
        this.rotation = nbt.getFloat(ROTATION);
        this.renderDistance = DisplayConfig.maxRenderDistance(nbt.getInt(RENDER_DISTANCE));
        this.flipX = nbt.getBoolean(FLIP_X);
        this.flipY = nbt.getBoolean(FLIP_Y);
        this.alpha = nbt.contains(ALPHA) ? nbt.getFloat(ALPHA) : alpha;
        this.brightness = nbt.contains(BRIGHTNESS) ? nbt.getFloat(BRIGHTNESS) : alpha;
        this.volume = nbt.contains(VOLUME) ? DisplayConfig.maxVolume(nbt.getInt(VOLUME)) : volume;
        this.maxVolumeDistance = nbt.contains(VOL_RANGE_MAX) ? DisplayConfig.maxVolumeDistance(nbt.getInt(VOL_RANGE_MAX)) : maxVolumeDistance;
        this.minVolumeDistance = nbt.contains(VOL_RANGE_MIN) ? Math.min(nbt.getInt(VOL_RANGE_MIN), maxVolumeDistance) : minVolumeDistance;
        this.paused = nbt.getBoolean(PAUSED);
        this.muted = nbt.getBoolean(MUTED);
        this.tick = nbt.getInt(TICK);
        this.tickMax = nbt.contains(TICK_MAX) ? nbt.getInt(TICK_MAX) : this.tickMax;
        this.loop = nbt.getBoolean(LOOP);

        if (displayTile.canHideModel()) {
            this.frameVisibility = nbt.getBoolean(VISIBLE_FRAME);
        }

        if (displayTile.canRenderBackside()) {
            this.renderBothSides = nbt.getBoolean(RENDER_BOTH_SIDES);
        }

        if (displayTile.canProject()) {
            projectionDistance = nbt.contains(PROJECTION_DISTANCE) ? DisplayConfig.maxProjectionDistance(nbt.getInt(PROJECTION_DISTANCE)) : projectionDistance;
            audioOffset = nbt.contains(AUDIO_OFFSET) ? nbt.getFloat(AUDIO_OFFSET) : audioOffset;
        }

        switch (nbt.getShort(DATA_V)) {
            case 1 -> {

            }

            default -> { // NO EXISTS
                if (!nbt.contains("maxx")) return; // no exists then ignore, prevents broke new data on 2.0
                this.min.x = nbt.getFloat("minx");
                this.min.y = nbt.getFloat("miny");
                this.max.x = nbt.getFloat("maxx");
                this.max.y = nbt.getFloat("maxy");

                this.flipX = nbt.getBoolean("flipX");
                this.flipY = nbt.getBoolean("flipY");

                this.maxVolumeDistance = DisplayConfig.maxVolumeDistance((int) nbt.getFloat("max"));
                this.minVolumeDistance = Math.min((int) nbt.getFloat("min"), maxVolumeDistance);

                this.renderDistance = nbt.getInt("render");

                if (displayTile.canHideModel()) {
                    this.frameVisibility = nbt.getBoolean("visibleFrame");
                }

                if (displayTile.canRenderBackside()) {
                    this.renderBothSides = nbt.getBoolean("bothSides");
                }
            }
        }

        this.restrictWidth();
        this.restrictHeight();
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
        float maxWidth = DisplayConfig.maxWidth();
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
        float height = DisplayConfig.maxHeight();
        if (getHeight() > height) {
            switch (getPosY()) {
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
    }

    public int getOffsetMode() {
        return (audioOffset == projectionDistance) ? 2 : (audioOffset == projectionDistance / 2f) ? 1 : 0;
    }

    public static CompoundTag build(DisplayScreen screen, DisplayTile tile) {
        CompoundTag nbt = new CompoundTag();

        nbt.putString(URL, screen.urlField.getText());
        nbt.putBoolean(ACTIVE, true); // reset

        if (tile.canResize()) {
            nbt.putFloat("width", Math.max(0.1F, (float) screen.widthField.getValue()));
            nbt.putFloat("height", Math.max(0.1F, (float) screen.heightField.getValue()));
            nbt.putInt("pos_x",  screen.pos_view.getX().ordinal());
            nbt.putInt("pos_y", screen.pos_view.getY().ordinal());
        }

        nbt.putBoolean(FLIP_X, screen.flip_x.value);
        nbt.putBoolean(FLIP_Y, screen.flip_y.value);

        nbt.putFloat(ROTATION, (float) screen.rotation.getValue());
        nbt.putFloat(ALPHA, (float) screen.visibility.getValue());
        nbt.putFloat(BRIGHTNESS, (float) screen.brightness.getValue());
        nbt.putInt(RENDER_DISTANCE, screen.render_distance.getIntValue());

        nbt.putInt(VOLUME, screen.volume.getIntValue());
        nbt.putInt(VOL_RANGE_MIN, screen.volume_min.getIntValue());
        nbt.putInt(VOL_RANGE_MAX, screen.volume_max.getIntValue());

        if (tile.canHideModel()) {
            nbt.putBoolean(VISIBLE_FRAME, screen.show_model.value);
        }

        if (tile.canRenderBackside()) {
            nbt.putBoolean(RENDER_BOTH_SIDES, screen.render_behind.value);
        }

        if (tile.canProject()) {
            nbt.putInt(PROJECTION_DISTANCE, screen.projection_distance.getIntValue());
            nbt.putInt("audio_offset_mode", screen.audioOffset.getState());
        }

        return nbt;
    }

    public static void sync(DisplayTile block, Player player, CompoundTag nbt) {
        String url = nbt.getString(URL);
        if (DisplayConfig.canSave(player, url)) {
            if (!block.data.url.equals(url)) {
                block.data.tick = 0;
                block.data.tickMax = -1;
            }
            block.data.url = url;
            block.data.active = nbt.getBoolean(ACTIVE);

            if (block.canResize()) {
                float width = WFMath.minFloat(nbt.getFloat("width"), DisplayConfig.maxWidth());
                float height = WFMath.minFloat(nbt.getFloat("height"), DisplayConfig.maxHeight());
                int posX = nbt.getInt("pos_x");
                int posY = nbt.getInt("pos_y");

                block.data.setWidth(PositionHorizontal.VALUES[posX], width);
                block.data.setHeight(PositionVertical.VALUES[posY], height);
            }

            block.data.flipX = nbt.getBoolean(FLIP_X);
            block.data.flipY = nbt.getBoolean(FLIP_Y);
            block.data.rotation = nbt.getFloat(ROTATION);
            block.data.alpha = nbt.getFloat(ALPHA);
            block.data.brightness = nbt.getFloat(BRIGHTNESS);
            block.data.renderDistance = DisplayConfig.maxRenderDistance(nbt.getInt(RENDER_DISTANCE));
            block.data.volume = DisplayConfig.maxVolume(nbt.getInt(VOLUME));
            block.data.maxVolumeDistance = DisplayConfig.maxVolumeDistance(nbt.getInt(VOL_RANGE_MAX));
            block.data.minVolumeDistance = Math.min(nbt.getInt(VOL_RANGE_MIN), block.data.maxVolumeDistance);
            if (block.data.minVolumeDistance > block.data.maxVolumeDistance) block.data.maxVolumeDistance = block.data.minVolumeDistance;

            if (block.canHideModel()) {
                block.data.frameVisibility = nbt.getBoolean(VISIBLE_FRAME);
            }

            if (block.canRenderBackside()) {
                block.data.renderBothSides = nbt.getBoolean(RENDER_BOTH_SIDES);
            }

            if (block.canProject()) {
                block.data.projectionDistance = DisplayConfig.maxProjectionDistance(nbt.getInt(PROJECTION_DISTANCE));

                int mode = nbt.getInt("audio_offset_mode");
                block.data.audioOffset = mode == 2 ? block.data.projectionDistance : mode == 1 ? block.data.projectionDistance / 2f : 0;
            }
        }

        block.setDirty();
    }
}