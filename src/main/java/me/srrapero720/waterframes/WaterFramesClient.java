package me.srrapero720.waterframes;

import me.srrapero720.waterframes.client.rendering.TextureWrapper;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import org.watermedia.api.image.ImageAPI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import team.creative.creativecore.client.CreativeCoreClient;

import static me.srrapero720.waterframes.WaterFrames.ID;
import static me.srrapero720.waterframes.WaterFrames.LOADING_ANIMATION;

public class WaterFramesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // CHECK FOR WATERMEDIA
        if (!WaterFrames.isInstalled("watermedia")) {
            WaterFrames.LOGGER.error("WATERFrAMES requires WaterMedia on client-side to run! Please install it.");
            return;
        }

        DisplaysRegistry.initClient();
        CreativeCoreClient.registerClientConfig(ID);
        DisplayTile.initClient();

        // EVENTS
        ClientTickEvents.END_CLIENT_TICK.register(client -> WaterFrames.tick());
        ClientLifecycleEvents.CLIENT_STARTED.register(client ->
                DisplaysRegistry.registerTexture(LOADING_ANIMATION, new TextureWrapper.Renderer(ImageAPI.loadingGif(WaterFrames.ID)))
        );
    }
}
