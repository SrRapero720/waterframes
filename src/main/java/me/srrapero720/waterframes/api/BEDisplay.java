package me.srrapero720.waterframes.api;

import me.srrapero720.waterframes.common.blockentities.BEFrame;
import me.srrapero720.waterframes.core.tools.UrlTool;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.client.displays.IDisplay;
import me.srrapero720.waterframes.client.displays.ImageDisplay;
import me.srrapero720.waterframes.common.packets.ActionPacket;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageCache;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import java.util.concurrent.atomic.AtomicBoolean;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public abstract class BEDisplay<DATA extends DataBlock> extends BlockEntity {
    public final DATA data;
    @OnlyIn(Dist.CLIENT) public volatile ImageCache imageCache;
    @OnlyIn(Dist.CLIENT) public volatile IDisplay display;
    @OnlyIn(Dist.CLIENT) public volatile String parsedUrl;
    @OnlyIn(Dist.CLIENT) private final AtomicBoolean released = new AtomicBoolean(false);


    public BEDisplay(DATA data, BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.data = data;
    }

    public void setUrl(String url) {
        this.data.url = url;
        this.parsedUrl = UrlTool.fixUrl(this.data.url);
    }

    public String getUrl() { return this.data.url; }
    public String getParsedUrl() { return UrlTool.fixUrl(this.data.url); }

    @OnlyIn(Dist.CLIENT)
    public synchronized IDisplay requestDisplay() {
        String url = getParsedUrl();
        if (released.get()) {
            imageCache = null;
            return null;
        }

        if (imageCache == null || !imageCache.url.equals(url)) {
            imageCache = ImageAPI.getCache(url, Minecraft.getInstance());
            cleanDisplay();
        }

        switch (imageCache.getStatus()) {
            case READY -> {
                if (display != null) return display;
                if (imageCache.isVideo()) return display = IDisplay.create(new Vec3d(worldPosition), data);
                else return display = new ImageDisplay(imageCache);
            }
            case WAITING -> {
                cleanDisplay();
                imageCache.load();
                return display;
            }
            case LOADING -> { return ImageDisplay.LOADING_GIF; }
            case FORGOTTEN -> {
                LOGGER.warn("Cached picture is forgotten, cleaning and reloading");
                imageCache = null;
                return null;
            }
            case FAILED -> { return null; }
            default -> {
                LOGGER.warn("WATERMeDIA Behavior is modified, this shouldn't be executed");
                return null;
            }
        }
    }

    public float getSizeX() { return this.data.max.x - this.data.min.x; }
    public float getSizeY() { return this.data.max.y - this.data.min.y; }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        data.save(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        data.load(nbt);
    }


    @OnlyIn(Dist.CLIENT)
    private void cleanDisplay() {
        if (display != null) {
            display.release();
            display = null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void release() {
        cleanDisplay();
        released.set(true);
    }

    public void play() {
        WaterFrames.NETWORK.sendToClient(new ActionPacket(worldPosition, data.playing = true, data.tick), level, worldPosition);
    }

    public void pause() {
        WaterFrames.NETWORK.sendToClient(new ActionPacket(worldPosition, data.playing = false, data.tick), level, worldPosition);
    }

    public void stop() {
        WaterFrames.NETWORK.sendToClient(new ActionPacket(worldPosition, data.playing = false, data.tick = 0), level, worldPosition);
    }

    public void setDirty() {
        if (this.level != null) {
            this.level.blockEntityChangedWithoutNeighborUpdates(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        } else LOGGER.warn("Cannot be stored block data, level is NULL");
    }

    @Override
    public void setRemoved() {
        if (isClient()) release();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (isClient()) release();
        super.onChunkUnloaded();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        data.load(tag);
        setDirty();
    }

    @Override public @NotNull CompoundTag getUpdateTag() { return this.saveWithFullMetadata(); }
    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    /* SPECIAL TICKS */
    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (blockEntity instanceof BEDisplay<?> be) {
            if (be.isClient()) {
                IDisplay display = be.requestDisplay();
                if (display != null && display.canTick()) display.tick(be.data);
            }
            if (be.data.playing) be.data.tick++;

            // EXTRA IMPORTANT TICKERS FOR OTHER TILES
            if (blockEntity instanceof BEFrame frame) BEFrame.tick(level, pos, state, frame);
        }
    }

    /* TOOLS */
    public boolean isClient() { return (this.level != null && this.level.isClientSide); }
}