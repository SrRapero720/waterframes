package me.srrapero720.waterframes;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import me.srrapero720.waterframes.custom.blocks.BlockEntityWaterFrame;
import me.srrapero720.waterframes.custom.blocks.WaterPictureFrame;
import me.srrapero720.waterframes.custom.displayers.texture.TextureCache;
import me.srrapero720.waterframes.custom.render.WaterFramesRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.CreativeBlockModel;
import team.creative.creativecore.client.render.model.CreativeItemBoxModel;

import static me.srrapero720.waterframes.WaterFrames.REGISTRY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class LittleFramesClient {

    @OnlyIn(Dist.CLIENT)
    public static void setup() {
        MinecraftForge.EVENT_BUS.register(TextureCache.class);

        CreativeCoreClient.registerClientConfig(WaterFrames.ID);

        CreativeCoreClient.registerBlockModel(new ResourceLocation(WaterFrames.ID, "frame"), new CreativeBlockModel() {
            public final ModelProperty<Boolean> visibility = new ModelProperty<>();
            public final ModelDataMap visible = new ModelDataMap.Builder().withInitial(visibility, true).build();
            public final ModelDataMap invisible = new ModelDataMap.Builder().withInitial(visibility, false).build();
            @Override
            public List<? extends RenderBox> getBoxes(BlockState blockState, IModelData iModelData, Random random) {
                if (Boolean.FALSE.equals(iModelData.getData(visibility))) return new ArrayList<>();
                var box = new RenderBox(WaterPictureFrame.box(blockState.getValue(WaterPictureFrame.FACING)), Blocks.OAK_PLANKS);
                return Collections.singletonList(box);
            }

            @Override
            public @NotNull IModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull IModelData iModelData) {
                BlockEntity be = level.getBlockEntity(blockPos);
                if (be instanceof BlockEntityWaterFrame frame)
                    return frame.visibleFrame ? visible : invisible;
                return visible;
            }
        });

        BlockEntityRenderers.register((BlockEntityType<BlockEntityWaterFrame>) REGISTRY.blockEntityOnly("frame"), WaterFramesRenderer::new);
    }

}
