package me.srrapero720.waterframes.common.screens;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import me.srrapero720.waterframes.common.screens.styles.ScreenStyles;
import me.srrapero720.waterframes.common.screens.widgets.WidgetTripleTable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import team.creative.creativecore.common.gui.*;
import team.creative.creativecore.common.gui.controls.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.controls.simple.GuiIcon;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

import java.util.Iterator;
import java.util.function.Consumer;

public class RemoteControlScreen extends GuiLayer {
    private static final int WIDTH = 60;
    private static final int HEIGHT = 180;

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
        super("remote_screen", WIDTH, HEIGHT);
        this.player = player;
        this.nbt = nbt;
        this.item = item;

        this.align = Align.STRETCH;
        this.flow = GuiFlow.STACK_Y;
        this.tile = tile;

        this.signal = new GuiIcon("signal_icon", IconStyles.SIGNAL_4);

        this.active = new GuiButtonIcon("active_toggle", IconStyles.OFF_ON, button -> tile.setActive(true, !tile.data.active)) {
            @Override
            @Environment(EnvType.CLIENT)
            public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
                return ScreenStyles.RED_BACKGROUND;
            }

            @Override
            @Environment(EnvType.CLIENT)
            public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
                return ScreenStyles.RED_BORDER;
            }
        };

        this.muted = new GuiButtonIcon("muted_toggle", IconStyles.VOLUME_MUTE, button -> tile.setMute(true, !tile.data.muted)) {
            @Override
            @Environment(EnvType.CLIENT)
            public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
                return ScreenStyles.BLUE_BACKGROUND;
            }

            @Override
            @Environment(EnvType.CLIENT)
            public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
                return ScreenStyles.BLUE_BORDER;
            }
        };

        this.arrowUp = new GuiButtonIcon("arrow_up", IconStyles.ARROW_UP, button -> {});
        this.arrowDown = new GuiButtonIcon("arrow_down", IconStyles.ARROW_DOWN, button -> {});
        this.arrowLeft = new GuiButtonIcon("arrow_left", IconStyles.ARROW_LEFT, button -> {});
        this.arrowRight = new GuiButtonIcon("arrow_right", IconStyles.ARROW_RIGHT, button -> {});
        this.arrowCenter = new GuiButtonIcon("arrow_center", IconStyles.ARROW_CENTER, button -> {});

        this.arrowUp.setEnabled(false);
        this.arrowDown.setEnabled(false);
        this.arrowLeft.setEnabled(false);
        this.arrowRight.setEnabled(false);
        this.arrowCenter.setEnabled(false);

        this.reload = new GuiButtonIcon("reload", IconStyles.RELOAD, button -> { if (tile.imageCache != null) tile.imageCache.reload(); });

        this.play = new GuiButtonIcon("pause", IconStyles.PAUSE, button -> tile.setPause(true, true));
        this.pause = new GuiButtonIcon("play", IconStyles.PLAY, button -> tile.setPause(true, false));
        this.stop = new GuiButtonIcon("stop", IconStyles.STOP, button -> tile.setStop(true));

        this.volumeUp = new GuiButtonIcon("volume_up", IconStyles.VOLUME_UP, button -> tile.volumeUp(true));
        this.volumeDown = new GuiButtonIcon("volume_down", IconStyles.VOLUME_DOWN, button -> tile.volumeDown(true));

        this.channelUp = new GuiButtonIcon("channel_up", IconStyles.CHANNEL_UP, button -> {});
        this.channelDown = new GuiButtonIcon("channel_down", IconStyles.CHANNEL_DOWN, button -> {});
        this.channelUp.setTooltip("waterframes.common.soon").setEnabled(false);
        this.channelDown.setTooltip("waterframes.common.soon").setEnabled(false);

        this.rewind = new GuiButtonIcon("fast_backward", IconStyles.FAST_BACKWARD, button -> tile.rewind(true));
        this.fastfoward = new GuiButtonIcon("fast_forward", IconStyles.FAST_FOWARD, button -> tile.fastFoward(true));
    }


    @Override
    public void create() {
        this.add(new WidgetTripleTable(GuiFlow.STACK_Y)
                .spaceBetween()
                .addLeft(this.active.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .addCenter(this.signal.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true).setExpandable())
                .addRight(this.muted.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .setAllExpandableX()
                .setFixedX()
        );

        this.add(new GuiParent().setExpandableY());

        this.add(new WidgetTripleTable(GuiFlow.STACK_Y)
                .spaceBetween()
                .addCenter(this.arrowUp.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .createRow()
                .addCenter(new GuiParent().setDim(1, 2))
                .createRow()
                .addLeft(this.arrowLeft.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .addCenter(this.arrowCenter.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .addRight(this.arrowRight.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .createRow()
                .addCenter(new GuiParent().setDim(1, 2))
                .createRow()
                .addCenter(this.arrowDown.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
        );

        this.add(new GuiParent().setExpandableY());

        this.add(new WidgetTripleTable(GuiFlow.STACK_Y)
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

        this.add(new GuiParent().setExpandableY());

        this.add(new WidgetTripleTable(GuiFlow.STACK_Y)
                .spaceBetween()
                .addLeft(this.pause.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .addCenter(this.play.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .addRight(this.stop.setDim(BUTTON_SIZE, BUTTON_SIZE).setSquared(true))
                .createRow()
                .addCenter(new GuiParent().setDim(1, 2))
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
        if (distance < WFConfig.maxRcDis()) {
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
                int diff = (int) ((distance / WFConfig.maxRcDis()) * 100); // 100 - far | 0 - closer
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

    @Override
    @Environment(EnvType.CLIENT)
    public GuiStyle getStyle() {
        return ScreenStyles.REMOTE_CONTROL;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
        return ScreenStyles.SCREEN_BACKGROUND;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
        return ScreenStyles.SCREEN_BORDER;
    }

    public static void hyperIterate(Iterator<GuiChildControl> iterator, Consumer<GuiControl> consumer) {
        while (iterator.hasNext()) {
            var item = iterator.next().control;
            if (item instanceof GuiParent parent) {
                hyperIterate(parent.iterator(), consumer);
            } else {
                consumer.accept(item);
            }
        }
    }
}