package me.srrapero720.waterframes;

import me.srrapero720.waterframes.custom.blocks.Frame;
import me.srrapero720.waterframes.custom.packets.FramesPacket;
import me.srrapero720.waterframes.custom.renderer.FramesRenderer;
import me.srrapero720.waterframes.custom.tiles.TileFrame;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class WFRegistry {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WaterFrames.ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WaterFrames.ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WaterFrames.ID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WaterFrames.ID);

    public static RegistryObject<Frame> FRAME = BLOCKS.register("frame", Frame::new);
    public static RegistryObject<BlockEntityType<TileFrame>> TILE_FRAME = TILES.register("frame", () -> BlockEntityType.Builder.of(TileFrame::new, FRAME.get()).build(null));
    public static final RegistryObject<CreativeModeTab> WATERTAB = TABS.register("tab", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
            .icon(() -> new ItemStack(FRAME.get()))
            .title(Component.translatable("itemGroup.waterframes")).build()
    );


    public static void register() {
        ITEMS.register("frame", () -> new BlockItem(FRAME.get(), new Item.Properties()));


        BLOCKS.register(WaterFrames.bus());
        ITEMS.register(WaterFrames.bus());
        TILES.register(WaterFrames.bus());
        TABS.register(WaterFrames.bus());

        WaterFrames.bus().addListener(WFRegistry::common);
        WaterFrames.bus().addListener(WFRegistry::creativeTabs);
        if (FMLEnvironment.dist.isClient()) WaterFrames.bus().addListener(WFRegistry::client);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, WFConfig.SPEC, "waterframes.toml");
    }

    private static void common(final FMLCommonSetupEvent event) {
        WaterFrames.NETWORK.registerType(FramesPacket.class, FramesPacket::new);
    }

    @OnlyIn(Dist.CLIENT)
    private static void client(final FMLClientSetupEvent event) {
        BlockEntityRenderers.register(WFRegistry.TILE_FRAME.get(), (x) -> new FramesRenderer());
    }

    private static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == WATERTAB.getKey()) event.accept(FRAME);
    }
}
