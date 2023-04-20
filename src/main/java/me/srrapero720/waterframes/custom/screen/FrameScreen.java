package me.srrapero720.waterframes.custom.screen;

import me.srrapero720.waterframes.FramesConfig;
import me.srrapero720.waterframes.custom.blocks.TileFrame;
import me.srrapero720.waterframes.display.texture.TextureCache;
import me.srrapero720.waterframes.display.texture.TextureSeeker;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.parent.GuiColumn;
import team.creative.creativecore.common.gui.controls.parent.GuiRow;
import team.creative.creativecore.common.gui.controls.parent.GuiTable;
import team.creative.creativecore.common.gui.controls.simple.*;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiIcon;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.DisplayColor;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.text.TextListBuilder;

import java.util.List;

public class FrameScreen extends GuiLayer {
    public TileFrame frame;
    
    public float scaleMultiplier;
    
    public GuiTextfield url;
    
    public final GuiSyncLocal<EndTag> PLAY = getSyncHolder().register("play", x -> frame.play());
    public final GuiSyncLocal<EndTag> PAUSE = getSyncHolder().register("pause", x -> frame.pause());
    public final GuiSyncLocal<EndTag> STOP = getSyncHolder().register("stop", x -> frame.stop());
    
    public final GuiSyncLocal<CompoundTag> SET_DATA = getSyncHolder().register("set_data", nbt -> {
        String url = nbt.getString("url");
        if (FramesConfig.canUse(getPlayer(), url)) {
            frame.setURL(url);
            float sizeX = (float) Math.min(FramesConfig.MAX_SIZE.get(), nbt.getFloat("x"));
            float sizeY = (float) Math.min(FramesConfig.MAX_SIZE.get(), nbt.getFloat("y"));
            int posX = nbt.getByte("posX");
            int posY = nbt.getByte("posY");
            if (posX == 0) {
                frame.min.x = 0;
                frame.max.x = sizeX;
            } else if (posX == 1) {
                float middle = sizeX / 2;
                frame.min.x = 0.5F - middle;
                frame.max.x = 0.5F + middle;
            } else {
                frame.min.x = 1 - sizeX;
                frame.max.x = 1;
            }
            
            if (posY == 0) {
                frame.min.y = 0;
                frame.max.y = sizeY;
            } else if (posY == 1) {
                float middle = sizeY / 2;
                frame.min.y = 0.5F - middle;
                frame.max.y = 0.5F + middle;
            } else {
                frame.min.y = 1 - sizeY;
                frame.max.y = 1;
            }
            
            frame.renderDistance = Math.min(FramesConfig.MAX_RENDER_DISTANCE.get(), nbt.getInt("render"));
            frame.rotation = nbt.getFloat("rotation");
            frame.visibleFrame = nbt.getBoolean("visibleFrame");
            frame.bothSides = nbt.getBoolean("bothSides");
            frame.loop = nbt.getBoolean("loop");
            frame.flipX = nbt.getBoolean("flipX");
            frame.flipY = nbt.getBoolean("flipY");
            frame.volume = nbt.getFloat("volume");
            frame.minDistance = nbt.getFloat("min");
            frame.maxDistance = nbt.getFloat("max");
            frame.alpha = nbt.getFloat("transparency");
            frame.brightness = nbt.getFloat("brightness");
        }
        
        frame.markDirty();
    });
    
    public FrameScreen(TileFrame frame) {
        this(frame, 16);
    }
    
    public FrameScreen(TileFrame frame, int scaleSize) {
        super("waterframe", 250, 210);
        this.frame = frame;
        this.scaleMultiplier = 1F / (scaleSize);
    }
    
    @Override
    public void create() {
        GuiButton save = new GuiButton("save", x -> {
            CompoundTag nbt = new CompoundTag();
            GuiTextfield url = get("url");
            GuiCounterDecimal sizeX = get("sizeX");
            GuiCounterDecimal sizeY = get("sizeY");

            GuiStateButton buttonPosX = get("posX");
            GuiStateButton buttonPosY = get("posY");
            GuiSlider rotation = get("rotation");

            GuiCheckBox flipX = get("flipX");
            GuiCheckBox flipY = get("flipY");
            GuiCheckBox visibleFrame = get("visibleFrame");
            GuiCheckBox bothSides = get("bothSides");

            GuiSteppedSlider renderDistance = get("distance");

            GuiSlider transparency = get("transparency");
            GuiSlider brightness = get("brightness");

            GuiCheckBox loop = get("loop");
            GuiSlider volume = get("volume");
            GuiSteppedSlider min = get("range_min");
            GuiSteppedSlider max = get("range_max");

            nbt.putByte("posX", (byte) buttonPosX.getState());
            nbt.putByte("posY", (byte) buttonPosY.getState());

            nbt.putFloat("rotation", (float) rotation.value);

            nbt.putBoolean("flipX", flipX.value);
            nbt.putBoolean("flipY", flipY.value);
            nbt.putBoolean("visibleFrame", visibleFrame.value);
            nbt.putBoolean("bothSides", bothSides.value);

            nbt.putInt("render", (int) renderDistance.value);

            nbt.putFloat("transparency", (float) transparency.value);
            nbt.putFloat("brightness", (float) brightness.value);

            nbt.putBoolean("loop", loop.value);
            nbt.putFloat("volume", (float) volume.value);
            nbt.putFloat("min", min.getValue());
            nbt.putFloat("max", max.getValue());

            nbt.putString("url", url.getText());
            nbt.putFloat("x", Math.max(0.1F, sizeX.getValue()));
            nbt.putFloat("y", Math.max(0.1F, sizeY.getValue()));
            SET_DATA.send(nbt);
        });
        save.setTranslate("gui.waterframes.save");

        align = Align.STRETCH;
        flow = GuiFlow.STACK_Y;

        url = new WidgetTextField(save, "url", frame.getRealURL());
        url.setMaxStringLength(512);
        add(url);
        GuiLabel error = new GuiLabel("error").setDefaultColor(ColorUtils.RED);
        if (frame.isClient() && frame.cache != null && frame.cache.getError() != null)
            error.setTranslate(frame.cache.getError());
        add(error);

        // SIZE -----------------------
        GuiParent size = new GuiParent(GuiFlow.STACK_X);
        size.spacing = 8;
        size.align = Align.STRETCH;
        add(size);
        GuiCounterDecimal counterDecimal;

        // LEFT
        size.add(counterDecimal = new GuiCounterDecimal("sizeX", frame.getSizeX(), 0, Float.MAX_VALUE) {
            @Override
            public float stepUp(float value) {
                int scaled = (int) (value / scaleMultiplier);
                scaled++;
                return Math.min(max, scaled * scaleMultiplier);
            }

            @Override
            public float stepDown(float value) {
                int scaled = (int) (value / scaleMultiplier);
                scaled--;
                return Math.max(min, scaled * scaleMultiplier);
            }
        });
        counterDecimal.setExpandableX();
        counterDecimal.spacing = 2;
        counterDecimal.align = Align.CENTER;
        counterDecimal.add(new GuiButton("reX", but -> {
            GuiCounterDecimal sizeXField = get("sizeX", GuiCounterDecimal.class);
            GuiCounterDecimal sizeYField = get("sizeY", GuiCounterDecimal.class);

            float x = sizeXField.getValue();

            if (frame.display != null)
                sizeYField.setValue(frame.display.getHeight() / (frame.display.getWidth() / x));
        }).setTitle(new TextComponent("x->y")).setExpandableY());

        // RIGHT
        size.add(counterDecimal = new GuiCounterDecimal("sizeY", frame.getSizeY(), 0, Float.MAX_VALUE) {
            @Override
            public float stepUp(float value) {
                int scaled = (int) (value / scaleMultiplier);
                scaled++;
                return Math.min(max, scaled * scaleMultiplier);
            }

            @Override
            public float stepDown(float value) {
                int scaled = (int) (value / scaleMultiplier);
                scaled--;
                return Math.max(min, scaled * scaleMultiplier);
            }
        });
        counterDecimal.setExpandableX();
        counterDecimal.spacing = 2;
        counterDecimal.align = Align.CENTER;
        counterDecimal.add(new GuiButton("reY", but -> {
            GuiCounterDecimal sizeXField = get("sizeX", GuiCounterDecimal.class);
            GuiCounterDecimal sizeYField = get("sizeY", GuiCounterDecimal.class);

            float y = sizeYField.getValue();

            if (frame.display != null)
                sizeXField.setValue(frame.display.getWidth() / (frame.display.getHeight() / y));
        }).setTitle(new TextComponent("y->x")).setExpandableY());


        GuiTable flip_grid = new GuiTable();
        add(flip_grid);
        GuiColumn flip_left;
        GuiColumn flip_right;

        flip_grid.addRow(new GuiRow(flip_left = new GuiColumn(GuiFlow.STACK_X), flip_right = new GuiColumn(GuiFlow.STACK_Y)));
        flip_grid.align = Align.CENTER;
        flip_left.align = Align.LEFT;
        flip_right.align = Align.RIGHT;

        flip_left.add(new GuiStateButton("posX", frame.min.x == 0 ? 0 : frame.max.x == 1 ? 2 : 1, new TextListBuilder()
                .addTranslated("gui.waterframes.posx.", "left", "center", "right")));

        flip_left.add(new GuiStateButton("posY", frame.min.y == 0 ? 0 : frame.max.y == 1 ? 2 : 1, new TextListBuilder()
                .addTranslated("gui.waterframes.posy.", "top", "center", "bottom")));

        flip_right.add(new GuiCheckBox("flipX", frame.flipX).setTranslate("gui.waterframes.flipx"));
        flip_right.add(new GuiCheckBox("flipY", frame.flipY).setTranslate("gui.waterframes.flipy"));


        GuiTable table = new GuiTable();
        table.spacing = 2;
        add(table);
        GuiColumn left;
        GuiColumn right;

        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("t_label").setTitle(new TranslatableComponent("gui.waterframes.rotation").append(":")));
        right.add(new GuiSlider("rotation", 130, 10, frame.rotation, 0, 360));
        right.align = Align.RIGHT;

        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("t_label").setTitle(new TranslatableComponent("gui.waterframes.transparency").append(":")));
        right.add(new GuiSlider("transparency", 130, 10, frame.alpha, 0, 1));
        right.align = Align.RIGHT;

        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("b_label").setTitle(new TranslatableComponent("gui.waterframes.brightness").append(":")));
        right.add(new GuiSlider("brightness", 130, 10, frame.brightness, 0, 1));
        right.align = Align.RIGHT;

        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("d_label").setTitle(new TranslatableComponent("gui.waterframes.distance").append(":")));
        right.add(new GuiSteppedSlider("distance", 130, 10, frame.renderDistance, 5, 1024));
        right.align = Align.RIGHT;


        GuiParent rendering = new GuiParent(GuiFlow.STACK_X);
        rendering.align = Align.RIGHT;
        rendering.spacing = 8;
        add(rendering);
        rendering.add(new GuiCheckBox("visibleFrame", frame.visibleFrame).setTranslate("gui.waterframes.visibleFrame"));
        rendering.add(new GuiCheckBox("bothSides", frame.bothSides).setTranslate("gui.waterframes.bothSides"));

        GuiTable volume = new GuiTable();
        add(volume);
        GuiColumn volume_left;
        GuiColumn volume_right;

        volume.addRow(new GuiRow(volume_left = new GuiColumn(GuiFlow.STACK_X), volume_right = new GuiColumn(GuiFlow.STACK_X)));
        volume_left.add(new GuiLabel("v_label").setTitle(new TranslatableComponent("gui.waterframes.volume").append(":")));
        volume_right.add(new GuiSlider("volume", 130, 10, frame.volume, 0, 1));
        volume_right.align = Align.RIGHT;

        GuiParent range = new GuiParent(GuiFlow.STACK_X);
        add(range);
        range.add(new GuiLabel("range_label").setTitle(new TranslatableComponent("gui.waterframes.range").append(" (min/max):")));
        range.add(new GuiSteppedSlider("range_min", 63, 10, (int) frame.minDistance, 0, 512).setExpandableX());
        range.add(new GuiSteppedSlider("range_max", 63, 10, (int) frame.maxDistance, 0, 512).setExpandableX());

        GuiTable playgrid = new GuiTable();
        add(playgrid);
        GuiColumn play_left;
        GuiColumn play_right;

        playgrid.addRow(new GuiRow(play_left = new GuiColumn(GuiFlow.STACK_X), play_right = new GuiColumn(GuiFlow.STACK_X)));
        play_right.align = Align.RIGHT;

        play_left.add(new GuiIconButton("play", GuiIcon.PLAY, button -> PLAY.send(EndTag.INSTANCE)));
        play_left.add(new GuiIconButton("pause", GuiIcon.PAUSE, button -> PAUSE.send(EndTag.INSTANCE)));
        play_left.add(new GuiIconButton("stop", GuiIcon.STOP, button -> STOP.send(EndTag.INSTANCE)));
        play_left.add(new GuiCheckBox("loop", frame.loop).setTranslate("gui.waterframes.loop"));

        save.setEnabled(FramesConfig.canUse(getPlayer(), url.getText()));
        play_right.add(save);
        play_right.add(new GuiButton("reload", x -> {
            synchronized (TextureSeeker.LOCK) {
                if (Screen.hasShiftDown()) TextureCache.reloadAll();
                else if (frame.cache != null) frame.cache.reload();
            }
        }).setTranslate("gui.waterframes.reload").setTooltip(new TextBuilder().translate("gui.waterframes.reload.tooltip").build()));
        
    }

    public static class WidgetTextField extends team.creative.creativecore.common.gui.controls.simple.GuiTextfield {
        public static final StyleDisplay WARN_DISABLED_BORDER = new DisplayColor(0.196F, 0, 0, 1);
        public static final StyleDisplay WARN_DISABLED_BACKGROUND = new DisplayColor(0.588F, 0.352F, 0.352F, 1);
        public static final StyleDisplay WARN_WARNING_BORDER = new DisplayColor(0.196F, 0, 0, 1);
        public static final StyleDisplay WARN_WARNING_BACKGROUND = new DisplayColor(0.588F, 0.588F, 0.352F, 1);
        private GuiButton saveButton;

        public WidgetTextField(GuiButton saveButton, String name, String text) {
            super(name, text);
            this.saveButton = saveButton;
        }

        @Override
        public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
            if (!canUse(true))
                return FramesConfig.ENABLE_WHITELIST.get() ? WARN_DISABLED_BORDER : WARN_WARNING_BORDER;
            return super.getBorder(style, display);
        }

        @Override
        public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
            if (!canUse(true))
                return FramesConfig.ENABLE_WHITELIST.get() ? WARN_DISABLED_BACKGROUND : WARN_WARNING_BACKGROUND;
            return super.getBackground(style, display);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            boolean pressed = super.keyPressed(keyCode, scanCode, modifiers);
            saveButton.setEnabled(canUse(false));
            return pressed;
        }

        @Override
        public List<Component> getTooltip() {
            if (!canUse(false))
                return new TextBuilder().text(ChatFormatting.RED + "" + ChatFormatting.BOLD).translate("label.waterframes.not_whitelisted.name").build();
            else if (!canUse(true))
                return new TextBuilder().text(ChatFormatting.GOLD + "").translate("label.waterframes.whitelist_warning.name").build();
            return null;
        }

        protected boolean canUse(boolean ignoreToggle) {
            return FramesConfig.canUse(getPlayer(), getText(), ignoreToggle);
        }
    }
}
