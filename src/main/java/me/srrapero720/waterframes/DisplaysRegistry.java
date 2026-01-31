package me.srrapero720.waterframes;

import me.srrapero720.waterframes.client.rendering.TextureWrapper;
import me.srrapero720.waterframes.client.rendering.DisplayRenderer;
import me.srrapero720.waterframes.common.block.*;
import me.srrapero720.waterframes.common.block.entity.*;
import me.srrapero720.waterframes.common.commands.WaterFramesCommand;
import me.srrapero720.waterframes.common.item.RemoteControl;
import me.srrapero720.waterframes.common.network.packets.*;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;
import java.util.function.Supplier;

import static me.srrapero720.waterframes.common.network.DisplayNetwork.*;
import static me.srrapero720.waterframes.WaterFrames.*;

@Mod.EventBusSubscriber(modid = ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DisplaysRegistry {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ID);

    /* BLOCKS */
    public static final RegistryObject<DisplayBlock>
            FRAME = BLOCKS.register("frame", FrameBlock::new),
            PROJECTOR = BLOCKS.register("projector", ProjectorBlock::new),
            TV = BLOCKS.register("tv", TvBlock::new),
            BIG_TV = BLOCKS.register("big_tv", BigTvBlock::new),
            TV_BOX = BLOCKS.register("tv_box", TVBoxBlock::new);
//            GOLDEN_PROJECTOR = BLOCKS.register("golden_projector", ProjectorBlock::new);

    /* ITEMS */
    public static final RegistryObject<Item>
            REMOTE_ITEM = ITEMS.register("remote", () -> new RemoteControl(remoteProp())),
            FRAME_ITEM = ITEMS.register("frame", () -> new BlockItem(FRAME.get(), prop())),
            PROJECTOR_ITEM = ITEMS.register("projector", () -> new BlockItem(PROJECTOR.get(), prop())),
            TV_ITEM = ITEMS.register("tv", () -> new BlockItem(TV.get(), prop())),
            BIG_TV_ITEM = ITEMS.register("big_tv", () -> new BlockItem(BIG_TV.get(), prop())),
            TV_BOX_ITEM = ITEMS.register("tv_box", () -> new BlockItem(TV_BOX.get(), prop()));
//            GOLDEN_PROJECTOR_ITEM = ITEMS.register("golden_projector", () -> new BlockItem(GOLDEN_PROJECTOR.get(), prop().tab(null)));

    /* TILES */
    public static final RegistryObject<BlockEntityType<DisplayTile>>
            TILE_FRAME = tile("frame", FrameTile::new, FRAME),
            TILE_PROJECTOR = tile("projector", ProjectorTile::new, PROJECTOR),
            TILE_TV = tile("tv", TvTile::new, TV),
            TILE_BIG_TV = tile("big_tv", BigTvTile::new, BIG_TV),
            TILE_TV_BOX = tile("tv_box", TVBoxTile::new, TV_BOX);
//            TILE_GOLDEN_PROJECTOR = tile("golden_projector", ProjectorTile::new, GOLDEN_PROJECTOR);

    /* TABS */
    public static final RegistryObject<CreativeModeTab> WATERTAB = TABS.register("tab", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
            .icon(() -> new ItemStack(FRAME.get()))
            .title(Component.translatable("itemGroup.waterframes"))
            .build()
    );

    /* PERMISSIONS */
    public static final PermissionNode<Boolean>
            PERM_DISPLAYS_EDIT = permission("waterframes.displays.save", false, "Save changes on displays", "Allows saving whitelisted URLs"),
            PERM_DISPLAYS_INTERACT = permission("waterframes.displays.interact", true, "Interact with displays", "Allows interact (open) the Gui of all displays"),
            PERM_DISPLAYS_INTERACT_FRAME = permission("waterframes.displays.interact.frame", true, "Interact with frame display", "Allows interact (open) the Gui of frame display"),
            PERM_DISPLAYS_INTERACT_PROJECTOR = permission("waterframes.displays.interact.projector", true, "Interact with frame display", "Allows interact (open) the Gui of projector display"),
            PERM_DISPLAYS_INTERACT_TV = permission("waterframes.displays.interact.tv", true, "Interact with frame display", "Allows interact (open) the Gui of tv display"),
            PERM_REMOTE_INTERACT = permission("waterframes.remote.interact", true, "Interact with remotes", "Allows interact (open) the Gui of remotes"),
            PERM_REMOTE_BIND = permission("waterframes.remote.bind", true, "Bind remotes to displays", "Allows bind displays on remotes"),
            PERM_WHITELIST_BYPASS = permission("waterframes.whitelist.bypass", false, "Bypass whitelisted URLS", "Allows to bypass whitelisted URLS");

    @SuppressWarnings("unchecked")
    private static PermissionNode<Boolean> permission(String node, boolean def, String title, String desc) {
        return new PermissionNode<>(WaterFrames.ID, node, PermissionTypes.BOOLEAN, (player, uuid, context) -> def)
                .setInformation(Component.literal(title), Component.literal(desc));
    }

    public static boolean getPermBoolean(UUID player, PermissionNode<Boolean> node) {
        return PermissionAPI.getOfflinePermission(player, node);
    }

    private static RegistryObject<BlockEntityType<DisplayTile>> tile(String name, BlockEntityType.BlockEntitySupplier<DisplayTile> creator, Supplier<DisplayBlock> block) {
        return TILES.register(name, () -> BlockEntityType.Builder.of(creator, block.get()).build(null));
    }

    private static Item.Properties remoteProp() {
        return new Item.Properties().stacksTo(1).rarity(Rarity.RARE).setNoRepair().fireResistant();
    }

    private static Item.Properties prop() {
        return new Item.Properties().stacksTo(16).rarity(Rarity.RARE);
    }

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
        TABS.register(bus);
    }

    @SubscribeEvent
    public static void registerPermissions(PermissionGatherEvent.Nodes e) {
        e.addNodes(
                PERM_DISPLAYS_EDIT,
                PERM_DISPLAYS_INTERACT,
                PERM_DISPLAYS_INTERACT_FRAME,
                PERM_DISPLAYS_INTERACT_PROJECTOR,
                PERM_DISPLAYS_INTERACT_TV,
                PERM_REMOTE_BIND,
                PERM_REMOTE_INTERACT,
                PERM_WHITELIST_BYPASS
        );
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
            NET.registerType(DataListSyncPacket.class, DataListSyncPacket::new);
            NET.registerType(NextPacket.class, NextPacket::new);
            NET.registerType(PreviousPacket.class, PreviousPacket::new);
            NET.registerType(ActivePacket.class, ActivePacket::new);
            NET.registerType(LoopPacket.class, LoopPacket::new);
            NET.registerType(MutePacket.class, MutePacket::new);
            NET.registerType(PausePacket.class, PausePacket::new);
            NET.registerType(TimePacket.class, TimePacket::new);
            NET.registerType(VolumePacket.class, VolumePacket::new);
            NET.registerType(VolumeRangePacket.class, VolumeRangePacket::new);
            NET.registerType(PositionPacket.class, PositionPacket::new);
        }

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void init(FMLClientSetupEvent e) {
            LOGGER.info(IT, "Running WATERFrAMES v{}", ModList.get().getModFileById(ID).versionString());
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