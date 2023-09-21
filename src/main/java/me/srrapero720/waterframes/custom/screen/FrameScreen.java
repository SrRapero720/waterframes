package me.srrapero720.waterframes.custom.screen;

import me.srrapero720.waterframes.api.data.FrameData;
import me.srrapero720.waterframes.common.screen.widgets.*;
import me.srrapero720.waterframes.custom.rendering.screen.widgets.*;
import me.srrapero720.waterframes.custom.screen.widgets.*;
import me.srrapero720.waterframes.custom.screen.widgets.custom.CustomIcons;
import me.srrapero720.waterframes.core.WaterConfig;
import me.srrapero720.waterframes.custom.screen.widgets.custom.CustomStyles;
import me.srrapero720.waterframes.custom.block.entity.FrameTile;
import me.srrapero720.watermedia.api.image.ImageAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.*;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.text.TextListBuilder;

public class FrameScreen extends GuiLayer {
    public FrameTile frame;
    public float scaleMultiplier;
    public GuiTextfield widgetUrl;
    
    public final GuiSyncLocal<EndTag> PLAY = getSyncHolder().register("play", x -> frame.play());
    public final GuiSyncLocal<EndTag> PAUSE = getSyncHolder().register("pause", x -> frame.pause());
    public final GuiSyncLocal<EndTag> STOP = getSyncHolder().register("stop", x -> frame.stop());
    public final GuiSyncLocal<CompoundTag> SET_DATA = getSyncHolder().register("set_data", nbt -> FrameData.sync(frame, getPlayer(), nbt));

    public FrameScreen(FrameTile frame) {
        super("frame_screen", 230, 210);
        this.frame = frame;
        this.scaleMultiplier = 1F / 16;
        align = Align.STRETCH;
        flow = GuiFlow.STACK_Y;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) { return CustomStyles.BACKGROUND_COLOR; }
    @OnlyIn(Dist.CLIENT)
    @Override
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) { return CustomStyles.BACKGROUND_BORDER; }

    @Override
    public void create() {
        // HARDCODED REQUERIMENT
        GuiButton widgetSave = (GuiButton) new GuiButton("save", x -> SET_DATA.send(FrameData.build(this)))
                .setTranslate("gui.waterframes.save");
        widgetUrl = new WidgetTextField(widgetSave, "url", frame.getUrl());
        widgetUrl.setSuggestion("https://i.imgur.com/1yCDs5C.mp4");


        WidgetDoubleTable widgetUrlStatus = new WidgetDoubleTable(() -> new WidgetColum(GuiFlow.STACK_Y)).setSpacing(4);

        widgetUrlStatus.addOnFirst(new WidgetLabel("media_label", 0.8f).setTitle(new TextComponent("URL")));
        widgetUrlStatus.addOnFirst(widgetUrl.setExpandableX());
        if (isClient()) widgetUrlStatus.addOnSecond(new WidgetStatusIcon("", 25, 25, CustomIcons.STATUS_OK, () -> frame.imageCache));

        // IMAGE SIZE
        GuiParent widgetSizeParent = new WidgetParent(GuiFlow.STACK_X).setSpacing(8).setAlign(Align.STRETCH);
        widgetSizeParent.add(new WidgetCounterDecimal("width", frame.getSizeX(), 0, (float) WaterConfig.maxWidth(), scaleMultiplier)
                .expandX()
                .setSpacing(0)
                .setAlign(Align.CENTER)
                .add2(new GuiButton("reX", but -> {
                    WidgetCounterDecimal sizeXField = get("width", WidgetCounterDecimal.class);
                    WidgetCounterDecimal sizeYField = get("height", WidgetCounterDecimal.class);

                    float x = sizeXField.getValue();

                    if (frame.display != null)
                        sizeYField.setValue(frame.display.height() / (frame.display.width() / x));
                }).setTitle(new TextComponent("x->y")))
        );

        widgetSizeParent.add(new WidgetCounterDecimal("height", frame.getSizeY(), 0, (float) WaterConfig.maxHeight(), scaleMultiplier)
                .expandX()
                .setSpacing(0)
                .setAlign(Align.CENTER)
                .add2(new GuiButton("reY", but -> {
                    WidgetCounterDecimal sizeXField = get("width", WidgetCounterDecimal.class);
                    WidgetCounterDecimal sizeYField = get("height", WidgetCounterDecimal.class);

                    float y = sizeYField.getValue();

                    if (frame.display != null)
                        sizeXField.setValue(frame.display.width() / (frame.display.height() / y));
                }).setTitle(new TextComponent("y->x"))));

        widgetSizeParent.add(new WidgetParent(GuiFlow.STACK_Y)
                .add2(new GuiCheckBox("flip_x", frame.data.flipX).setTranslate("gui.waterframes.flipx"))
                .add2(new GuiCheckBox("flip_y", frame.data.flipY).setTranslate("gui.waterframes.flipy")));

        WidgetDoubleTable widgetImageSettings = new WidgetDoubleTable(() -> new WidgetColum(GuiFlow.STACK_Y)).setSpacing(2);
        widgetImageSettings.setExpandableY();

        // ROTATION
        widgetImageSettings.addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                .add2(new WidgetIcon("r_icon", 12, 12, CustomIcons.ROTATION))
                .add2(new WidgetSlider("rotation", 130, 10, frame.data.rotation, 0, 360, WidgetSlider.ANGLE)));

        // TRANSPARENCY
        widgetImageSettings.addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                .add2(new WidgetIcon("t_icon", 12, 12, CustomIcons.TRANSPARENCY))
                .add2(new WidgetSlider("alpha", 130, 10, frame.data.alpha, 0, 1, WidgetSlider.PERCENT)));

        // BRIGHTNESS
        widgetImageSettings.addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                .add2(new WidgetIcon("b_icon", 12, 12, CustomIcons.BRIGHTNESS))
                .add2(new WidgetSlider("brightness", 130, 10, frame.data.brightness, 0, 1, WidgetSlider.PERCENT)));

        // DISTANCE
        widgetImageSettings.addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                .add2(new WidgetIcon("d_icon", 12, 12, CustomIcons.DISTANCE))
                .add2(new GuiSteppedSlider("render_distance", 130, 10, frame.data.renderDistance, 5, 1024)));

        widgetImageSettings.addOnFirst(new WidgetParent(GuiFlow.STACK_X).setAlign(Align.STRETCH)
                .add2(new GuiCheckBox("visible_frame", frame.data.visibleFrame).setTranslate("gui.waterframes.visibleFrame"))
                .add2(new GuiCheckBox("render_both", frame.data.bothSides).setTranslate("gui.waterframes.bothSides")));


        // IMAGE POSITION
        widgetImageSettings.getSecondRow().setAlign(Align.CENTER);
        widgetImageSettings.addOnSecond(new WidgetIcon("posView", 40, 40, CustomIcons.POS_CORD[frame.data.getPosX()][frame.data.getPosY()]));
        widgetImageSettings.addOnSecond(new GuiStateButton("pos_x", frame.data.getPosX(), new TextListBuilder()
                .addTranslated("gui.waterframes.posx.", "left", "center", "right")));

        widgetImageSettings.addOnSecond(new GuiStateButton("pos_y", frame.data.getPosY(), new TextListBuilder()
                .addTranslated("gui.waterframes.posy.", "top", "center", "bottom")));


        WidgetDoubleTable widgetMediaSettings = new WidgetDoubleTable(() -> new WidgetColum(GuiFlow.STACK_Y)).setSpacing(4);
        widgetMediaSettings.getFirstRow().setExpandableX();
        widgetMediaSettings.addOnFirst(new WidgetParent("", GuiFlow.STACK_X)
                .add2(new GuiIconButton("play", CustomIcons.PLAY, button -> PLAY.send(EndTag.INSTANCE)))
                .add2(new GuiIconButton("pause", CustomIcons.PAUSE, button -> PAUSE.send(EndTag.INSTANCE)))
                .add2(new GuiIconButton("stop", CustomIcons.STOP, button -> STOP.send(EndTag.INSTANCE))));
        widgetMediaSettings.addOnFirst(new GuiCheckBox("loop", frame.data.loop).setTranslate("gui.waterframes.loop"));

        widgetMediaSettings.getSecondRow().setAlign(Align.RIGHT).setExpandableX();
        widgetMediaSettings.addOnSecond(new WidgetParent("", GuiFlow.STACK_X)
                .add2(new WidgetIcon("v_icon", 12, 12, CustomIcons.VOLUME))
                .add2(new WidgetSlider("volume", 100, 10, frame.data.volume, 0, (double) WaterConfig.maxAudioVolume() / 100, WidgetSlider.PERCENT).setExpandableX())
                .setAlign(Align.RIGHT));


        GuiSteppedSlider rangeMin;
        widgetMediaSettings.addOnSecond(new WidgetParent("", GuiFlow.STACK_X)
                .add2(new WidgetIcon("v_icon", 12, 12, CustomIcons.VOLUME_RANGE_MIN))
                .add2(rangeMin = (GuiSteppedSlider) new GuiSteppedSlider("volume_min_range", 63, 10, frame.data.minVolumeDistance, 0, 512).setExpandableX())
                .add2(new WidgetIcon("v_icon", 12, 12, CustomIcons.VOLUME_RANGE_MAX))
                .add2(new GuiSteppedSlider("volume_max_range", 63, 10, frame.data.maxVolumeDistance, 0, WaterConfig.maxAudioDistance()) {
                    @Override
                    public void setValue(double value) {
                        super.setValue((int) value);
                        rangeMin.maxValue = (int) value;

                        if (rangeMin.getValue() > this.value) rangeMin.setValue(value >= 0 ? (int) value : 0);
                    }
                }.setExpandableX())
                .setAlign(Align.RIGHT));

        WidgetDoubleTable widgetScreenActions = new WidgetDoubleTable().setSpacing(2);
        widgetScreenActions.addOnFirst(new GuiButton("reload_all", x -> ImageAPI.reloadCache())
                .setTitle(new TextComponent("Reload All")).setTooltip(new TextBuilder().translate("WARNING: reload can stun or crash your game").build()));

        widgetScreenActions.addOnSecond(widgetSave);
        widgetSave.setEnabled(WaterConfig.canUse(getPlayer(), widgetUrl.getText()));

        widgetScreenActions.getSecondRow().setAlign(Align.RIGHT);
        widgetScreenActions.addOnSecond(new GuiButton("reload", x -> frame.imageCache.reload()).setTranslate("gui.waterframes.reload"));

        this.add(widgetUrlStatus);
        this.add(widgetSizeParent);
        this.add(new WidgetLabel("tex_label", 0.8f).setTitle(new TextComponent("Texture settings")));
        this.add(widgetImageSettings);
        this.add(new WidgetLabel("media_label", 0.8f).setTitle(new TextComponent("Media settings")));
        this.add(widgetMediaSettings);
        this.add(widgetScreenActions);
    }

    @Override
    public void tick() {
        super.tick();
        WidgetIcon posIc = get("posView");
        posIc.setIcon(CustomIcons.POS_CORD[((GuiStateButton) get("pos_x")).getState()][((GuiStateButton) get("pos_y")).getState()]);
    }
}