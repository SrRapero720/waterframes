package me.srrapero720.waterframes.common.block.data;

import me.srrapero720.waterframes.common.block.entity.FrameTile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;

/* HERE STARTS ALL IMPL CLASSES */
public class FrameData extends DisplayData {
    public static final String VISIBLE_FRAME = "frame_visibility";
    public static final String RENDER_BOTH_SIDES = "render_both";

    public boolean frameVisibility = true;
    public boolean renderBothSides = false;

    @Override
    public void save(CompoundTag nbt) {
        super.save(nbt);
        nbt.putBoolean(VISIBLE_FRAME, frameVisibility);
        nbt.putBoolean(RENDER_BOTH_SIDES, renderBothSides);
    }

    public void load(CompoundTag nbt) {
        super.load(nbt);
        frameVisibility = nbt.getBoolean(VISIBLE_FRAME);
        renderBothSides = nbt.getBoolean(RENDER_BOTH_SIDES);

        switch (nbt.getInt(DATA_V)) {
            case 1 -> {

            }

            default -> { // NO EXISTS
                frameVisibility = nbt.getBoolean("visibleFrame");
                renderBothSides = nbt.getBoolean("bothSides");
            }
        }
    }

    public static CompoundTag build(GuiLayer gui) {
        CompoundTag nbt = DisplayData.build(gui);
        GuiCheckBox frameVisibility = gui.get(VISIBLE_FRAME);
        nbt.putBoolean(VISIBLE_FRAME, frameVisibility.value);

        GuiCheckBox renderBoth = gui.get(RENDER_BOTH_SIDES);
        nbt.putBoolean(RENDER_BOTH_SIDES, renderBoth.value);
        return nbt;
    }

    public static void sync(FrameTile block, Player player, CompoundTag nbt) {
        sync(block, player, nbt, data -> {
            block.data.frameVisibility = nbt.getBoolean(VISIBLE_FRAME);
            block.data.renderBothSides = nbt.getBoolean(RENDER_BOTH_SIDES);
        });
    }
}