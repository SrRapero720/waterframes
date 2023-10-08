package me.srrapero720.waterframes.common.data;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screen.widgets.WidgetCounterDecimal;
import me.srrapero720.waterframes.util.FrameTools;
import me.srrapero720.waterframes.util.FrameConfig;
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
    public Vec2f min = new Vec2f(0, 0);
    public Vec2f max = new Vec2f(1, 1);

    public boolean flipX = false;
    public boolean flipY = false;

    public float rotation = 0;
    public float alpha = 1;
    public float brightness = 1;
    public int renderDistance = 32;

    public int volume = 100;
    public int minVolumeDistance = 5;
    public int maxVolumeDistance = 20;

    public boolean loop = true;
    public boolean playing = true;
    public int tick = 0;
    public int tickMax = -1;

    public int getPosX() { return min.x == 0 ? 0 : max.x == 1 ? 2 : 1; }
    public int getPosY() { return min.y == 0 ? 0 : max.y == 1 ? 2 : 1; }

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
        rotation = nbt.getFloat(ROTATION);
        renderDistance = Math.min(FrameConfig.maxRenderDistance(), nbt.getInt(RENDER_DISTANCE));
        flipX = nbt.getBoolean(FLIP_X);
        flipY = nbt.getBoolean(FLIP_Y);
        alpha = nbt.contains(ALPHA) ? nbt.getFloat(ALPHA) : 1;
        brightness = nbt.contains(BRIGHTNESS) ? nbt.getFloat(BRIGHTNESS) : 1;
        volume = nbt.getInt(VOLUME);
        minVolumeDistance = nbt.contains(VOL_RANGE_MIN) ? nbt.getInt(VOL_RANGE_MIN) : 5;
        maxVolumeDistance = nbt.contains(VOL_RANGE_MAX) ? nbt.getInt(VOL_RANGE_MAX) : 25;
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
        if (FrameConfig.canUse(player, url)) {
            if (!block.getUrl().equals(url)) {
                block.data.tick = 0;
                block.data.tickMax = -1;
            }
            block.setUrl(url);

            float width = (float) FrameTools.minFloat(FrameConfig.maxWidth(), nbt.getFloat("width"));
            float height = (float) FrameTools.minFloat(FrameConfig.maxHeight(), nbt.getFloat("height"));
            int posX = nbt.getByte("pos_x");
            int posY = nbt.getByte("pos_y");
            if (posX == 0) {
                block.data.min.x = 0;
                block.data.max.x = width;
            } else if (posX == 1) {
                float middle = width / 2;
                block.data.min.x = 0.5F - middle;
                block.data.max.x = 0.5F + middle;
            } else {
                block.data.min.x = 1 - width;
                block.data.max.x = 1;
            }

            if (posY == 0) {
                block.data.min.y = 0;
                block.data.max.y = height;
            } else if (posY == 1) {
                float middle = height / 2;
                block.data.min.y = 0.5F - middle;
                block.data.max.y = 0.5F + middle;
            } else {
                block.data.min.y = 1 - height;
                block.data.max.y = 1;
            }

            block.data.flipX = nbt.getBoolean(FLIP_X);
            block.data.flipY = nbt.getBoolean(FLIP_Y);
            block.data.rotation = nbt.getFloat(ROTATION);
            block.data.alpha = nbt.getFloat(ALPHA);
            block.data.brightness = nbt.getFloat(BRIGHTNESS);
            block.data.renderDistance = Math.min(FrameConfig.maxRenderDistance(), nbt.getInt(RENDER_DISTANCE));
            block.data.loop = nbt.getBoolean(LOOP);
            block.data.volume = Math.min(FrameConfig.maxAudioVolume(), nbt.getInt(VOLUME));
            block.data.minVolumeDistance = nbt.getInt(VOL_RANGE_MIN);
            block.data.maxVolumeDistance = Math.min(FrameConfig.maxAudioDistance(), nbt.getInt(VOL_RANGE_MAX));
            if (block.data.minVolumeDistance > block.data.maxVolumeDistance) block.data.maxVolumeDistance = block.data.minVolumeDistance;
            if (extra != null) extra.set(block.data);
        }

        block.setDirty();
    }

    public interface ExtraData<T extends DisplayData> {
        void set(T data);
    }
}