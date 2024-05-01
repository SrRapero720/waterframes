package me.srrapero720.waterframes;

import me.srrapero720.waterframes.client.rendering.DisplayRenderer;
import me.srrapero720.waterframes.common.block.*;
import me.srrapero720.waterframes.common.block.entity.*;
import me.srrapero720.waterframes.common.commands.WaterFramesCommand;
import me.srrapero720.waterframes.common.item.RemoteControl;
import me.srrapero720.waterframes.common.network.packets.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

import static me.srrapero720.waterframes.common.network.DisplayNetwork.*;
import static me.srrapero720.waterframes.WaterFrames.*;

public class WFRegistry {

    /* BLOCKS */
    public static final DisplayBlock
            FRAME = Registry.register(BuiltInRegistries.BLOCK, resloc("frame"), new FrameBlock()),
            PROJECTOR = Registry.register(BuiltInRegistries.BLOCK, resloc("projector"), new ProjectorBlock()),
            TV = Registry.register(BuiltInRegistries.BLOCK, resloc("tv"), new TvBlock()),
            BIG_TV = Registry.register(BuiltInRegistries.BLOCK, resloc("big_tv"), new BigTvBlock()),
            TV_BOX = Registry.register(BuiltInRegistries.BLOCK, resloc("tv_box"), new TVBoxBlock());
//            GOLDEN_PROJECTOR = BLOCKS.register("golden_projector", ProjectorBlock::new);

    /* ITEMS */
    public static final Item
            REMOTE_ITEM = Registry.register(BuiltInRegistries.ITEM, resloc("remote"), new RemoteControl(remoteProp())),
            FRAME_ITEM = Registry.register(BuiltInRegistries.ITEM, resloc("frame"), new BlockItem(FRAME, prop())),
            PROJECTOR_ITEM = Registry.register(BuiltInRegistries.ITEM, resloc("projector"), new BlockItem(PROJECTOR, prop())),
            TV_ITEM = Registry.register(BuiltInRegistries.ITEM, resloc("tv"), new BlockItem(TV, prop())),
            BIG_TV_ITEM = Registry.register(BuiltInRegistries.ITEM, resloc("big_tv"), new BlockItem(BIG_TV, prop())),
            TV_BOX_ITEM = Registry.register(BuiltInRegistries.ITEM, resloc("tv_box"), new BlockItem(TV_BOX, prop()));
//            GOLDEN_PROJECTOR_ITEM = ITEMS.register("golden_projector", () -> new BlockItem(GOLDEN_PROJECTOR.get(), prop().tab(null)));

    /* TILES */
    public static final BlockEntityType<DisplayTile>
            TILE_FRAME = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, resloc("frame"), tile(FrameTile::new, () -> FRAME)),
            TILE_PROJECTOR = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, resloc("projector"), tile(ProjectorTile::new, () -> PROJECTOR)),
            TILE_TV = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, resloc("tv"), tile(TvTile::new, () -> TV)),
            TILE_BIG_TV = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, resloc("big_tv"), tile(BigTvTile::new, () -> BIG_TV)),
            TILE_TV_BOX = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, resloc("tv_box"), tile(TVBoxTile::new, () -> TV_BOX));

    /* TABS */
    public static final CreativeModeTab WATERTAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, resloc("tab"), FabricItemGroup.builder()
            .icon(() -> new ItemStack(FRAME_ITEM))
            .title(Component.translatable("itemGroup.waterframes"))
            .displayItems((itemDisplayParameters, output) -> {
                output.accept(REMOTE_ITEM);
                output.accept(FRAME_ITEM);
                output.accept(PROJECTOR_ITEM);
                output.accept(TV_ITEM);
                output.accept(BIG_TV_ITEM);
                output.accept(TV_BOX_ITEM);
            })
            .build());

    private static BlockEntityType<DisplayTile> tile(BlockEntityType.BlockEntitySupplier<DisplayTile> creator, Supplier<DisplayBlock> block) {
        return BlockEntityType.Builder.of(creator, block.get()).build(null);
    }

    private static Item.Properties remoteProp() {
        return new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant();
    }

    private static Item.Properties prop() {
        return new Item.Properties().stacksTo(16).rarity(Rarity.RARE);
    }

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> WaterFramesCommand.register(dispatcher));
        NET.registerType(DataSyncPacket.class, DataSyncPacket::new);
        NET.registerType(ActivePacket.class, ActivePacket::new);
        NET.registerType(LoopPacket.class, LoopPacket::new);
        NET.registerType(MutePacket.class, MutePacket::new);
        NET.registerType(PausePacket.class, PausePacket::new);
        NET.registerType(TimePacket.class, TimePacket::new);
        NET.registerType(VolumePacket.class, VolumePacket::new);
        NET.registerType(VolumeRangePacket.class, VolumeRangePacket::new);
    }

    @Environment(EnvType.CLIENT)
    public static void registerTexture(ResourceLocation location, AbstractTexture texture) {
        Minecraft.getInstance().getTextureManager().register(location, texture);
    }

    @Environment(EnvType.CLIENT)
    public static void unregisterTexture(ResourceLocation location) {
        Minecraft.getInstance().getTextureManager().release(location);
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> WaterFramesCommand.registerClient(dispatcher));
        BlockEntityRenderers.register(TILE_FRAME, DisplayRenderer::new);
        BlockEntityRenderers.register(TILE_PROJECTOR, DisplayRenderer::new);
        BlockEntityRenderers.register(TILE_TV, DisplayRenderer::new);
        BlockEntityRenderers.register(TILE_BIG_TV, DisplayRenderer::new);
        BlockEntityRenderers.register(TILE_TV_BOX, DisplayRenderer::new);
    }

    public static ResourceLocation resloc(String name) {
        return new ResourceLocation(ID, name);
    }

}