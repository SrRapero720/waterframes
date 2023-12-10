package me.srrapero720.waterframes.util.events;

import net.minecraftforge.eventbus.api.Event;

public class ClientPauseUpdateEvent extends Event {
    private final boolean paused;
    public ClientPauseUpdateEvent(boolean isPaused) {
        this.paused = isPaused;
    }

    public boolean isPaused() {
        return paused;
    }
}