package me.srrapero720.waterframes.custom.screen;

import me.srrapero720.waterframes.api.data.BasicData;
import me.srrapero720.waterframes.custom.data.FrameData;
import me.srrapero720.waterframes.core.WaterConfig;
import me.srrapero720.waterframes.custom.block.entity.FrameTile;
import me.srrapero720.waterframes.custom.screen.widgets.*;
import me.srrapero720.waterframes.custom.screen.widgets.custom.CustomIcons;
import me.srrapero720.waterframes.custom.screen.widgets.custom.CustomStyles;
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
import team.creative.creativecore.common.util.text.TextListBuilder;

public class FrameScreen extends GuiLayer {
    public FrameTile frame;
    public float scaleMultiplier;
    public GuiTextfield inputUrl;
    
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
        GuiButton buttonSave = (GuiButton) new GuiButton("save", x -> SET_DATA.send(FrameData.build(this))).setTranslate("gui.waterframes.save");
        inputUrl = new WidgetTextField(buttonSave, BasicData.URL, frame.getUrl()).setSuggest("https://i.imgur.com/1yCDs5C.mp4");

        WidgetDoubleTable parentURL = new WidgetDoubleTable(GuiFlow.STACK_Y)
                .addOnFirst(new WidgetLabel("media_label", 0.75f).setTitle(new TextComponent("URL")))
                .addOnFirst(inputUrl.setExpandableX())
                .addOnSecondIf(new WidgetStatusIcon("", 25, 25, CustomIcons.STATUS_OK, () -> frame.imageCache), isClient())
                .setSpacing(4);

        // IMAGE SIZE
        GuiParent parentSize = new WidgetParent(GuiFlow.STACK_X).setSpacing(8).setAlign(Align.STRETCH);
        parentSize.add(new WidgetCounterDecimal("width", frame.getSizeX(), 0, WaterConfig.maxWidth(), scaleMultiplier)
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

        parentSize.add(new WidgetCounterDecimal("height", frame.getSizeY(), 0, WaterConfig.maxHeight(), scaleMultiplier)
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

        parentSize.add(new WidgetParent(GuiFlow.STACK_Y)
                .add2(new GuiCheckBox(BasicData.FLIP_X, frame.data.flipX).setTranslate("gui.waterframes.flipx"))
                .add2(new GuiCheckBox(BasicData.FLIP_Y, frame.data.flipY).setTranslate("gui.waterframes.flipy")));

        WidgetDoubleTable parentTexSettings = new WidgetDoubleTable(() -> new WidgetColum(GuiFlow.STACK_Y)).setSpacing(2).expandY()
                .addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                        .add2(new WidgetIcon("r_icon", 12, 12, CustomIcons.ROTATION))
                        .add2(new WidgetSlider(BasicData.ROTATION, 130, 10, frame.data.rotation, 0, 360, WidgetSlider.ANGLE)))
                .addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                        .add2(new WidgetIcon("t_icon", 12, 12, CustomIcons.TRANSPARENCY))
                        .add2(new WidgetSlider(BasicData.ALPHA, 130, 10, frame.data.alpha, 0, 1, WidgetSlider.PERCENT)))
                .addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                        .add2(new WidgetIcon("b_icon", 12, 12, CustomIcons.BRIGHTNESS))
                        .add2(new WidgetSlider(BasicData.BRIGHTNESS, 130, 10, frame.data.brightness, 0, 1, WidgetSlider.PERCENT)))
                .addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                        .add2(new WidgetIcon("d_icon", 12, 12, CustomIcons.DISTANCE))
                        .add2(new GuiSteppedSlider(BasicData.RENDER_DISTANCE, 130, 10, frame.data.renderDistance, 5, 1024)))
                .addOnFirst(new WidgetParent(GuiFlow.STACK_X).setAlign(Align.STRETCH)
                        .add2(new GuiCheckBox(FrameData.VISIBLE_FRAME, frame.data.visibleFrame).setTranslate("gui.waterframes.visibleFrame"))
                        .add2(new GuiCheckBox(FrameData.RENDER_BOTH_SIDES, frame.data.bothSides).setTranslate("gui.waterframes.bothSides")))
                // IMAGE POSITION
                .addOnSecond(new WidgetIcon("posView", 40, 40, CustomIcons.POS_CORD[frame.data.getPosX()][frame.data.getPosY()]))
                .addOnSecond(new GuiStateButton("pos_x", frame.data.getPosX(), new TextListBuilder()
                        .addTranslated("gui.waterframes.posx.", "left", "center", "right")))
                .addOnSecond(new GuiStateButton("pos_y", frame.data.getPosY(), new TextListBuilder()
                        .addTranslated("gui.waterframes.posy.", "top", "center", "bottom")));

        parentTexSettings.getSecondRow().setAlign(Align.CENTER);

        WidgetDoubleTable parentMedia = new WidgetDoubleTable(() -> new WidgetColum(GuiFlow.STACK_Y)).setSpacing(4);
        parentMedia.getFirstRow().setExpandableX();
        parentMedia.addOnFirst(new WidgetParent("", GuiFlow.STACK_X)
                .add2(new GuiIconButton("play", CustomIcons.PLAY, button -> PLAY.send(EndTag.INSTANCE)))
                .add2(new GuiIconButton("pause", CustomIcons.PAUSE, button -> PAUSE.send(EndTag.INSTANCE)))
                .add2(new GuiIconButton("stop", CustomIcons.STOP, button -> STOP.send(EndTag.INSTANCE))));
        parentMedia.addOnFirst(new GuiCheckBox(BasicData.LOOP, frame.data.loop).setTranslate("gui.waterframes.loop"));

        parentMedia.getSecondRow().setAlign(Align.RIGHT).setExpandableX();
        parentMedia.addOnSecond(new WidgetParent("", GuiFlow.STACK_X)
                .add2(new WidgetIcon("v_icon", 12, 12, CustomIcons.VOLUME))
                .add2(new WidgetSlider(BasicData.VOLUME, 100, 10, frame.data.volume, 0, WaterConfig.maxAudioVolume(), WidgetSlider.PERCENT).setExpandableX())
                .setAlign(Align.RIGHT));

        GuiSteppedSlider rangeMin;
        parentMedia.addOnSecond(new WidgetParent("", GuiFlow.STACK_X)
                .add2(new WidgetIcon("v_icon", 12, 12, CustomIcons.VOLUME_RANGE_MIN))
                .add2(rangeMin = (GuiSteppedSlider) new GuiSteppedSlider(BasicData.VOL_RANGE_MIN, 63, 10, frame.data.minVolumeDistance, 0, Math.min(WaterConfig.maxAudioDistance(), frame.data.maxVolumeDistance)).setExpandableX())
                .add2(new WidgetIcon("v_icon", 12, 12, CustomIcons.VOLUME_RANGE_MAX))
                .add2(new WidgetSteppedSlider(BasicData.VOL_RANGE_MAX, rangeMin, 63, 10, frame.data.maxVolumeDistance, 0, WaterConfig.maxAudioDistance()).setExpandableX())
                .setAlign(Align.RIGHT));

        WidgetDoubleTable parentActions = new WidgetDoubleTable().setSpacing(2)
                .addOnFirst(new GuiButton("reload_all", x -> ImageAPI.reloadCache()).setTitle(new TextComponent("Reload All")))
                .addOnSecond(buttonSave.setEnabled(WaterConfig.canUse(getPlayer(), inputUrl.getText())))
                .addOnSecond(new GuiButton("reload", x -> frame.imageCache.reload()).setTranslate("gui.waterframes.reload"))
                .setSpacing(2);
        parentActions.getSecondRow().setAlign(Align.RIGHT);

        this.add(parentURL);
        this.add(parentSize);
        this.add(new WidgetLabel("tex_label", 0.8f).setTitle(new TextComponent("Texture settings")));
        this.add(parentTexSettings);
        this.add(new WidgetLabel("media_label", 0.8f).setTitle(new TextComponent("Media settings")));
        this.add(parentMedia);
        this.add(parentActions);
    }

    @Override
    public void tick() {
        super.tick();
        WidgetIcon posIc = get("posView");
        posIc.setIcon(CustomIcons.POS_CORD[((GuiStateButton) get("pos_x")).getState()][((GuiStateButton) get("pos_y")).getState()]);
    }
}