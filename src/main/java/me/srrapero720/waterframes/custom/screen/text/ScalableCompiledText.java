package me.srrapero720.waterframes.custom.screen.text;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import team.creative.creativecore.client.render.text.CompiledText;
import team.creative.creativecore.client.render.text.Linebreaker;
import team.creative.creativecore.client.render.text.WidthLimitedCharSink;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.util.text.AdvancedComponent;
import team.creative.creativecore.common.util.type.list.SingletonList;

import java.util.*;

public class ScalableCompiledText {
    private int maxWidth;
    private int maxHeight;
    public int usedWidth;
    public int usedHeight;
    public int lineSpacing = 2;
    public boolean shadow = true;
    public int defaultColor = -1;
    public float scale;
    public Align alignment;
    private List<CompiledLine> lines;
    private List<Component> original;

    public ScalableCompiledText(int width, int height) {
        this(width, height, 1.0f);
    }

    public ScalableCompiledText(int width, int height, float scale) {
        this.alignment = Align.LEFT;
        this.maxWidth = width;
        this.maxHeight = height;
        this.scale = scale;
        this.setText(Collections.emptyList());
    }

    public void setMaxHeight(int height) {
        this.maxHeight = height;
    }

    public void setDimension(int width, int height) {
        this.maxWidth = width;
        this.maxHeight = height;
        this.compile();
    }

    public void setScale(float s) {
        this.scale = s;
    }

    public float getScale() {
        return this.scale;
    }

    public int getMaxWidht() {
        return this.maxWidth;
    }

    public int getMaxHeight() {
        return this.maxHeight;
    }

    public void setText(Component component) {
        this.setText(new SingletonList<>(component));
    }

    public void setText(List<Component> components) {
        this.original = components;
        this.compile();
    }

    private void compile() {
        if (!FMLEnvironment.dist.isDedicatedServer()) {
            List<Component> copy = new ArrayList<>();

            for (Component component : this.original) {
                copy.add(component.copy());
            }

            this.lines = new ArrayList();
            this.compileNext(null, true, copy);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private CompiledLine compileNext(CompiledLine currentLine, boolean newLine, List<? extends FormattedText> components) {
        FormattedText component;
        for(Iterator<? extends FormattedText> var4 = components.iterator(); var4.hasNext(); currentLine = this.compileNext(currentLine, component)) {
            component = (FormattedText)var4.next();
            if (newLine) {
                this.lines.add(currentLine = new CompiledLine());
            }
        }

        return currentLine;
    }

    @OnlyIn(Dist.CLIENT)
    private CompiledLine compileNext(CompiledLine currentLine, boolean newLine, FormattedText component) {
        if (newLine) {
            this.lines.add(currentLine = new CompiledLine());
        }

        return this.compileNext(currentLine, component);
    }

    private CompiledLine compileNext(CompiledLine currentLine, FormattedText component) {
        List<Component> siblings = null;
        if (component instanceof Component && !((Component)component).getSiblings().isEmpty()) {
            siblings = new ArrayList(((Component)component).getSiblings());
            ((Component)component).getSiblings().clear();
        }

        FormattedText next = currentLine.add(component);
        if (next != null) {
            this.lines.add(currentLine = new CompiledLine());
            currentLine = this.compileNext(currentLine, false, next);
        }

        if (siblings != null) {
            currentLine = this.compileNext(currentLine, false, (List)siblings);
        }

        return currentLine;
    }

    @OnlyIn(Dist.CLIENT)
    public int getTotalHeight() {
        int height = -this.lineSpacing;

        CompiledLine line;
        for(Iterator var2 = this.lines.iterator(); var2.hasNext(); height += line.height + this.lineSpacing) {
            line = (CompiledLine)var2.next();
        }

        return height;
    }

    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack stack) {
        if (this.lines != null) {
            this.usedWidth = 0;
            this.usedHeight = -this.lineSpacing;
            stack.pushPose();
            stack.scale(scale, scale, scale);

            for (CompiledLine line : this.lines) {
                switch (this.alignment) {
                    case LEFT:
                        line.render(stack);
                        this.usedWidth = Math.max(this.usedWidth, line.width);
                        break;
                    case CENTER:
                        stack.pushPose();
                        stack.translate((double) (this.maxWidth / 2 - line.width / 2), 0.0, 0.0);
                        line.render(stack);
                        this.usedWidth = Math.max(this.usedWidth, this.maxWidth);
                        stack.popPose();
                        break;
                    case RIGHT:
                        stack.pushPose();
                        stack.translate((double) (this.maxWidth - line.width), 0.0, 0.0);
                        line.render(stack);
                        this.usedWidth = Math.max(this.usedWidth, this.maxWidth);
                        stack.popPose();
                    case STRETCH:
                }

                int height = line.height + this.lineSpacing;
                stack.translate(0.0, height, 0.0);
                this.usedHeight += height;
                if (this.usedHeight > this.maxHeight) {
                    break;
                }
            }

            stack.popPose();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public FormattedTextSplit splitByWidth(FormattedText text, int width, Style style, final boolean force) {
        Font font = Minecraft.getInstance().font;
        final WidthLimitedCharSink charSink = new WidthLimitedCharSink((float)width, font.getSplitter());
        final ComponentCollector head = new ComponentCollector();
        final ComponentCollector tail = new ComponentCollector();
        text.visit((FormattedText.StyledContentConsumer<FormattedText>) (style1, text1) -> {
            charSink.resetPosition();
            if (!StringDecomposer.iterateFormatted(text1, style1, charSink)) {
                Linebreaker breaker = charSink.lastBreaker();
                if (!force && breaker == null) {
                    tail.append(FormattedText.of(text1, style1));
                } else {
                    String sHead;
                    String sTail;
                    if (breaker == null) {
                        sHead = text1.substring(0, charSink.getPosition());
                        sTail = text1.substring(charSink.getPosition());
                    } else {
                        int pos = charSink.lastBreakerPos();
                        sHead = text1.substring(0, pos + (breaker.includeChar && breaker.head ? 1 : 0));
                        sTail = text1.substring(pos + (breaker.includeChar && !breaker.head ? 0 : 1));
                    }

                    if (!sHead.isEmpty()) {
                        head.append(FormattedText.of(sHead, style1));
                    }

                    if (!sTail.isEmpty()) {
                        tail.append(FormattedText.of(sTail, style1));
                    }
                }
            } else if (!text1.isEmpty()) {
                head.append(FormattedText.of(text1, style1));
            }

            return Optional.empty();
        }, style).orElse(null);
        return new FormattedTextSplit(head, tail);
    }

    @OnlyIn(Dist.CLIENT)
    public int getTotalWidth() {
        return this.calculateWidth(0, true, this.original);
    }

    @OnlyIn(Dist.CLIENT)
    private int calculateWidth(int width, boolean newLine, List<? extends FormattedText> components) {

        for (FormattedText component : components) {
            int result = this.calculateWidth(component);
            if (newLine) {
                width = Math.max(width, result);
            } else {
                width += result;
            }
        }

        return width;
    }

    @OnlyIn(Dist.CLIENT)
    private int calculateWidth(FormattedText component) {
        Font font = Minecraft.getInstance().font;
        int width = 0;
        if (component instanceof AdvancedComponent advanced) {
            if (!advanced.isEmpty()) {
                width += advanced.getWidth(font);
            }
        } else {
            width += font.width(component);
        }

        if (component instanceof Component && !((Component)component).getSiblings().isEmpty()) {
            width += this.calculateWidth(0, false, ((Component)component).getSiblings());
        }

        return width;
    }

    public CompiledText copy() {
        CompiledText copy = new CompiledText(this.maxWidth, this.maxHeight);
        copy.alignment = this.alignment;
        copy.lineSpacing = this.lineSpacing;
        copy.shadow = this.shadow;
        List<Component> components = new ArrayList();
        Iterator<Component> var3 = this.original.iterator();

        while(var3.hasNext()) {
            Component component = (Component)var3.next();
            components.add(component.copy());
        }

        copy.setText(components);
        return copy;
    }

    public static ScalableCompiledText createAnySize() {
        return new ScalableCompiledText(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public class CompiledLine {
        private List<FormattedText> components = new ArrayList();
        private int height = 0;
        private int width = 0;

        public CompiledLine() {
        }

        @OnlyIn(Dist.CLIENT)
        public void render(PoseStack stack) {
            Font font = Minecraft.getInstance().font;
            int xOffset = 0;
            MultiBufferSource.BufferSource renderType = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

            int width;
            for(Iterator<FormattedText> var5 = this.components.iterator(); var5.hasNext(); xOffset += width) {
                FormattedText text = (FormattedText)var5.next();
                int height;
                if (text instanceof AdvancedComponent) {
                    width = ((AdvancedComponent)text).getWidth(font);
                    height = ((AdvancedComponent)text).getHeight(font);
                } else {
                    width = font.width(text);
                    Objects.requireNonNull(font);
                    height = 9;
                }

                int yOffset = 0;
                if (height < this.height) {
                    yOffset = (this.height - height) / 2;
                }

                stack.pushPose();
                stack.translate((double)xOffset, (double)yOffset, 0.0);
                if (text instanceof AdvancedComponent) {
                    ((AdvancedComponent)text).render(stack, font, ScalableCompiledText.this.defaultColor);
                } else {
                    font.drawInBatch(Language.getInstance().getVisualOrder(text), 0.0F, 0.0F, ScalableCompiledText.this.defaultColor, ScalableCompiledText.this.shadow, stack.last().pose(), renderType, false, 0, 15728880);
                    renderType.endBatch();
                }

                stack.popPose();
            }

        }

        @OnlyIn(Dist.CLIENT)
        public void updateDimension(int width, int height) {
            this.width = Math.max(width, this.width);
            this.height = Math.max(height, this.height);
        }

        public FormattedText add(FormattedText component) {
            Font font = Minecraft.getInstance().font;
            int remainingWidth = ScalableCompiledText.this.maxWidth - this.width;
            if (component instanceof AdvancedComponent advanced) {
                if (advanced.isEmpty()) {
                    return null;
                } else {
                    int textWidth = advanced.getWidth(font);
                    if (remainingWidth > textWidth) {
                        this.components.add(advanced);
                        this.updateDimension(this.width + textWidth, advanced.getHeight(font));
                        return null;
                    } else if (advanced.canSplit()) {
                        List<AdvancedComponent> remaining = advanced.split(remainingWidth, this.width == 0);
                        AdvancedComponent toAdd = (AdvancedComponent)remaining.remove(0);
                        this.components.add(toAdd);
                        this.updateDimension(this.width + toAdd.getWidth(font), toAdd.getHeight(font));
                        return remaining.isEmpty() ? null : (FormattedText)remaining.get(0);
                    } else if (this.width == 0) {
                        this.components.add(advanced);
                        this.updateDimension(this.width + textWidth, advanced.getHeight(font));
                        return null;
                    } else {
                        return advanced;
                    }
                }
            } else {
                int textWidthx = font.width(component);
                int var10001;
                if (remainingWidth >= textWidthx) {
                    this.components.add(component);
                    var10001 = this.width + textWidthx;
                    Objects.requireNonNull(font);
                    this.updateDimension(var10001, 9);
                    return null;
                } else {
                    FormattedTextSplit split = ScalableCompiledText.this.splitByWidth(component, remainingWidth, Style.EMPTY, this.width == 0);
                    if (split != null && (split.head != null || this.width == 0)) {
                        if (split.head != null) {
                            var10001 = this.width + font.width(split.head);
                            Objects.requireNonNull(font);
                            this.updateDimension(var10001, 9);
                            this.components.add(split.head);
                            return split.tail;
                        } else {
                            var10001 = this.width + font.width(split.tail);
                            Objects.requireNonNull(font);
                            this.updateDimension(var10001, 9);
                            this.components.add(split.tail);
                            return null;
                        }
                    } else {
                        return component;
                    }
                }
            }
        }
    }

    public static class FormattedTextSplit {
        public final FormattedText head;
        public final FormattedText tail;

        public FormattedTextSplit(FormattedText head, FormattedText tail) {
            this.head = head;
            this.tail = tail;
        }

        public FormattedTextSplit(ComponentCollector head, ComponentCollector tail) {
            this.head = head.getResult();
            this.tail = tail.getResult();
        }
    }
}