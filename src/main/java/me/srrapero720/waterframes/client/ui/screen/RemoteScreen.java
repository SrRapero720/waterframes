package me.srrapero720.waterframes.client.ui.screen;

import me.srrapero720.waterframes.DisplaysConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterui.format.UIVar;
import me.srrapero720.waterui.screen.WaterScreen;
import me.srrapero720.waterframes.client.ui.Icons;
import me.srrapero720.waterui.theme.Icon;
import me.srrapero720.waterframes.common.block.data.types.PositionHorizontal;
import me.srrapero720.waterframes.common.block.data.types.PositionVertical;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.world.entity.player.Player;

public class RemoteScreen extends WaterScreen {
    private static final int WIDTH = 90;
    private static final int HEIGHT = 210;

    private final DisplayTile tile;
    private final Player player;

    public RemoteScreen(Player player, DisplayTile tile) {
        super(WaterFrames.asResource("remote"), WIDTH, HEIGHT);
        this.tile = tile;
        this.player = player;

        // ONE-LINERS REGISTER STRAIGHT INTO THE HOST STORE (§5.6, §12.2); COMPOSED GATES MIX
        // CAPABILITY && RANGE HERE BECAUSE THE FORMAT HAS NO EXPRESSIONS. remote.ui DECLARES THE repeat
        this.registerVar("in_range", this::inRange);
        this.registerVar("move_ready", () -> tile.caps.resizes() && this.inRange());
        this.registerVar("channel_ready", () -> !tile.data.playlist.isEmpty() && this.inRange());
        this.registerVar("mute_icon", () -> Icons.volume(tile.data.volume, tile.data.muted));

        this.registerUIEvent("power", () -> tile.setActive(true, !tile.data.active));
        this.registerUIEvent("mute", () -> tile.setMute(true, !tile.data.muted));
        this.registerUIEvent("arrowUp", () -> tile.position(true, null, tile.data.getPosY().up()));
        this.registerUIEvent("arrowDown", () -> tile.position(true, null, tile.data.getPosY().down()));
        this.registerUIEvent("arrowLeft", () -> tile.position(true, tile.data.getPosX().left(), null));
        this.registerUIEvent("arrowRight", () -> tile.position(true, tile.data.getPosX().right(), null));
        this.registerUIEvent("arrowCenter", () -> tile.position(true, PositionHorizontal.CENTER, PositionVertical.CENTER));
        this.registerUIEvent("reload", this::reload);
        this.registerUIEvent("play", () -> tile.setPause(true, false));
        this.registerUIEvent("pause", () -> tile.setPause(true, true));
        this.registerUIEvent("stop", () -> tile.setStop(true));
        this.registerUIEvent("volumeUp", () -> tile.volumeUp(true));
        this.registerUIEvent("volumeDown", () -> tile.volumeDown(true));
        this.registerUIEvent("channelUp", () -> tile.nextUrl(true));
        this.registerUIEvent("channelDown", () -> tile.prevUrl(true));
        // SKIPPING IS A SESSION COMMAND: THE PLAYER FORWARDS IT TO THE SERVER CLOCK ON ITS OWN
        this.registerUIEvent("rewind", () -> { if (tile.follower() != null) tile.follower().rewind(); });
        this.registerUIEvent("fastForward", () -> { if (tile.follower() != null) tile.follower().forward(); });
    }

    // SIGNAL BARS FADE WITH DISTANCE; OUT OF RANGE SHOWS THE DEAD ICON. THE ONE VAR WITH REAL
    // LOGIC, SO THE ONE STILL WORTH AN ANNOTATED METHOD (§5.6)
    @UIVar("signal_icon")
    public Icon signalIcon() {
        double distance = WaterFrames.getDistance(tile, player.position());
        double range = DisplaysConfig.maxRcDis();
        if (distance >= range) return Icons.SIGNAL_0;
        int pct = (int) (distance / range * 100);
        return pct < 25 ? Icons.SIGNAL_4 : pct < 50 ? Icons.SIGNAL_3 : pct < 75 ? Icons.SIGNAL_2 : Icons.SIGNAL_1;
    }

    // RANGE GATE: EVERY POWER AND TRANSPORT BUTTON DIMS ONCE THE PLAYER STEPS OUT OF RC RANGE
    private boolean inRange() {
        return WaterFrames.getDistance(tile, player.position()) < DisplaysConfig.maxRcDis();
    }

    @Override
    protected void build() {
        // PRIME THE TICKED BINDINGS SO THE FIRST FRAME SHOWS THE CORRECT ENABLED STATE
        this.tick();
    }

    private void reload() {
        if (tile.mrl == null) return;
        tile.mrl.reload();
        tile.cleanDisplay();
    }

    @Override
    public void tick() {
        super.tick();
        if (tile.isRemoved()) this.close();
    }
}
