package me.srrapero720.waterframes.util;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.client.display.DisplayControl;
import me.srrapero720.waterframes.client.renderer.FrameRender;
import me.srrapero720.waterframes.client.renderer.ProjectorRender;
import me.srrapero720.waterframes.client.renderer.TvRender;
import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.block.FrameBlock;
import me.srrapero720.waterframes.common.block.ProjectorBlock;
import me.srrapero720.waterframes.common.block.TvBlock;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.block.entity.FrameTile;
import me.srrapero720.waterframes.common.block.entity.ProjectorTile;
import me.srrapero720.waterframes.common.block.entity.TvTile;
import me.srrapero720.waterframes.common.commands.WaterFramesCommand;
import me.srrapero720.waterframes.common.item.RemoteControl;
import me.srrapero720.waterframes.common.network.DisplaysNet;
import me.srrapero720.waterframes.util.events.ClientPauseUpdateEvent;
import me.srrapero720.waterframes.util.exceptions.IllegalModException;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

public class FrameRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WaterFrames.ID);
    public static final DeferredRegister<Block> BLOCKS =  DeferredRegister.create(ForgeRegistries.BLOCKS, WaterFrames.ID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, WaterFrames.ID);
    public static final CreativeModeTab TAB = new CreativeModeTab(WaterFrames.ID) {
        @Override public ItemStack makeIcon() { return new ItemStack(FRAME_ITEM.get()); }
    };

    /* BLOCKS */
    public static final RegistryObject<DisplayBlock>
            FRAME = BLOCKS.register("frame", FrameBlock::new),
            PROJECTOR = BLOCKS.register("projector", ProjectorBlock::new),
            TV = BLOCKS.register("tv", TvBlock::new);

    /* ITEMS */
    public static final RegistryObject<Item>
            REMOTE_ITEM = ITEMS.register("remote", () -> new RemoteControl(new Item.Properties().tab(TAB))),
            FRAME_ITEM = ITEMS.register("frame", () -> new BlockItem(FRAME.get(), new Item.Properties().tab(TAB))),
            PROJECTOR_ITEM = ITEMS.register("projector", () -> new BlockItem(PROJECTOR.get(), new Item.Properties().tab(TAB))),
            TV_ITEM = ITEMS.register("tv", () -> new BlockItem(TV.get(), new Item.Properties().tab(TAB)));

    /* TILES */
    public static final RegistryObject<BlockEntityType<DisplayTile>>
            TILE_FRAME = tile("frame", FrameTile::new, FRAME),
            TILE_PROJECTOR = tile("projector", ProjectorTile::new, PROJECTOR),
            TILE_TV = tile("tv", TvTile::new, TV);

    public static void init(IEventBus bus) {
        if (FMLLoader.getDist().isClient()) {
            bus.addListener(Client::init);
        }
        bus.addListener(Common::init);

        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
    }

    private static RegistryObject<BlockEntityType<DisplayTile>> tile(String name, BlockEntityType.BlockEntitySupplier<DisplayTile> creator, Supplier<DisplayBlock> block) {
        return TILES.register(name, () -> BlockEntityType.Builder.of(creator, block.get()).build(null));
    }

    @Mod.EventBusSubscriber(modid = WaterFrames.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    private static class Common {
        private static void init(FMLCommonSetupEvent event) { common(); }
        private static void common() {
            DisplaysNet.register();
            if (FrameTools.isLoadingMod("stellarity")) {
                throw new IllegalModException("stellarity", "breaks displays rendering");
            }
            if (FrameTools.isLoadingMod("xenon")) {
                throw new IllegalModException("xenon", "is not supported", "Embeddium or Sodium");
            }
        }

        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            WaterFramesCommand.register(event.getDispatcher());
        }
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WaterFrames.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    private static class Client {
        private static void init(FMLClientSetupEvent event) { client(); }

        @OnlyIn(Dist.CLIENT)
        private static void client() {
            BlockEntityRenderers.register(FrameRegistry.TILE_FRAME.get(), (x) -> new FrameRender());
            BlockEntityRenderers.register(FrameRegistry.TILE_PROJECTOR.get(), (x) -> new ProjectorRender());
            BlockEntityRenderers.register(FrameRegistry.TILE_TV.get(), (x) -> new TvRender());
        }

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onUnloadingLevel(WorldEvent.Unload event) {
            LevelAccessor level = event.getWorld();
            if (level != null && level.isClientSide()) DisplayControl.release();
        }

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) FrameTools.tick();
        }

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onPause(ClientPauseUpdateEvent event) {
            if (event.isPaused()) DisplayControl.pause();
        }
    }
}