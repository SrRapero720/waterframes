package me.srrapero720.waterframes.common.screens;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.compat.videoplayer.VPCompat;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import me.srrapero720.waterframes.common.network.packets.DataSyncPacket;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import me.srrapero720.waterframes.common.screens.styles.ScreenStyles;
import me.srrapero720.waterframes.common.screens.widgets.*;
import me.srrapero720.waterframes.common.compat.creativecore.IScalableText;
import org.watermedia.api.image.ImageCache;
import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import team.creative.creativecore.common.gui.*;
import team.creative.creativecore.common.gui.controls.simple.*;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.parser.DoubleValueParser;
import team.creative.creativecore.common.gui.parser.IntValueParser;
import team.creative.creativecore.common.gui.parser.LongValueParser;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;
import team.creative.creativecore.common.util.type.Color;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

public class DisplayScreen extends GuiLayer {
    protected static final float SCALE = 1F / 16;
    protected static final int WIDTH = 225;
    protected static final int HEIGHT = 210;

    // IMPORTANT
    public final DisplayTile tile;

    // LABELS
    private final GuiLabel url_l = new GuiLabel("media_label").setTranslate("waterframes.gui.label.url");

    // ICONS
    private final GuiIcon rot_i = (GuiIcon) new GuiIcon("r_icon", IconStyles.ROTATION).setSquared(true).setTooltip("waterframes.gui.icon.rotation");
    private final GuiIcon vis_i = (GuiIcon) new GuiIcon("t_icon", IconStyles.TRANSPARENCY).setSquared(true).setTooltip("waterframes.gui.icon.alpha");
    private final GuiIcon bright_i = (GuiIcon) new GuiIcon("b_icon", IconStyles.BRIGHTNESS).setSquared(true).setTooltip("waterframes.gui.icon.brightness");
    private final GuiIcon render_i = (GuiIcon) new GuiIcon("r_icon", IconStyles.DISTANCE).setSquared(true).setTooltip("waterframes.gui.icon.render_distance");
    private final GuiIcon project_i = (GuiIcon) new GuiIcon("pd_icon",  IconStyles.PROJECTION_DISTANCE).setSquared(true).setTooltip("waterframes.gui.icon.projection_distance");
    private final GuiIcon vol_i = (GuiIcon) new GuiIcon("v_icon", IconStyles.VOLUME).setSquared(true).setTooltip("waterframes.gui.icon.volume");
    private final GuiIcon vol_min_i = (GuiIcon) new GuiIcon("v_min_icon", IconStyles.VOLUME_RANGE_MIN).setSquared(true).setTooltip("waterframes.gui.icon.volume_min");
    private final GuiIcon vol_max_i = (GuiIcon) new GuiIcon("v_max_icon", IconStyles.VOLUME_RANGE_MAX).setSquared(true).setTooltip("waterframes.gui.icon.volume_max");

    // WIDGETS INSTANCES
    public final GuiButtonIcon save;
    public final WidgetURLTextField url;

    public final GuiCounterDecimal widthField;
    public final GuiCounterDecimal heightField;

    public final GuiSlider rotation;
    public final GuiSteppedSlider alpha;
    public final GuiSteppedSlider brightness;
    public final GuiSteppedSlider render_distance;
    public final GuiSlider projection_distance;

    public final GuiCheckBox show_model;
    public final GuiCheckBox lit;
    public final GuiCheckButtonIcon mirror;
    public final GuiCheckButtonIcon loop;

    public final GuiCheckButtonIcon playback;
    public final GuiControl stop;

    public final GuiStateButtonIcon audio_offset;

    public final GuiButtonIcon reload;
    public GuiControl seekbar;

    public final GuiButtonIcon videoplayer;

    // WIDGETS
    public final GuiCheckBox flip_x;
    public final GuiCheckBox flip_y;

    public final GuiSteppedSlider volume;
    public final GuiSteppedSlider volume_min;
    public final GuiSteppedSlider volume_max;

    // ICONS
    public final WidgetClickableArea pos_view;

    public DisplayScreen(DisplayTile tile) {
        super("display_screen", WIDTH, HEIGHT);
        this.setAlign(Align.STRETCH);
        this.flow = GuiFlow.STACK_Y;
        this.tile = tile;

        this.url = new WidgetURLTextField(this.tile);

        var resize_y = new GuiButtonIcon("rs_y", IconStyles.EXPAND_Y, this::resizeYOnRatio);
        var resize_x = new GuiButtonIcon("rs_x", IconStyles.EXPAND_X, this::resizeXOnRation);

        this.widthField = new GuiCounterDecimal("width", BigDecimal.valueOf(tile.data.getWidth()).setScale(2, RoundingMode.CEILING).doubleValue(), 0.1, WFConfig.maxWidth(), ControlFormatting.CLICKABLE_NO_PADDING);
        this.widthField.setSpacing(0).setStep(SCALE).setAlign(Align.STRETCH).setVAlign(VAlign.STRETCH);

        this.widthField.buttons.setVAlign(VAlign.STRETCH);
        this.widthField.get("value").setTooltip("waterframes.common.width");
        this.widthField.addControl(resize_y.setDim(16, 16));

        this.heightField = new GuiCounterDecimal("height", BigDecimal.valueOf(tile.data.getHeight()).setScale(2, RoundingMode.CEILING).doubleValue(), 0.1, WFConfig.maxHeight(), ControlFormatting.CLICKABLE_NO_PADDING);
        this.heightField.setSpacing(0).setStep(SCALE).setAlign(Align.STRETCH).setVAlign(VAlign.STRETCH);

        this.heightField.buttons.setVAlign(VAlign.STRETCH);
        this.heightField.get("value").setTooltip("waterframes.common.height");
        this.heightField.addControl(resize_x.setDim(16, 16));

        this.flip_x = new GuiCheckBox(DisplayData.FLIP_X, tile.data.flipX);
        this.flip_y = new GuiCheckBox(DisplayData.FLIP_Y, tile.data.flipY);
        this.flip_x.setTranslate("waterframes.gui.flip_x");
        this.flip_y.setTranslate("waterframes.gui.flip_y");

        this.rotation = new GuiSlider(DisplayData.ROTATION, tile.data.rotation, 0, 360, DoubleValueParser.ANGLE);
        this.alpha = new GuiSteppedSlider(DisplayData.ALPHA, tile.data.alpha, 0, 255, (v, max) -> (Math.round(((v != 0 && max != 0 ? (float) v / (float) max : 0) * 100))) + "%");
        this.brightness = new GuiSteppedSlider(DisplayData.BRIGHTNESS, tile.data.brightness, 0, 255, (v, max) -> Math.round(((v != 0 && max != 0 ? (float) v / (float) max : 0) * 100)) + "%");
        this.render_distance = new GuiSteppedSlider(DisplayData.RENDER_DISTANCE, tile.data.renderDistance, 4, WFConfig.maxRenDis(), IntValueParser.BLOCKS);
        this.projection_distance = new GuiSlider(DisplayData.PROJECTION_DISTANCE, tile.data.projectionDistance, 4, WFConfig.maxProjDis(), DoubleValueParser.BLOCKS);
        this.audio_offset = new GuiStateButtonIcon(DisplayData.AUDIO_OFFSET, IconStyles.AUDIO_POS_BLOCK, IconStyles.AUDIO_POS_PICTURE, IconStyles.AUDIO_POS_CENTER);
        this.audio_offset.setControlFormatting(ControlFormatting.CLICKABLE_NO_PADDING)
                .setState(tile.data.getAudioPosition().ordinal())
                .setShadow(Color.NONE);

        this.mirror = new GuiCheckButtonIcon(DisplayData.RENDER_BOTH_SIDES, IconStyles.MIRROR_ON, IconStyles.MIRROR_OFF, tile.data.renderBothSides);
        this.mirror.setControlFormatting(ControlFormatting.CLICKABLE_NO_PADDING)
                .setShadow(Color.NONE);

        this.show_model = new GuiCheckBox("show_model", tile.canHideModel() && tile.isVisible());
        this.show_model.setTranslate("waterframes.gui.show_model");

        this.lit = new GuiCheckBox("lit", tile.canHideModel() && tile.isVisible());
        this.lit.setTranslate("waterframes.gui.lit");

        this.pos_view = new WidgetClickableArea("pos_view", tile.data.getPosX(), tile.data.getPosY());

        this.playback = new GuiCheckButtonIcon("playback", IconStyles.PLAY, IconStyles.PAUSE, tile.data.paused, button -> tile.setPause(true, !tile.data.paused));
        this.stop = new GuiButtonIcon("stop", IconStyles.STOP, button -> tile.setStop(true));
        this.loop = new GuiCheckButtonIcon(DisplayData.LOOP, IconStyles.REPEAT_ON, IconStyles.REPEAT_OFF, tile.data.loop, button -> tile.loop(true, !tile.data.loop));

        this.volume = new GuiSteppedSlider(DisplayData.VOLUME, tile.data.volume, 0, WFConfig.maxVol(), (v, max) -> v + "%");
        this.volume_min = new GuiSteppedSlider(DisplayData.VOL_RANGE_MIN, tile.data.minVolumeDistance, 0, Math.min(tile.data.maxVolumeDistance, WFConfig.maxVolDis()), IntValueParser.BLOCKS);
        this.volume_max = new GuiSteppedSlider(DisplayData.VOL_RANGE_MAX, tile.data.maxVolumeDistance, 0, WFConfig.maxVolDis(), IntValueParser.BLOCKS);
        this.volume_max.setMinSlider(this.volume_min);

        this.seekbar = new GuiSeekBar("seek", () -> tile.data.tick, () -> tile.data.tickMax, LongValueParser.TIME_DURATION_TICK)
                .setOnTimeUpdate(v -> tile.data.tick = (int) v)
                .setOnLastTimeUpdate(v -> tile.syncTime(true, (int) v, tile.data.tickMax));

        this.reload = new GuiButtonIcon("reload", IconStyles.RELOAD, x -> tile.imageCache.reload());
        this.reload.setTooltip("waterframes.gui.reload");
        if (isClient()){
            this.reload.setEnabled(enableReload());
        }
        this.save = new GuiButtonIcon("save", IconStyles.SAVE, click -> DisplayNetwork.sendServer(new DataSyncPacket(tile.getBlockPos(), DisplayData.build(this, tile))));
        this.save.setTooltip("waterframes.gui.save");

        if (VPCompat.installed()) {
            this.videoplayer = new GuiButtonIcon("", IconStyles.VIDEOPLAYER_PLAY, button -> {
                VPCompat.playVideo(tile.data.uri.toString(), tile.data.volume, false, true);
                tile.setPause(true, true);
            });
            this.videoplayer.setTooltip("waterframes.gui.videoplayer");
            if (isClient()) {
                this.videoplayer.setEnabled(enableVideoPlayer());
            }
        } else {
            this.videoplayer = null;
        }

        IScalableText.setScale(url_l, 0.75f);
        IScalableText.setScale(url, 0.80f);

        IScalableText.setScale(rotation, 0.90f);
        IScalableText.setScale(alpha, 0.90f);
        IScalableText.setScale(brightness, 0.90f);
        IScalableText.setScale(render_distance, 0.90f);
        IScalableText.setScale(projection_distance, 0.90f);
        IScalableText.setScale(volume, 0.90f);
        IScalableText.setScale(volume_min, 0.90f);
        IScalableText.setScale(volume_max, 0.90f);

        if (!tile.caps.resizes()) {
            this.setDim(WIDTH - 10, HEIGHT - 60);
        }
    }

    @Override
    public boolean isClient() {
        return tile.isClient();
    }

    @Override
    public void create() {
        if (!isClient()) return;
        // URL FIELD
        this.add(new WidgetPairTable(GuiFlow.STACK_Y, 4)
                .addLeft(url_l)
                .addLeft(url.setExpandableX())
                .addRight(new WidgetStatusIcon("", IconStyles.STATUS_OK, tile).setDim(30, 30)));


        // IMAGE SIZE
        this.add(tile.caps.resizes(), () -> new GuiParent("", GuiFlow.STACK_X, Align.STRETCH)
                .add(this.widthField.setExpandableX())
                .add(this.heightField.setExpandableX())
                .add(new GuiParent(GuiFlow.STACK_Y)
                        .add(flip_x)
                        .add(flip_y)
                )
                .setSpacing(4)
        );

        // PICTURE PROPERTIES
        this.add(new GuiParent().setDim(-1, 4));
        this.add(new WidgetPairTable(GuiFlow.STACK_Y, 2)
                .addLeft(new GuiParent(GuiFlow.STACK_X)
                        .add(vis_i)
                        .add(alpha.setDim(-1, 12).setExpandableX())
                        .add(bright_i)
                        .add(brightness.setDim(-1, 12).setExpandableX())
                        .setVAlign(VAlign.CENTER)
                )
                .addLeft(tile.caps.resizes(), () -> new GuiParent(GuiFlow.STACK_X)
                        .add(rot_i)
                        .add(rotation.setDim(-1, 12).setExpandableX())
                        .setVAlign(VAlign.CENTER)
                )
                .addLeft(new GuiParent(GuiFlow.STACK_X)
                        .add(render_i)
                        .add(render_distance.setDim(-1, 12).setExpandableX())
                        .add(tile.caps.renderBehind(), () -> mirror.setDim(23, 12))
                        .setVAlign(VAlign.CENTER)
                )
                .addLeft(tile.caps.projects(), () -> new GuiParent(GuiFlow.STACK_X)
                        .add(project_i)
                        .add(projection_distance.setDim(-1, 12).setExpandableX())
                        .add(audio_offset.setDim(23, 12))
                        .setVAlign(VAlign.CENTER)
                )
                .addLeft(new GuiParent(GuiFlow.STACK_X)
                        .add(tile.canHideModel(), () -> {
                            show_model.set(tile.isVisible());
                            return show_model;
                        })
                        .add(!WFConfig.forceLightOnPlay(), () -> {
                            lit.set(tile.data.lit);
                            return lit;
                        })
                        .setSpacing(6)
                )
                .applyOnLeft(column -> {
                    column.setSpacing(2);
                })
                .addRight(tile.caps.resizes(), () -> pos_view.setDim(64, 64))
                .addRight(!tile.caps.resizes(), () -> flip_x)
                .addRight(!tile.caps.resizes(), () -> flip_y)
                .setAlignRight(Align.RIGHT)
                .setSpacing(8)
                .setExpandableY()
        );

        // MEDIA SETTINGS
        this.add(new WidgetPairTable(GuiFlow.STACK_Y, 2)
                .spaceBetween()
                .addLeft(new GuiParent(GuiFlow.STACK_X)
                        .add(VPCompat.installed(), () -> this.videoplayer.setDim(12, 12))
                        .setExpandableX()
                )
                .addLeft(new GuiParent(GuiFlow.STACK_X)
                        .add(this.loop.setDim(12, 12))
                        .add(this.playback.setDim(16, 12).setSquared(true))
                        .add(this.stop.setDim(12, 12))
                )
                .applyOnLeft(column -> column.setDim(60, -1))

                .addRight(new GuiParent("", GuiFlow.STACK_X, Align.RIGHT)
                        .add(this.vol_i.setDim(12, 12).setIcon(IconStyles.getVolumeIcon(tile.data.volume, tile.data.muted)))
                        .add(this.volume.setDim(-1, 16).setExpandableX())
                        .setVAlign(VAlign.CENTER)
                )
                .addRight(new GuiParent("", GuiFlow.STACK_X, Align.RIGHT)
                        .add(this.vol_min_i)
                        .add(this.volume_min.setDim(63, 12).setExpandableX())
                        .add(this.vol_max_i)
                        .add(this.volume_max.setDim(63, 12).setExpandableX())
                )
                .setVAlignRight(VAlign.CENTER)
        );

        // SEEKBAR + buttons
        this.add(new GuiParent(GuiFlow.STACK_X)
                .add(this.seekbar.setDim(-1, 14 + 4).setExpandableX())
                .add(this.reload.setDim(14, 14))
                .add(this.save.setDim(28, 14).setSquared(true).setEnabled(WFConfig.canSave(getPlayer(), url.getText())))
        );
    }

    public void resizeYOnRatio(int click) {
        if (click == GLFW.GLFW_MOUSE_BUTTON_1 && tile.display != null) {
            this.heightField.setValue(getYSizeRatio());
        }
    }

    public void resizeXOnRation(int click) {
        if (click == GLFW.GLFW_MOUSE_BUTTON_1 && tile.display != null) {
            this.widthField.setValue(getXSizeRatio());
        }
    }

    public float getYSizeRatio() {
        if (tile.display == null) return -1;
        return (float) (tile.display.height() / (tile.display.width() / widthField.getValue()));
    }

    public float getXSizeRatio() {
        if (tile.display == null) return -1;
        return (float) (tile.display.width() / (tile.display.height() / heightField.getValue()));
    }

    @Override
    public void tick() {
        super.tick();
        if (tile.isRemoved()) {
            this.closeTopLayer();
            return;
        }

        if (!isClient())
            return;

        float ySize = getYSizeRatio();
        this.widthField.get("rs_y").setTooltip(List.of(
                translatable("waterframes.gui.resize.y"),
                (ySize == -1)
                        ? translatable("waterframes.gui.resize.no_size")
                        : translatable("waterframes.gui.resize.size", ChatFormatting.AQUA.toString() + ySize)
        ));

        float xSize = getXSizeRatio();
        this.heightField.get("rs_x").setTooltip(List.of(
                translatable("waterframes.gui.resize.x"),
                (ySize == -1)
                        ? translatable("waterframes.gui.resize.no_size")
                        : translatable("waterframes.gui.resize.size", ChatFormatting.AQUA.toString() + xSize)
        ));

        // TOOLTIPS REFRESH
        this.playback.setState(tile.data.paused);
        this.playback.setTooltip(Collections.singletonList(
                translatable("waterframes.gui.playback", ChatFormatting.AQUA +  translate("waterframes.common." + (this.playback.value ? "paused" : "playing")))
        ));

        this.loop.setTooltip(Collections.singletonList(
                translatable("waterframes.gui.loop", (this.loop.value
                        ? ChatFormatting.GREEN
                        : ChatFormatting.RED) + translate("waterframes.common." + this.loop.value))
        ));

        this.mirror.setTooltip(List.of(
                translatable("waterframes.gui.mirror.1"),
                translatable("waterframes.gui.mirror.2",
                        (this.mirror.value ? ChatFormatting.GREEN : ChatFormatting.RED) + translate("waterframes.common." + this.mirror.value)))
        );

        this.audio_offset.setTooltip(List.of(
                translatable("waterframes.gui.audio_pos.1"),
                translatable("waterframes.gui.audio_pos.2",
                        ChatFormatting.AQUA + translate("waterframes.gui.audio_pos.states." + this.audio_offset.getState()))
        ));

        // OTHER UPDATES
        this.vol_i.setIcon(IconStyles.getVolumeIcon(volume.getIntValue(), tile.data.muted));
        this.save.setEnabled(WFConfig.canSave(getPlayer(), this.url.getText()));
        this.reload.setEnabled(enableReload());
        if (videoplayer != null) {
            videoplayer.setEnabled(enableVideoPlayer());
        }
    }

    public boolean enableVideoPlayer() {
        return tile.data.uri != null && tile.imageCache != null && tile.imageCache.getStatus() == ImageCache.Status.READY;
    }

    public boolean enableReload() {
        return tile.imageCache != null && !this.url.getText().isEmpty() && tile.data.uri != null && tile.data.uri.equals(WaterFrames.createURI(this.url.getText()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) { return ScreenStyles.SCREEN_BACKGROUND; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) { return ScreenStyles.SCREEN_BORDER; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiStyle getStyle() { return ScreenStyles.DISPLAYS; }
}
