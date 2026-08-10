package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.DisplaysConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.client.display.Display;
import me.srrapero720.waterframes.client.ui.screen.DisplayScreen;
import me.srrapero720.waterframes.client.ui.screen.RemoteScreen;
import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.block.data.DisplayCaps;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.data.types.PositionHorizontal;
import me.srrapero720.waterframes.common.block.data.types.PositionVertical;
import me.srrapero720.waterframes.common.media.DisplayBridge;
import me.srrapero720.waterframes.common.network.ControlPacket;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import me.srrapero720.waterframes.common.network.packets.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.watermedia.api.media.MRL;
import org.watermedia.api.media.MediaAPI;
import org.watermedia.api.media.players.MediaPlayer;
import org.watermedia.api.media.players.ServerMediaPlayer;
import org.watermedia.api.media.players.sync.Config;
import me.srrapero720.waterframes.common.util.geo.Axis;
import me.srrapero720.waterframes.common.util.geo.Facing;
import me.srrapero720.waterframes.common.util.geo.AlignedBox;

public class DisplayTile extends BlockEntity {
    public final DisplayData data;
    public final DisplayCaps caps;
    public MRL mrl;
    public Display display;
    private boolean isReleased;

    // SERVER SIDE CLOCK: WATERMEDIA OWNS THE TIMELINE AND BROADCASTS IT, THE TILE OWNS ITS LIFE
    private ServerMediaPlayer clock;
    private String clockKey;

    // this is more a runtime-block calculation variables, doesn't fit on DisplayData
    private int lightLevel = 0;
    private int analogRedstoneLevel = 0;

    public DisplayTile(DisplayData data, DisplayCaps caps, BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.data = data;
        this.caps = caps;
    }

    public Display activeDisplay() {
        return display;
    }

    /** Clock of this display's session, owned by the server and null while nothing plays. */
    public ServerMediaPlayer clock() {
        return this.clock;
    }

    /** Player following the session on this client, null until the media opens. */
    public MediaPlayer follower() {
        return this.display != null ? this.display.player() : null;
    }

    /**
     * Keeps the session clock pointed at what the display plays. A clock latches the first
     * duration it is told for its whole life, so new media always means a new instance, and
     * two sources of the same media are two different media as far as time goes.
     */
    private void requestClock() {
        String url = this.data.getUrl();
        if (url == null || url.isEmpty()) {
            this.cleanClock();
            return;
        }

        String key = url + '#' + this.data.getSource();
        if (!key.equals(this.clockKey)) {
            this.cleanClock();
            this.clockKey = key;
            DisplayBridge bridge = new DisplayBridge(this.level, this.getBlockPos());
            this.clock = MediaAPI.createPlayer(bridge, Config.Capability.CONTROLS);
            this.clock.repeat(this.data.loop);
            this.clock.start();
            WaterFrames.LOGGER.debug("Opened media session {} on {}", bridge.session(), key);
        }

        // A PAUSED OR SWITCHED OFF DISPLAY HOLDS ITS SESSION INSTEAD OF LOSING IT, AND THE POSITION
        // WITH IT; STOPPED AND ENDED CLOCKS ARE LEFT ALONE, THEY ARE NOT WAITING FOR ANYTHING
        boolean hold = this.data.paused || !this.data.active;
        if (hold && this.clock.playing()) this.clock.pause(true);
        else if (!hold && this.clock.paused()) this.clock.pause(false);
    }

    /**
     * Sends the session back to the beginning. Media that did not change keeps its clock: a
     * fresh one would start counting revisions from zero, and every viewer already following
     * this display would read the new session as stale traffic and ignore it.
     */
    public void restartClock() {
        if (this.clock != null) this.clock.start();
    }

    /** Drops the session; the next tick opens a new one if the display still has media. */
    public void cleanClock() {
        if (this.clock != null) {
            this.clock.release();
            this.clock = null;
        }
        this.clockKey = null;
    }

    public Display requestDisplay() {
        if (this.isReleased) {
            this.cleanDisplay();
            this.mrl = null;
            return null;
        }

        // WITHOUT A URL THE MRL MUST GO TOO: KEEPING IT MADE THE TILE REBUILD A PLAYER FROM THE
        // STALE MEDIA ON EVERY TICK AND RELEASE IT ON THE NEXT ONE
        String currentUrl = this.data.getUrl();
        if (!this.data.active || currentUrl == null || currentUrl.isEmpty()) {
            this.cleanDisplay();
            this.mrl = null;
            return null;
        }

        if (this.mrl == null || !this.mrl.uri.toString().equals(currentUrl)) {
            this.mrl = MediaAPI.mrl(currentUrl);
            this.cleanDisplay();
        }

        // ONE MEDIA HOLDS SEVERAL SOURCES, AND MOVING BETWEEN THEM IS A NEW PLAYER JUST THE SAME
        if (this.display != null && this.display.sourceIndex() != this.data.getSource()) this.cleanDisplay();

        // Create display once MRL is ready
        if (this.mrl.status().loaded()) {
            if (this.display != null) return this.display;
            return this.display = new Display(this);
        }

        return display;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        this.data.save(nbt, this);
        super.saveAdditional(nbt, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        this.data.load(nbt, this);
        super.loadAdditional(nbt, registries);
    }

    public void cleanDisplay() {
        if (this.display != null) {
            this.display.release();
            this.display = null;
        }
    }

    private void release() {
        this.cleanDisplay();
        this.isReleased = true;
    }

    public AlignedBox getRenderBox() {
        return this.caps.getBox(this, getDirection(), getAttachedFace(), true);
    }

    @Override
    public void setRemoved() {
        if (this.isClient()) this.release();
        this.cleanClock();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (this.isClient()) this.release();
        this.cleanClock();
        super.onChunkUnloaded();
    }

    public LevelChunk getChunk() {
        return this.getLevel().getChunkAt(this.getBlockPos());
    }

    public int getLightLevel() {
        return lightLevel;
    }

    public int getAnalogOutput() {
        return analogRedstoneLevel;
    }

    private int getLightLevel$internal() {
        return !this.data.hasUrl() ? 0 : (int) (((float) this.data.brightness / 255f) * level.getMaxLightLevel());
    }

    // THE SIGNAL READS THE SESSION CLOCK, WHICH IS THE ONE THAT KNOWS HOW FAR THE MEDIA GOT
    private int getAnalogOutput$internal() {
        if (this.clock == null || !this.data.active) return 0;
        long duration = this.clock.duration();
        if (duration <= 0) return 0;
        return Math.round((this.clock.time() / (float) duration) * (BlockStateProperties.MAX_LEVEL_15 - 1)) + 1;
    }

    // FROM THE CLIENT THE COMMAND TRAVELS TO THE SERVER, WHICH APPLIES IT AND ECHOES IT TO THE
    // CHUNK; FROM THE SERVER IT IS APPLIED HERE AND PUSHED TO THE VIEWERS DIRECTLY
    private void send(boolean clientSide, ControlPacket packet) {
        if (clientSide) DisplayNetwork.sendServer(packet);
        else            DisplayNetwork.sendClient(packet, this);
    }

    public void setActive(boolean clientSide, boolean mode) {
        this.send(clientSide, new ActivePacket(this.getBlockPos(), mode));
    }

    public void setMute(boolean clientSide, boolean mode) {
        this.send(clientSide, new MutePacket(this.getBlockPos(), mode));
    }

    public void setPause(boolean clientSide, boolean pause) {
        this.send(clientSide, new PausePacket(this.getBlockPos(), pause, false));
    }

    public void setStop(boolean clientSide) {
        this.send(clientSide, new PausePacket(this.getBlockPos(), true, true));
    }

    public void volumeUp(boolean clientSide) {
        this.send(clientSide, new VolumePacket(this.getBlockPos(), this.data.volume + 5));
    }

    public void volumeDown(boolean clientSide) {
        this.send(clientSide, new VolumePacket(this.getBlockPos(), this.data.volume - 5));
    }

    public void nextUrl(boolean clientSide) {
        this.send(clientSide, new NextPacket(this.getBlockPos()));
    }

    public void prevUrl(boolean clientSide) {
        this.send(clientSide, new PreviousPacket(this.getBlockPos()));
    }

    public void loop(boolean clientSide, boolean loop) {
        this.send(clientSide, new LoopPacket(this.getBlockPos(), loop));
    }

    public void position(boolean clientSide, PositionHorizontal horizontal, PositionVertical vertical) {
        this.send(clientSide, new PositionPacket(this.getBlockPos(), horizontal, vertical));
    }

    public void tick(BlockState state) {
        boolean refresh = false;

        if (this.isServer()) {
            this.requestClock();
            // A PLAYLIST THAT IS NOT LOOPING MOVES ON WHEN THE CLOCK REACHES THE END; THE NEXT ENTRY
            // MAY WELL BE THE SAME URL, AND IT STILL HAS TO START OVER
            if (this.clock != null && this.clock.ended() && !this.data.loop && this.data.nextUrl()) {
                this.restartClock();
                this.setDirty();
            }

            // REDSTONE, WHERE THE CLOCK IT MEASURES LIVES AND THE ONLY SIDE A COMPARATOR READS
            int redstoneLevel = getAnalogOutput$internal();
            if (this.analogRedstoneLevel != redstoneLevel) {
                this.analogRedstoneLevel = redstoneLevel;
                refresh = true;
            }
        }

        // LIGHT
        boolean lightOnPlay = DisplaysConfig.forceLightOnPlay() || DisplaysConfig.useLightOnPlay() && this.data.lit;
        int calculatedLight = getLightLevel$internal();
        if (lightOnPlay && this.lightLevel != calculatedLight) {
            lightLevel = calculatedLight;
            refresh = true;
        }

        // DO NOT SPAM BLOCKSTATES AND CHUNK UPDATES WHEN ISN'T NEEDED
        if (refresh) {
            this.level.getChunkSource().getLightEngine().checkBlock(this.getBlockPos());
            this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        }

        if (this.isClient()) {
            Display display = this.requestDisplay();
            if (display != null && display.canTick()) display.tick();
        }
    }

    public boolean isClient() {
        return this.level != null && this.level.isClientSide;
    }

    public boolean isServer() {
        return this.level != null && !this.level.isClientSide;
    }

    /** Opens this display's settings or remote screen. Client-only: no server caller ever links its body. */
    public void openScreen(boolean remote) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        client.setScreen(remote ? new RemoteScreen(client.player, this) : new DisplayScreen(this));
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(this.getDisplayBlock().getFacing());
    }

    public Direction getAttachedFace() {
        return this.getBlockState().getValue(DisplayBlock.ATTACHED_FACE);
    }

    public boolean canHideModel() {
        return this.getBlockState().hasProperty(DisplayBlock.VISIBLE);
    }

    public boolean isVisible() {
        return this.getBlockState().getValue(DisplayBlock.VISIBLE);
    }

    public void setVisibility(boolean visible) {
        this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(DisplayBlock.VISIBLE, visible), DisplayBlock.UPDATE_CLIENTS);
    }

    public DisplayBlock getDisplayBlock() {
        return (DisplayBlock) this.getBlockState().getBlock();
    }

    public boolean isPowered() {
        return this.getBlockState().getValue(DisplayBlock.POWERED);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        this.data.load(tag, this);
        this.setDirty();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return super.saveWithFullMetadata(pRegistries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void setDirty() {
        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), DisplayBlock.UPDATE_ALL);
        }
    }

    public static AlignedBox getBasicBox(DisplayTile tile) {
        final var facing = Facing.of(tile.getDirection());
        final var box = new AlignedBox();

        if (facing.positive) box.max(facing.axis, tile.data.projectionDistance);
        else box.min(facing.axis, 1f - tile.data.projectionDistance);

        Axis one = facing.one();
        Axis two = facing.two();

        if (facing.axis != Axis.Z) {
            one = facing.two();
            two = facing.one();
        }

        box.min(one, tile.data.min.x);
        box.max(one, tile.data.max.x);

        box.min(two, tile.data.min.y);
        box.max(two, tile.data.max.y);

        if (tile.caps.projects() && (facing == Facing.NORTH || facing == Facing.EAST)) {
            switch (tile.data.getPosX()) {
                case LEFT -> {
                    box.min(one, 1 - tile.data.getWidth());
                    box.max(one, 1);
                }
                case RIGHT -> {
                    box.max(one, tile.data.getWidth());
                    box.min(one, 0f);
                }
            }
        }

        if (!tile.caps.projects() && (facing == Facing.WEST || facing == Facing.SOUTH)) {
            switch (tile.data.getPosX()) {
                case LEFT -> {
                    box.min(one, 1 - tile.data.getWidth());
                    box.max(one, 1);
                }
                case RIGHT -> {
                    box.max(one, tile.data.getWidth());
                    box.min(one, 0f);
                }
            }
        }
        return box;
    }

}