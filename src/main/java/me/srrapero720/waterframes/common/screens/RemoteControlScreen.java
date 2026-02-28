package me.srrapero720.waterframes.common.screens;

import me.srrapero720.waterframes.DisplaysConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.data.types.PositionHorizontal;
import me.srrapero720.waterframes.common.block.data.types.PositionVertical;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import me.srrapero720.waterframes.common.screens.styles.ScreenStyles;
import me.srrapero720.waterframes.common.screens.widgets.WidgetTripleTable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import team.creative.creativecore.common.gui.*;
import team.creative.creativecore.common.gui.control.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.control.simple.GuiIcon;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

import java.util.Iterator;
import java.util.function.Consumer;

public class RemoteControlScreen extends GuiLayer {
    private static final int WIDTH = 66;
    private static final int HEIGHT = 186;

    private static final int BUTTON_SIZE = 12;

    public GuiButtonIcon active;
    public GuiButtonIcon muted;
    public GuiButtonIcon reload;
    public GuiIcon signal;

    public GuiButtonIcon arrowUp;
    public GuiButtonIcon arrowDown;
    public GuiButtonIcon arrowLeft;
    public GuiButtonIcon arrowRight;
    public GuiButtonIcon arrowCenter;

    public GuiButtonIcon play;
    public GuiButtonIcon pause;
    public GuiButtonIcon stop;
    public GuiButtonIcon volumeUp;
    public GuiButtonIcon volumeDown;

    public GuiButtonIcon channelUp;
    public GuiButtonIcon channelDown;

    public GuiButtonIcon rewind;
    public GuiButtonIcon fastfoward;

    private boolean allEnabled = true;

    protected final DisplayTile tile;
    private final Player player;
    private final CompoundTag nbt;
    private final Item item;

    public RemoteControlScreen(Player player, DisplayTile tile, CompoundTag nbt, Item item) {
        super(true, "remote_screen", WIDTH, HEIGHT);
        this.player = player;
        this.nbt = nbt;
        this.item = item;

        this.setAlign(Align.STRETCH);
        this.setFlow(GuiFlow.STACK_Y);
        this.tile = tile;

        this.signal = new GuiIcon(this, "signal_icon", IconStyles.SIGNAL_4);

        this.active = new GuiButtonIcon(this, "active_toggle", IconStyles.OFF_ON, button -> tile.setActive(true, !tile.data.active));

        this.muted = new GuiButtonIcon(this, "muted_toggle", IconStyles.VOLUME_0, button -> tile.setMute(true, !tile.data.muted));

        this.arrowUp = new GuiButtonIcon(this, "arrow_up", IconStyles.ARROW_UP, button -> {
            tile.position(true, null, tile.data.getPosY().up());
        });
        this.arrowDown = new GuiButtonIcon(this, "arrow_down", IconStyles.ARROW_DOWN, button -> {
            tile.position(true, null, tile.data.getPosY().down());
        });
        this.arrowLeft = new GuiButtonIcon(this, "arrow_left", IconStyles.ARROW_LEFT, button -> {
            tile.position(true, tile.data.getPosX().left(), null);
        });
        this.arrowRight = new GuiButtonIcon(this, "arrow_right", IconStyles.ARROW_RIGHT, button -> {
            tile.position(true, tile.data.getPosX().right(), null);
        });
        this.arrowCenter = new GuiButtonIcon(this, "arrow_center", IconStyles.ARROW_CENTER, button -> {
            tile.position(true, PositionHorizontal.CENTER, PositionVertical.CENTER);
        });

        if (!tile.caps.resizes()) {
            this.arrowUp.setEnabled(false);
            this.arrowDown.setEnabled(false);
            this.arrowLeft.setEnabled(false);
            this.arrowRight.setEnabled(false);
            this.arrowCenter.setEnabled(false);
        }

        this.reload = new GuiButtonIcon(this, "reload", IconStyles.RELOAD, button -> { if (tile.imageCache != null) tile.imageCache.reload(); });

        this.play = new GuiButtonIcon(this, "pause", IconStyles.PAUSE, button -> tile.setPause(true, true));
        this.pause = new GuiButtonIcon(this, "play", IconStyles.PLAY, button -> tile.setPause(true, false));
        this.stop = new GuiButtonIcon(this, "stop", IconStyles.STOP, button -> tile.setStop(true));

        this.volumeUp = new GuiButtonIcon(this, "volume_up", IconStyles.VOLUME_UP, button -> tile.volumeUp(true));
        this.volumeDown = new GuiButtonIcon(this, "volume_down", IconStyles.VOLUME_DOWN, button -> tile.volumeDown(true));

        this.channelUp = new GuiButtonIcon(this, "channel_up", IconStyles.CHANNEL_UP, button -> tile.nextUri(true));
        this.channelDown = new GuiButtonIcon(this, "channel_down", IconStyles.CHANNEL_DOWN, button -> tile.prevUri(true));
        if (this.tile.data.uris.isEmpty()) {
            this.channelDown.setEnabled(false);
            this.channelUp.setEnabled(false);
        }

        this.rewind = new GuiButtonIcon(this, "fast_backward", IconStyles.FAST_BACKWARD, button -> tile.rewind(true));
        this.fastfoward = new GuiButtonIcon(this, "fast_forward", IconStyles.FAST_FOWARD, button -> tile.fastFoward(true));
    }


    @Override
    public void create() {
        this.add(new WidgetTripleTable(this, GuiFlow.STACK_Y)
                .spaceBetween()
                .addLeft(this.active.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .addCenter(this.signal.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true).setExpandable())
                .addRight(this.muted.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .setAllExpandableX()
                .setFixedX()
        );

        this.add(new GuiParent(this).setExpandableY());

        this.add(new WidgetTripleTable(this, GuiFlow.STACK_Y)
                .spaceBetween()
                .addCenter(this.arrowUp.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .createRow()
                .addCenter(new GuiParent(this).setDim(1, 2))
                .createRow()
                .addLeft(this.arrowLeft.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .addCenter(this.arrowCenter.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .addRight(this.arrowRight.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .createRow()
                .addCenter(new GuiParent(this).setDim(1, 2))
                .createRow()
                .addCenter(this.arrowDown.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
        );

        this.add(new GuiParent(this).setExpandableY());

        this.add(new WidgetTripleTable(this, GuiFlow.STACK_Y)
                .spaceBetween()
                .addLeft(this.volumeUp.setDim(BUTTON_SIZE, BUTTON_SIZE + 2).setSquared(true))
                .addRight(this.channelUp.setDim(BUTTON_SIZE, BUTTON_SIZE + 2).setSquared(true))
                .setAllExpandableX()
                .createRow()
                .addLeft(this.volumeDown.setDim(BUTTON_SIZE, BUTTON_SIZE + 2).setSquared(true))
                .addCenter(this.reload.setDim(BUTTON_SIZE, BUTTON_SIZE + 2).setSquared(true))
                .addRight(this.channelDown.setDim(BUTTON_SIZE, BUTTON_SIZE + 2).setSquared(true))
                .setAllExpandableX()
                .setFixedX()
        );

        this.add(new GuiParent(this).setExpandableY());

        this.add(new WidgetTripleTable(this, GuiFlow.STACK_Y)
                .spaceBetween()
                .addLeft(this.pause.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .addCenter(this.play.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .addRight(this.stop.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .createRow()
                .addCenter(new GuiParent(this).setDim(1, 2))
                .createRow()
                .addLeft(this.rewind.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .addRight(this.fastfoward.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .setAllExpandableX()
                .setFixedX()
        );
        this.tick();
    }

    @Override
    public void tick() {
        super.tick();
        if (tile.isRemoved()) {
            this.closeTopLayer();
        }
        if (!isClient()) return;

        double distance = WaterFrames.getDistance(tile, player.position());
        if (distance < DisplaysConfig.maxRcDis()) {
            if (!allEnabled) {
                this.allEnabled = true;
                hyperIterate(this.iterator(), c -> {
                    if (!c.name.contains("arrow") && !c.name.contains("channel")) {
                        c.setEnabled(allEnabled);
                    }
                });
            }
            if (distance == 0) {
                this.signal.setIcon(IconStyles.SIGNAL_4);
            } else {
                int diff = (int) ((distance / DisplaysConfig.maxRcDis()) * 100); // 100 - far | 0 - closer
                if (diff < 25) {
                    this.signal.setIcon(IconStyles.SIGNAL_4);
                } else if (diff < 50) {
                    this.signal.setIcon(IconStyles.SIGNAL_3);
                } else if (diff < 75) {
                    this.signal.setIcon(IconStyles.SIGNAL_2);
                } else if (diff < 100) {
                    this.signal.setIcon(IconStyles.SIGNAL_1);
                }
            }
        } else {
            this.signal.setIcon(IconStyles.SIGNAL_0);
            if (allEnabled) {
                this.allEnabled = false;
                hyperIterate(this.iterator(), c -> {
                    if (c.getClass() != GuiIcon.class) {
                        c.setEnabled(allEnabled);
                    }
                });
            }
        }
    }

    public static void hyperIterate(Iterator<GuiControl> iterator, Consumer<GuiControl> consumer) {
        while (iterator.hasNext()) {
            var item = iterator.next();
            if (item instanceof GuiParent parent) {
                hyperIterate(parent.iterator(), consumer);
            } else {
                consumer.accept(item);
            }
        }
    }
}