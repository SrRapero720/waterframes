package me.srrapero720.waterframes;

import me.srrapero720.watercore.internal.WConfig;
import me.srrapero720.watercore.internal.WRegistry;
import me.srrapero720.waterframes.custom.blocks.TileFrame;
import me.srrapero720.waterframes.custom.blocks.Frame;
import me.srrapero720.waterframes.custom.displayers.texture.TextureCache;
import me.srrapero720.waterframes.custom.packets.FramesPacket;
import me.srrapero720.waterframes.custom.render.FramesRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.creative.creativecore.common.network.CreativeNetwork;

@Mod(WaterFrames.ID)
public class WaterFrames {
    public static final String ID = "waterframes";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    public static final WRegistry REGISTRY = new WRegistry(ID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1.2", LOGGER, new ResourceLocation(ID, "main"));

    public static IEventBus bus() { return FMLJavaModLoadingContext.get().getModEventBus(); }
    public WaterFrames() {
        this.register();

        WaterFrames.bus().addListener(this::common);
        if (FMLEnvironment.dist.isClient()) WaterFrames.bus().addListener(this::client);
    }

    public void register() {
        REGISTRY.register(WRegistry.Type.BLOCKS, new ResourceLocation(ID, "frame"), Frame::new);
        REGISTRY.register(WRegistry.Type.ITEM, new ResourceLocation(ID, "frame"), () ->
                new BlockItem(REGISTRY.blockOnly("frame"), new Item.Properties().tab(WRegistry.tab("main"))));

        REGISTRY.register(WRegistry.Type.BLOCK_ENTITIES, new ResourceLocation(ID, "frame"), () ->
                BlockEntityType.Builder.of(TileFrame::new, REGISTRY.blockOnly("frame")));
        REGISTRY.register(bus());

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER, WConfig.SPEC, "waterframes-server.toml");
    }


    public void common(final FMLCommonSetupEvent event) {
        NETWORK.registerType(FramesPacket.class, FramesPacket::new);
    }

    @OnlyIn(Dist.CLIENT)
    public void client(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(TextureCache.class);
        BlockEntityRenderers.register((BlockEntityType<TileFrame>) REGISTRY.blockEntityOnly("frame"), FramesRenderer::new);
    }
}
