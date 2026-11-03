package me.srrapero720.waterframes;

import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.block.data.DisplayCaps;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.commands.WaterFramesCommand;
import me.srrapero720.waterframes.common.compat.sable.SableCompat;
import me.srrapero720.waterframes.common.compat.valkyrienskies.VSCompat;
import me.srrapero720.waterframes.common.item.RemoteControl;
import me.srrapero720.waterframes.common.item.data.CodecManager;
import me.srrapero720.waterframes.common.item.data.RemoteData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import static me.srrapero720.waterframes.WaterFrames.ID;

@Mod(WaterFrames.ID)
@EventBusSubscriber(modid = ID)
public class WaterFrames {
    static final Marker IT = MarkerManager.getMarker(WaterFrames.class.getName());
    public static final String ID = "waterframes";
    public static final String NAME = "WATERFrAMES";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    /* REGISTERS */
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    private static final DeferredRegister<DataComponentType<?>> DATA = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);

    /* DATA */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RemoteData>> REMOTE_DATA = DATA.register("remote", () -> new DataComponentType.Builder<RemoteData>()
            .persistent(CodecManager.REMOTE_CODEC)
            .networkSynchronized(CodecManager.REMOTE_STREAM_CODEC)
            .build()
    );

    /* PERMISSIONS */
    public static final PermissionNode<Boolean>
            PERM_DISPLAYS_EDIT = permission("waterframes.displays.save", false, "Save changes on displays", "Allows saving whitelisted URLs"),
            PERM_DISPLAYS_INTERACT = permission("waterframes.displays.interact", true, "Interact with displays", "Allows interact (open) the Gui of all displays"),
            PERM_DISPLAYS_INTERACT_FRAME = permission("waterframes.displays.interact.frame", true, "Interact with frame display", "Allows interact (open) the Gui of frame display"),
            PERM_DISPLAYS_INTERACT_PROJECTOR = permission("waterframes.displays.interact.projector", true, "Interact with projector display", "Allows interact (open) the Gui of projector display"),
            PERM_DISPLAYS_INTERACT_TV = permission("waterframes.displays.interact.tv", true, "Interact with tv display", "Allows interact (open) the Gui of tv display"),
            PERM_REMOTE_INTERACT = permission("waterframes.remote.interact", true, "Interact with remotes", "Allows interact (open) the Gui of remotes"),
            PERM_REMOTE_BIND = permission("waterframes.remote.bind", true, "Bind remotes to displays", "Allows bind displays on remotes"),
            PERM_WHITELIST_BYPASS = permission("waterframes.whitelist.bypass", false, "Bypass whitelisted URLS", "Allows to bypass whitelisted URLS");

    /* BLOCKS */
    public static final DeferredBlock<DisplayBlock> FRAME = BLOCKS.register("frame", () -> DisplayBlock.of(DisplayCaps.FRAME));
    public static final DeferredBlock<DisplayBlock> TV_BOX = BLOCKS.register("tv_box", () -> DisplayBlock.of(DisplayCaps.TV_BOX));
    public static final DeferredBlock<DisplayBlock> BIG_TV = BLOCKS.register("big_tv", () -> DisplayBlock.of(DisplayCaps.BIG_TV));
    public static final DeferredBlock<DisplayBlock> TV = BLOCKS.register("tv", () -> DisplayBlock.of(DisplayCaps.TV));
    public static final DeferredBlock<DisplayBlock> PROJECTOR = BLOCKS.register("projector", () -> DisplayBlock.of(DisplayCaps.PROJECTOR));

    /* TILES */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DisplayTile>> TILE_TV_BOX = tile("tv_box", TV_BOX);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DisplayTile>> TILE_TV = tile("tv", TV);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DisplayTile>> TILE_BIG_TV = tile("big_tv", BIG_TV);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DisplayTile>> TILE_PROJECTOR = tile("projector", PROJECTOR);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DisplayTile>> TILE_FRAME = tile("frame", FRAME);
    public static final DeferredItem<Item> FRAME_ITEM = ITEMS.register("frame", () -> new BlockItem(FRAME.get(), prop()));

    /* ITEMS */
    public static final DeferredItem<Item> REMOTE_ITEM = ITEMS.register("remote", () -> new RemoteControl(remoteProp()));
    public static final DeferredItem<Item> PROJECTOR_ITEM = ITEMS.register("projector", () -> new BlockItem(PROJECTOR.get(), prop()));
    public static final DeferredItem<Item> TV_BOX_ITEM = ITEMS.register("tv_box", () -> new BlockItem(TV_BOX.get(), prop()));
    public static final DeferredItem<Item> TV_ITEM = ITEMS.register("tv", () -> new BlockItem(TV.get(), prop()));
    public static final DeferredItem<Item> BIG_TV_ITEM = ITEMS.register("big_tv", () -> new BlockItem(BIG_TV.get(), prop()));

    /* TABS */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("tab", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
            .icon(() -> new ItemStack(FRAME.get()))
            .title(Component.translatable("itemGroup.waterframes"))
            .build()
    );

    /* SOUNDS */
    public static final DeferredHolder<SoundEvent, SoundEvent> DISPLAY_SOUND = SOUNDS.register("display", () -> SoundEvent.createVariableRangeEvent(WaterFrames.asResource("display")));

    // BOOTSTRAP
    public WaterFrames(IEventBus bus, ModContainer container) {
        LOGGER.info(IT, "Running WATERFrAMES v{}", ModList.get().getModFileById(ID).versionString());
        WaterFramesConfig.init(bus, container);
        DATA.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
        TABS.register(bus);
        SOUNDS.register(bus);
    }

    public static ResourceLocation asResource(String id) {
        return Objects.requireNonNull(ResourceLocation.tryBuild(ID, id));
    }

    public static boolean isInstalled(String modId) {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    /**
     * Checks if a URL string is valid for use with MRL.
     * @param url the URL string to validate
     * @return true if valid
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) return false;

        // Local file check
        File f = new File(url);
        if (!f.isDirectory() && f.exists()) return true;

        // Basic URL validation - let MRL handle detailed validation
        return url.contains("://") || url.startsWith("water://");
    }

    public static double getDistance(DisplayTile tile, Position playerPos) {
        return getDistance(tile.level, tile.getBlockPos(), playerPos);
    }

    public static double getDistance(Level level, BlockPos pos, Position position) {
        if (SableCompat.installed() && WaterFramesConfig.sableCompat()) {
            return Math.sqrt(SableCompat.getSquaredDistance(level, pos, position));
        }
        if (VSCompat.installed() && WaterFramesConfig.vsEurekaCompat()) {
            return Math.sqrt(VSCompat.getSquaredDistance(level, pos, position));
        }
        return Math.sqrt(pos.distToLowCornerSqr(position.x(), position.y(), position.z()));
    }

    public static float deltaFrames() { return Minecraft.getInstance().isPaused() ? 1.0F : Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false); }

    @SuppressWarnings("unchecked")
    private static PermissionNode<Boolean> permission(String node, boolean def, String title, String desc) {
        return new PermissionNode<>(WaterFrames.ID, node, PermissionTypes.BOOLEAN, (player, uuid, context) -> def)
                .setInformation(Component.literal(title), Component.literal(desc));
    }

    public static boolean getPermBoolean(UUID player, PermissionNode<Boolean> node) {
        return PermissionAPI.getOfflinePermission(player, node);
    }

    /** The block entity type of a display kind; the tile looks its own up here when the block creates it. */
    public static BlockEntityType<DisplayTile> tileType(final DisplayCaps caps) {
        return switch (caps) {
            case FRAME -> TILE_FRAME.get();
            case PROJECTOR -> TILE_PROJECTOR.get();
            case TV -> TILE_TV.get();
            case BIG_TV -> TILE_BIG_TV.get();
            case TV_BOX -> TILE_TV_BOX.get();
        };
    }

    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<DisplayTile>> tile(final String name, final Supplier<DisplayBlock> block) {
        return TILES.register(name, () -> BlockEntityType.Builder.of((pos, state) -> new DisplayTile(tileType(((DisplayBlock) state.getBlock()).caps), pos, state), block.get()).build(null));
    }

    private static Item.Properties remoteProp() {
        return new Item.Properties().stacksTo(1).rarity(Rarity.RARE).setNoRepair().fireResistant();
    }

    private static Item.Properties prop() {
        return new Item.Properties().stacksTo(16).rarity(Rarity.RARE);
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

    // A SERVER LEVEL GOING AWAY TAKES ITS MEDIA SESSIONS WITH IT; THE TILES THEMSELVES MAY NEVER HEAR ABOUT IT
    @SubscribeEvent
    public static void onLevelUnload(final LevelEvent.Unload event) {
        if (event.getLevel() instanceof final Level level && !level.isClientSide) DisplayTile.close(level);
    }

    @SubscribeEvent
    public static void onCreativeTabsLoading(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == TAB.getKey()) {
            event.accept(REMOTE_ITEM, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(FRAME_ITEM, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(PROJECTOR_ITEM, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(TV_ITEM, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(BIG_TV_ITEM, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(TV_BOX_ITEM, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}