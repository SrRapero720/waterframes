package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.client.display.TextureDisplay;
import me.srrapero720.waterframes.common.block.FrameBlock;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.util.FrameNet;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageCache;
import me.srrapero720.watermedia.api.math.MathAPI;
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

public abstract class DisplayTile<DATA extends DisplayData> extends BlockEntity {
    public final DATA data;
    @OnlyIn(Dist.CLIENT) public volatile ImageCache imageCache;
    @OnlyIn(Dist.CLIENT) public volatile TextureDisplay display;
    @OnlyIn(Dist.CLIENT) public volatile String parsedUrl;
    private final AtomicBoolean released = new AtomicBoolean(false); // clientside

    public DisplayTile(DATA data, BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.data = data;
    }

    public void setUrl(String url) {
        this.data.url = url;
        if (isClient()) this.parsedUrl = this.data.url;
    }

    public String getUrl() { return this.data.url; }

    @OnlyIn(Dist.CLIENT)
    public synchronized TextureDisplay requestDisplay() {
        if (this.data.url.isEmpty() && display != null) {
            cleanDisplay();
            return null;
        }

        if (released.get()) {
            imageCache = null;
            return null;
        }

        if (imageCache == null || !imageCache.url.equals(this.data.url)) {
            imageCache = ImageAPI.getCache(this.data.url, Minecraft.getInstance());
            this.cleanDisplay();
        }

        switch (imageCache.getStatus()) {
            case LOADING, FAILED, READY -> {
                if (display != null) return display;
                return display = new TextureDisplay(imageCache, new Vec3d(worldPosition), this);
            }

            case WAITING -> {
                cleanDisplay();
                imageCache.load();
                return display;
            }

            case FORGOTTEN -> {
                LOGGER.warn("Cached picture is forgotten, cleaning and reloading");
                imageCache = null;
                return null;
            }

            default -> {
                LOGGER.warn("WATERMeDIA Behavior is modified, this shouldn't be executed");
                return null;
            }
        }
    }

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

    public void play() { FrameNet.sendPlaybackState(worldPosition, level, data.playing = true, data.tick); }
    public void pause() { FrameNet.sendPlaybackState(worldPosition, level, data.playing = false, data.tick); }
    public void stop() { FrameNet.sendPlaybackState(worldPosition, level, data.playing = false, data.tick = 0); }
    public void volumeUp() { FrameNet.sendVolumeUpdate(worldPosition, level, data.minVolumeDistance, data.maxVolumeDistance, data.volume = DisplayConfig.maxVolume(data.volume + 5)); }
    public void volumeDown() { FrameNet.sendVolumeUpdate(worldPosition, level, data.minVolumeDistance, data.maxVolumeDistance, data.volume = DisplayConfig.maxVolume(data.volume - 5)); }
    public void fastForward() { FrameNet.sendPlaybackState(worldPosition, level, data.playing, data.tick += MathAPI.msToTick(5000)); }
    public void fastBackwards() { FrameNet.sendPlaybackState(worldPosition, level, data.playing, data.tick -= MathAPI.msToTick(5000)); }
    public void toggleActive() { FrameNet.sendActiveToggle(worldPosition, level, data.active = !data.active); }

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
        if (blockEntity instanceof DisplayTile<?> be) {
            if (be.isClient()) {
                TextureDisplay display = be.requestDisplay();
                if (display != null && display.canTick()) display.tick(pos);
            }
            if (be.data.playing) {
                if ((be.data.tick <= be.data.tickMax) || be.data.tickMax == -1) {
                    be.data.tick++;
                } else {
                    if (be.data.loop) be.data.tick = 0;
                }
            }

            // EXTRA IMPORTANT TICKERS FOR OTHER TILES
            if (blockEntity instanceof FrameTile frame) {
                if (state.getValue(FrameBlock.VISIBLE) != frame.data.frameVisibility) {
                    var brandNewState = state.setValue(FrameBlock.VISIBLE, frame.data.frameVisibility);
                    level.setBlock(pos, brandNewState, 0);
                }
            }
        }
    }

    /* TOOLS */
    public boolean isClient() { return (this.level != null && this.level.isClientSide); }
}