package me.srrapero720.waterframes.watercore_supplier;

import me.srrapero720.watercore.internal.WaterRegistry;
import me.srrapero720.waterframes.LittleFramesRegistry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DefaultTabSupplier extends CreativeModeTab {
    final String iconName;

    public DefaultTabSupplier(String label, String item_registry) {
        super(label);
        this.iconName = item_registry;
    }

    public @NotNull ItemStack makeIcon() {
        return new ItemStack(LittleFramesRegistry.CREATIVE_PICTURE_FRAME.get());
    }
}
