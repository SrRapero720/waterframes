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

    public String url = "";
    public final Vec2f min = new Vec2f(0, 0);
    public final Vec2f max = new Vec2f(1, 1);

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
    }

    public void load(CompoundTag nbt) {
        url = nbt.getString(URL);
        min.x = nbt.getFloat(MIN_X);
        min.y = nbt.getFloat(MIN_Y);
        max.x = nbt.getFloat(MAX_X);
        max.y = nbt.getFloat(MAX_Y);
        float width;
        if (getWidth() > (width = DisplayConfig.maxWidth())) {
            switch (getPosX()) {
                case 0 -> {
                    min.x = 0;
                    max.x = width;
                }
                case 1 -> {
                    float middle = width / 2;
                    min.x = 0.5F - middle;
                    max.x = 0.5F + middle;
                }
                default -> {
                    min.x = 1 - width;
                    max.x = 1;
                }
            }
        }

        float height;
        if (getHeight() > (height = DisplayConfig.maxHeight())) {
            switch (getPosY()) {
                case 0 -> {
                    min.y = 0;
                    max.y = height;
                }
                case 1 -> {
                    float middle = height / 2;
                    min.y = 0.5F - middle;
                    max.y = 0.5F + middle;
                }
                default -> {
                    min.y = 1 - height;
                    max.y = 1;
                }
            }
        }

        rotation = nbt.getFloat(ROTATION);
        renderDistance = Math.min(DisplayConfig.maxRenderDistance(), nbt.getInt(RENDER_DISTANCE));
        flipX = nbt.getBoolean(FLIP_X);
        flipY = nbt.getBoolean(FLIP_Y);
        alpha = nbt.contains(ALPHA) ? nbt.getFloat(ALPHA) : alpha;
        brightness = nbt.contains(BRIGHTNESS) ? nbt.getFloat(BRIGHTNESS) : alpha;
        volume = nbt.contains(VOLUME) ? Math.min(nbt.getInt(VOLUME), DisplayConfig.maxVolume()) : volume;
        maxVolumeDistance = nbt.contains(VOL_RANGE_MAX) ? Math.min(nbt.getInt(VOL_RANGE_MAX), DisplayConfig.maxVolumeDistance()) : maxVolumeDistance;
        minVolumeDistance = nbt.contains(VOL_RANGE_MIN) ? Math.min(nbt.getInt(VOL_RANGE_MIN), maxVolumeDistance) : minVolumeDistance;
        playing = nbt.getBoolean(PLAYING);
        tick = nbt.getInt(TICK);
        tickMax = nbt.getInt(TICK_MAX);
        loop = nbt.getBoolean(LOOP);
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
        String url = nbt.getString("url");
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