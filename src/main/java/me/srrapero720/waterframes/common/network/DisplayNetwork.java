package me.srrapero720.waterframes.common.network;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.packets.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.watermedia.api.media.players.MediaPlayer;

import java.nio.ByteBuffer;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

@EventBusSubscriber(modid = WaterFrames.ID)
public class DisplayNetwork {
    // BUMP ON ANY PAYLOAD OR CODEC CHANGE; MISMATCHED CLIENTS ARE REJECTED WHILE JOINING
    private static final String VERSION = "4";

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String name) {
        return new CustomPacketPayload.Type<>(WaterFrames.asResource(name));
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        // NEOFORGE DISPATCHES HANDLERS ON THE MAIN THREAD BY DEFAULT, SO TOUCHING THE LEVEL IS SAFE
        PayloadRegistrar registrar = event.registrar(VERSION);

        control(registrar, ActivePacket.TYPE, ActivePacket.CODEC);
        control(registrar, LoopPacket.TYPE, LoopPacket.CODEC);
        control(registrar, MutePacket.TYPE, MutePacket.CODEC);
        control(registrar, NextPacket.TYPE, NextPacket.CODEC);
        control(registrar, PausePacket.TYPE, PausePacket.CODEC);
        control(registrar, PositionPacket.TYPE, PositionPacket.CODEC);
        control(registrar, PreviousPacket.TYPE, PreviousPacket.CODEC);
        control(registrar, VolumePacket.TYPE, VolumePacket.CODEC);
        control(registrar, VolumeRangePacket.TYPE, VolumeRangePacket.CODEC);

        registrar.playBidirectional(MediaSyncPacket.TYPE, MediaSyncPacket.CODEC, DisplayNetwork::onMediaSync);
        registrar.playToServer(DataSyncPacket.TYPE, DataSyncPacket.CODEC, DisplayNetwork::onData);
        registrar.playToServer(DataListSyncPacket.TYPE, DataListSyncPacket.CODEC, DisplayNetwork::onData);
        registrar.playToClient(OpenScreenPacket.TYPE, OpenScreenPacket.CODEC, DisplayNetwork::onOpenScreen);
    }

    /**
     * Session traffic is routed, never applied: the clock lives on the server and the players
     * that follow it on the clients, and each side hands the bytes to the one it owns.
     */
    private static void onMediaSync(MediaSyncPacket packet, IPayloadContext context) {
        // A SESSION OUTLIVING ITS DISPLAY IS ORDINARY (A BROKEN BLOCK, AN UNLOADED CHUNK), NOT AN ERROR
        if (!(context.player().level().getBlockEntity(packet.pos()) instanceof DisplayTile tile)) return;
        MediaPlayer player = context.flow().isServerbound() ? tile.clock() : tile.follower();
        if (player == null) return;

        try {
            player.sync(ByteBuffer.wrap(packet.payload()));
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Dropped a malformed media session payload for {}", packet.pos());
        }
    }

    // tile.openScreen IS CLIENT-ONLY, SO THE UI CLASSES ARE NEVER LINKED ON A DEDICATED SERVER
    private static void onOpenScreen(OpenScreenPacket packet, IPayloadContext context) {
        if (context.player().level().getBlockEntity(packet.pos()) instanceof DisplayTile tile) tile.openScreen(packet.remote());
    }

    /** Asks a single player to open a display GUI; callers must have checked permissions first. */
    public static void openScreen(ServerPlayer player, BlockPos pos, boolean remote) {
        PacketDistributor.sendToPlayer(player, new OpenScreenPacket(pos, remote));
    }

    private static <T extends ControlPacket> void control(PayloadRegistrar registrar, CustomPacketPayload.Type<T> type,
                                                          StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        registrar.playBidirectional(type, codec, DisplayNetwork::onControl);
    }

    private static void onControl(ControlPacket packet, IPayloadContext context) {
        DisplayTile tile = tile(packet.pos(), context);
        if (tile == null) return;

        packet.apply(tile);
        if (context.flow().isClientbound()) packet.applyClient(tile);
        else broadcast(packet, tile);
    }

    private static void onData(DataPacket packet, IPayloadContext context) {
        DisplayTile tile = tile(packet.pos(), context);
        if (tile != null) packet.applyServer(tile, (ServerPlayer) context.player());
    }

    private static DisplayTile tile(BlockPos pos, IPayloadContext context) {
        if (context.player().level().getBlockEntity(pos) instanceof DisplayTile tile) return tile;
        LOGGER.error("Received a packet pointing to an invalid DisplayTile position {}", pos);
        return null;
    }

    private static void broadcast(ControlPacket packet, DisplayTile tile) {
        if (tile.getLevel() instanceof ServerLevel level) sendChunk(level, packet.pos(), packet);
    }

    /** Pushes a payload to everyone who can see the block, which is everyone who cares about it. */
    public static void sendChunk(ServerLevel level, BlockPos pos, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(pos), payload);
    }

    public static void sendServer(ControlPacket packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void sendServer(DataPacket packet) {
        PacketDistributor.sendToServer(packet);
    }

    /** Server originated change: applies it locally and pushes it to everyone watching the chunk. */
    public static void sendClient(ControlPacket packet, DisplayTile tile) {
        packet.apply(tile);
        broadcast(packet, tile);
    }
}
