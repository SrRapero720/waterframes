package me.srrapero720.waterframes.custom.cc_gui;

import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
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
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.text.TextListBuilder;
import me.srrapero720.waterframes.custom.displayers.texture.TextureCache;
import me.srrapero720.waterframes.custom.displayers.texture.TextureSeeker;
import me.srrapero720.waterframes.custom.blocks.BlockEntityWaterFrame;

public class GuiWaterFrame extends GuiLayer {
    
    public BlockEntityWaterFrame frame;
    
    public float scaleMultiplier;
    
    public GuiTextfield url;
    
    public final GuiSyncLocal<EndTag> PLAY = getSyncHolder().register("play", x -> frame.play());
    public final GuiSyncLocal<EndTag> PAUSE = getSyncHolder().register("pause", x -> frame.pause());
    public final GuiSyncLocal<EndTag> STOP = getSyncHolder().register("stop", x -> frame.stop());
    
    public final GuiSyncLocal<CompoundTag> SET_DATA = getSyncHolder().register("set_data", nbt -> {
        String url = nbt.getString("url");
        if (WaterFrames.CONFIG.canUse(getPlayer(), url)) {
            frame.setURL(url);
            float sizeX = (float) Math.min(WaterFrames.CONFIG.sizeLimitation, nbt.getFloat("x"));
            float sizeY = (float) Math.min(WaterFrames.CONFIG.sizeLimitation, nbt.getFloat("y"));
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
            
            frame.renderDistance = Math.min(WaterFrames.CONFIG.maxRenderDistance, nbt.getInt("render"));
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
    
    public GuiWaterFrame(BlockEntityWaterFrame frame) {
        this(frame, 16);
    }
    
    public GuiWaterFrame(BlockEntityWaterFrame frame, int scaleSize) {
        super("waterframe", 250, 220);
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
        save.setTranslate("gui.save");
        
        align = Align.STRETCH;
        flow = GuiFlow.STACK_Y;
        
        url = new GuiTextField(save, "url", frame.getRealURL());
        url.setMaxStringLength(512);
        add(url);
        GuiLabel error = new GuiLabel("error").setDefaultColor(ColorUtils.RED);
        if (frame.isClient() && frame.cache != null && frame.cache.getError() != null)
            error.setTranslate(frame.cache.getError());
        add(error);
        
        GuiParent size = new GuiParent(GuiFlow.STACK_X);
        size.align = Align.STRETCH;
        add(size);
        
        size.add(new GuiCounterDecimal("sizeX", frame.getSizeX(), 0, Float.MAX_VALUE) {
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
        
        size.add(new GuiButton("reX", but -> {
            GuiCounterDecimal sizeXField = get("sizeX", GuiCounterDecimal.class);
            GuiCounterDecimal sizeYField = get("sizeY", GuiCounterDecimal.class);
            
            float x = sizeXField.getValue();
            
            if (frame.display != null)
                sizeYField.setValue(frame.display.getHeight() / (frame.display.getWidth() / x));
        }).setTitle(new TextComponent("x->y")));
        
        size.add(new GuiCounterDecimal("sizeY", frame.getSizeY(), 0, Float.MAX_VALUE) {
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
        
        size.add(new GuiButton("reY", but -> {
            GuiCounterDecimal sizeXField = get("sizeX", GuiCounterDecimal.class);
            GuiCounterDecimal sizeYField = get("sizeY", GuiCounterDecimal.class);
            
            float y = sizeYField.getValue();
            
            if (frame.display != null)
                sizeXField.setValue(frame.display.getWidth() / (frame.display.getHeight() / y));
        }).setTitle(new TextComponent("y->x")));
        
        GuiParent flip = new GuiParent(GuiFlow.STACK_X);
        add(flip);
        
        flip.add(new GuiCheckBox("flipX", frame.flipX).setTranslate("gui.waterframes.flipx"));
        flip.add(new GuiCheckBox("flipY", frame.flipY).setTranslate("gui.waterframes.flipy"));
        
        GuiParent align = new GuiParent(GuiFlow.STACK_X);
        add(align);
        
        align.add(new GuiStateButton("posX", frame.min.x == 0 ? 0 : frame.max.x == 1 ? 2 : 1, new TextListBuilder()
                .addTranslated("gui.waterframes.posx.", "left", "center", "right")));
        align.add(new GuiStateButton("posY", frame.min.y == 0 ? 0 : frame.max.y == 1 ? 2 : 1, new TextListBuilder()
                .addTranslated("gui.waterframes.posy.", "top", "center", "bottom")));
        
        GuiTable table = new GuiTable();
        add(table);
        GuiColumn left;
        GuiColumn right;
        
        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("t_label").setTitle(new TranslatableComponent("gui.waterframes.rotation").append(":")));
        right.add(new GuiSlider("rotation", frame.rotation, 0, 360).setExpandableX());
        
        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("t_label").setTitle(new TranslatableComponent("gui.waterframes.transparency").append(":")));
        right.add(new GuiSlider("transparency", frame.alpha, 0, 1).setExpandableX());
        
        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("b_label").setTitle(new TranslatableComponent("gui.waterframes.brightness").append(":")));
        right.add(new GuiSlider("brightness", frame.brightness, 0, 1).setExpandableX());
        
        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("d_label").setTitle(new TranslatableComponent("gui.waterframes.distance").append(":")));
        right.add(new GuiSteppedSlider("distance", frame.renderDistance, 5, 1024).setExpandableX());
        
        GuiParent rendering = new GuiParent(GuiFlow.STACK_X);
        add(rendering);
        
        rendering.add(new GuiCheckBox("visibleFrame", frame.visibleFrame).setTranslate("gui.waterframes.visibleFrame"));
        rendering.add(new GuiCheckBox("bothSides", frame.bothSides).setTranslate("gui.waterframes.bothSides"));
        
        GuiParent play = new GuiParent(GuiFlow.STACK_X);
        add(play);
        
        play.add(new GuiIconButton("play", GuiIcon.PLAY, button -> PLAY.send(EndTag.INSTANCE)));
        play.add(new GuiIconButton("pause", GuiIcon.PAUSE, button -> PAUSE.send(EndTag.INSTANCE)));
        play.add(new GuiIconButton("stop", GuiIcon.STOP, button -> STOP.send(EndTag.INSTANCE)));
        
        play.add(new GuiCheckBox("loop", frame.loop).setTranslate("gui.waterframes.loop"));
        play.add(new GuiLabel("v_label").setTranslate("gui.waterframes.volume"));
        play.add(new GuiSlider("volume", frame.volume, 0, 1));
        
        GuiParent range = new GuiParent(GuiFlow.STACK_X);
        add(range);
        
        range.add(new GuiLabel("range_label").setTranslate("gui.waterframes.range"));
        range.add(new GuiSteppedSlider("range_min", (int) frame.minDistance, 0, 512).setExpandableX());
        range.add(new GuiSteppedSlider("range_max", (int) frame.maxDistance, 0, 512).setExpandableX());
        
        GuiParent bottom = new GuiParent(GuiFlow.STACK_X);
        bottom.align = Align.RIGHT;
        add(bottom);
        save.setEnabled(WaterFrames.CONFIG.canUse(getPlayer(), url.getText()));
        bottom.add(save);
        bottom.add(new GuiButton("reload", x -> {
            synchronized (TextureSeeker.LOCK) {
                if (Screen.hasShiftDown())
                    TextureCache.reloadAll();
                else if (frame.cache != null)
                    frame.cache.reload();
            }
        }).setTranslate("gui.waterframes.reload").setTooltip(new TextBuilder().translate("gui.waterframes.reloadtooltip").build()));
        
    }

}
