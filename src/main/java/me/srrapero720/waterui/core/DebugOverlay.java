package me.srrapero720.waterui.core;

import com.mojang.blaze3d.platform.InputConstants;
import me.srrapero720.waterui.layout.ParentLinear;
import me.srrapero720.waterui.layout.FrameLayout;
import me.srrapero720.waterui.widget.Button;
import me.srrapero720.waterui.widget.ComboButton;
import me.srrapero720.waterui.widget.ProgressBar;
import me.srrapero720.waterui.widget.Slider;
import me.srrapero720.waterui.widget.Switch;
import me.srrapero720.waterui.widget.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Layout inspector toggled with F3 + W. Outlines the box of every element and details the
 * hovered one: margin, padding and content fills, the slot grid of the layout that owns it,
 * the displacement its anchor applied, and a panel with the numbers behind all of it.
 */
public final class DebugOverlay {
    // TREE OUTLINES: CONTAINERS, LEAVES, AND HIDDEN ELEMENTS STILL HOLDING THEIR ROOM
    private static final int PARENT = 0xA0E64FA8;
    private static final int LEAF = 0xA038D8E6;
    private static final int HIDDEN = 0x50FFFFFF;

    // HOVER FILLS, ONE PER BOX RING LIKE A BROWSER INSPECTOR
    private static final int MARGIN = 0x50F5A623;
    private static final int PADDING = 0x5058C46B;
    private static final int CONTENT = 0x502383F5;
    private static final int HOVER = 0xFFFFFFFF;

    private static final int GRID = 0x907B61FF;
    private static final int GAP = 0x307B61FF;
    private static final int SHIFT = 0xFFFFE95E;

    private static final int PANEL_BACK = 0xE0101418;
    private static final int PANEL_EDGE = 0xFF3C4650;
    private static final int PANEL_TITLE = 0xFF38D8E6;
    private static final int PANEL_PATH = 0xFFFFE95E;
    private static final int PANEL_TEXT = 0xFFE0E0E0;
    private static final int LINE = 10;

    // RIDES OVER EVERY STACKED LAYER; TOOLTIPS ARE SKIPPED WHILE THE INSPECTOR IS ACTIVE
    private static final int DEPTH = 500;

    private static boolean active;

    private DebugOverlay() {}

    public static boolean active() {
        return active;
    }

    /** Clears the inspector so it does not persist across screens. */
    public static void reset() {
        active = false;
    }

    /** Consumes the pressed key when it completes the F3 + W chord, toggling the inspector. */
    public static boolean chord(int key) {
        if (key != GLFW.GLFW_KEY_W || !chordHeld()) return false;
        active = !active;
        return true;
    }

    /** Whether F3 is physically held, which turns any key into a chord instead of input. */
    public static boolean chordHeld() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_F3);
    }

    /** Paints the whole inspector over the given layer root, on top of everything it drew. */
    public static void render(GuiGraphics graphics, AbstractParent root, int mouseX, int mouseY, int width, int height) {
        Element hovered = root.pick(mouseX, mouseY);
        if (hovered == null && root.hovered(mouseX, mouseY)) hovered = root;

        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(0f, 0f, DEPTH);

        outlines(graphics, root, false);
        if (hovered != null) {
            // DETAILS OF A SCROLLED ELEMENT STAY INSIDE ITS VIEWPORT
            ParentLinear viewport = viewport(hovered);
            if (viewport != null) graphics.enableScissor(viewport.bounds.x, viewport.bounds.y,
                    viewport.bounds.right(), viewport.bounds.bottom());
            grid(graphics, hovered);
            boxes(graphics, hovered);
            shifts(graphics, hovered);
            if (viewport != null) graphics.disableScissor();
        }
        panel(graphics, hovered, mouseX, mouseY, width, height);

        graphics.flush();
        graphics.pose().popPose();
    }

    // EVERY ELEMENT GETS ITS BOUNDS TRACED; SCROLLED CONTENT STAYS CLIPPED TO ITS VIEWPORT
    private static void outlines(GuiGraphics graphics, Element element, boolean veiled) {
        veiled |= element.hidden;
        Rect box = element.bounds;
        graphics.renderOutline(box.x, box.y, box.width, box.height,
                veiled ? HIDDEN : element instanceof AbstractParent ? PARENT : LEAF);
        if (!(element instanceof AbstractParent parent)) return;
        boolean clipped = element instanceof ParentLinear layout && (layout.scrollsX() || layout.scrollsY());
        if (clipped) graphics.enableScissor(box.x, box.y, box.right(), box.bottom());
        for (Element child: parent.children) if (child.visible) outlines(graphics, child, veiled);
        if (clipped) graphics.disableScissor();
    }

    // NEAREST SCROLLING VIEWPORT OVER THE ELEMENT
    private static ParentLinear viewport(Element element) {
        for (AbstractParent up = element.parent; up != null; up = up.parent) {
            if (up instanceof ParentLinear layout && (layout.scrollsX() || layout.scrollsY())) return layout;
        }
        return null;
    }

    // INSPECTOR FILLS OF THE HOVERED ELEMENT: MARGIN AND PADDING RINGS AROUND THE CONTENT BOX
    private static void boxes(GuiGraphics graphics, Element element) {
        Rect box = element.bounds;
        Spacing margin = element.margin();
        ring(graphics, box.x - margin.left(), box.y - margin.top(), box.right() + margin.right(),
                box.bottom() + margin.bottom(), box.x, box.y, box.right(), box.bottom(), MARGIN);

        int contentX = element.contentX(), contentY = element.contentY();
        int contentRight = contentX + element.contentWidth(), contentBottom = contentY + element.contentHeight();
        ring(graphics, box.x, box.y, box.right(), box.bottom(), contentX, contentY, contentRight, contentBottom, PADDING);
        graphics.fill(contentX, contentY, contentRight, contentBottom, CONTENT);
        graphics.renderOutline(box.x, box.y, box.width, box.height, HOVER);
    }

    // FOUR STRIPS BETWEEN THE OUTER AND THE INNER BOX, PAINTING THE RING WITHOUT OVERDRAW
    private static void ring(GuiGraphics graphics, int x1, int y1, int x2, int y2,
                             int ix1, int iy1, int ix2, int iy2, int color) {
        if (y1 < iy1) graphics.fill(x1, y1, x2, iy1, color);
        if (iy2 < y2) graphics.fill(x1, iy2, x2, y2, color);
        if (x1 < ix1) graphics.fill(x1, iy1, ix1, iy2, color);
        if (ix2 < x2) graphics.fill(ix2, iy1, x2, iy2, color);
    }

    // SLOT GRID OF THE LAYOUT OWNING THE HOVERED ELEMENT, WITH THE SPACING GAPS TINTED
    private static void grid(GuiGraphics graphics, Element hovered) {
        ParentLinear layout = hovered instanceof ParentLinear own ? own
                : hovered.parent instanceof ParentLinear owner ? owner : null;
        if (layout == null) return;

        boolean vertical = layout.orientation == ParentLinear.Orientation.VERTICAL;
        int x = layout.contentX(), y = layout.contentY();
        int right = x + layout.contentWidth(), bottom = y + layout.contentHeight();
        graphics.renderOutline(x, y, right - x, bottom - y, GRID);

        int previous = Integer.MIN_VALUE;
        for (Element child: layout.children) {
            if (!child.visible) continue;
            Spacing margin = child.margin();
            int start = vertical ? child.bounds.y - margin.top() : child.bounds.x - margin.left();
            int end = vertical ? child.bounds.bottom() + margin.bottom() : child.bounds.right() + margin.right();
            if (previous != Integer.MIN_VALUE && start > previous) {
                if (vertical) graphics.fill(x, previous, right, start, GAP);
                else graphics.fill(previous, y, start, bottom, GAP);
            }
            if (vertical) {
                graphics.fill(x, start, right, start + 1, GRID);
                graphics.fill(x, end - 1, right, end, GRID);
            } else {
                graphics.fill(start, y, start + 1, bottom, GRID);
                graphics.fill(end - 1, y, end, bottom, GRID);
            }
            previous = end;
        }
    }

    // DISTANCE THE ANCHOR MOVED THE HOVERED ELEMENT AWAY FROM THE FLOW ORIGIN OF ITS PARENT
    private static void shifts(GuiGraphics graphics, Element element) {
        Font font = Minecraft.getInstance().font;
        Rect box = element.bounds;
        if (anchoredX(element)) {
            int shift = shiftX(element);
            if (shift != 0) {
                int from = box.x - shift;
                int y = box.y + box.height / 2;
                graphics.fill(Math.min(from, box.x), y, Math.max(from, box.x), y + 1, SHIFT);
                graphics.fill(from, y - 2, from + 1, y + 3, SHIFT);
                graphics.drawString(font, (shift > 0 ? "+" : "") + shift, Math.min(from, box.x) + 2, y - LINE, SHIFT, true);
            }
        }
        if (anchoredY(element)) {
            int shift = shiftY(element);
            if (shift != 0) {
                int from = box.y - shift;
                int x = box.x + box.width / 2;
                graphics.fill(x, Math.min(from, box.y), x + 1, Math.max(from, box.y), SHIFT);
                graphics.fill(x - 2, from, x + 3, from + 1, SHIFT);
                graphics.drawString(font, (shift > 0 ? "+" : "") + shift, x + 3, Math.min(from, box.y) + 1, SHIFT, true);
            }
        }
    }

    // A STACK ANCHORS BOTH AXES; A ROW OR A COLUMN ONLY ANCHORS ITS CROSS AXIS
    private static boolean anchoredX(Element element) {
        return element.parent instanceof FrameLayout || (element.parent instanceof ParentLinear layout
                && layout.orientation == ParentLinear.Orientation.VERTICAL);
    }

    private static boolean anchoredY(Element element) {
        return element.parent instanceof FrameLayout || (element.parent instanceof ParentLinear layout
                && layout.orientation == ParentLinear.Orientation.HORIZONTAL);
    }

    private static int shiftX(Element element) {
        return element.bounds.x - element.parent.contentX() - element.margin().left();
    }

    private static int shiftY(Element element) {
        return element.bounds.y - element.parent.contentY() - element.margin().top();
    }

    // READOUT PANEL WITH THE NUMBERS, DOCKED ON WHICHEVER HALF THE CURSOR IS NOT
    private static void panel(GuiGraphics graphics, Element hovered, int mouseX, int mouseY, int width, int height) {
        Font font = Minecraft.getInstance().font;
        List<String> lines = new ArrayList<>();
        lines.add("WaterUI Debug (F3+W)");
        lines.add("screen " + width + "x" + height + " @" + (int) Minecraft.getInstance().getWindow().getGuiScale()
                + "x - mouse " + mouseX + "," + mouseY);

        int pathLine = -1;
        if (hovered != null) {
            Rect box = hovered.bounds;
            lines.add("");
            pathLine = lines.size();
            lines.add(path(hovered));
            lines.add("bounds " + box.x + "," + box.y + " " + box.width + "x" + box.height
                    + " - content " + hovered.contentWidth() + "x" + hovered.contentHeight());

            String size = "size " + sizeName(hovered.width()) + " x " + sizeName(hovered.height());
            if (hovered.width() == Element.FILL || hovered.height() == Element.FILL) size += " - weight " + hovered.weight();
            lines.add(size);
            lines.add("margin " + edges(hovered.margin()) + " - padding " + edges(hovered.padding())
                    + " - border " + hovered.borderWidth());

            String anchor = "anchor " + anchorName(hovered.anchor());
            if (hovered.alignMask() != 0) anchor += " - align " + anchorName(hovered.alignMask());
            if (anchoredX(hovered)) anchor += " - dx " + shiftX(hovered);
            if (anchoredY(hovered)) anchor += " - dy " + shiftY(hovered);
            lines.add(anchor);

            if (hovered instanceof ParentLinear layout) {
                String prefix = hovered instanceof ComboButton ? "combo " : "";
                String info = prefix + "layout " + layout.orientation.name().toLowerCase() + " - spacing " + layout.spacing
                        + " - align " + anchorName(layout.containerAlign()) + " - children " + layout.children.size();
                if (layout.justify() != null) info += " - justify " + layout.justify().name().toLowerCase();
                if (layout.scrollsX() || layout.scrollsY()) {
                    info += " - scroll" + (layout.scrollsX() ? "X" : "") + (layout.scrollsY() ? "Y" : "");
                    if (layout.scrollsX()) info += " offsetX=" + layout.scrollOffsetX();
                    if (layout.scrollsY()) info += " offsetY=" + layout.scrollOffsetY();
                }
                lines.add(info);
            }

            String state = (hovered.enabled ? "" : " disabled") + (hovered.elevation != 0 ? " elevation " + hovered.elevation : "");
            if (hovered.outsideEdge() != 0) state += " outside edge=" + anchorName(hovered.outsideEdge()) + " align=" + anchorName(hovered.outsideAlign());

            // WIDGET-SPECIFIC PROPERTIES ONLY WHEN THEY DEVIATE FROM DEFAULTS
            float ts = -1;
            if (hovered instanceof Switch sw) ts = sw.textScale();
            else if (hovered instanceof Slider sl) ts = sl.textScale();
            else if (hovered instanceof ProgressBar pb) ts = pb.textScale();
            if (ts > 0 && ts != 1f) state += " textScale " + ts;

            if (hovered instanceof Text txt) {
                if (txt.fontScale() != 1f) state += " fontScale " + txt.fontScale();
                if (txt.maxLines() > 1) state += " lines " + txt.maxLines();
                if (txt.ellipsized()) state += " ellipsize";
            }
            if (hovered instanceof Button btn && btn.fused()) state += " fused";

            if (!state.isEmpty()) lines.add("state" + state);
        }

        int panelWidth = 0;
        for (String line: lines) panelWidth = Math.max(panelWidth, font.width(line));
        panelWidth += 8;
        int panelHeight = lines.size() * LINE + 6;
        int x = mouseX < width / 2 ? width - panelWidth - 4 : 4;

        graphics.fill(x, 4, x + panelWidth, 4 + panelHeight, PANEL_BACK);
        graphics.renderOutline(x, 4, panelWidth, panelHeight, PANEL_EDGE);
        for (int i = 0; i < lines.size(); i++) {
            int color = i == 0 ? PANEL_TITLE : i == pathLine ? PANEL_PATH : PANEL_TEXT;
            graphics.drawString(font, lines.get(i), x + 4, 8 + i * LINE, color, false);
        }
    }

    // OWNERSHIP CHAIN DOWN TO THE HOVERED ELEMENT, CAPPED TO ITS LAST FOUR STEPS
    private static String path(Element element) {
        List<String> chain = new ArrayList<>();
        for (Element step = element; step != null; step = step.parent) chain.add(0, name(step));
        int start = Math.max(0, chain.size() - 4);
        String path = String.join(" > ", chain.subList(start, chain.size()));
        return start > 0 ? "... > " + path : path;
    }

    // ANONYMOUS WIDGETS REPORT THEIR NEAREST NAMED ANCESTOR CLASS
    private static String name(Element element) {
        Class<?> type = element.getClass();
        while (type.getSimpleName().isEmpty()) type = type.getSuperclass();
        return type.getSimpleName();
    }

    private static String sizeName(int size) {
        return size == Element.FILL ? "FILL" : size == Element.CONTAIN ? "CONTAIN" : size + "px";
    }

    private static String edges(Spacing spacing) {
        if (spacing.top() == spacing.right() && spacing.right() == spacing.bottom() && spacing.bottom() == spacing.left()) {
            return String.valueOf(spacing.top());
        }
        return spacing.top() + "," + spacing.right() + "," + spacing.bottom() + "," + spacing.left();
    }

    private static String anchorName(int anchor) {
        if (anchor == 0) return "none";
        // ONE NAME PER BIT, IN THE DECLARATION ORDER OF THE Anchor FLAGS
        String[] names = {"START", "END", "CENTER_H", "STRETCH_H", "TOP", "BOTTOM", "CENTER_V", "STRETCH_V"};
        StringBuilder joined = new StringBuilder();
        for (int bit = 0; bit < names.length; bit++) {
            if ((anchor & (1 << bit)) == 0) continue;
            if (!joined.isEmpty()) joined.append('|');
            joined.append(names[bit]);
        }
        return joined.toString();
    }
}
