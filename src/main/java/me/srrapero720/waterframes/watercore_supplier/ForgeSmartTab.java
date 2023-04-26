package me.srrapero720.waterframes.watercore_supplier;

import jdk.jfr.Experimental;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

@Experimental
public class ForgeSmartTab extends CreativeModeTab {
    private final ResourceLocation location;
    public ForgeSmartTab(String label, ResourceLocation registry) {
        super(label);
        this.location = registry;
    }

    @Override
    public @NotNull ItemStack makeIcon() { return new ItemStack(ForgeRegistries.ITEMS.getValue(this.location)); }
}
