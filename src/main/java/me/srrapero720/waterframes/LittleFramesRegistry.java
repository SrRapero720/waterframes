package me.srrapero720.waterframes;

import me.srrapero720.waterframes.watercore_supplier.DefaultTabSupplier;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.srrapero720.watercore.SrRegistry;
import net.srrapero720.watercore.custom.tabs.DefaultTab;
import me.srrapero720.waterframes.custom.blocks.BlockEntityWaterFrame;
import me.srrapero720.waterframes.custom.blocks.WaterPictureFrame;

import java.util.function.Supplier;

@Deprecated(since = "1.18.2")
//Future replacement: WATERegister
public class LittleFramesRegistry {
    static {
    }

    // ITEMS
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WaterFrames.ID);
    public static final CreativeModeTab TAB = new DefaultTabSupplier("waterframes", "waterframe");
    
    // BLOCKS
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WaterFrames.ID);
    public static final RegistryObject<Block> CREATIVE_PICTURE_FRAME = register("waterframe", () -> new WaterPictureFrame());
    
    private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup) {
        RegistryObject<T> ret = BLOCKS.register(name, sup);
        ITEMS.register(name, () -> new BlockItem(ret.get(), new Item.Properties().tab(TAB)));
        return ret;
    }
    
    // BLOCK_ENTITY
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, WaterFrames.ID);
    
    public static final RegistryObject<BlockEntityType<BlockEntityWaterFrame>> BE_CREATIVE_FRAME = registerBlockEntity("waterframe", () -> BlockEntityType.Builder
            .of(BlockEntityWaterFrame::new, CREATIVE_PICTURE_FRAME.get()));
    
    public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> registerBlockEntity(String name, Supplier<BlockEntityType.Builder<T>> sup) {
        return BLOCK_ENTITIES.register(name, () -> sup.get().build(Util.fetchChoiceType(References.BLOCK_ENTITY, name)));
    }
    
}
