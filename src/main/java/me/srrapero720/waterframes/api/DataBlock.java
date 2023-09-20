package me.srrapero720.waterframes.api;

import me.srrapero720.waterframes.common.blockentities.BEFrame;
import me.srrapero720.waterframes.common.blockentities.BEProjector;
import me.srrapero720.waterframes.common.screen.widgets.WidgetCounterDecimal;
import me.srrapero720.waterframes.core.WaterConfig;
import me.srrapero720.waterframes.core.tools.MathTool;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.controls.simple.*;
import team.creative.creativecore.common.util.math.vec.Vec2f;

public abstract class DataBlock {
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

    public int volume = 1;
    public int minVolumeDistance = 5;
    public int maxVolumeDistance = 20;

    public boolean loop = true;
    public boolean playing = true;
    public int tick = 0;

    public int getPosX() { return min.x == 0 ? 0 : max.x == 1 ? 2 : 1; }
    public int getPosY() { return min.y == 0 ? 0 : max.y == 1 ? 2 : 1; }

    public void save(CompoundTag nbt) {
        nbt.putString("url", url);
        nbt.putFloat("min_x", min.x);
        nbt.putFloat("min_y", min.y);
        nbt.putFloat("max_x", max.x);
        nbt.putFloat("max_y", max.y);
        nbt.putFloat("rotation", rotation);
        nbt.putInt("render_distance", renderDistance);
        nbt.putBoolean("flip_x", flipX);
        nbt.putBoolean("flip_y", flipY);
        nbt.putFloat("alpha", alpha);
        nbt.putFloat("brightness", brightness);
        nbt.putInt("volume", volume);
        nbt.putInt("volume_min_range", minVolumeDistance);
        nbt.putInt("volume_max_range", maxVolumeDistance);
        nbt.putBoolean("playing", playing);
        nbt.putInt("tick", tick);
        nbt.putBoolean("loop", loop);
    }

    public void load(CompoundTag nbt) {
        url = nbt.getString("url");
        min.x = nbt.getFloat("min_x");
        min.y = nbt.getFloat("min_y");
        max.x = nbt.getFloat("max_x");
        max.y = nbt.getFloat("max_y");
        rotation = nbt.getFloat("rotation");
        renderDistance = Math.min(WaterConfig.maxRenderDistance(), nbt.getInt("render_distance"));
        flipX = nbt.getBoolean("flip_x");
        flipY = nbt.getBoolean("flip_y");
        alpha = nbt.contains("alpha") ? nbt.getFloat("alpha") : 1;
        brightness = nbt.contains("brightness") ? nbt.getFloat("brightness") : 1;
        volume = nbt.getInt("volume");
        minVolumeDistance = nbt.contains("volume_min_range") ? nbt.getInt("volume_min_range") : 5;
        maxVolumeDistance = nbt.contains("volume_max_range") ? nbt.getInt("volume_max_range") : 25;
        playing = nbt.getBoolean("playing");
        tick = nbt.getInt("tick");
        loop = nbt.getBoolean("loop");
    }


    public static CompoundTag build(GuiLayer gui) {
        CompoundTag nbt = new CompoundTag();

        GuiTextfield url = gui.get("url");
        nbt.putString("url", url.getText());

        WidgetCounterDecimal width = gui.get("width");
        WidgetCounterDecimal height = gui.get("height");
        GuiStateButton buttonPosX = gui.get("pos_x");
        GuiStateButton buttonPosY = gui.get("pos_y");
        nbt.putFloat("width", Math.max(0.1F, width.getValue()));
        nbt.putFloat("height", Math.max(0.1F, height.getValue()));
        nbt.putByte("pos_x",  (byte) buttonPosX.getState());
        nbt.putByte("pos_y", (byte) buttonPosY.getState());


        GuiCheckBox flipX = gui.get("flip_x");
        GuiCheckBox flipY = gui.get("flip_y");
        nbt.putBoolean("flip_x", flipX.value);
        nbt.putBoolean("flip_y", flipY.value);

        GuiSlider rotation = gui.get("rotation");
        nbt.putFloat("rotation", (float) rotation.value);

        GuiSlider alpha = gui.get("alpha");
        nbt.putFloat("alpha", (float) alpha.value);

        GuiSlider brightness = gui.get("brightness");
        nbt.putFloat("brightness", (float) brightness.value);

        GuiSteppedSlider renderDistance = gui.get("render_distance");
        nbt.putInt("render_distance", (int) renderDistance.value);

        GuiCheckBox loop = gui.get("loop");
        nbt.putBoolean("loop", loop.value);

        GuiSlider volume = gui.get("volume");
        nbt.putInt("volume", (int) volume.value);

        GuiSteppedSlider min = gui.get("volume_min_range");
        GuiSteppedSlider max = gui.get("volume_max_range");
        nbt.putInt("volume_min_range", min.getValue());
        nbt.putInt("volume_max_range", max.getValue());

        return nbt;
    }

    public static <D extends DataBlock, T extends BEDisplay<?>> void sync(T block, Player player, CompoundTag nbt, ExtraData<D> extra) {
        String url = nbt.getString("url");
        if (WaterConfig.canUse(player, url)) {
            if (!block.getUrl().equals(url)) block.data.tick = 0;
            block.setUrl(url);

            float width = (float) MathTool.min(WaterConfig.maxWidth(), nbt.getFloat("width"));
            float height = (float) MathTool.min(WaterConfig.maxHeight(), nbt.getFloat("height"));
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

            block.data.flipX = nbt.getBoolean("flip_x");
            block.data.flipY = nbt.getBoolean("flip_y");
            block.data.rotation = nbt.getFloat("rotation");
            block.data.alpha = nbt.getFloat("alpha");
            block.data.brightness = nbt.getFloat("brightness");
            block.data.renderDistance = Math.min(WaterConfig.maxRenderDistance(), nbt.getInt("render_distance"));
            block.data.loop = nbt.getBoolean("loop");
            block.data.volume = Math.min(WaterConfig.maxAudioVolume(), nbt.getInt("volume"));
            block.data.minVolumeDistance = nbt.getInt("volume_min_range");
            block.data.maxVolumeDistance = Math.min(WaterConfig.maxAudioDistance(), nbt.getInt("volume_max_range"));
            // PREVENTS HACKED VERSIONS OF WF DO SHIT
            if (block.data.minVolumeDistance > block.data.maxVolumeDistance) block.data.maxVolumeDistance = block.data.minVolumeDistance;
            if (extra != null) extra.set((D) block.data);
        }

        block.setDirty();
    }

    /* HERE STARTS ALL IMPL CLASSES */
    public static class Frame extends DataBlock {
        public static final String VISIBLE_FRAME = "frame_visibility";
        public static final String RENDER_BOTH_SIDES = "render_both";

        public boolean visibleFrame = true;
        public boolean bothSides = false;

        @Override
        public void save(CompoundTag nbt) {
            super.save(nbt);
            nbt.putBoolean("frame_visibility", visibleFrame);
            nbt.putBoolean("render_both", bothSides);
        }

        public void load(CompoundTag nbt) {
            super.load(nbt);
            visibleFrame = nbt.getBoolean("frame_visibility");
            bothSides = nbt.getBoolean("render_both");
        }

        public static CompoundTag build(GuiLayer gui) {
            CompoundTag nbt = DataBlock.build(gui);
            GuiCheckBox frameVisibility = gui.get("frame_visibility");
            nbt.putBoolean("frame_visibility", frameVisibility.value);

            GuiCheckBox renderBoth = gui.get("render_both");
            nbt.putBoolean("render_both", renderBoth.value);
            return nbt;
        }

        public static void sync(BEFrame block, Player player, CompoundTag nbt) {
            DataBlock.sync(block, player, nbt, data -> {
                block.data.visibleFrame = nbt.getBoolean("frame_visibility");
                block.data.bothSides = nbt.getBoolean("render_both");
            });
        }
    }

    public static class Projector extends DataBlock {
        public static final String PROJECTION_DISTANCE = "projection_distance";
        public static final String AUDIO_ORIGIN = "audio_origin";

        public int projectionDistance = 8;
        public int audioOrigin = 0; // 0 = block - 1 = center - 2 = projection

        @Override
        public void save(CompoundTag nbt) {
            super.save(nbt);
            nbt.putInt("projection_distance", projectionDistance);
            nbt.putInt("audio_origin", audioOrigin);
        }

        @Override
        public void load(CompoundTag nbt) {
            super.load(nbt);
            projectionDistance = nbt.contains("projection_distance") ? nbt.getInt("projection_distance") : projectionDistance;
            audioOrigin = nbt.contains("audio_origin") ? nbt.getInt("audio_origin") : audioOrigin;
        }

        public static CompoundTag build(GuiLayer gui) {
            CompoundTag nbt = DataBlock.build(gui);
            GuiStateButton audioCenter = gui.get("audio_origin");
            nbt.putInt("audio_origin", audioCenter.getState());

            GuiSteppedSlider projection_distance = gui.get("projection_distance");
            nbt.putInt("projection_distance", (int) projection_distance.value);
            return nbt;
        }

        public static void sync(BEProjector block, Player player, CompoundTag nbt) {
            DataBlock.sync(block, player, nbt, data -> {
                block.data.audioOrigin = nbt.getInt("audio_origin");
                block.data.projectionDistance = nbt.getInt("projection_distance");
            });
        }
    }

    public static class BoomBox extends DataBlock {
        @Override
        public void save(CompoundTag nbt) {
            super.save(nbt);
        }

        @Override
        public void load(CompoundTag nbt) {
            super.load(nbt);
        }
    }

    public static class TV extends DataBlock {
        @Override
        public void save(CompoundTag nbt) {
            super.save(nbt);
        }

        @Override
        public void load(CompoundTag nbt) {
            super.load(nbt);
        }
    }

    public interface ExtraData<T extends DataBlock> {
        void set(T data);
    }
}