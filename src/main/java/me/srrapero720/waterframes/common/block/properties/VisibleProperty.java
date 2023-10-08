package me.srrapero720.waterframes.common.block.properties;

import me.srrapero720.waterframes.common.block.FrameBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class VisibleProperty extends BooleanProperty {
    public static final VisibleProperty VISIBLE_PROPERTY = new VisibleProperty("frame");

    protected VisibleProperty(String pName) {
        super(pName);
    }
}