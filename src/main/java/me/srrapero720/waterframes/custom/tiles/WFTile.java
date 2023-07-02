package me.srrapero720.waterframes.custom.tiles;

import me.srrapero720.waterframes.display.texture.TextureData;
import me.srrapero720.waterframes.displays.Display;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.util.math.vec.Vec3d;

public abstract class WFTile extends BlockEntity {
    protected String url = "";
    public float volume = 1;
    public float minDistance = 5;
    public float maxDistance = 20;

    public boolean loop = true;
    public int tick = 0;
    public boolean playing = true;

    @OnlyIn(Dist.CLIENT)
    public TextureData texture;

    @OnlyIn(Dist.CLIENT)
    public Display display;

    public WFTile(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @OnlyIn(Dist.CLIENT)
    public String getURL() {
        return parseUrl(url);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isURLEmpty() { return url.isEmpty(); }
    public String getRealURL() { return url; }
    public void setURL(String url) { this.url = url; }

    @OnlyIn(Dist.CLIENT)
    public static @NotNull String parseUrl(@NotNull String url) {
        return url.replaceAll("\\{playername}", Minecraft.getInstance().player.getName().getString())
                .replaceAll("\\{displayname}", Minecraft.getInstance().player.getDisplayName().getString())
                .replaceAll("\\{uuid}", Minecraft.getInstance().player.getStringUUID())
                .replace("mc://", ("file:///" + FMLPaths.GAMEDIR.get().toAbsolutePath()).replace("\\", "/") + "/");
    }

    public Display requestDisplay() {
        String url = getURL();
        if (texture == null || !texture.url.equals(url)) {
            texture = TextureData.get(url);
            if (display != null)
                display.release();
            display = null;
        }
        if (!texture.isVideo() && (!texture.ready() || texture.getError() != null))
            return null;
        if (display != null)
            return display;
        return display = texture.createDisplay(new Vec3d(worldPosition), url, volume, minDistance, maxDistance, loop, false);
    }
}
