package me.srrapero720.waterframes;

import me.srrapero720.waterframes.custom.blocks.Frame;
import me.srrapero720.waterframes.custom.blocks.TileFrame;
import me.srrapero720.waterframes.custom.packets.FramesPacket;
import me.srrapero720.waterframes.custom.renderer.FramesRenderer;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.creative.creativecore.common.network.CreativeNetwork;

@Mod(WaterFrames.ID)
public class WaterFrames {
    public static final String ID = "waterframes";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
//    public static final WRegistry REGISTRY = new WRegistry(ID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1.2", LogManager.getLogger(ID), new ResourceLocation(ID, "main"));

    // EXTRAPOLAR
    public static final ForgeSmartTab TAB = new ForgeSmartTab("waterframes", new ResourceLocation(ID, "frame"));
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ID);

    public static RegistryObject<Frame> FRAME;
    public static RegistryObject<BlockEntityType<TileFrame>> TILE_FRAME;

    public static IEventBus bus() { return FMLJavaModLoadingContext.get().getModEventBus(); }
    public WaterFrames() {
        this.register();

        WaterFrames.bus().addListener(this::common);
        if (FMLEnvironment.dist.isClient()) WaterFrames.bus().addListener(this::client);
    }

    public void register() {
        FRAME = BLOCKS.register("frame", Frame::new);
        TILE_FRAME = TILES.register("frame", () -> BlockEntityType.Builder.of(TileFrame::new, FRAME.get()).build(null));
        ITEMS.register("frame", () -> new BlockItem(FRAME.get(), new Item.Properties().tab(TAB)));

        BLOCKS.register(bus());
        ITEMS.register(bus());
        TILES.register(bus());

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, WFConfig.SPEC, "waterframes-server.toml");
    }


    public void common(final FMLCommonSetupEvent event) {
        NETWORK.registerType(FramesPacket.class, FramesPacket::new);
    }

    @OnlyIn(Dist.CLIENT)
    public void client(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(TextureCache.class);
        BlockEntityRenderers.register(TILE_FRAME.get(), FramesRenderer::new);
    }
}
