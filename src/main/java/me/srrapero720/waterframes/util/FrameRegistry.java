package me.srrapero720.waterframes.util;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.FrameBlock;
import me.srrapero720.waterframes.common.block.ProjectorBlock;
import me.srrapero720.waterframes.common.block.entity.FrameTile;
import me.srrapero720.waterframes.common.block.entity.ProjectorTile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class FrameRegistry {
    public static final DeferredRegister<Item> ITEMS = reg(ForgeRegistries.ITEMS);
    public static final DeferredRegister<Block> BLOCKS =  reg(ForgeRegistries.BLOCKS);
    public static final DeferredRegister<BlockEntityType<?>> TILES = reg(ForgeRegistries.BLOCK_ENTITIES);

    /* TABS */
    public static final CreativeModeTab TAB = tab(new ResourceLocation(WaterFrames.ID, "frame"));

    /* BLOCKS */
    public static final RegistryObject<FrameBlock> FRAME = BLOCKS.register("frame", () -> new FrameBlock());
    public static final RegistryObject<ProjectorBlock> PROJECTOR = BLOCKS.register("projector", ProjectorBlock::new);
    public static final RegistryObject<ProjectorBlock> DARK_PROJECTOR = BLOCKS.register("dark_projector", ProjectorBlock::new);

    /* ITEMS */
    public static final RegistryObject<Item> FRAME_ITEM = ITEMS.register("frame", () -> new BlockItem(FRAME.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> PROJECTOR_ITEM = ITEMS.register("projector", () -> new BlockItem(PROJECTOR.get(), new Item.Properties().tab(TAB)));

    /* TILES */
    public static final RegistryObject<BlockEntityType<FrameTile>> TILE_FRAME = tile("frame", FrameTile::new, FRAME);
    public static final RegistryObject<BlockEntityType<ProjectorTile>> TILE_PROJECTOR = tile("projector", ProjectorTile::new, PROJECTOR);

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
    }

    private static <B extends IForgeRegistryEntry<B>> DeferredRegister<B> reg(IForgeRegistry<B> registry) {
        return DeferredRegister.create(registry, WaterFrames.ID);
    }

    private static <T extends BlockEntity, B extends BaseEntityBlock> RegistryObject<BlockEntityType<T>> tile(String name, BlockEntityType.BlockEntitySupplier<? extends T> creator, Supplier<B> block) {
        return TILES.register(name, () -> BlockEntityType.Builder.<T>of(creator, block.get()).build(null));
    }

    private static CreativeModeTab tab(ResourceLocation location) {
        return new CreativeModeTab(WaterFrames.ID) {
            @Override
            public @NotNull ItemStack makeIcon() {
                return new ItemStack(ForgeRegistries.ITEMS.getValue(location));
            }
        };
    }
}