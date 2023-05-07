package me.srrapero720.waterframes.custom.blocks.properties;

import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class VisibleProperty extends BooleanProperty {
    public static VisibleProperty create() { return new VisibleProperty("frame"); }
    protected VisibleProperty(String pName) { super(pName); }
}
