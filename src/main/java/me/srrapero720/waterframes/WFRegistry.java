package me.srrapero720.waterframes;

import com.google.gson.JsonParser;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import me.srrapero720.waterframes.custom.blocks.Frame;
import me.srrapero720.waterframes.custom.blocks.Projector;
import me.srrapero720.waterframes.custom.packets.FramesPacket;
import me.srrapero720.waterframes.custom.renderer.FramesRenderer;
import me.srrapero720.waterframes.custom.renderer.ProjectorRenderer;
import me.srrapero720.waterframes.custom.tiles.TileFrame;
import me.srrapero720.waterframes.custom.tiles.TileProjector;
import me.srrapero720.waterframes.display.VideoDisplay;
import me.srrapero720.waterframes.display.texture.TextureCache;
import me.srrapero720.waterframes.watercore_supplier.ForgeSmartTab;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.io.IOUtils;

@Mod.EventBusSubscriber(modid = WaterFrames.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WFRegistry {
    public static final ForgeSmartTab TAB = new ForgeSmartTab(WaterFrames.ID, new ResourceLocation(WaterFrames.ID, "frame"));
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WaterFrames.ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WaterFrames.ID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WaterFrames.ID);

    public static RegistryObject<Frame> FRAME = BLOCKS.register("frame", Frame::new);
    public static RegistryObject<BlockEntityType<TileFrame>> TILE_FRAME = TILES.register("frame", () -> BlockEntityType.Builder.of(TileFrame::new, FRAME.get()).build(null));

    public static void register() {
        ITEMS.register("frame", () -> new BlockItem(FRAME.get(), new Item.Properties().tab(TAB)));
        BLOCKS.register(WaterFrames.bus());
        ITEMS.register(WaterFrames.bus());
        TILES.register(WaterFrames.bus());

        WaterFrames.bus().addListener(WFRegistry::common);
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

    @SubscribeEvent
    public static void onRenderTickEvent(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) TextureCache.renderTick();
    }

    @SubscribeEvent
    public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            VideoDisplay.tick();
            TextureCache.clientTick();
        }
    }

    @SubscribeEvent
    public static void onUnloadingLevel(LevelEvent.Unload unload) {
        if (unload.getLevel() != null && unload.getLevel().isClientSide()) {
            TextureCache.unload();
            VideoDisplay.unload();
        }
    }
}
