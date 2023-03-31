package me.srrapero720.waterframes;

import me.srrapero720.waterframes.custom.blocks.BlockEntityWaterFrame;
import me.srrapero720.waterframes.custom.blocks.WaterPictureFrame;
import me.srrapero720.waterframes.watercore_supplier.DefaultTabSupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Deprecated(since = "1.18.2")
//Future replacement: WATERegister
public class LittleFramesRegistry {
    // REGISTERS
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WaterFrames.ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WaterFrames.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, WaterFrames.ID);

    // TAB
    public static final CreativeModeTab TAB = new DefaultTabSupplier("waterframes", "frame");

    // REGISTRY DATA
    public static final RegistryObject<Block> WATERFRAME_BLOCK;
    public static final RegistryObject<Item> WATERFRAME_ITEM;
    public static final RegistryObject<BlockEntityType<BlockEntityWaterFrame>> WATERFRAME_BLOCKENTITY;

    static {
         WATERFRAME_BLOCK = BLOCKS.register("frame", WaterPictureFrame::new);
         WATERFRAME_ITEM = ITEMS.register("frame", () -> new BlockItem(WATERFRAME_BLOCK.get(), new Item.Properties().tab(TAB)));
         WATERFRAME_BLOCKENTITY = BLOCK_ENTITIES.register("frame", () ->
                 BlockEntityType.Builder.of(BlockEntityWaterFrame::new, WATERFRAME_BLOCK.get()).build(null));
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }


}
