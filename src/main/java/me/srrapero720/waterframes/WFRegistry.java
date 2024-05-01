package me.srrapero720.waterframes;

import me.srrapero720.waterframes.client.rendering.TextureWrapper;
import me.srrapero720.waterframes.client.rendering.DisplayRenderer;
import me.srrapero720.waterframes.common.block.*;
import me.srrapero720.waterframes.common.block.entity.*;
import me.srrapero720.waterframes.common.commands.WaterFramesCommand;
import me.srrapero720.waterframes.common.item.RemoteControl;
import me.srrapero720.waterframes.common.item.data.CodecManager;
import me.srrapero720.waterframes.common.item.data.RemoteData;
import me.srrapero720.waterframes.common.network.packets.*;
import me.srrapero720.watermedia.api.image.ImageAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforgespi.locating.IModFile;

import java.util.function.Supplier;

import static me.srrapero720.waterframes.common.network.DisplayNetwork.*;
import static me.srrapero720.waterframes.WaterFrames.*;
import static me.srrapero720.watermedia.WaterMedia.IT;

@EventBusSubscriber(modid = ID, bus = EventBusSubscriber.Bus.GAME)
public class WFRegistry {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final DeferredRegister.Blocks BLOCKS =  DeferredRegister.createBlocks(ID);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    public static final DeferredRegister<DataComponentType<?>> DATA = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ID);

    /* DATA */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RemoteData>> REMOTE_DATA = DATA.register("remote", () -> new DataComponentType.Builder<RemoteData>()
                    .persistent(CodecManager.REMOTE_CODEC)
                    .networkSynchronized(CodecManager.REMOTE_STREAM_CODEC)
                    .build()
    );

    /* BLOCKS */
    public static final DeferredBlock<DisplayBlock>
            FRAME = BLOCKS.register("frame", () -> new FrameBlock()),
            PROJECTOR = BLOCKS.register("projector", () -> new ProjectorBlock()),
            TV = BLOCKS.register("tv", () -> new TvBlock()),
            BIG_TV = BLOCKS.register("big_tv", () -> new BigTvBlock()),
            TV_BOX = BLOCKS.register("tv_box", () -> new TVBoxBlock());
//            GOLDEN_PROJECTOR = BLOCKS.register("golden_projector", ProjectorBlock::new);

    /* ITEMS */
    public static final DeferredItem<Item>
            REMOTE_ITEM = ITEMS.register("remote", () -> new RemoteControl(remoteProp())),
            FRAME_ITEM = ITEMS.register("frame", () -> new BlockItem(FRAME.get(), prop())),
            PROJECTOR_ITEM = ITEMS.register("projector", () -> new BlockItem(PROJECTOR.get(), prop())),
            TV_ITEM = ITEMS.register("tv", () -> new BlockItem(TV.get(), prop())),
            BIG_TV_ITEM = ITEMS.register("big_tv", () -> new BlockItem(BIG_TV.get(), prop())),
            TV_BOX_ITEM = ITEMS.register("tv_box", () -> new BlockItem(TV_BOX.get(), prop()));
//            GOLDEN_PROJECTOR_ITEM = ITEMS.register("golden_projector", () -> new BlockItem(GOLDEN_PROJECTOR.get(), prop().tab(null)));

    /* TILES */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DisplayTile>>
            TILE_FRAME = tile("frame", FrameTile::new, FRAME),
            TILE_PROJECTOR = tile("projector", ProjectorTile::new, PROJECTOR),
            TILE_TV = tile("tv", TvTile::new, TV),
            TILE_BIG_TV = tile("big_tv", BigTvTile::new, BIG_TV),
            TILE_TV_BOX = tile("tv_box", TVBoxTile::new, TV_BOX);
//            TILE_GOLDEN_PROJECTOR = tile("golden_projector", ProjectorTile::new, GOLDEN_PROJECTOR);

    /* TABS */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WATERTAB = TABS.register("tab", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
            .icon(() -> new ItemStack(FRAME.get()))
            .title(Component.translatable("itemGroup.waterframes"))
            .build()
    );

    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<DisplayTile>> tile(String name, BlockEntityType.BlockEntitySupplier<DisplayTile> creator, Supplier<DisplayBlock> block) {
        return TILES.register(name, () -> BlockEntityType.Builder.of(creator, block.get()).build(null));
    }

    private static Item.Properties remoteProp() {
        return new Item.Properties().stacksTo(1).rarity(Rarity.RARE).setNoRepair().fireResistant();
    }

    private static Item.Properties prop() {
        return new Item.Properties().stacksTo(16).rarity(Rarity.RARE);
    }

    public static void init(IEventBus bus, ModContainer container) {
        DATA.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
        TABS.register(bus);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        WaterFramesCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerCommands(RegisterClientCommandsEvent event) {
        WaterFramesCommand.registerClient(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerConnects(PlayerEvent.PlayerLoggedInEvent event) {
        var playername = event.getEntity().getGameProfile().getName();
        if (playername.equals("Belupe_")) { // Belupe_: Anti-license reinforcement
            event.getEntity().getServer().execute(() -> {
                throw new UnsupportedOperationException("Belupe_ is not allowed to use this mod");
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerTexture(ResourceLocation location, AbstractTexture texture) {
        Minecraft.getInstance().getTextureManager().register(location, texture);
    }

    @OnlyIn(Dist.CLIENT)
    public static void unregisterTexture(ResourceLocation location) {
        Minecraft.getInstance().getTextureManager().release(location);
    }

    @EventBusSubscriber(modid = WaterFrames.ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onCreativeTabsLoading(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == WATERTAB.getKey()) {
                event.accept(REMOTE_ITEM, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                event.accept(FRAME_ITEM, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                event.accept(PROJECTOR_ITEM, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                event.accept(TV_ITEM, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                event.accept(BIG_TV_ITEM, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                event.accept(TV_BOX_ITEM, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }

        @SubscribeEvent
        public static void init(FMLCommonSetupEvent event) {
            NET.registerType(DataSyncPacket.class, DataSyncPacket::new);
            NET.registerType(ActivePacket.class, ActivePacket::new);
            NET.registerType(LoopPacket.class, LoopPacket::new);
            NET.registerType(MutePacket.class, MutePacket::new);
            NET.registerType(PausePacket.class, PausePacket::new);
            NET.registerType(TimePacket.class, TimePacket::new);
            NET.registerType(VolumePacket.class, VolumePacket::new);
            NET.registerType(VolumeRangePacket.class, VolumeRangePacket::new);
        }

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void init(FMLClientSetupEvent e) {
            LOGGER.info(IT, "Running WATERFrAMES v{}", ModList.get().getModFileById(ID).versionString());
        }

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void registerOtherStuff(FMLClientSetupEvent e) {
            registerTexture(LOADING_ANIMATION, new TextureWrapper.Renderer(ImageAPI.loadingGif(WaterFrames.ID)));
        }

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void registerTileRenderer(EntityRenderersEvent.RegisterRenderers e) {
            BlockEntityRenderers.register(TILE_FRAME.get(), DisplayRenderer::new);
            BlockEntityRenderers.register(TILE_PROJECTOR.get(), DisplayRenderer::new);
            BlockEntityRenderers.register(TILE_TV.get(), DisplayRenderer::new);
            BlockEntityRenderers.register(TILE_BIG_TV.get(), DisplayRenderer::new);
            BlockEntityRenderers.register(TILE_TV_BOX.get(), DisplayRenderer::new);
        }
    }

    public static class UnsupportedModException extends UnsupportedOperationException {
        private static final String MSG_REASON = "§fMod §6'%s' §fis not compatible with §e'%s' §fbecause §c%s §fplease remove it";

        public UnsupportedModException(String modid, String reason) {
            super(String.format(MSG_REASON, modid, NAME, reason));
        }
    }
}