package me.srrapero720.waterframes;

import me.srrapero720.watercore.internal.WRegistry;
import me.srrapero720.waterframes.custom.blocks.BlockEntityWaterFrame;
import me.srrapero720.waterframes.custom.blocks.WaterPictureFrame;
import me.srrapero720.waterframes.custom.packets.WaterFramePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.network.CreativeNetwork;

@Mod(WaterFrames.ID)
public class WaterFrames {
    public static final String ID = "waterframes";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    public static final WRegistry REGISTRY = new WRegistry(ID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1.1", LOGGER, new ResourceLocation(ID, "main"));

    public static LittleFramesConfig CONFIG;

    public static IEventBus bus() { return FMLJavaModLoadingContext.get().getModEventBus(); }
    public WaterFrames() {
        this.register();

        WaterFrames.bus().addListener(this::common);
        if (FMLEnvironment.dist.isClient()) WaterFrames.bus().addListener(this::client);
    }

    public void register() {
        REGISTRY.register(WRegistry.Type.BLOCKS, new ResourceLocation(ID, "frame"), WaterPictureFrame::new);
        REGISTRY.register(WRegistry.Type.ITEM, new ResourceLocation(ID, "frame"), () ->
                new BlockItem(REGISTRY.blockOnly("frame"), new Item.Properties().tab(WRegistry.tab("main"))));

        REGISTRY.register(WRegistry.Type.BLOCK_ENTITIES, new ResourceLocation(ID, "frame"), () ->
                BlockEntityType.Builder.of(BlockEntityWaterFrame::new, REGISTRY.blockOnly("frame")));
        REGISTRY.register(bus());
    }


    public void common(final FMLCommonSetupEvent event) {
        CreativeConfigRegistry.ROOT.registerValue(ID, CONFIG = new LittleFramesConfig());
        NETWORK.registerType(WaterFramePacket.class, WaterFramePacket::new);
    }

    public void client(final FMLClientSetupEvent event) {
        LittleFramesClient.setup();
    }
}
