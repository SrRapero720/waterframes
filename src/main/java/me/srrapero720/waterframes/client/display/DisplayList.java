package me.srrapero720.waterframes.client.display;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPauseChangeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.ArrayList;
import java.util.List;

/** Every display alive on this client, for the operations that reach all of them at once. */
@EventBusSubscriber(value = Dist.CLIENT, modid = WaterFrames.ID)
public final class DisplayList {
    private static final List<Display> DISPLAYS = new ArrayList<>();

    private DisplayList() {}

    public static void add(final Display display) {
        DISPLAYS.add(display);
    }

    public static void remove(final Display display) {
        DISPLAYS.remove(display);
    }

    /** Refetches every media on screen and rebuilds its player. */
    public static void reloadAll() {
        for (final Display display: List.copyOf(DISPLAYS)) display.tile().reload();
    }

    // RELEASING A DISPLAY REMOVES IT FROM THE LIST, SO THE WALK GOES OVER A COPY
    public static void release() {
        for (final Display display: List.copyOf(DISPLAYS)) display.release();
        DISPLAYS.clear();
    }

    @SubscribeEvent
    public static void onUnloadingLevel(final LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) release();
    }

    // THE PAUSE MENU FREEZES THE INTEGRATED SERVER, SO ITS SESSION CLOCKS MUST FREEZE WITH IT; THE
    // EVENT ONLY EVER FIRES IN TRUE SINGLEPLAYER, A REMOTE SERVER NEVER STOPS FOR ONE PLAYER
    @SubscribeEvent
    public static void onClientPause(final ClientPauseChangeEvent.Post event) {
        final IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) return;
        final boolean paused = event.isPaused();
        server.execute(() -> DisplayTile.holdSessions(paused));
    }
}
