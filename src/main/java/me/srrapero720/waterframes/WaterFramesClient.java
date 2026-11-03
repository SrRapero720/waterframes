package me.srrapero720.waterframes;

import me.srrapero720.waterframes.client.rendering.DisplayRenderer;
import me.srrapero720.waterframes.client.ui.screen.widget.ScreenWidgets;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.commands.WaterFramesCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

import java.util.function.Consumer;
import java.util.function.Function;

import static me.srrapero720.waterframes.WaterFrames.*;
import static me.srrapero720.waterframes.WaterFrames.ID;

@EventBusSubscriber(modid = ID, value = Dist.CLIENT)
public class WaterFramesClient {

    public static void openScreen(DisplayTile tile, Function<DisplayTile, Screen> screen) {
        Minecraft.getInstance().setScreen(screen.apply(tile));
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent e) {
        ScreenWidgets.register();
    }

    @SubscribeEvent
    public static void registerCommands(RegisterClientCommandsEvent event) {
        WaterFramesCommand.registerClient(event.getDispatcher());
    }

    @SubscribeEvent
    public static void registerTileRenderer(EntityRenderersEvent.RegisterRenderers e) {
        BlockEntityRenderers.register(TILE_FRAME.get(), DisplayRenderer::new);
        BlockEntityRenderers.register(TILE_PROJECTOR.get(), DisplayRenderer::new);
        BlockEntityRenderers.register(TILE_TV.get(), DisplayRenderer::new);
        BlockEntityRenderers.register(TILE_BIG_TV.get(), DisplayRenderer::new);
        BlockEntityRenderers.register(TILE_TV_BOX.get(), DisplayRenderer::new);
    }

    public static void registerTexture(ResourceLocation location, AbstractTexture texture) {
        Minecraft.getInstance().getTextureManager().register(location, texture);
    }

    public static void unregisterTexture(ResourceLocation location) {
        Minecraft.getInstance().getTextureManager().release(location);
    }
}
