package me.srrapero720.waterframes.common.screen;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import me.srrapero720.waterframes.common.screens.styles.ScreenStyles;
import me.srrapero720.waterframes.common.screens.widgets.WidgetDoubleTable;
import me.srrapero720.waterframes.common.screens.widgets.WidgetParent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

public class RemoteControlScreen extends DisplayScreen<DisplayTile> {
    private final Player player;
    private final CompoundTag nbt;
    private final Item item;

    public RemoteControlScreen(Player player, DisplayTile tile, CompoundTag nbt, Item item) {
        super("remote_screen", tile, 80, 150);
        this.player = player;
        this.nbt = nbt;
        this.item = item;
    }

    @Override
    protected void onCreate() {
        WidgetDoubleTable first_table = new WidgetDoubleTable(GuiFlow.STACK_X)
                .addLeft(
                        new GuiButtonIcon("active_toggle", 12, 12, IconStyles.OFF_ON, button -> activeToggle.send(EndTag.INSTANCE)) {
                            @Override
                            @OnlyIn(Dist.CLIENT)
                            public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
                                return ScreenStyles.RED_BACKGROUND;
                            }

                            @Override
                            public GuiStyle getStyle() {
                                return super.getStyle();
                            }

                            @Override
                            @OnlyIn(Dist.CLIENT)
                            public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
                                return ScreenStyles.RED_BORDER;
                            }
                        });

        add(first_table);

        add(new WidgetParent(GuiFlow.STACK_Y)
                .addWidget(new GuiButtonIcon("volume_up", 12, 16, IconStyles.VOLUME_RANGE_MAX, button -> volumeUpAction.send(EndTag.INSTANCE)))
                .addWidget(new GuiButtonIcon("volume_down", 12, 16, IconStyles.VOLUME_RANGE_MIN, button -> volumeDownAction.send(EndTag.INSTANCE)))
        );

        add(new WidgetParent(GuiFlow.STACK_X)
                .addWidget(new GuiButtonIcon("pause", IconStyles.PAUSE, button -> pauseAction.send(EndTag.INSTANCE)))
                .addWidget(new GuiButtonIcon("play", 20, 12, IconStyles.PLAY, button -> playAction.send(EndTag.INSTANCE)))
                .addWidget(new GuiButtonIcon("stop", IconStyles.STOP, button -> stopAction.send(EndTag.INSTANCE)))
        );

        add(new WidgetParent(GuiFlow.STACK_X)
                .addWidget(new GuiButtonIcon("fast_backward", IconStyles.FAST_BACKWARD, button -> fastBackward.send(EndTag.INSTANCE)))
                .addWidget(new GuiButtonIcon("empty", 20, 12, IconStyles.STOP, button -> {}))
                .addWidget(new GuiButtonIcon("fast_foward", IconStyles.FAST_FOWARD, button -> fastForward.send(EndTag.INSTANCE)))
        );
    }

    @Override
    protected void syncData(DisplayTile tileBlock, Player player, CompoundTag tag) {

    }
}