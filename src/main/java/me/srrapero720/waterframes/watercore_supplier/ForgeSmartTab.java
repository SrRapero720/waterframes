package me.srrapero720.waterframes.watercore_supplier;

import jdk.jfr.Experimental;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@Experimental
public class ForgeSmartTab extends CreativeModeTab {
    @SuppressWarnings("DataFlowIssue")
    public ForgeSmartTab(Component label, ResourceLocation registry) {
        super(new Builder(Row.BOTTOM, 16).icon(() -> new ItemStack(ForgeRegistries.ITEMS.getValue(registry))).title(label));
    }
}
