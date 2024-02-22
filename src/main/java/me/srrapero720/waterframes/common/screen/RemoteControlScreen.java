package me.srrapero720.waterframes.common.screen;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screen.widgets.*;
import me.srrapero720.waterframes.common.screen.widgets.styles.WidgetIcons;
import me.srrapero720.waterframes.common.screen.widgets.styles.WidgetStyles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.simple.GuiIconButton;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

public class RemoteControlScreen extends DisplayScreen<DisplayTile<?>> {
    private final Player player;
    private final CompoundTag nbt;
    private final Item item;

    public RemoteControlScreen(Player player, DisplayTile<?> tile, CompoundTag nbt, Item item) {
        super("remote_screen", tile, 80, 150);
        this.player = player;
        this.nbt = nbt;
        this.item = item;
    }

    @Override
    protected void onCreate() {
        WidgetDoubleTable first_table = new WidgetDoubleTable()
                .addOnFirst(
                        new GuiIconButton("active_toggle", 12, 12, WidgetIcons.OFF_ON, button -> activeToggle.send(EndTag.INSTANCE)) {
                            @Override
                            @OnlyIn(Dist.CLIENT)
                            public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
                                return WidgetStyles.RED_BACKGROUND;
                            }

                            @Override
                            public GuiStyle getStyle() {
                                return super.getStyle();
                            }

                            @Override
                            @OnlyIn(Dist.CLIENT)
                            public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
                                return WidgetStyles.RED_BORDER;
                            }
                        });

        add(first_table);

        add(new WidgetParent("", GuiFlow.STACK_Y)
                .add2(new GuiIconButton("volume_up", 12, 16, WidgetIcons.VOLUME_RANGE_MAX, button -> volumeUpAction.send(EndTag.INSTANCE)))
                .add2(new GuiIconButton("volume_down", 12, 16, WidgetIcons.VOLUME_RANGE_MIN, button -> volumeDownAction.send(EndTag.INSTANCE)))
        );

        add(new WidgetParent("", GuiFlow.STACK_X)
                .add2(new GuiIconButton("pause", WidgetIcons.PAUSE, button -> pauseAction.send(EndTag.INSTANCE)))
                .add2(new GuiIconButton("play", 20, 12, WidgetIcons.PLAY, button -> playAction.send(EndTag.INSTANCE)))
                .add2(new GuiIconButton("stop", WidgetIcons.STOP, button -> stopAction.send(EndTag.INSTANCE)))
        );

        add(new WidgetParent("", GuiFlow.STACK_X)
                .add2(new GuiIconButton("fast_backward", WidgetIcons.FAST_BACKWARD, button -> fastBackward.send(EndTag.INSTANCE)))
                .add2(new GuiIconButton("empty", 20, 12, WidgetIcons.STOP, button -> {}))
                .add2(new GuiIconButton("fast_foward", WidgetIcons.FAST_FOWARD, button -> fastForward.send(EndTag.INSTANCE)))
        );
    }

    @Override
    protected void syncData(DisplayTile<?> tileBlock, Player player, CompoundTag tag) {

    }
}