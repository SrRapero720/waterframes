package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.core.Element;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class Text extends Element {
    private Component text = Component.empty();
    /** 0 falls back to the theme colour. */
    public int color;
    // GLYPH SCALE, FOR SECONDARY LINES THAT READ AS FINE PRINT
    private float fontScale = 1f;
    // TRIMS THE TEXT TO THE BOX AND MARKS THE CUT WITH AN ELLIPSIS
    private boolean ellipsize;
    // HOW MANY LINES THE TEXT MAY WRAP OVER; OVERFLOW IS CUT
    private int maxLines = 1;

    // WRAP CACHE: A RING OF ROOM→LINES ENTRIES SO MEASUREMENT AND DRAW STOP THRASHING
    private static final int WRAP_SLOTS = 4;
    private String wrapText;
    private int wrapCount;
    private int wrapNext;
    private final int[] wrapRooms = new int[WRAP_SLOTS];
    @SuppressWarnings("unchecked")
    private final List<String>[] wrapLines = new List[WRAP_SLOTS];

    public Text() {}

    public Text(Component text) {
        this.text = text;
    }

    public static Text translated(String key) {
        return new Text(Component.translatable(key));
    }

    public Text color(int color) {
        this.color = color;
        return this;
    }

    public Text fontScale(float fontScale) {
        this.fontScale = fontScale;
        this.wrapCount = 0;
        this.dirty();
        return this;
    }

    public Text ellipsize(boolean ellipsize) {
        this.ellipsize = ellipsize;
        this.wrapCount = 0;
        this.dirty();
        return this;
    }

    public Text ellipsize() {
        return this.ellipsize(true);
    }

    public Text lines(int lines) {
        this.maxLines = Math.max(1, lines);
        this.wrapCount = 0;
        this.dirty();
        return this;
    }

    /** Same content is a no-op, so live rows can push their state every tick without a reflow. */
    public Text text(Component text) {
        if (this.text.equals(text)) return this;
        this.text = text;
        this.dirty();
        return this;
    }

    /** Alias for the format layer's {@code component} property name. */
    public Text component(Component text) {
        return this.text(text);
    }

    public Component text() {
        return text;
    }

    public float fontScale() {
        return fontScale;
    }

    public int maxLines() {
        return maxLines;
    }

    public boolean ellipsized() {
        return ellipsize;
    }

    @Override
    protected int prefContentWidth(int available) {
        return Math.round(font().width(text) * fontScale);
    }

    @Override
    protected int prefContentHeight(int width, int available) {
        int line = Math.round(font().lineHeight * fontScale);
        if (maxLines == 1) return line;
        // ACTUAL WRAP AT THE GIVEN WIDTH, CAPPED AT maxLines, AT LEAST 1 LINE
        return line * Math.max(1, Math.min(maxLines, this.wrap(this.room(width)).size()));
    }

    // THE TEXT CAN SHRINK DOWN TO A SINGLE LINE ON ANY WIDTH
    @Override
    protected int minContentHeight(int width, int available) {
        return Math.round(font().lineHeight * fontScale);
    }

    // A LINE THAT MAY BE CUT SHORT CAN SHRINK DOWN TO ITS ELLIPSIS MARK
    @Override
    protected int minContentWidth(int available) {
        return ellipsize || maxLines > 1 ? Math.round(font().width("...") * fontScale) : prefContentWidth(available);
    }

    // BOX WIDTH BACK IN GLYPH SPACE, WHERE THE FONT MEASURES
    private int room(int width) {
        return Math.max(1, Math.round(width / fontScale));
    }

    // THE STRING AS IT FITS THE ROOM: WHOLE, CUT WITH AN ELLIPSIS MARKER, OR HARD-CUT
    private String fit(String raw, int room) {
        if (font().width(raw) <= room) return raw;
        if (ellipsize) return font().plainSubstrByWidth(raw, Math.max(0, room - font().width("..."))) + "...";
        // HARD-CUT WITHOUT SUFFIX: drawString DOES NOT CLIP, SO OVERFLOW PAINTS PAST THE BOX
        return font().plainSubstrByWidth(raw, room);
    }

    // WORD WRAP IN GLYPH SPACE: EVERY LINE TAKES WHAT FITS AND THE LAST ONE MARKS THE CUT
    private List<String> wrap(int room) {
        String raw = text.getString();
        if (!raw.equals(wrapText)) {
            this.wrapText = raw;
            this.wrapCount = 0;
            this.wrapNext = 0;
        }
        for (int i = 0; i < wrapCount; i++) {
            if (wrapRooms[i] == room) return wrapLines[i];
        }

        List<String> lines;
        if (maxLines == 1) {
            lines = List.of(this.fit(raw, room));
        } else {
            lines = new ArrayList<>(maxLines);
            String rest = raw;
            while (!rest.isEmpty() && lines.size() < maxLines) {
                if (lines.size() == maxLines - 1 || font().width(rest) <= room) {
                    lines.add(this.fit(rest, room));
                    break;
                }
                String cut = font().plainSubstrByWidth(rest, room);
                if (cut.isEmpty()) break;
                int space = cut.lastIndexOf(' ');
                if (space > 0) cut = cut.substring(0, space);
                lines.add(cut);
                rest = rest.substring(cut.length()).trim();
            }
        }
        this.wrapRooms[wrapNext] = room;
        this.wrapLines[wrapNext] = lines;
        this.wrapNext = (wrapNext + 1) % WRAP_SLOTS;
        if (wrapCount < WRAP_SLOTS) wrapCount++;
        return lines;
    }

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        int color = this.color != 0 ? this.color : textColor();
        List<String> lines = this.wrap(this.room(contentWidth()));

        // THE BLOCK CENTRES ON THE GLYPH HEIGHT OF ITS LAST LINE, WHICH KEEPS THE SINGLE LINE
        // CASE SITTING EXACTLY WHERE IT ALWAYS DID
        int step = Math.round(font().lineHeight * fontScale);
        int block = step * (lines.size() - 1) + Math.round(GLYPH_HEIGHT * fontScale);
        int y = contentY() + Math.max(0, (contentHeight() - block) / 2);

        if (fontScale == 1f) {
            for (String line: lines) {
                graphics.drawString(font(), line, contentX(), y, color, shadow());
                y += step;
            }
            return;
        }

        // SCALING HAS TO GO THROUGH THE POSE, drawString ONLY TAKES WHOLE PIXELS
        graphics.pose().pushPose();
        graphics.pose().translate(contentX(), y, 0);
        graphics.pose().scale(fontScale, fontScale, 1f);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font(), lines.get(i), 0, i * font().lineHeight, color, shadow());
        }
        graphics.pose().popPose();
    }
}
