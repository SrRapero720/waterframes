package me.srrapero720.waterframes;

import me.srrapero720.waterframes.client.rendering.TextureWrapper;
import me.srrapero720.waterframes.client.rendering.DisplayRenderer;
import me.srrapero720.waterframes.common.block.*;
import me.srrapero720.waterframes.common.block.entity.*;
import me.srrapero720.waterframes.common.commands.WaterFramesCommand;
import me.srrapero720.waterframes.common.item.RemoteControl;
import me.srrapero720.waterframes.common.network.packets.*;
import me.srrapero720.watermedia.api.image.ImageAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.resource.PathResourcePack;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.function.Supplier;

import static me.srrapero720.waterframes.common.network.DisplayNetwork.*;
import static me.srrapero720.waterframes.WaterFrames.*;

@Mod.EventBusSubscriber(modid = ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WFRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    public static final DeferredRegister<Block> BLOCKS =  DeferredRegister.create(ForgeRegistries.BLOCKS, ID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ID);
    public static final CreativeModeTab TAB = new CreativeModeTab(ID) {
        @Override public ItemStack makeIcon() { return new ItemStack(FRAME_ITEM.get()); }
    };

    /* BLOCKS */
    public static final RegistryObject<DisplayBlock>
            FRAME = BLOCKS.register("frame", FrameBlock::new),
            PROJECTOR = BLOCKS.register("projector", ProjectorBlock::new),
            TV = BLOCKS.register("tv", TvBlock::new),
            BIG_TV = BLOCKS.register("big_tv", BigTvBlock::new);

    /* ITEMS */
    public static final RegistryObject<Item>
            REMOTE_ITEM = ITEMS.register("remote", () -> new RemoteControl(new Item.Properties().tab(TAB))),
            FRAME_ITEM = ITEMS.register("frame", () -> new BlockItem(FRAME.get(), prop())),
            PROJECTOR_ITEM = ITEMS.register("projector", () -> new BlockItem(PROJECTOR.get(), prop())),
            TV_ITEM = ITEMS.register("tv", () -> new BlockItem(TV.get(), prop())),
            BIG_TV_ITEM = ITEMS.register("big_tv", () -> new BlockItem(BIG_TV.get(), prop()));

    /* TILES */
    public static final RegistryObject<BlockEntityType<DisplayTile>>
            TILE_FRAME = tile("frame", FrameTile::new, FRAME),
            TILE_PROJECTOR = tile("projector", ProjectorTile::new, PROJECTOR),
            TILE_TV = tile("tv", TvTile::new, TV),
            TILE_BIG_TV = tile("big_tv", BigTvTile::new, BIG_TV);

    private static RegistryObject<BlockEntityType<DisplayTile>> tile(String name, BlockEntityType.BlockEntitySupplier<DisplayTile> creator, Supplier<DisplayBlock> block) {
        return TILES.register(name, () -> BlockEntityType.Builder.of(creator, block.get()).build(null));
    }

    private static Item.Properties prop() {
        return new Item.Properties().stacksTo(16).tab(TAB).rarity(Rarity.EPIC);
    }

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
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

    @OnlyIn(Dist.CLIENT)
    public static void registerTexture(ResourceLocation location, AbstractTexture texture) {
        Minecraft.getInstance().getTextureManager().register(location, texture);
    }

    @OnlyIn(Dist.CLIENT)
    public static void unregisterTexture(ResourceLocation location) {
        Minecraft.getInstance().getTextureManager().release(location);
    }

    @Mod.EventBusSubscriber(modid = WaterFrames.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
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
        public static void init(FMLClientSetupEvent e) {
            if (WaterFrames.isInstalled("mr_stellarity") && (WFConfig.isDevMode())) {
                throw new UnsupportedModException("mr_stellarity", "breaks picture rendering, overwrites the Minecraft core shaders and i can't do nothing to avoid that");
            }
        }

        @SubscribeEvent
        public static void registerResourcePacks(AddPackFindersEvent e) {
            if (e.getPackType() == PackType.CLIENT_RESOURCES) {
                IModFile modFile = ModList.get().getModFileById(ID).getFile();
                e.addRepositorySource((consumer, constructor) ->
                        consumer.accept(Pack.create(ID + "/voxeloper", false, () -> new ModPackResources("WaterFrames: Voxeloper", modFile, "resourcepacks/wf_voxeloper"), constructor, Pack.Position.TOP, PackSource.DEFAULT))
                );
            }
        }

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void registerOtherStuff(FMLClientSetupEvent e) {
            registerTexture(LOADING_ANIMATION, new TextureWrapper.Renderer(ImageAPI.loadingGif(WaterFrames.ID)));
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void registerTileRenderer(EntityRenderersEvent.RegisterRenderers e) {
            BlockEntityRenderers.register(TILE_FRAME.get(), DisplayRenderer::new);
            BlockEntityRenderers.register(TILE_PROJECTOR.get(), DisplayRenderer::new);
            BlockEntityRenderers.register(TILE_TV.get(), DisplayRenderer::new);
            BlockEntityRenderers.register(TILE_BIG_TV.get(), DisplayRenderer::new);
        }
    }

    public static class UnsupportedModException extends UnsupportedOperationException {
        private static final String MSG = "§fMod §6'%s' §fis not compatible with §e'%s'§f. please remove it";
        private static final String MSG_REASON = "§fMod §6'%s' §fis not compatible with §e'%s' §fbecause §c%s §fplease remove it";
        private static final String MSG_REASON_ALT = "§fMod §6'%s' §fis not compatible with §e'%s' §fbecause §c%s §fuse §a'%s' §finstead";

        public UnsupportedModException(String modid) {
            super(String.format(MSG, modid, NAME));
        }

        public UnsupportedModException(String modid, String reason) {
            super(String.format(MSG_REASON, modid, NAME, reason));
        }

        public UnsupportedModException(String modid, String reason, String alternatives) {
            super(String.format(MSG_REASON_ALT, modid, NAME, reason, alternatives));
        }
    }

    public static class ModPackResources extends PathResourcePack {
        protected final IModFile modFile;
        protected final String sourcePath;

        public ModPackResources(String name, IModFile modFile, String sourcePath) {
            super(name, modFile.findResource(sourcePath));
            this.modFile = modFile;
            this.sourcePath = sourcePath;
        }

        @NotNull
        protected Path resolve(String... paths) {
            String[] allPaths = new String[paths.length + 1];
            allPaths[0] = this.sourcePath;
            System.arraycopy(paths, 0, allPaths, 1, paths.length);
            return this.modFile.findResource(allPaths);
        }
    }

}