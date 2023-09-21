package me.srrapero720.waterframes.api.data;

import me.srrapero720.waterframes.custom.block.entity.ProjectorTile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;

public class ProjectorData extends BasicData {
    public static final String PROJECTION_DISTANCE = "projection_distance";
    public static final String AUDIO_ORIGIN = "audio_origin";

    public int projectionDistance = 8;
    public int audioOrigin = 0; // 0 = block - 1 = center - 2 = projection

    @Override
    public void save(CompoundTag nbt) {
        super.save(nbt);
        nbt.putInt(PROJECTION_DISTANCE, projectionDistance);
        nbt.putInt(AUDIO_ORIGIN, audioOrigin);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        projectionDistance = nbt.contains(PROJECTION_DISTANCE) ? nbt.getInt(PROJECTION_DISTANCE) : projectionDistance;
        audioOrigin = nbt.contains(AUDIO_ORIGIN) ? nbt.getInt(AUDIO_ORIGIN) : audioOrigin;
    }

    public static CompoundTag build(GuiLayer gui) {
        CompoundTag nbt = BasicData.build(gui);
        GuiStateButton audioCenter = gui.get(AUDIO_ORIGIN);
        nbt.putInt(AUDIO_ORIGIN, audioCenter.getState());

        GuiSteppedSlider projection_distance = gui.get(PROJECTION_DISTANCE);
        nbt.putInt(PROJECTION_DISTANCE, (int) projection_distance.value);
        return nbt;
    }

    public static void sync(ProjectorTile block, Player player, CompoundTag nbt) {
        BasicData.sync(block, player, nbt, data -> {
            block.data.audioOrigin = nbt.getInt(AUDIO_ORIGIN);
            block.data.projectionDistance = nbt.getInt(PROJECTION_DISTANCE);
        });
    }
}