package me.srrapero720.waterframes.common.data;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.common.block.entity.ProjectorTile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;

public class ProjectorData extends DisplayData {
    public static final String PROJECTION_DISTANCE = "projection_distance";
    public static final String AUDIO_OFFSET = "audio_offset";

    public int projectionDistance = Math.min(8, DisplayConfig.maxProjectionDistance());
    public float audioOffset = 0;

    @Override
    public void save(CompoundTag nbt) {
        super.save(nbt);
        nbt.putInt(PROJECTION_DISTANCE, projectionDistance);
        nbt.putFloat(AUDIO_OFFSET, audioOffset);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        projectionDistance = nbt.contains(PROJECTION_DISTANCE) ? Math.min(nbt.getInt(PROJECTION_DISTANCE), DisplayConfig.maxProjectionDistance()) : projectionDistance;
        audioOffset = nbt.contains(AUDIO_OFFSET) ? nbt.getFloat(AUDIO_OFFSET) : audioOffset;
    }

    public int getOffsetMode() {
        return (audioOffset == projectionDistance) ? 2 : (audioOffset == projectionDistance / 2f) ? 1 : 0;
    }

    public static CompoundTag build(GuiLayer gui) {
        CompoundTag nbt = DisplayData.build(gui);
        GuiSteppedSlider projection_distance = gui.get(PROJECTION_DISTANCE);
        nbt.putInt(PROJECTION_DISTANCE, (int) projection_distance.value);

        GuiStateButton audioCenter = gui.get(AUDIO_OFFSET);
        nbt.putInt("audio_offset_mode", audioCenter.getState());
        return nbt;
    }

    public static void sync(ProjectorTile block, Player player, CompoundTag nbt) {
        DisplayData.sync(block, player, nbt, data -> {
            block.data.projectionDistance = Math.min(nbt.getInt(PROJECTION_DISTANCE), DisplayConfig.maxProjectionDistance());

            int mode = nbt.getInt("audio_offset_mode");
            block.data.audioOffset = mode == 2 ? block.data.projectionDistance : mode == 1 ? block.data.projectionDistance / 2f : 0;
        });
    }
}