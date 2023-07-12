package me.srrapero720.waterframes;

import me.srrapero720.waterframes.common.blocks.Frame;
import me.srrapero720.waterframes.common.blocks.Projector;
import me.srrapero720.waterframes.common.packets.FramesPacket;
import me.srrapero720.waterframes.client.renderer.FramesRenderer;
import me.srrapero720.waterframes.client.renderer.ProjectorRenderer;
import me.srrapero720.waterframes.common.tiles.BEFrame;
import me.srrapero720.waterframes.common.tiles.BEProjector;
import me.srrapero720.waterframes.api.displays.TextureData;
import me.srrapero720.waterframes.api.displays.VideoDisplay;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
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
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = WaterFrames.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WFRegistry {
    /* REGISTERS */
    private static final ForgeSmartTab TAB = new ForgeSmartTab("waterframes", new ResourceLocation(WaterFrames.ID, "frame"));
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WaterFrames.ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WaterFrames.ID);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, WaterFrames.ID);

    /* ITEMS */


    /* BLOCKS */
    public static final RegistryObject<Frame> FRAME = BLOCKS.register("frame", Frame::new);
    public static final RegistryObject<Projector> PROJECTOR = BLOCKS.register("projector", Projector::new);

    /* BLOCK ENTITIES */
    public static final RegistryObject<BlockEntityType<BEFrame>> TILE_FRAME = TILES.register("frame", () -> BlockEntityType.Builder.of(BEFrame::new, FRAME.get()).build(null));
    public static final RegistryObject<BlockEntityType<BEProjector>> TILE_PROJECTOR = TILES.register("projector", () -> BlockEntityType.Builder.of(BEProjector::new, PROJECTOR.get()).build(null));

    public static void init() {
        ITEMS.register("frame", () -> new BlockItem(FRAME.get(), new Item.Properties().tab(TAB)));
        ITEMS.register("projector", () -> new BlockItem(PROJECTOR.get(), new Item.Properties().tab(TAB)));

        BLOCKS.register(WaterFrames.bus());
        ITEMS.register(WaterFrames.bus());
        TILES.register(WaterFrames.bus());

        WaterFrames.bus().addListener(WFRegistry::common);
        if (FMLEnvironment.dist.isClient()) WaterFrames.bus().addListener(WFRegistry::client);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, WFConfig.SPEC, "WaterFrames.toml");
    }

    public static void common(final FMLCommonSetupEvent event) {
        WaterFrames.registerPacket(FramesPacket.class, FramesPacket::new);
    }

    public static void client(final FMLClientSetupEvent event) {
        BlockEntityRenderers.register(WFRegistry.TILE_FRAME.get(), (x) -> new FramesRenderer());
        BlockEntityRenderers.register(WFRegistry.TILE_PROJECTOR.get(), (x) -> new ProjectorRenderer());
    }

    /* FORGE EVENTS */
    @SubscribeEvent
    public static void onRenderTickEvent(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) TextureData.tick();
    }

    @SubscribeEvent
    public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) VideoDisplay.tick();
    }

    @SubscribeEvent
    public static void onUnloadingLevel(WorldEvent.Unload unload) {
        if (unload.getWorld() != null && unload.getWorld().isClientSide()) {
            TextureData.unload();
            VideoDisplay.clearAll();
        }
    }

    public static final class ForgeSmartTab extends CreativeModeTab {
        private final ResourceLocation location;
        public ForgeSmartTab(String label, ResourceLocation registry) {
            super(label);
            this.location = registry;
        }

        @Override
        public @NotNull ItemStack makeIcon() { return new ItemStack(ForgeRegistries.ITEMS.getValue(this.location)); }
    }
}
