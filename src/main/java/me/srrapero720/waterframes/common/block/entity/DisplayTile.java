package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.client.display.TextureDisplay;
import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.WFNetwork;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageCache;
import me.srrapero720.watermedia.api.math.MathAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
            this.cleanDisplay();
            return null;
        }

        if (this.isReleased) {
            this.imageCache = null;
            return null;
        }

        if (this.imageCache == null || !this.imageCache.url.equals(this.data.url)) {
            this.imageCache = ImageAPI.getCache(this.data.url, Minecraft.getInstance());
            this.cleanDisplay();
        }

        switch (imageCache.getStatus()) {
            case LOADING, FAILED, READY -> {
                if (this.display != null) return this.display;
                return this.display = new TextureDisplay(this);
            }

            case WAITING -> {
                this.cleanDisplay();
                this.imageCache.load();
                return display;
            }

            case FORGOTTEN -> {
                LOGGER.warn("Cached picture is forgotten, cleaning and reloading");
                this.imageCache = null;
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
        this.data.save(nbt, this);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        this.data.load(nbt, this);
        super.load(nbt);
    }

    @OnlyIn(Dist.CLIENT)
    private void cleanDisplay() {
        if (this.display != null) {
            this.display.release();
            this.display = null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void release() {
        this.cleanDisplay();
        this.isReleased = true;
    }

    @Override
    public void setRemoved() {
        if (this.isClient()) this.release();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (this.isClient()) this.release();
        super.onChunkUnloaded();
    }

    public void setActiveMode(boolean mode) {
        assert isClient();
        WFNetwork.sendActiveServer(this, mode);
    }
    public void setMutedMode(boolean mode) {
        assert isClient();
        WFNetwork.sendMutedServer(this, mode);
    }
    public void setPauseMode(boolean pause) {
        assert isClient();
        WFNetwork.sendPlaybackServer(this, pause, this.data.tick);
    }
    public void stop() {
        assert isClient();
        WFNetwork.sendPlaybackServer(this, true, 0);
    }
    public void volumeUp() {
        assert isClient();
        WFNetwork.sendVolumeServer(this, this.data.minVolumeDistance, this.data.maxVolumeDistance,  DisplayConfig.maxVolume(this.data.volume + 5));
    }
    public void volumeDown() {
        assert isClient();
        WFNetwork.sendVolumeServer(this, this.data.minVolumeDistance, this.data.maxVolumeDistance, DisplayConfig.maxVolume(this.data.volume - 5));
    }
    public void fastForward() {
        assert isClient();
        WFNetwork.sendPlaytimeServer(this, Math.min(data.tick + MathAPI.msToTick(5000), data.tickMax), data.tickMax);
    }
    public void fastBackwards() {
        WFNetwork.sendPlaytimeServer(this, Math.max(data.tick - MathAPI.msToTick(5000), 0), data.tickMax);
    }

    public boolean isClient() {
        return this.level != null && this.level.isClientSide;
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(this.getDisplayBlock().getFacing());
    }

    public DisplayBlock getDisplayBlock() {
        return (DisplayBlock) this.getBlockState().getBlock();
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

            if (state.hasProperty(DisplayBlock.VISIBLE) && state.getValue(DisplayBlock.VISIBLE) != tile.data.frameVisibility) {
                level.setBlock(pos, state.setValue(DisplayBlock.VISIBLE, tile.data.frameVisibility), 0);
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