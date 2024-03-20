package me.srrapero720.waterframes.common.screens;

import it.unimi.dsi.fastutil.Pair;
import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.DisplaysNet;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import me.srrapero720.waterframes.common.screens.styles.ScreenStyles;
import me.srrapero720.waterframes.common.screens.widgets.*;
import me.srrapero720.waterframes.cossporting.Crossponent;
import me.srrapero720.waterframes.util.FrameNet;
import me.srrapero720.watermedia.api.image.ImageAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.*;
import team.creative.creativecore.common.gui.controls.simple.*;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.text.TextListBuilder;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class DisplayScreen<TILE extends DisplayTile> extends GuiLayer {
    protected static final float SCALE = 1F / 16;

    // IMPORTANT
    protected final TILE tile;
    protected final GuiSyncLocal<CompoundTag> syncAction = null;

    // LABELS
    private final WidgetLabel url_label = new WidgetLabel("media_label", 0.75f).setTranslate("waterframes.gui.label.url");
    private final WidgetLabel tex_label = new WidgetLabel("tex_label", 0.75f).setTranslate("waterframes.gui.label.texture_settings");
    private final WidgetLabel media_label = new WidgetLabel("media_label", 0.75f).setTranslate("waterframes.gui.label.media_settings");

    // ICONS
    private final GuiIcon rotIcon = (GuiIcon) new GuiIcon("r_icon", 12, 12, IconStyles.ROTATION).setDim(12, 12).setTooltip("waterframes.gui.icon.rotation");
    private final GuiIcon transIcon = (GuiIcon) new GuiIcon("t_icon", 12, 12, IconStyles.TRANSPARENCY).setDim(12, 12).setTooltip("waterframes.gui.icon.transparency");
    private final GuiIcon brightIcon = (GuiIcon) new GuiIcon("b_icon", 12, 12, IconStyles.BRIGHTNESS).setDim(12, 12).setTooltip("waterframes.gui.icon.brightness");
    private final GuiIcon renderDistanceIcon = (GuiIcon) new GuiIcon("r_icon", 12, 12, IconStyles.DISTANCE).setDim(12, 12).setTooltip("waterframes.gui.icon.render_distance");
    private final GuiIcon projectDistanceIcon = (GuiIcon) new GuiIcon("pd_icon", 12, 12, IconStyles.PROJECTION_DISTANCE).setDim(12, 12).setTooltip("waterframes.gui.icon.projection_distance");
    private final GuiIcon volumeIcon = (GuiIcon) new GuiIcon("v_icon", 12, 10, IconStyles.VOLUME).setDim(12, 10).setTooltip("waterframes.gui.icon.volume");
    private final GuiIcon volumeMinIcon = (GuiIcon) new GuiIcon("v_min_icon", 12, 10, IconStyles.VOLUME_RANGE_MIN).setDim(12, 10).setTooltip("waterframes.gui.icon.volume_min");
    private final GuiIcon volumeMaxIcon = (GuiIcon) new GuiIcon("v_max_icon", 12, 10, IconStyles.VOLUME_RANGE_MAX).setDim(12, 10).setTooltip("waterframes.gui.icon.volume_max");

    // WIDGETS INSTANCES
    private final GuiButton saveButton;
    private final WidgetURLTextField urlTextField;

    private final GuiCounterDecimal widthTextField;
    private final GuiCounterDecimal heightTextField;

    private final GuiSlider rotation;
    private final GuiSlider alpha;
    private final GuiSlider brightness;
    private final GuiSlider renderDistance;
    private final GuiSlider projectionDistance;

    private final GuiCheckBox showModel;
    private final GuiCheckBox renderBehind;
    private final WidgetCheckButton loop;

    private final GuiControl playback;
    private final GuiControl stop;

    private final GuiStateIconButton audioOffset;

    private final GuiButton reloadAll;
    private final GuiButton reloadBtn;
    private final GuiControl seekBar;

    // WIDGETS
    private final GuiCheckBox flipXWidget;
    private final GuiCheckBox flipYWidget;

    protected GuiStateButton posXButton;
    protected GuiStateButton posYButton;
    private final GuiSlider volume;
    private final GuiSteppedSlider volumeMinSlider;
    private final WidgetSteppedSlider volumeMaxSlider;

    // ICONS
    private final GuiIcon positionViewer;

    public DisplayScreen(TILE tile) {
        super("display_screen", 260, 245);
        this.setAlign(Align.STRETCH);
        this.flow = GuiFlow.STACK_Y;
        this.tile = tile;

        this.saveButton = (GuiButton) new GuiButton("save", x -> {}).setTranslate("waterframes.gui.save");
        this.urlTextField = new WidgetURLTextField(this.tile, this.saveButton);
        this.widthTextField = (GuiCounterDecimal) new GuiCounterDecimal("width", tile.data.getWidth(), 0, DisplayConfig.maxWidth(), ControlFormatting.CLICKABLE_NO_PADDING)
                .setSpacing(0)
                .setStep(SCALE)
                .setAlign(Align.STRETCH)
                .setVAlign(VAlign.STRETCH);
        this.widthTextField.buttons.setVAlign(VAlign.STRETCH);

        this.heightTextField = (GuiCounterDecimal) new GuiCounterDecimal("height", tile.data.getHeight(), 0, DisplayConfig.maxHeight(), ControlFormatting.CLICKABLE_NO_PADDING)
                .setSpacing(0)
                .setStep(SCALE)
                .setAlign(Align.STRETCH)
                .setVAlign(VAlign.STRETCH);
        this.heightTextField.buttons.setVAlign(VAlign.STRETCH);

        this.widthTextField.addControl(new GuiButtonIcon("expandY", IconStyles.EXPAND_Y, integer -> {
            if (tile.display != null) {
                this.heightTextField.setValue(tile.display.height() / (tile.display.width() / widthTextField.getValue()));
            }
        }).setDim(16, 16).setTooltip("waterframes.gui.expand_x"));

        this.heightTextField.addControl(new GuiButtonIcon("expandX", IconStyles.EXPAND_X, integer -> {
            if (tile.display != null) {
                this.heightTextField.setValue(tile.display.width() / (tile.display.height() / widthTextField.getValue()));
            }
        }).setDim(16, 16).setTooltip("waterframes.gui.expand_y"));

        this.flipXWidget = new GuiCheckBox(DisplayData.FLIP_X, tile.data.flipX).setTranslate("waterframes.gui.flip_x");
        this.flipYWidget = new GuiCheckBox(DisplayData.FLIP_Y, tile.data.flipY).setTranslate("waterframes.gui.flip_y");

        this.rotation = new GuiSlider(DisplayData.ROTATION, tile.data.rotation, 0, 360, ValueParsers.ANGLE);
        this.alpha = new GuiSlider(DisplayData.ALPHA, tile.data.alpha, 0, 1, ValueParsers.PERCENT);
        this.brightness = new GuiSlider(DisplayData.BRIGHTNESS, tile.data.brightness, 0, 1, ValueParsers.PERCENT);
        this.renderDistance = new GuiSteppedSlider(DisplayData.RENDER_DISTANCE, tile.data.renderDistance, 5, 1024, ValueParsers.BLOCKS);
        this.projectionDistance = new GuiSteppedSlider(DisplayData.PROJECTION_DISTANCE, tile.data.projectionDistance, 4, 128, ValueParsers.BLOCKS);
        this.audioOffset = new GuiStateIconButton(DisplayData.AUDIO_OFFSET, IconStyles.AUDIO_POS_BLOCK, IconStyles.AUDIO_POS_PICTURE, IconStyles.AUDIO_POS_CENTER) {
            @Override
            public List<Component> getTooltip() {
                Component mode = switch (this.getState()) {
                    case 0 -> Crossponent.translatable("waterframes.gui.audio_pos.block");
                    case 1 -> Crossponent.translatable("waterframes.gui.audio_pos.projection");
                    case 2 -> Crossponent.translatable("waterframes.gui.audio_pos.center");
                    default -> throw new IllegalStateException("Given state is illegal");
                };
                return Collections.singletonList(Crossponent.translatableParse("waterframes.gui.audio_pos", mode));
            }
        }.setState(tile.data.getOffsetMode());
        this.audioOffset.setShadow(null);

        this.showModel = new GuiCheckBox(DisplayData.VISIBLE_FRAME, tile.data.frameVisibility).setTranslate("waterframes.gui.show_model");
        this.renderBehind = new GuiCheckBox(DisplayData.RENDER_BOTH_SIDES, tile.data.renderBothSides).setTranslate("waterframes.gui.render_behind");

        final int posXOrdinal = tile.data.getPosX().ordinal();
        final int posYOrdinal = tile.data.getPosY().ordinal();
        this.positionViewer = new GuiIcon("posView", 0, 0, IconStyles.POS_CORD[posXOrdinal][posYOrdinal]);
        this.posXButton = new GuiStateButton("pos_x", posXOrdinal, new TextListBuilder().addTranslated("waterframes.gui.pos_x.", "left", "right", "center"));
        this.posYButton = new GuiStateButton("pos_y", posYOrdinal, new TextListBuilder().addTranslated("waterframes.gui.pos_y.", "top", "bottom", "center"));

        this.playback = new GuiButtonIcon("playback", IconStyles.PLAY, button -> DisplaysNet.sendPlayBackState(tile));
        this.stop = new GuiButtonIcon("stop", IconStyles.STOP, button -> {});
        this.loop = new WidgetCheckButton(DisplayData.LOOP, tile.data.loop) {
            @Override
            public List<Component> getTooltip() {
                Component is = this.value
                        ? Crossponent.translatable("waterframes.common.enabled", ChatFormatting.GREEN)
                        : Crossponent.translatable("waterframes.common.disabled", ChatFormatting.RED);

                return Collections.singletonList(Crossponent.translatableParse("waterframes.gui.loop", is));
            }
        }.setIconsState(Pair.of(IconStyles.REPEAT_ON, IconStyles.REPEAT_OFF));

        this.volume = new GuiSlider(DisplayData.VOLUME, tile.data.volume, 0, DisplayConfig.maxVolume(), ValueParsers.PERCENT);
        this.volumeMinSlider = new GuiSteppedSlider(DisplayData.VOL_RANGE_MIN, tile.data.minVolumeDistance, 0, Math.min(tile.data.maxVolumeDistance, DisplayConfig.maxVolumeDistance()));
        this.volumeMaxSlider = new WidgetSteppedSlider(DisplayData.VOL_RANGE_MAX, volumeMinSlider, tile.data.maxVolumeDistance, 0, DisplayConfig.maxVolumeDistance());

        this.seekBar = new GuiSeekBar("seek", () -> tile.data.tick, () -> tile.display != null ? tile.display.durationInTicks() : 1, ValueParsers.TIME_DURATION_TICK)
                .setPosConsumer(v -> tile.data.tick = (int) v)
                .setLastPosConsumer(v -> FrameNet.syncPlaybackState(tile.getBlockPos(), tile.data.playing, tile.data.tick = (int) v))
                .setDim(150, 18)
                .setExpandableX();

        this.reloadAll = (GuiButton) new GuiButton("reload_all", x -> ImageAPI.reloadCache()).setTranslate("waterframes.gui.reload.all").setTooltip("waterframes.gui.reload.all.warning");
        this.reloadBtn = (GuiButton) new GuiButton("reload", x -> tile.imageCache.reload()).setTranslate("waterframes.gui.reload");
    }

    @Override
    public void create() {
        if (!isClient()) return;
        // URL FIELD
        final var table = new WidgetDoubleTable(GuiFlow.STACK_Y, 4)
                .addLeft(url_label)
                .addLeft(urlTextField.expandX())
                .addRight(new WidgetStatusIcon("", 25, 25, IconStyles.STATUS_OK, () -> this.tile.imageCache));
        this.add(table);


        // IMAGE SIZE
        var sizeTable = new WidgetParent(GuiFlow.STACK_X, Align.STRETCH, 4);
        if (tile.canResize()) {
            sizeTable.addWidget(this.widthTextField.setExpandableX())
                    .addWidget(this.heightTextField.setExpandableX())
                    .addWidget(new WidgetParent(GuiFlow.STACK_Y)
                            .addWidget(flipXWidget)
                            .addWidget(flipYWidget)
                    );
            this.add(sizeTable);
        }

        // PICTURE PROPERTIES
        final var basicOptions = new WidgetParent(GuiFlow.STACK_X, Align.STRETCH, 1)
                .addWidgetIf(tile.canHideModel(), showModel)
                .addWidgetIf(tile.canRenderBackside(), renderBehind)
                .addWidgetIf(!tile.canResize(), flipXWidget, flipYWidget);

        this.add(tex_label);
        this.add(new WidgetDoubleTable(GuiFlow.STACK_Y, 2)
                .addLeft(new WidgetParent(GuiFlow.STACK_X).addWidget(rotIcon).addWidget(rotation.setDim(130, 12)).setVAlign(VAlign.CENTER))
                .addLeft(new WidgetParent(GuiFlow.STACK_X).addWidget(transIcon).addWidget(alpha.setDim(130, 12)).setVAlign(VAlign.CENTER))
                .addLeft(new WidgetParent(GuiFlow.STACK_X).addWidget(brightIcon).addWidget(brightness.setDim(130, 12)).setVAlign(VAlign.CENTER))
                .addLeft(new WidgetParent(GuiFlow.STACK_X).addWidget(renderDistanceIcon).addWidget(renderDistance.setDim(130, 12)).setVAlign(VAlign.CENTER))
                .addLeftIf(tile.canProject(), new WidgetParent(GuiFlow.STACK_X).addWidget(projectDistanceIcon).addWidget(projectionDistance.setDim(115, 13)).addWidget(audioOffset.setDim(18, 8)).setVAlign(VAlign.CENTER))
                .addLeftIf(!basicOptions.isEmpty(), basicOptions)
                .addRightIf(tile.canResize(), positionViewer.setDim(40, 40), posXButton, posYButton)
                .setAlignRight(Align.CENTER)
                .setExpandableY()
        );

        // MEDIA SETTINGS
        this.add(media_label);
        final var mediaSettingsTable = new WidgetDoubleTable(GuiFlow.STACK_X, 4)
                .addRight(this.volumeIcon, this.volume.setDim(100, 16).setExpandableX())
                .setAlignRight(Align.RIGHT)
                .setVAlignRight(VAlign.CENTER)
                .setLeftExpandableX()
                .setRightExpandableX()
                .createRow()
                .addRight(
                        this.volumeMinIcon,
                        this.volumeMinSlider.setDim(63, 10).setExpandableX(),
                        this.volumeMaxIcon,
                        this.volumeMaxSlider.setDim(63, 10).setExpandableX())
                .setAlignRight(Align.RIGHT)
                .setVAlignRight(VAlign.CENTER)
                .setLeftExpandableX()
                .setRightExpandableX();

        this.add(mediaSettingsTable);

        // SEEKBAR + buttons
        this.add(new WidgetParent(GuiFlow.STACK_X,
                this.loop.setDim(14, 14),
                this.playback.setDim(14, 14),
                this.stop.setDim(14, 14),
                this.seekBar));

        // SAVE BUTTONS
        this.add(new WidgetDoubleTable(GuiFlow.STACK_X, Align.RIGHT, 2)
                .addLeft(this.reloadAll)
                .addRight(this.saveButton.setEnabled(DisplayConfig.canSave(getPlayer(), urlTextField.getText())))
                .addRight(this.reloadBtn)
                .setAlignRight(Align.RIGHT)
        );
    }

    @Override
    public void tick() {
        super.tick();
        if (!isClient()) return;
        volumeIcon.setIcon(IconStyles.getVolumeIcon((int) volume.value));
        positionViewer.setIcon(IconStyles.POS_CORD[posXButton.getState()][posYButton.getState()]);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) { return ScreenStyles.SCREEN_BACKGROUND; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) { return ScreenStyles.SCREEN_BORDER; }
}
