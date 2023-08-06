package me.srrapero720.waterframes.custom.tiles;

import me.srrapero720.waterframes.WFRegistry;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.custom.blocks.Frame;
import me.srrapero720.waterframes.display.IDisplay;
import me.srrapero720.waterframes.display.ImageDisplay;
import me.srrapero720.waterframes.display.VideoDisplay;
import me.srrapero720.waterframes.custom.packets.FramesPacket;
import me.srrapero720.waterframes.watercore_supplier.WCoreUtil;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.images.ImageCache;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.vec.Vec2f;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class TileFrame extends BlockEntity {

    @OnlyIn(Dist.CLIENT)
    public static @NotNull String parseUrl(@NotNull String url) {
        return url.replaceAll("\\{playername}", WCoreUtil.mc().player.getName().getString())
                .replaceAll("\\{displayname}", WCoreUtil.mc().player.getDisplayName().getString())
                .replaceAll("\\{uuid}", WCoreUtil.mc().player.getStringUUID())
                .replace("minecraft://",("file:///" + FMLPaths.GAMEDIR.get().toAbsolutePath()).replace("\\", "/") + "/");
    }

    private String url = "";
    public Vec2f min = new Vec2f(0, 0);
    public Vec2f max = new Vec2f(1, 1);


    public float rotation = 0;
    public boolean flipX = false;
    public boolean flipY = false;

    public boolean visibleFrame = true;
    public boolean bothSides = false;

    public float brightness = 1;
    public float alpha = 1;

    public int renderDistance = 128;

    public float volume = 1;
    public float minDistance = 5;
    public float maxDistance = 20;

    public boolean loop = true;
    public int tick = 0;
    public boolean playing = true;

    @OnlyIn(Dist.CLIENT)
    public ImageCache cache;

    @OnlyIn(Dist.CLIENT)
    public IDisplay display;

    public TileFrame(BlockPos pos, BlockState state) {
        super(WFRegistry.TILE_FRAME.get(), pos, state);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isURLEmpty() {
        return url.isEmpty();
    }

    @OnlyIn(Dist.CLIENT)
    public String getURL() {
        return parseUrl(url);
    }

    public String getRealURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public IDisplay requestDisplay() {
        String url = getURL();
        if (cache == null || !cache.originalURL.equals(url)) {
            cache = ImageCache.findOrCreate(url, Minecraft.getInstance()::execute);
            if (display != null) display.release();
            display = null;
        }

        switch (cache.getStatus()) {
            case READY -> {
                if (display != null) return display;
                if (cache.isVideo()) return display = VideoDisplay.create(new Vec3d(worldPosition), url, volume, minDistance, maxDistance, loop, playing);
                else return display = new ImageDisplay(cache);
            }
            case WAITING -> {
                cache.load();
                return display;
            }
            case LOADING -> {
                cleanDisplay(); // ensure display is cleaned
                return ImageDisplay.LOADING_GIF;
            }
            case FORGOTTEN -> {
                LOGGER.warn("Cached picture is forgotten, cleaning and reloading");
                cleanDisplay();
                cache = null;
                return null;
            }
            case FAILED -> {
                cleanDisplay();
                return null;
            }
            default -> {
                LOGGER.warn("WATERMeDIA Behavior is modified, this shouldn't be executed");
                return null;
            }
        }

//        return display = cache.createDisplay(new Vec3d(worldPosition), url, volume, minDistance, maxDistance, loop, playing);
    }

    private void cleanDisplay() {
        if (display != null) {
            display.release();
            display = null;
        }
    }

    public AlignedBox getBox() {
        Direction direction = getBlockState().getValue(Frame.FACING);
        Facing facing = Facing.get(direction);
        AlignedBox box = Frame.box(direction);

        Axis one = facing.one();
        Axis two = facing.two();

        if (facing.axis != Axis.Z) {
            one = facing.two();
            two = facing.one();
        }

        box.setMin(one, min.x);
        box.setMax(one, max.x);

        box.setMin(two, min.y);
        box.setMax(two, max.y);
        return box;
    }

    public float getSizeX() {
        return max.x - min.x;
    }

    public float getSizeY() {
        return max.y - min.y;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return getBox().getBB(getBlockPos());
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        savePicture(nbt);
    }

    public void play() {
        playing = true;
        WaterFrames.NETWORK.sendToClient(new FramesPacket(worldPosition, playing, tick), level, worldPosition);
    }

    public void pause() {
        playing = false;
        WaterFrames.NETWORK.sendToClient(new FramesPacket(worldPosition, playing, tick), level, worldPosition);
    }

    public void stop() {
        playing = false;
        tick = 0;
        WaterFrames.NETWORK.sendToClient(new FramesPacket(worldPosition, playing, tick), level, worldPosition);
    }

    protected void savePicture(CompoundTag nbt) {
        nbt.putString("url", url);
        nbt.putFloat("minx", min.x);
        nbt.putFloat("miny", min.y);
        nbt.putFloat("maxx", max.x);
        nbt.putFloat("maxy", max.y);
        nbt.putFloat("rotation", rotation);
        nbt.putInt("render", renderDistance);
        nbt.putBoolean("visibleFrame", visibleFrame);
        nbt.putBoolean("bothSides", bothSides);
        nbt.putBoolean("flipX", flipX);
        nbt.putBoolean("flipY", flipY);
        nbt.putFloat("alpha", alpha);
        nbt.putFloat("brightness", brightness);

        nbt.putFloat("volume", volume);
        nbt.putFloat("min", minDistance);
        nbt.putFloat("max", maxDistance);

        nbt.putBoolean("playing", playing);
        nbt.putInt("tick", tick);
        nbt.putBoolean("loop", loop);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        loadPicture(nbt);
    }

    protected void loadPicture(CompoundTag nbt) {
        url = nbt.getString("url");
        min.x = nbt.getFloat("minx");
        min.y = nbt.getFloat("miny");
        max.x = nbt.getFloat("maxx");
        max.y = nbt.getFloat("maxy");
        rotation = nbt.getFloat("rotation");
        renderDistance = nbt.getInt("render");
        visibleFrame = nbt.getBoolean("visibleFrame");
        bothSides = nbt.getBoolean("bothSides");
        flipX = nbt.getBoolean("flipX");
        flipY = nbt.getBoolean("flipY");
        alpha = nbt.contains("alpha") ? nbt.getFloat("alpha") : 1;
        brightness = nbt.contains("brightness") ? nbt.getFloat("brightness") : 1;
        volume = nbt.getFloat("volume");
        minDistance = nbt.contains("min") ? nbt.getFloat("min") : 5;
        maxDistance = nbt.contains("max") ? nbt.getFloat("max") : 20;
        playing = nbt.getBoolean("playing");
        tick = nbt.getInt("tick");
        loop = nbt.getBoolean("loop");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (blockEntity instanceof TileFrame be) {
            if (state.getValue(Frame.VISIBLE) != be.visibleFrame) {
                var brandNewState = state.setValue(Frame.VISIBLE, be.visibleFrame);
                level.setBlock(pos, brandNewState, 0);
            }


            if (level.isClientSide) {
                IDisplay display = be.requestDisplay();
                if (display != null) display.tick(be.getURL(), be.volume, be.minDistance, be.maxDistance, be.playing, be.loop, be.tick);
            }
            if (be.playing) be.tick++;
        }
    }

    @Override
    public void setRemoved() {
        if (this.level.isClientSide && display != null) display.release();
    }

    @Override
    public void onChunkUnloaded() {
        if (this.level.isClientSide && display != null) display.release();
    }

    public void markDirty() {
        this.level.blockEntityChanged(this.worldPosition);
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        loadPicture(tag);
        markDirty();
    }

    public boolean isClient() { return this.level != null && this.level.isClientSide; }
}
