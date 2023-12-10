package me.srrapero720.waterframes.common.block.data;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screen.widgets.WidgetCounterDecimal;
import me.srrapero720.waterframes.util.FrameTools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.controls.simple.*;
import team.creative.creativecore.common.util.math.vec.Vec2f;

public abstract class DisplayData {
    public static final String URL = "url";
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
    public static final String PLAYING = "playing";
    public static final String TICK = "tick";
    public static final String TICK_MAX = "tick_max";
    public static final String DATA_V = "data_v";

    public static final int V = 1;

    public String url = "";
    public final Vec2f min = new Vec2f(0f, 0f);
    public final Vec2f max = new Vec2f(1f, 1f);

    public boolean flipX = false;
    public boolean flipY = false;

    public float rotation = 0;
    public float alpha = 1;
    public float brightness = 1;
    public int renderDistance = Math.min(32, DisplayConfig.maxRenderDistance());

    public int volume = DisplayConfig.maxVolume();
    public int maxVolumeDistance = Math.min(20, DisplayConfig.maxVolumeDistance());
    public int minVolumeDistance = Math.min(5, maxVolumeDistance);

    public boolean loop = true;
    public boolean playing = true;
    public int tick = 0;
    public int tickMax = -1;

    public int getPosX() { return this.min.x == 0 ? 0 : this.max.x == 1 ? 2 : 1; }
    public int getPosY() { return this.min.y == 0 ? 0 : this.max.y == 1 ? 2 : 1; }
    public float getWidth() { return this.max.x - this.min.x; }
    public float getHeight() { return this.max.y - this.min.y; }

    public void save(CompoundTag nbt) {
        nbt.putString(URL, url);
        nbt.putFloat(MIN_X, min.x);
        nbt.putFloat(MIN_Y, min.y);
        nbt.putFloat(MAX_X, max.x);
        nbt.putFloat(MAX_Y, max.y);
        nbt.putFloat(ROTATION, rotation);
        nbt.putInt(RENDER_DISTANCE, renderDistance);
        nbt.putBoolean(FLIP_X, flipX);
        nbt.putBoolean(FLIP_Y, flipY);
        nbt.putFloat(ALPHA, alpha);
        nbt.putFloat(BRIGHTNESS, brightness);
        nbt.putInt(VOLUME, volume);
        nbt.putInt(VOL_RANGE_MIN, minVolumeDistance);
        nbt.putInt(VOL_RANGE_MAX, maxVolumeDistance);
        nbt.putBoolean(PLAYING, playing);
        nbt.putInt(TICK, tick);
        nbt.putInt(TICK_MAX, tickMax);
        nbt.putBoolean(LOOP, loop);
        nbt.putInt(DATA_V, V);
    }

    public void load(CompoundTag nbt) {
        this.url = nbt.getString(URL);
        this.min.x = nbt.getFloat(MIN_X);
        this.min.y = nbt.getFloat(MIN_Y);
        this.max.x = nbt.getFloat(MAX_X);
        this.max.y = nbt.getFloat(MAX_Y);
        this.rotation = nbt.getFloat(ROTATION);
        this.renderDistance = DisplayConfig.maxRenderDistance(nbt.getInt(RENDER_DISTANCE));
        this.flipX = nbt.getBoolean(FLIP_X);
        this.flipY = nbt.getBoolean(FLIP_Y);
        this.alpha = nbt.contains(ALPHA) ? nbt.getFloat(ALPHA) : alpha;
        this.brightness = nbt.contains(BRIGHTNESS) ? nbt.getFloat(BRIGHTNESS) : alpha;
        this.volume = nbt.contains(VOLUME) ? DisplayConfig.maxVolume(nbt.getInt(VOLUME)) : volume;
        this.maxVolumeDistance = nbt.contains(VOL_RANGE_MAX) ? DisplayConfig.maxVolumeDistance(nbt.getInt(VOL_RANGE_MAX)) : maxVolumeDistance;
        this.minVolumeDistance = nbt.contains(VOL_RANGE_MIN) ? Math.min(nbt.getInt(VOL_RANGE_MIN), maxVolumeDistance) : minVolumeDistance;
        this.playing = nbt.getBoolean(PLAYING);
        this.tick = nbt.getInt(TICK);
        this.tickMax = nbt.getInt(TICK_MAX);
        this.loop = nbt.getBoolean(LOOP);

        switch (nbt.getInt(DATA_V)) {
            case 1 -> {

            }

            default -> { // NO EXISTS
                this.min.x = nbt.getFloat("minx");
                this.min.y = nbt.getFloat("miny");
                this.max.x = nbt.getFloat("maxx");
                this.max.y = nbt.getFloat("maxy");

                this.flipX = nbt.getBoolean("flipX");
                this.flipY = nbt.getBoolean("flipY");

                this.minVolumeDistance = nbt.contains("min") ? (int) nbt.getFloat("min") : 5;
                this.maxVolumeDistance = nbt.contains("max") ? (int) nbt.getFloat("max") : 20;

                this.renderDistance = nbt.getInt("render");
            }
        }

        this.restrictWidth();
        this.restrictHeight();
    }

    private void restrictWidth() {
        float maxWidth = DisplayConfig.maxWidth();
        if (getWidth() > maxWidth) {
            switch (getPosX()) {
                case 2 -> {
                    this.min.x = 1 - maxWidth;
                    this.max.x = 1;
                }
                case 1 -> {
                    float middle = maxWidth / 2f;
                    this.min.x = 0.5F - middle;
                    this.max.x = 0.5F + middle;
                }
                default -> {
                    this.min.x = 0;
                    this.max.x = maxWidth;
                }
            }
        }
    }

    private void restrictHeight() {
        float height = DisplayConfig.maxHeight();
        if (getHeight() > height) {
            switch (getPosY()) {
                case 2 -> {
                    this.min.y = 1 - height;
                    this.max.y = 1;
                }
                case 1 -> {
                    float middle = height / 2;
                    this.min.y = 0.5F - middle;
                    this.max.y = 0.5F + middle;
                }
                default -> {
                    this.min.y = 0;
                    this.max.y = height;
                }
            }
        }
    }

    public static CompoundTag build(GuiLayer gui) {
        CompoundTag nbt = new CompoundTag();

        GuiTextfield url = gui.get(URL);
        nbt.putString(URL, url.getText());

        WidgetCounterDecimal width = gui.get("width");
        WidgetCounterDecimal height = gui.get("height");
        GuiStateButton buttonPosX = gui.get("pos_x");
        GuiStateButton buttonPosY = gui.get("pos_y");
        nbt.putFloat("width", Math.max(0.1F, width.getValue()));
        nbt.putFloat("height", Math.max(0.1F, height.getValue()));
        nbt.putByte("pos_x",  (byte) buttonPosX.getState());
        nbt.putByte("pos_y", (byte) buttonPosY.getState());


        GuiCheckBox flipX = gui.get(FLIP_X);
        GuiCheckBox flipY = gui.get(FLIP_Y);
        nbt.putBoolean(FLIP_X, flipX.value);
        nbt.putBoolean(FLIP_Y, flipY.value);

        GuiSlider rotation = gui.get(ROTATION);
        nbt.putFloat(ROTATION, (float) rotation.value);

        GuiSlider alpha = gui.get(ALPHA);
        nbt.putFloat(ALPHA, (float) alpha.value);

        GuiSlider brightness = gui.get(BRIGHTNESS);
        nbt.putFloat(BRIGHTNESS, (float) brightness.value);

        GuiSteppedSlider renderDistance = gui.get(RENDER_DISTANCE);
        nbt.putInt(RENDER_DISTANCE, (int) renderDistance.value);

        GuiCheckBox loop = gui.get(LOOP);
        nbt.putBoolean(LOOP, loop.value);

        GuiSlider volume = gui.get(VOLUME);
        nbt.putInt(VOLUME, (int) volume.value);

        GuiSteppedSlider min = gui.get(VOL_RANGE_MIN);
        GuiSteppedSlider max = gui.get(VOL_RANGE_MAX);
        nbt.putInt(VOL_RANGE_MIN, min.getValue());
        nbt.putInt(VOL_RANGE_MAX, max.getValue());

        return nbt;
    }

    public static <D extends DisplayData, T extends DisplayTile<D>> void sync(T block, Player player, CompoundTag nbt, ExtraData<D> extra) {
        String url = nbt.getString(URL);
        if (DisplayConfig.canSave(player, url)) {
            if (!block.getUrl().equals(url)) {
                block.data.tick = 0;
                block.data.tickMax = -1;
            }
            block.setUrl(url);

            float width = FrameTools.minFloat(nbt.getFloat("width"), DisplayConfig.maxWidth());
            float height = FrameTools.minFloat(nbt.getFloat("height"), DisplayConfig.maxHeight());
            int posX = nbt.getByte("pos_x");
            int posY = nbt.getByte("pos_y");

            switch (posX) {
                case 0 -> {
                    block.data.min.x = 0;
                    block.data.max.x = width;
                }
                case 1 -> {
                    float middle = width / 2;
                    block.data.min.x = 0.5F - middle;
                    block.data.max.x = 0.5F + middle;
                }
                default -> {
                    block.data.min.x = 1 - width;
                    block.data.max.x = 1;
                }
            }

            switch (posY) {
                case 0 -> {
                    block.data.min.y = 0;
                    block.data.max.y = height;
                }
                case 1 -> {
                    float middle = height / 2;
                    block.data.min.y = 0.5F - middle;
                    block.data.max.y = 0.5F + middle;
                }
                default -> {
                    block.data.min.y = 1 - height;
                    block.data.max.y = 1;
                }
            }

            block.data.flipX = nbt.getBoolean(FLIP_X);
            block.data.flipY = nbt.getBoolean(FLIP_Y);
            block.data.rotation = nbt.getFloat(ROTATION);
            block.data.alpha = nbt.getFloat(ALPHA);
            block.data.brightness = nbt.getFloat(BRIGHTNESS);
            block.data.renderDistance = Math.min(nbt.getInt(RENDER_DISTANCE), DisplayConfig.maxRenderDistance());
            block.data.loop = nbt.getBoolean(LOOP);
            block.data.volume = Math.min(nbt.getInt(VOLUME), DisplayConfig.maxVolume());
            block.data.maxVolumeDistance = Math.min(nbt.getInt(VOL_RANGE_MAX), DisplayConfig.maxVolumeDistance());
            block.data.minVolumeDistance = Math.min(nbt.getInt(VOL_RANGE_MIN), block.data.maxVolumeDistance);
            if (block.data.minVolumeDistance > block.data.maxVolumeDistance) block.data.maxVolumeDistance = block.data.minVolumeDistance;
            if (extra != null) extra.set(block.data);
        }

        block.setDirty();
    }

    public interface ExtraData<T extends DisplayData> {
        void set(T data);
    }
}