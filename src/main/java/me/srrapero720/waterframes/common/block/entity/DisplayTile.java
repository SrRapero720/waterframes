package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.client.display.TextureDisplay;
import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.network.DisplaysNet;
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

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public abstract class DisplayTile extends BlockEntity {
    public final DisplayData data;
    @OnlyIn(Dist.CLIENT) public ImageCache imageCache;
    @OnlyIn(Dist.CLIENT) public TextureDisplay display;
    @OnlyIn(Dist.CLIENT) private boolean isReleased;

    public abstract boolean canHideModel();
    public abstract boolean canRenderBackside();
    public abstract boolean canProject();
    public abstract boolean canResize();

    public DisplayTile(DisplayData data, BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.data = data;
    }

    @OnlyIn(Dist.CLIENT)
    public synchronized TextureDisplay requestDisplay() {
        if (this.data.url.isEmpty() && display != null) {
            cleanDisplay();
            return null;
        }

        if (isReleased) {
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
        data.save(nbt, this);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        data.load(nbt, this);
        super.load(nbt);
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
        isReleased = true;
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


    public void setActiveMode(boolean mode) {
        assert isClient();
        DisplaysNet.sendActiveServer(this, mode);
    }
    public void setMutedMode(boolean mode) {
        assert isClient();
        DisplaysNet.sendMutedServer(this, mode);
    }
    public void setPauseMode(boolean pause) {
        assert isClient();
        DisplaysNet.sendPlaybackServer(this, pause, this.data.tick);
    }
    public void stop() {
        assert isClient();
        DisplaysNet.sendPlaybackServer(this, true, 0);
    }
    public void volumeUp() {
        assert isClient();
        DisplaysNet.sendVolumeServer(this, this.data.minVolumeDistance, this.data.maxVolumeDistance,  DisplayConfig.maxVolume(this.data.volume + 5));
    }
    public void volumeDown() {
        assert isClient();
        DisplaysNet.sendVolumeServer(this, this.data.minVolumeDistance, this.data.maxVolumeDistance, DisplayConfig.maxVolume(this.data.volume - 5));
    }
    public void fastForward() {
        assert isClient();
        DisplaysNet.sendPlaytimeServer(this, Math.min(data.tick + MathAPI.msToTick(5000), data.tickMax), data.tickMax);
    }
    public void fastBackwards() {
        DisplaysNet.sendPlaytimeServer(this, Math.max(data.tick - MathAPI.msToTick(5000), 0), data.tickMax);
    }

    public boolean isClient() {
        return this.level != null && this.level.isClientSide;
    }

    /* SPECIAL TICKS */
    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
        if (be instanceof DisplayTile tile) {
            if (tile.isClient()) {
                TextureDisplay display = tile.requestDisplay();
                if (display != null && display.canTick()) display.tick(pos);
            }
            if (!tile.data.paused && tile.data.active) {
                if ((tile.data.tick <= tile.data.tickMax) || tile.data.tickMax == -1) {
                    tile.data.tick++;
                } else {
                    if (tile.data.loop) tile.data.tick = 0;
                }
            }

            // EXTRA IMPORTANT TICKERS FOR OTHER TILES
            if (tile instanceof FrameTile frame) {
                if (state.getValue(DisplayBlock.VISIBLE) != frame.data.frameVisibility) {
                    var brandNewState = state.setValue(DisplayBlock.VISIBLE, frame.data.frameVisibility);
                    level.setBlock(pos, brandNewState, 0);
                }
            }
        }
    }

    public void setDirty() {
        if (this.level != null) {
            this.level.blockEntityChanged(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        } else {
            LOGGER.warn("Cannot be stored block data, level is NULL");
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        data.load(tag, this);
        setDirty();
    }

    @Override public @NotNull CompoundTag getUpdateTag() { return this.saveWithFullMetadata(); }
    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}