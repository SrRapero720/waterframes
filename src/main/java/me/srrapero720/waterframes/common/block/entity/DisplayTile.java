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
import me.srrapero720.waterframes.common.network.ControlPacket;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import me.srrapero720.waterframes.common.network.packets.ActivePacket;
import me.srrapero720.waterframes.common.network.packets.LoopPacket;
import me.srrapero720.waterframes.common.network.packets.MediaSyncPacket;
import me.srrapero720.waterframes.common.network.packets.MutePacket;
import me.srrapero720.waterframes.common.network.packets.NextPacket;
import me.srrapero720.waterframes.common.network.packets.PausePacket;
import me.srrapero720.waterframes.common.network.packets.PositionPacket;
import me.srrapero720.waterframes.common.network.packets.PreviousPacket;
import me.srrapero720.waterframes.common.network.packets.SeekPacket;
import me.srrapero720.waterframes.common.network.packets.VolumePacket;
import me.srrapero720.waterframes.common.util.geo.AlignedBox;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.watermedia.api.player.MRL;
import org.watermedia.api.player.MediaPlayer;
import org.watermedia.api.player.PlayerAPI;
import org.watermedia.api.player.ServerMediaPlayer;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The one block entity behind every display. On the server it owns the media session: a
 * WaterMedia clock that keeps the timeline and whose snapshots it ships to the viewers. On the
 * client it owns the {@link Display}, the player that follows that session and draws it.
 */
public class DisplayTile extends BlockEntity {
    private static final Marker IT = MarkerManager.getMarker(DisplayTile.class.getSimpleName());
    // TICKS BETWEEN TWO SNAPSHOTS OF AN UNCHANGED CLOCK; IT COVERS A VIEWER THAT LOADED THE CHUNK IN BETWEEN
    private static final int SYNC_HEARTBEAT_TICKS = 20;
    /** How far the transport keys and a scroll over the seek bar jump, in milliseconds. */
    public static final long SKIP_MS = 5_000;

    // A VIEWER REPORT SHORTER THAN THIS IS NOISE OR A FORGERY, NO MEDIA ENDS THAT FAST
    private static final long MIN_REPORT_MS = 100;

    // EVERY SERVER TILE WITH A LIVE SESSION, AND THE ONES THE PAUSE MENU FROZE; SERVER THREAD ONLY
    private static final Set<DisplayTile> SESSIONS = new HashSet<>();
    private static final Set<DisplayTile> HELD = new HashSet<>();

    public final DisplayData data = new DisplayData();
    public final DisplayCaps caps;
    public MRL mrl;
    public Display display;
    private boolean released;
    // LAST SESSION SNAPSHOT HEARD ON THIS CLIENT, SO A PLAYER BORN BETWEEN TWO HEARTBEATS STARTS AT ONCE
    private byte[] pendingSync;

    // SERVER SIDE CLOCK: WATERMEDIA OWNS THE TIMELINE, THE TILE OWNS ITS LIFE AND CARRIES IT TO THE VIEWERS
    private ServerMediaPlayer clock;
    private String clockKey;
    private int clockRevision;
    private int clockHeartbeat;

    // RUNTIME BLOCK OUTPUTS, DERIVED EVERY TICK AND NEVER SAVED
    private int lightLevel;
    private int analogRedstoneLevel;

    public DisplayTile(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
        this.caps = ((DisplayBlock) state.getBlock()).caps;
        this.data.projectionDistance = this.caps.projectionDefault();
    }

    // ==========================================================================
    // PUBLIC API: SESSION
    // ==========================================================================

    /** Clock of this display's session, owned by the server and null while nothing plays. */
    public ServerMediaPlayer clock() {
        return this.clock;
    }

    /** Player following the session on this client, null until the media opens. */
    public MediaPlayer follower() {
        return this.display != null ? this.display.player() : null;
    }

    /** A viewer learned what the media is; only the first report counts, so a forged one cannot end a running session. */
    public void report(final long duration, final boolean live) {
        if (this.clock == null || this.clock.duration() > 0 || this.clock.live()) return;
        if (live) this.clock.live(true);
        else if (duration >= MIN_REPORT_MS) this.clock.syncDuration(duration);
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
        SESSIONS.remove(this);
        HELD.remove(this);
        this.dropClock();
    }

    // THE NEXT CLOCK ANNOUNCES ITSELF ON ITS FIRST TICK, WHATEVER REVISION IT STARTS FROM
    private void dropClock() {
        if (this.clock != null) this.clock.release();
        this.clock = null;
        this.clockKey = null;
        this.clockHeartbeat = SYNC_HEARTBEAT_TICKS;
    }

    // SHIPS THE CLOCK SNAPSHOT TO EVERY VIEWER OF THE CHUNK AND REMEMBERS WHICH REVISION THEY HEARD
    private void ship() {
        this.clockRevision = this.clock.revision();
        this.clockHeartbeat = 0;
        DisplayNetwork.sendChunk((ServerLevel) this.level, this.getBlockPos(), new MediaSyncPacket(this.getBlockPos(), this.clock.snapshot()));
    }

    /**
     * Freezes every running session while the singleplayer pause menu freezes the ticks, since the clocks run
     * on wall time, and lets exactly those go again afterwards. Both snapshots ship from here: a display in a
     * loaded chunk outside simulation distance never ticks, so it would otherwise stay frozen.
     */
    public static void holdSessions(final boolean hold) {
        if (hold) {
            for (final DisplayTile tile: SESSIONS) {
                if (!tile.clock.pause(true)) continue;
                HELD.add(tile);
                tile.ship();
            }
            return;
        }
        for (final DisplayTile tile: HELD) {
            if (SESSIONS.contains(tile) && !tile.data.paused && tile.data.active && tile.clock.pause(false)) tile.ship();
        }
        HELD.clear();
    }

    /** Releases every session of a level going away; the tiles themselves may never hear about it. */
    public static void releaseSessions(final Level level) {
        for (final Iterator<DisplayTile> it = SESSIONS.iterator(); it.hasNext();) {
            final DisplayTile tile = it.next();
            if (tile.level != level) continue;
            it.remove();
            HELD.remove(tile);
            tile.dropClock();
        }
    }

    // ==========================================================================
    // PUBLIC API: CLIENT DISPLAY
    // ==========================================================================

    public Display requestDisplay() {
        // WITHOUT A URL THE MRL MUST GO TOO: KEEPING IT MADE THE TILE REBUILD A PLAYER FROM THE
        // STALE MEDIA ON EVERY TICK AND RELEASE IT ON THE NEXT ONE
        final String url = this.data.getUrl();
        if (this.released || !this.data.active || url == null || url.isEmpty()) {
            this.cleanDisplay();
            this.mrl = null;
            return null;
        }

        if (this.mrl == null || !this.mrl.uri().toString().equals(url)) {
            this.mrl = PlayerAPI.mrl(url);
            this.cleanDisplay();
        }

        // ONE MEDIA HOLDS SEVERAL SOURCES, AND MOVING BETWEEN THEM IS A NEW PLAYER JUST THE SAME
        if (this.display != null && this.display.sourceIndex() != this.data.getSource()) this.cleanDisplay();

        if (this.display == null && this.mrl.status().loaded()) {
            this.display = new Display(this);
            // A SNAPSHOT HEARD BEFORE THE PLAYER EXISTED IS ITS ONLY WAY TO START BEFORE THE NEXT HEARTBEAT
            final MediaPlayer follower = this.display.player();
            if (follower != null && this.pendingSync != null) follower.sync(ByteBuffer.wrap(this.pendingSync));
        }
        return this.display;
    }

    /** Feeds a session snapshot to the follower, or keeps it for the player the next tick opens. */
    public void sync(final byte[] payload) {
        this.pendingSync = payload;
        final MediaPlayer follower = this.follower();
        if (follower != null) follower.sync(ByteBuffer.wrap(payload));
    }

    public void cleanDisplay() {
        if (this.display != null) {
            this.display.release();
            this.display = null;
        }
    }

    /** Refetches the media and drops the player, so the next tick rebuilds it from the fresh sources. */
    public void reload() {
        if (this.mrl != null) this.mrl.reload();
        this.cleanDisplay();
    }

    /** Opens this display's settings or remote screen. Client-only: no server caller ever links its body. */
    public void openScreen(final boolean remote) {
        final Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        client.setScreen(remote ? new RemoteScreen(client.player, this) : new DisplayScreen(this));
    }

    // ==========================================================================
    // PUBLIC API: CONTROLS
    // ==========================================================================

    public void setActive(final boolean clientSide, final boolean active) { this.send(clientSide, new ActivePacket(this.getBlockPos(), active)); }
    public void setMute(final boolean clientSide, final boolean muted) { this.send(clientSide, new MutePacket(this.getBlockPos(), muted)); }
    public void setPause(final boolean clientSide, final boolean paused) { this.send(clientSide, new PausePacket(this.getBlockPos(), paused, false)); }
    public void setStop(final boolean clientSide) { this.send(clientSide, new PausePacket(this.getBlockPos(), true, true)); }
    public void volumeUp(final boolean clientSide) { this.send(clientSide, new VolumePacket(this.getBlockPos(), this.data.volume + 5)); }
    public void volumeDown(final boolean clientSide) { this.send(clientSide, new VolumePacket(this.getBlockPos(), this.data.volume - 5)); }
    public void nextUrl(final boolean clientSide) { this.send(clientSide, new NextPacket(this.getBlockPos())); }
    public void prevUrl(final boolean clientSide) { this.send(clientSide, new PreviousPacket(this.getBlockPos())); }
    public void loop(final boolean clientSide, final boolean loop) { this.send(clientSide, new LoopPacket(this.getBlockPos(), loop)); }
    public void seek(final boolean clientSide, final long time) { this.send(clientSide, new SeekPacket(this.getBlockPos(), time, false)); }
    public void skip(final boolean clientSide, final long delta) { this.send(clientSide, new SeekPacket(this.getBlockPos(), delta, true)); }

    public void position(final boolean clientSide, final PositionHorizontal horizontal, final PositionVertical vertical) {
        this.send(clientSide, new PositionPacket(this.getBlockPos(), horizontal, vertical));
    }

    // FROM THE CLIENT THE COMMAND TRAVELS TO THE SERVER, WHICH APPLIES IT AND ECHOES IT TO THE
    // CHUNK; FROM THE SERVER IT IS APPLIED HERE AND PUSHED TO THE VIEWERS DIRECTLY
    private void send(final boolean clientSide, final ControlPacket packet) {
        if (clientSide) DisplayNetwork.sendServer(packet);
        else DisplayNetwork.sendClient(packet, this);
    }

    // ==========================================================================
    // PUBLIC API: BLOCK STATE
    // ==========================================================================

    public int getLightLevel() { return this.lightLevel; }
    public int getAnalogOutput() { return this.analogRedstoneLevel; }
    public boolean isClient() { return this.level != null && this.level.isClientSide; }
    public boolean isServer() { return this.level != null && !this.level.isClientSide; }
    public DisplayBlock getDisplayBlock() { return (DisplayBlock) this.getBlockState().getBlock(); }
    public Direction getDirection() { return this.getBlockState().getValue(this.caps.facing()); }
    public Direction getAttachedFace() { return this.getBlockState().getValue(DisplayBlock.ATTACHED_FACE); }
    public boolean isPowered() { return this.getBlockState().getValue(DisplayBlock.POWERED); }
    public boolean canHideModel() { return this.caps.hideModel(); }
    public boolean isVisible() { return !this.caps.hideModel() || this.getBlockState().getValue(DisplayBlock.VISIBLE); }

    public void setVisibility(final boolean visible) {
        // A LEGACY LOAD REACHES HERE BEFORE THE TILE HAS A LEVEL; THE STATE IS WRITTEN ONCE IT IS PLACED
        if (!this.caps.hideModel() || this.level == null) return;
        this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(DisplayBlock.VISIBLE, visible), DisplayBlock.UPDATE_CLIENTS);
    }

    public AlignedBox getRenderBox() {
        return this.caps.box(this, this.getDirection(), this.getAttachedFace());
    }

    /** Pushes the data to disk and to every client watching the block. */
    public void setDirty() {
        if (this.level == null) return;
        this.level.blockEntityChanged(this.getBlockPos());
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), DisplayBlock.UPDATE_ALL);
    }

    // ==========================================================================
    // TICK
    // ==========================================================================

    public void tick() {
        boolean refresh = false;

        if (this.isServer()) {
            this.requestClock();
            // A PLAYLIST THAT IS NOT LOOPING MOVES ON WHEN THE CLOCK REACHES THE END; THE NEXT ENTRY
            // MAY WELL BE THE SAME URL, AND IT STILL HAS TO START OVER
            if (this.clock != null && this.clock.ended() && !this.data.loop && this.data.nextUrl()) {
                this.restartClock();
                this.setDirty();
            }

            // THE COMPARATOR SIGNAL READS THE SESSION CLOCK, THE ONE THAT KNOWS HOW FAR THE MEDIA GOT, 1..15 OVER ITS LENGTH
            final long duration = this.clock != null && this.data.active ? this.clock.duration() : 0;
            final int redstoneLevel = duration <= 0 ? 0 : Math.round((this.clock.time() / (float) duration) * (BlockStateProperties.MAX_LEVEL_15 - 1)) + 1;
            if (this.analogRedstoneLevel != redstoneLevel) {
                this.analogRedstoneLevel = redstoneLevel;
                refresh = true;
            }
        }

        // LIGHT FOLLOWS THE BRIGHTNESS WHILE ENABLED AND GOES OUT THE TICK IT IS SWITCHED OFF
        final boolean lit = DisplaysConfig.forceLightOnPlay() || DisplaysConfig.useLightOnPlay() && this.data.lit;
        final int light = lit && this.data.active && this.data.hasUrl() ? (int) ((this.data.brightness / 255f) * this.level.getMaxLightLevel()) : 0;
        if (this.lightLevel != light) {
            this.lightLevel = light;
            refresh = true;
        }

        // DO NOT SPAM BLOCKSTATES AND CHUNK UPDATES WHEN ISN'T NEEDED
        if (refresh) {
            this.level.getChunkSource().getLightEngine().checkBlock(this.getBlockPos());
            this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        }

        if (this.isClient()) {
            final Display display = this.requestDisplay();
            if (display != null && display.canTick()) display.tick();
        }
    }

    // KEEPS THE SESSION CLOCK POINTED AT WHAT THE DISPLAY PLAYS. A CLOCK LATCHES THE FIRST DURATION IT IS TOLD FOR
    // ITS WHOLE LIFE, SO NEW MEDIA MEANS A NEW INSTANCE, AND TWO SOURCES OF ONE MEDIA ARE TWO MEDIA FOR TIME
    private void requestClock() {
        final String url = this.data.getUrl();
        if (url == null || url.isEmpty()) {
            this.cleanClock();
            return;
        }

        final String key = url + '#' + this.data.getSource();
        if (!key.equals(this.clockKey)) {
            this.cleanClock();
            this.clockKey = key;
            this.clock = PlayerAPI.serverPlayer();
            this.clock.repeat(this.data.loop);
            this.clock.start();
            SESSIONS.add(this);
            WaterFrames.LOGGER.debug(IT, "Opened media session at {} on {}", this.getBlockPos(), key);
        }

        // A PAUSED OR SWITCHED OFF DISPLAY HOLDS ITS SESSION INSTEAD OF LOSING IT, AND THE POSITION
        // WITH IT; STOPPED AND ENDED CLOCKS ARE LEFT ALONE, THEY ARE NOT WAITING FOR ANYTHING
        final boolean hold = this.data.paused || !this.data.active;
        if (hold && this.clock.playing()) this.clock.pause(true);
        else if (!hold && this.clock.paused()) this.clock.pause(false);

        // THE VIEWERS FOLLOW SNAPSHOTS: EVERY STATE CHANGE SHIPS AT ONCE AND A STEADY CLOCK REPEATS
        // ITSELF ON THE HEARTBEAT, SINCE THE FOLLOWERS EXTRAPOLATE THE TIME ON THEIR OWN IN BETWEEN
        this.clockHeartbeat++;
        if (this.clock.revision() != this.clockRevision || this.clockHeartbeat >= SYNC_HEARTBEAT_TICKS) this.ship();
    }

    // ==========================================================================
    // LIFECYCLE AND PERSISTENCE
    // ==========================================================================

    @Override
    public void setRemoved() {
        this.release();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        this.release();
        super.onChunkUnloaded();
    }

    private void release() {
        if (this.isClient()) {
            this.cleanDisplay();
            this.released = true;
        }
        this.cleanClock();
    }

    @Override
    protected void saveAdditional(final CompoundTag nbt, final HolderLookup.Provider registries) {
        this.data.save(nbt, this);
        super.saveAdditional(nbt, registries);
    }

    @Override
    protected void loadAdditional(final CompoundTag nbt, final HolderLookup.Provider registries) {
        final String before = this.data.getUrl() + '#' + this.data.getSource();
        this.data.load(nbt, this);
        // NEW MEDIA MEANS A NEW SESSION: A SNAPSHOT HEARD FOR THE OLD ONE MUST NOT START THE NEXT PLAYER. THE
        // SERVER SENDS THIS DATA BEFORE THE NEW CLOCK SPEAKS, SO WHAT ARRIVES AFTERWARDS IS THE RIGHT SNAPSHOT
        if (!before.equals(this.data.getUrl() + '#' + this.data.getSource())) this.pendingSync = null;
        super.loadAdditional(nbt, registries);
    }

    // THE WHOLE DATA TRAVELS TO THE CLIENT, WHICH ONLY LOADS IT: A RENDER-ONLY LEVEL (CREATE CONTRAPTIONS)
    // CANNOT TAKE A DIRTY MARK, AND A CLIENT HAS NOTHING TO BROADCAST
    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider registries) {
        return super.saveWithFullMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
