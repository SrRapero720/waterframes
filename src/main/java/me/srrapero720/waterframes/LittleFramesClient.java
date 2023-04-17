package me.srrapero720.waterframes;

import me.srrapero720.waterframes.custom.blocks.BlockEntityWaterFrame;
import me.srrapero720.waterframes.custom.displayers.texture.TextureCache;
import me.srrapero720.waterframes.custom.render.WaterFramesRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import team.creative.creativecore.client.CreativeCoreClient;

import static me.srrapero720.waterframes.WaterFrames.REGISTRY;

@OnlyIn(Dist.CLIENT)
public class LittleFramesClient {

    @OnlyIn(Dist.CLIENT)
    public static void setup() {
        MinecraftForge.EVENT_BUS.register(TextureCache.class);

        CreativeCoreClient.registerClientConfig(WaterFrames.ID);

        BlockEntityRenderers.register((BlockEntityType<BlockEntityWaterFrame>) REGISTRY.blockEntityOnly("frame"), WaterFramesRenderer::new);
    }

}
