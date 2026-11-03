package me.srrapero720.waterframes.common.network;

import me.srrapero720.waterframes.WaterFramesConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.packets.ActivePacket;
import me.srrapero720.waterframes.common.network.packets.DataListSyncPacket;
import me.srrapero720.waterframes.common.network.packets.DataSyncPacket;
import me.srrapero720.waterframes.common.network.packets.LoopPacket;
import me.srrapero720.waterframes.common.network.packets.MediaReportPacket;
import me.srrapero720.waterframes.common.network.packets.MediaSyncPacket;
import me.srrapero720.waterframes.common.network.packets.MutePacket;
import me.srrapero720.waterframes.common.network.packets.NextPacket;
import me.srrapero720.waterframes.common.network.packets.OpenScreenPacket;
import me.srrapero720.waterframes.common.network.packets.PausePacket;
import me.srrapero720.waterframes.common.network.packets.PositionPacket;
import me.srrapero720.waterframes.common.network.packets.PreviousPacket;
import me.srrapero720.waterframes.common.network.packets.SeekPacket;
import me.srrapero720.waterframes.common.network.packets.VolumePacket;
import me.srrapero720.waterframes.common.network.packets.VolumeRangePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;


import static me.srrapero720.waterframes.WaterFrames.LOGGER;

/**
 * Wire of the displays. Controls travel both ways and the server is the only authority: a
 * client command is checked against the sender's permissions and reach before it is applied
 * and echoed to the chunk, and a server change is applied locally and pushed the same way.
 */
@EventBusSubscriber(modid = WaterFrames.ID)
public final class DisplayNetwork {
    private static final Marker IT = MarkerManager.getMarker(DisplayNetwork.class.getSimpleName());
    // BUMP ON ANY PAYLOAD OR CODEC CHANGE; MISMATCHED CLIENTS ARE REJECTED WHILE JOINING
    private static final String VERSION = "6";

    // REPEATABLE ANOMALIES LOG THEIR FIRST OCCURRENCE ONLY; HANDLERS RUN ON THE MAIN THREAD
    private static boolean rejectWarned;
    private static boolean malformedWarned;

    private DisplayNetwork() {}

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(final String name) {
        return new CustomPacketPayload.Type<>(WaterFrames.asResource(name));
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // NEOFORGE DISPATCHES HANDLERS ON THE MAIN THREAD BY DEFAULT, SO TOUCHING THE LEVEL IS SAFE
        final PayloadRegistrar registrar = event.registrar(VERSION);

        control(registrar, ActivePacket.TYPE, ActivePacket.CODEC);
        control(registrar, LoopPacket.TYPE, LoopPacket.CODEC);
        control(registrar, MutePacket.TYPE, MutePacket.CODEC);
        control(registrar, NextPacket.TYPE, NextPacket.CODEC);
        control(registrar, PausePacket.TYPE, PausePacket.CODEC);
        control(registrar, PositionPacket.TYPE, PositionPacket.CODEC);
        control(registrar, PreviousPacket.TYPE, PreviousPacket.CODEC);
        control(registrar, SeekPacket.TYPE, SeekPacket.CODEC);
        control(registrar, VolumePacket.TYPE, VolumePacket.CODEC);
        control(registrar, VolumeRangePacket.TYPE, VolumeRangePacket.CODEC);

        registrar.playToClient(MediaSyncPacket.TYPE, MediaSyncPacket.CODEC, DisplayNetwork::onMediaSync);
        registrar.playToServer(MediaReportPacket.TYPE, MediaReportPacket.CODEC, DisplayNetwork::onMediaReport);
        registrar.playToServer(DataSyncPacket.TYPE, DataSyncPacket.CODEC, DisplayNetwork::onData);
        registrar.playToServer(DataListSyncPacket.TYPE, DataListSyncPacket.CODEC, DisplayNetwork::onData);
        registrar.playToClient(OpenScreenPacket.TYPE, OpenScreenPacket.CODEC, DisplayNetwork::onOpenScreen);
    }

    // ==========================================================================
    // PUBLIC API: SENDING
    // ==========================================================================

    /** Asks a single player to open a display GUI; callers must have checked permissions first. */
    public static void openScreen(final ServerPlayer player, final BlockPos pos, final boolean remote) {
        PacketDistributor.sendToPlayer(player, new OpenScreenPacket(pos, remote));
    }

    /** Pushes a payload to everyone who can see the block, which is everyone who cares about it. */
    public static void sendChunk(final ServerLevel level, final BlockPos pos, final CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(pos), payload);
    }

    public static void sendServer(final CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    /** Server originated change: applies it locally and pushes it to everyone watching the chunk. */
    public static void sendClient(final ControlPacket packet, final DisplayTile tile) {
        packet.apply(tile);
        tile.setChanged();
        if (tile.getLevel() instanceof final ServerLevel level) sendChunk(level, packet.pos(), packet);
    }

    // ==========================================================================
    // HELPERS: HANDLERS
    // ==========================================================================

    // SESSION TRAFFIC IS ROUTED, NEVER APPLIED: THE CLOCK LIVES ON THE SERVER AND THE FOLLOWERS ON THE CLIENTS;
    // DOWNSTREAM THE SNAPSHOT REACHES THE FOLLOWER, UPSTREAM THE VIEWER REPORT REACHES THE CLOCK
    private static void onMediaSync(final MediaSyncPacket packet, final IPayloadContext context) {
        final DisplayTile tile = tile(packet.pos(), context);
        if (tile == null) return;
        try {
            tile.sync(packet.payload());
        } catch (final IllegalArgumentException e) {
            if (malformedWarned) return;
            malformedWarned = true;
            LOGGER.warn(IT, "Dropped a malformed media session payload for {}", packet.pos(), e);
        }
    }

    // ONLY A PLAYER WHO ACTUALLY WATCHES THE BLOCK CAN TELL THE CLOCK WHAT THE MEDIA IS
    private static void onMediaReport(final MediaReportPacket packet, final IPayloadContext context) {
        final DisplayTile tile = tile(packet.pos(), context);
        if (tile != null && context.player() instanceof final ServerPlayer player && tile.getLevel() instanceof final ServerLevel level
                && level.getChunkSource().chunkMap.getPlayers(new ChunkPos(tile.getBlockPos()), false).contains(player)) tile.report(packet.duration(), packet.live());
    }

    // tile.openScreen IS CLIENT-ONLY, SO THE UI CLASSES ARE NEVER LINKED ON A DEDICATED SERVER
    private static void onOpenScreen(final OpenScreenPacket packet, final IPayloadContext context) {
        final DisplayTile tile = tile(packet.pos(), context);
        if (tile != null) tile.openScreen(packet.remote());
    }

    private static void onControl(final ControlPacket packet, final IPayloadContext context) {
        final DisplayTile tile = tile(packet.pos(), context);
        if (tile == null) return;
        if (context.flow().isClientbound()) {
            packet.apply(tile);
            packet.applyClient(tile);
            return;
        }
        if (!allowed(context, tile)) return;
        packet.apply(tile);
        // A CONTROL IS STATE TOO: WITHOUT THE DIRTY MARK A PAUSE OR A MUTE WOULD BE LOST ON THE NEXT RESTART
        tile.setChanged();
        if (tile.getLevel() instanceof final ServerLevel level) sendChunk(level, packet.pos(), packet);
    }

    private static void onData(final DataPacket packet, final IPayloadContext context) {
        final DisplayTile tile = tile(packet.pos(), context);
        if (tile != null && allowed(context, tile)) packet.applyServer(tile, (ServerPlayer) context.player());
    }

    // ==========================================================================
    // HELPERS: GATES
    // ==========================================================================

    // THE SENDER MUST BE ALLOWED TO USE THE DISPLAY AND BE WITHIN REACH OF IT; ANYTHING ELSE IS A FORGED OR STALE
    // PACKET AND IS DROPPED WITHOUT TOUCHING THE BLOCK. ONE WARNING: A HOSTILE CLIENT MUST NOT FLOOD THE LOG
    private static boolean allowed(final IPayloadContext context, final DisplayTile tile) {
        if (context.player() instanceof final ServerPlayer player && WaterFramesConfig.canControl(player, tile)) return true;
        if (!rejectWarned) {
            rejectWarned = true;
            LOGGER.warn(IT, "Dropped a display command from {} for {}: not allowed or out of reach", context.player().getName().getString(), tile.getBlockPos());
        }
        return false;
    }

    // A PACKET OUTLIVING ITS DISPLAY IS ORDINARY (A BROKEN BLOCK, AN UNLOADED CHUNK), NOT AN ERROR. THE CHUNK IS
    // NEVER LOADED FOR IT: A FORGED FAR AWAY POSITION WOULD OTHERWISE MAKE THE SERVER GENERATE TERRAIN ON DEMAND
    private static DisplayTile tile(final BlockPos pos, final IPayloadContext context) {
        final Level level = context.player().level();
        if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof final DisplayTile tile) return tile;
        LOGGER.debug(IT, "Dropped a packet pointing at {}, no display there", pos);
        return null;
    }

    private static <T extends ControlPacket> void control(final PayloadRegistrar registrar, final CustomPacketPayload.Type<T> type,
                                                          final StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        registrar.playBidirectional(type, codec, DisplayNetwork::onControl);
    }
}
