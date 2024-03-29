package me.srrapero720.waterframes.common.screens;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import me.srrapero720.waterframes.common.screens.styles.ScreenStyles;
import me.srrapero720.waterframes.common.screens.widgets.WidgetPairTable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

public class RemoteControlScreen extends GuiLayer {
    public GuiButtonIcon active;
    public GuiButtonIcon muted;
    public GuiButtonIcon reload;

    public GuiButtonIcon play;
    public GuiButtonIcon pause;
    public GuiButtonIcon stop;
    public GuiButtonIcon volumeUp;
    public GuiButtonIcon volumeDown;

    protected final DisplayTile tile;
    private final Player player;
    private final CompoundTag nbt;
    private final Item item;

    public RemoteControlScreen(Player player, DisplayTile tile, CompoundTag nbt, Item item) {
        super("remote_screen", 82, 210);
        this.player = player;
        this.nbt = nbt;
        this.item = item;

        this.align = Align.STRETCH;
        this.flow = GuiFlow.STACK_Y;
        this.tile = tile;

        this.active = new GuiButtonIcon("active_toggle", IconStyles.OFF_ON, button -> tile.setActiveMode(!tile.data.active)) {
            @Override
            @OnlyIn(Dist.CLIENT)
            public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
                return ScreenStyles.RED_BACKGROUND;
            }

            @Override
            @OnlyIn(Dist.CLIENT)
            public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
                return ScreenStyles.RED_BORDER;
            }
        };

        this.muted = new GuiButtonIcon("muted_toggle", IconStyles.MUTE, button -> tile.setMutedMode(!tile.data.muted)) {
            @Override
            @OnlyIn(Dist.CLIENT)
            public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
                return ScreenStyles.BLUE_BACKGROUND;
            }

            @Override
            @OnlyIn(Dist.CLIENT)
            public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
                return ScreenStyles.BLUE_BORDER;
            }
        };

        this.reload = new GuiButtonIcon("reload", IconStyles.RELOAD, button -> { if (tile.imageCache != null) tile.imageCache.reload(); });

        this.play = new GuiButtonIcon("pause", IconStyles.PAUSE, button -> tile.setPauseMode(true));
        this.pause = new GuiButtonIcon("play", IconStyles.PLAY, button -> tile.setPauseMode(false));
        this.stop = new GuiButtonIcon("stop", IconStyles.STOP, button -> tile.stop());

        this.volumeUp = new GuiButtonIcon("volume_up", IconStyles.VOLUME_UP, button -> tile.volumeUp());
        this.volumeDown = new GuiButtonIcon("volume_down", IconStyles.VOLUME_DOWN, button -> tile.volumeDown());
    }


    @Override
    public void create() {
        this.add(new WidgetPairTable(GuiFlow.STACK_X)
                .addLeft(this.active)
                .addRight(this.muted)
                .setAlignRight(Align.RIGHT)
        );

        this.add(new GuiParent().setDim(0, 20));

        this.add(new GuiParent("", GuiFlow.STACK_X, Align.STRETCH)
                .addWidget(new GuiParent("", GuiFlow.STACK_Y, Align.LEFT)
                        .addWidget(this.volumeUp.setDim(12, 25).setExpandableX())
                        .addWidget(this.volumeDown.setDim(12, 25).setExpandableX())
                        .setSpacing(0)
                )
                .addWidget(reload)
                .addWidget(new GuiParent("", GuiFlow.STACK_Y, Align.LEFT)
                        .addWidget(this.volumeUp.setDim(12, 25).setExpandableX())
                        .addWidget(this.volumeDown.setDim(12, 25).setExpandableX())
                        .setSpacing(0)
                )
                .setVAlign(VAlign.BOTTOM)
        );

        this.add(new GuiParent().setDim(0, 20));

        this.add(new GuiParent("", GuiFlow.STACK_X, Align.CENTER)
                .addWidget(this.pause.setExpandableX())
                .addWidget(this.play.setExpandableX())
                .addWidget(this.stop.setExpandableX())
        );

        this.add(new WidgetPairTable(GuiFlow.STACK_X)
                .addLeft(new GuiButtonIcon("fast_backward", IconStyles.FAST_BACKWARD, button -> tile.fastBackwards()))
                .addRight(new GuiButtonIcon("fast_foward", IconStyles.FAST_FOWARD, button -> tile.fastForward()))
                .setAlignRight(Align.RIGHT)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) { return ScreenStyles.SCREEN_BACKGROUND; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) { return ScreenStyles.SCREEN_BORDER; }
}