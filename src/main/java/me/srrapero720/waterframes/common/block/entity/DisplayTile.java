package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.client.display.TextureDisplay;
import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.block.FrameBlock;
import me.srrapero720.waterframes.common.data.DisplayData;
import me.srrapero720.waterframes.util.FrameTools;
import me.srrapero720.waterframes.util.FrameNet;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import java.util.concurrent.atomic.AtomicBoolean;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public abstract class DisplayTile<DATA extends DisplayData> extends BlockEntity {
    public final DATA data;
    @OnlyIn(Dist.CLIENT) public volatile ImageCache imageCache;
    @OnlyIn(Dist.CLIENT) public volatile TextureDisplay display;
    @OnlyIn(Dist.CLIENT) public volatile String parsedUrl;
    @OnlyIn(Dist.CLIENT) private final AtomicBoolean released = new AtomicBoolean(false);


    public AlignedBox getBox(DirectionProperty directionProperty, float thickness, boolean gifSquare) {
        Direction direction = getBlockState().getValue(directionProperty);
        Facing facing = Facing.get(direction);
        AlignedBox box = DisplayBlock.box(direction, thickness);

        Axis one = facing.one();
        Axis two = facing.two();

        if (facing.axis != Axis.Z) {
            one = facing.two();
            two = facing.one();
        }

        if (gifSquare) {
            box.setMin(one, 0.1f);
            box.setMax(one, 0.9f);

            box.setMin(two, 0.1f);
            box.setMax(two, 0.9f);
        } else {
            box.setMin(one, this.data.min.x);
            box.setMax(one, this.data.max.x);

            box.setMin(two, this.data.min.y);
            box.setMax(two, this.data.max.y);
        }

        return box;
    }
    public abstract AlignedBox getBox();
    public abstract AlignedBox getGifBox();

    public DisplayTile(DATA data, BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.data = data;
    }

    public void setUrl(String url) {
        this.data.url = url;
        this.parsedUrl = FrameTools.patchUrl(this.data.url);
    }

    public String getUrl() { return this.data.url; }
    public String getParsedUrl() { return FrameTools.patchUrl(this.data.url); }

    @OnlyIn(Dist.CLIENT)
    public synchronized TextureDisplay requestDisplay() {
        if (getUrl().isEmpty() && display != null) {
            cleanDisplay(false);
            return null;
        }

        String url = getParsedUrl();
        if (released.get()) {
            imageCache = null;
            return null;
        }

        if (imageCache == null || !imageCache.url.equals(url)) {
            imageCache = ImageAPI.getCache(url, Minecraft.getInstance());
            cleanDisplay(false);
        }

        switch (imageCache.getStatus()) {
            case LOADING, FAILED, READY -> {
                if (display != null) return display;
                return display = new TextureDisplay(imageCache, new Vec3d(worldPosition), this);
            }

            case WAITING -> {
                cleanDisplay(false);
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
    private void cleanDisplay(boolean quiet) {
        if (display != null) {
            display.release(quiet);
            display = null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void release() {
        cleanDisplay(false);
        released.set(true);
    }

    public void play() { FrameNet.sendPlaybackState(worldPosition, level, data.playing = true, data.tick); }
    public void pause() { FrameNet.sendPlaybackState(worldPosition, level, data.playing = false, data.tick); }
    public void stop() { FrameNet.sendPlaybackState(worldPosition, level, data.playing = false, data.tick = 0); }
    public void fastbackwards() { FrameNet.sendPlaybackState(worldPosition, level, data.playing = true, data.tick + MathAPI.msToTick(5000)); }
    public void fastforward() { FrameNet.sendPlaybackState(worldPosition, level, data.playing = false, data.tick + MathAPI.msToTick(5000)); }
    public void fastBackwards() { FrameNet.sendPlaybackState(worldPosition, level, data.playing = true, data.tick + MathAPI.msToTick(5000)); }
    public void fastForward() { FrameNet.sendPlaybackState(worldPosition, level, data.playing = false, data.tick + MathAPI.msToTick(5000)); }

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
                if (state.getValue(FrameBlock.VISIBLE) != frame.data.visibleFrame) {
                    var brandNewState = state.setValue(FrameBlock.VISIBLE, frame.data.visibleFrame);
                    level.setBlock(pos, brandNewState, 0);
                }
            }
        }
    }

    /* TOOLS */
    public boolean isClient() { return (this.level != null && this.level.isClientSide); }
}