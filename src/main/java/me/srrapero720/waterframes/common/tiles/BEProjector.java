package me.srrapero720.waterframes.common.tiles;

import me.srrapero720.waterframes.WFRegistry;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.api.tiles.BEFrameBase;
import me.srrapero720.waterframes.common.blocks.Projector;
import me.srrapero720.waterframes.common.packets.FramesPacket;
import me.srrapero720.waterframes.api.displays.Display;
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
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.vec.Vec2f;
import team.creative.creativecore.common.util.math.vec.Vec3d;

public class BEProjector extends BEFrameBase {
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
    public int projectionDistance = 4;

    public BEProjector(BlockPos pos, BlockState state) {
        super(WFRegistry.TILE_PROJECTOR.get(), pos, state);
    }

    public AlignedBox getBox() {
        Direction direction = getBlockState().getValue(Projector.FACING);
        Facing facing = Facing.get(direction);
        AlignedBox box = Projector.box(direction, projectionDistance);

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

    public float getSizeX() { return max.x - min.x; }
    public float getSizeY() { return max.y - min.y; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() { return getBox().getBB(getBlockPos()); }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        savePicture(nbt);
    }

    public void play() {
        playing = true;
        WaterFrames.sendPacketToClient(new FramesPacket(worldPosition, playing, tick), level, worldPosition);
    }

    public void pause() {
        playing = false;
        WaterFrames.sendPacketToClient(new FramesPacket(worldPosition, playing, tick), level, worldPosition);
    }

    public void stop() {
        playing = false;
        tick = 0;
        WaterFrames.sendPacketToClient(new FramesPacket(worldPosition, playing, tick), level, worldPosition);
    }

    protected void savePicture(CompoundTag nbt) {
        nbt.putString("url", url);
        nbt.putFloat("minx", min.x);
        nbt.putFloat("miny", min.y);
        nbt.putFloat("maxx", max.x);
        nbt.putFloat("maxy", max.y);
        nbt.putFloat("rotation", rotation);
        nbt.putInt("render", renderDistance);
        nbt.putInt("projection_distance", projectionDistance);
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
        projectionDistance = nbt.contains("projection_distance") ? nbt.getInt("projection_distance") : 8;
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
        if (blockEntity instanceof BEProjector be) {
            if (level.isClientSide) {
                Display display = be.requestDisplay();
                if (display != null) display.tick(new Vec3d(pos), be.getURL(), be.volume, be.minDistance, be.maxDistance, be.playing, be.loop, be.tick);}
            if (be.playing) be.tick++;
        }
    }

    @Override
    public void setRemoved() {
        if (this.level.isClientSide && display != null)
            display.release();
    }

    @Override
    public void onChunkUnloaded() {
        if (this.level.isClientSide && display != null)
            display.release();
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
