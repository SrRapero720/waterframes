package me.srrapero720.waterui.layout;

import me.srrapero720.waterui.core.Anchor;
import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.core.AbstractParent;
import me.srrapero720.waterui.theme.ScrollTheme;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Stacks its children on one axis. Every child is first given the size it prefers and only
 * the space left over afterwards is shared between the ones set to {@link Element#FILL} on
 * that axis, which is what keeps a row of icons at their natural size while a slider next
 * to them takes the rest. Optionally scrolls on either or both axes in place.
 */
public class ParentLinear extends AbstractParent {
    public enum Orientation { HORIZONTAL, VERTICAL }

    public Orientation orientation;
    // GAP BETWEEN SIBLINGS IN GUI PIXELS
    public int spacing = 2;
    private int containerAlign;
    private Justify justify = Justify.START;

    // SCROLL STATE: WHEN ENABLED THE AXIS IS UNBOUNDED AND THE VISIBLE SLICE CLIPS
    private boolean scrollX;
    private boolean scrollY;
    private int scrollOffsetX;
    private int scrollOffsetY;
    private int contentLengthX;
    private int contentLengthY;
    private boolean draggingX, draggingY;
    private double dragOriginX, dragOriginY;
    private int dragScrollX, dragScrollY;
    private Element scrollFocused;

    public ParentLinear() {
        this(Orientation.HORIZONTAL);
    }

    public ParentLinear(Orientation orientation) {
        this.orientation = orientation;
    }

    public ParentLinear orientation(Orientation orientation) {
        this.orientation = orientation;
        this.dirty();
        return this;
    }

    public static ParentLinear row() {
        return new ParentLinear(Orientation.HORIZONTAL);
    }

    public static ParentLinear column() {
        return new ParentLinear(Orientation.VERTICAL);
    }

    public ParentLinear spacing(int spacing) {
        this.spacing = spacing;
        return this;
    }

    /** Container default for children's cross-axis placement; uses Anchor bit constants. */
    public ParentLinear alignChildren(int mask) {
        this.containerAlign = mask;
        return this;
    }

    public int containerAlign() {
        return containerAlign;
    }

    public ParentLinear justify(Justify justify) {
        this.justify = justify;
        return this;
    }

    public Justify justify() {
        return justify;
    }

    public ParentLinear scrollX(boolean scrollX) {
        this.scrollX = scrollX;
        return this;
    }

    public ParentLinear scrollY(boolean scrollY) {
        this.scrollY = scrollY;
        return this;
    }

    public boolean scrollsX() {
        return scrollX;
    }

    public boolean scrollsY() {
        return scrollY;
    }

    public int scrollOffsetX() {
        return scrollOffsetX;
    }

    public int scrollOffsetY() {
        return scrollOffsetY;
    }

    /** Scrolls until the child sits centred in the viewport, once the next layout runs. */
    public void focus(Element child) {
        this.scrollFocused = child;
    }

    /** Adjusts the scroll position by the given amount, clamping to the valid range. */
    public void scrollBy(int dx, int dy) {
        if (scrollX && maxScrollX() > 0) this.scrollOffsetX = Math.clamp(scrollOffsetX + dx, 0, maxScrollX());
        if (scrollY && maxScrollY() > 0) this.scrollOffsetY = Math.clamp(scrollOffsetY + dy, 0, maxScrollY());
        this.arrange();
    }

    private boolean vertical() {
        return orientation == Orientation.VERTICAL;
    }

    // MAX SCROLL USES THE BAR-REDUCED VIEWPORT: THE SCROLLBAR EATS INTO THE CONTENT AREA
    private int maxScrollX() {
        int viewport = contentWidth();
        if (scrollY) viewport = Math.max(0, viewport - scrollBarWidth());
        return Math.max(0, contentLengthX - viewport);
    }

    private int maxScrollY() {
        int viewport = contentHeight();
        if (scrollX) viewport = Math.max(0, viewport - scrollBarWidth());
        return Math.max(0, contentLengthY - viewport);
    }

    // SCROLLBAR WIDTH RESERVED FROM THE CONTENT AREA ON EACH SCROLLING AXIS
    private int scrollBarWidth() {
        return (scrollX || scrollY) ? theme().scroll().width() : 0;
    }

    @Override
    protected int prefContentWidth(int available) {
        // A SCROLLING MAIN AXIS NEEDS NOTHING OF ITS OWN; A scrollY COLUMN RESERVES THE BAR WIDTH
        if (scrollX && !vertical()) return scrollBarWidth();
        if (scrollY && vertical()) return scrollBarWidth();
        return this.measure(true, false, 0, available);
    }

    @Override
    protected int prefContentHeight(int width, int available) {
        if (scrollY && vertical()) return 0;
        if (scrollX && !vertical()) return scrollBarWidth();
        return this.measure(false, false, width, available);
    }

    @Override
    protected int minContentWidth(int available) {
        if (scrollX && !vertical()) return scrollBarWidth();
        if (scrollY && vertical()) return scrollBarWidth();
        return this.measure(true, true, 0, available);
    }

    @Override
    protected int minContentHeight(int width, int available) {
        if (scrollY && vertical()) return 0;
        if (scrollX && !vertical()) return scrollBarWidth();
        return this.measure(false, true, width, available);
    }

    // ONE PASS FOR EVERY MEASUREMENT: THE STACKED AXIS ADDS SIZES PLUS SPACING AND THE CROSS
    // AXIS TAKES THE LARGEST CHILD. RUNS INSIDE THE RECURSIVE LAYOUT, SO IT ALLOCATES NOTHING
    private int measure(boolean axisX, boolean min, int width, int available) {
        // A HORIZONTAL ROW MEASURES CHILD HEIGHTS AT EACH CHILD'S REAL WIDTH: FIXED CHILDREN AT THEIR
        // OWN, FILL CHILDREN SPLITTING THE LEFTOVER — THE ROW WIDTH UNDERCOUNTS WRAPPED TEXT LINES (M1)
        int fillLeft = 0;
        float fillWeight = 0f;
        if (!axisX && !vertical()) {
            int used = 0, count = 0;
            for (Element child: children) {
                if (!child.visible) continue;
                count++;
                if (child.width() == Element.FILL) fillWeight += child.weight();
                else used += child.layoutWidth(available) + child.margin().horizontal();
            }
            fillLeft = Math.max(0, width - used - Math.max(0, count - 1) * spacing);
        }

        int total = 0, max = 0, count = 0;
        for (Element child: children) {
            if (!child.visible) continue;
            int size;
            if (axisX) {
                size = (min ? child.minWidth(available) : child.layoutWidth(available)) + child.margin().horizontal();
            } else {
                int cw = width;
                if (!vertical()) {
                    cw = child.width() == Element.FILL && fillWeight > 0
                            ? Math.round(fillLeft * child.weight() / fillWeight)
                            : child.layoutWidth(available);
                }
                size = (min ? child.minHeight(cw, available) : child.layoutHeight(cw, available)) + child.margin().vertical();
            }
            total += size;
            max = Math.max(max, size);
            count++;
        }
        return axisX == vertical() ? max : total + Math.max(0, count - 1) * spacing;
    }

    private List<Element> shown() {
        List<Element> shown = new ArrayList<>(children.size());
        for (Element child: children) if (child.visible) shown.add(child);
        return shown;
    }

    @Override
    protected void arrange() {
        List<Element> shown = this.shown();
        if (shown.isEmpty()) return;

        boolean vertical = this.vertical();
        int originX = contentX();
        int originY = contentY();
        int boxWidth = contentWidth();
        int boxHeight = contentHeight();

        // RESERVE SCROLLBAR SPACE: THE BAR SITS ON THE RIGHT EDGE (Y) OR BOTTOM EDGE (X)
        int barWidth = scrollBarWidth();
        if (scrollY) boxWidth = Math.max(0, boxWidth - barWidth);
        if (scrollX) boxHeight = Math.max(0, boxHeight - barWidth);

        // ON A SCROLLING AXIS THE CHILDREN SPREAD AT FULL PREFERRED SIZE — THERE IS NO VIEWPORT LIMIT
        boolean mainScrolls = vertical ? scrollY : scrollX;
        boolean crossScrolls = vertical ? scrollX : scrollY;
        int mainSpace = mainScrolls ? Integer.MAX_VALUE / 4 : (vertical ? boxHeight : boxWidth);
        int crossSpace = crossScrolls ? Integer.MAX_VALUE / 4 : (vertical ? boxWidth : boxHeight);

        int count = shown.size();
        int[] cross = new int[count];
        int[] main = new int[count];

        int used = Math.max(0, count - 1) * spacing;
        if (vertical) {
            // VERTICAL: CROSS (WIDTH) FIRST SO layoutHeight RECEIVES THE FINAL WIDTH
            for (int i = 0; i < count; i++) {
                Element child = shown.get(i);
                int room = crossSpace - child.margin().horizontal();
                int childCross = this.crossAnchor(child, true);
                boolean fill = ((childCross & Anchor.STRETCH_H) != 0) || child.width() == Element.FILL;
                int base = child.layoutWidth(room);
                int size = fill ? room : Math.min(room, base);
                cross[i] = child.clampWidth(size, room);
                child.crossHint(cross[i]);
            }
            for (int i = 0; i < count; i++) {
                Element child = shown.get(i);
                used += child.margin().vertical();
                main[i] = child.layoutHeight(cross[i], mainSpace);
                used += main[i];
            }
            int leftover = mainSpace - used;
            if (!mainScrolls) {
                if (leftover > 0) this.share(shown, main, cross, leftover, mainSpace, true);
                else if (leftover < 0) this.shrink(shown, main, cross, -leftover, mainSpace, true);
            }
        } else {
            // HORIZONTAL: MAIN (WIDTH) FIRST, SHARE/SHRINK, THEN CROSS (HEIGHT) WITH FINAL WIDTHS
            for (int i = 0; i < count; i++) {
                Element child = shown.get(i);
                used += child.margin().horizontal();
                main[i] = child.layoutWidth(mainSpace);
                used += main[i];
            }
            int leftover = mainSpace - used;
            if (!mainScrolls) {
                if (leftover > 0) this.share(shown, main, cross, leftover, mainSpace, false);
                else if (leftover < 0) this.shrink(shown, main, cross, -leftover, mainSpace, false);
            }
            for (int i = 0; i < count; i++) {
                Element child = shown.get(i);
                int room = crossSpace - child.margin().vertical();
                int childCross = this.crossAnchor(child, false);
                boolean fill = ((childCross & Anchor.STRETCH_V) != 0) || child.height() == Element.FILL;
                int base = child.layoutHeight(main[i], room);
                int size = fill ? room : Math.min(room, base);
                cross[i] = child.clampHeight(size, main[i], room);
                child.crossHint(cross[i]);
            }
        }

        // RECALCULATE LEFTOVER FOR JUSTIFY AFTER SHARE/SHRINK
        int totalUsed = Math.max(0, count - 1) * spacing;
        for (int i = 0; i < count; i++) {
            totalUsed += main[i] + (vertical ? shown.get(i).margin().vertical() : shown.get(i).margin().horizontal());
        }
        int viewMain = vertical ? boxHeight : boxWidth;
        int justifyLeftover = mainScrolls ? 0 : Math.max(0, viewMain - totalUsed);

        // TRACK CONTENT LENGTH FOR SCROLLING
        if (mainScrolls) {
            if (vertical) this.contentLengthY = totalUsed;
            else this.contentLengthX = totalUsed;
        }
        if (crossScrolls) {
            int maxCross = 0;
            for (int i = 0; i < count; i++) {
                maxCross = Math.max(maxCross, cross[i] + (vertical ? shown.get(i).margin().horizontal() : shown.get(i).margin().vertical()));
            }
            if (vertical) this.contentLengthX = maxCross;
            else this.contentLengthY = maxCross;
        }

        // RESOLVE PENDING FOCUS: ONE LAYOUT TO LEARN WHERE THE TARGET IS, THEN SCROLL CENTERS IT
        if (scrollFocused != null) {
            this.positionChildren(shown, main, cross, count, vertical, originX, originY, viewMain, boxWidth, boxHeight, 0, 0, 0);
            if (vertical) {
                this.scrollOffsetY = scrollFocused.bounds.y - originY - (boxHeight - scrollFocused.bounds.height) / 2;
            } else {
                this.scrollOffsetX = scrollFocused.bounds.x - originX - (boxWidth - scrollFocused.bounds.width) / 2;
            }
            this.scrollFocused = null;
        }

        // CLAMP SCROLL TO VALID RANGE
        if (scrollX) this.scrollOffsetX = Math.clamp(scrollOffsetX, 0, maxScrollX());
        if (scrollY) this.scrollOffsetY = Math.clamp(scrollOffsetY, 0, maxScrollY());

        // JUSTIFY OFFSET AND GAP
        int justifyOffset = 0;
        if (justifyLeftover > 0) {
            justifyOffset = switch (justify) {
                case CENTER -> justifyLeftover / 2;
                case END -> justifyLeftover;
                default -> 0;
            };
        }

        this.positionChildren(shown, main, cross, count, vertical, originX, originY,
                viewMain, boxWidth, boxHeight, justifyOffset, justifyLeftover, mainScrolls ? 0 : justifyLeftover);
    }

    // POSITION ALONG THE MAIN AXIS, PLACING EACH CHILD ON THE CROSS AXIS BY ITS ALIGNMENT
    private void positionChildren(List<Element> shown, int[] main, int[] cross, int count,
                                  boolean vertical, int originX, int originY,
                                  int viewMain, int boxWidth, int boxHeight,
                                  int justifyOffset, int justifyLeftover, int betweenBudget) {
        int crossSpace = vertical ? boxWidth : boxHeight;
        int cursor = (vertical ? originY : originX) + justifyOffset;

        // SCROLL OFFSET SHIFTS THE CONTENT BEHIND THE VIEWPORT
        if (vertical) cursor -= scrollOffsetY;
        else cursor -= scrollOffsetX;
        if (vertical) originX -= scrollOffsetX;
        else originY -= scrollOffsetY;

        for (int i = 0; i < count; i++) {
            Element child = shown.get(i);
            var margin = child.margin();
            int room = crossSpace - (vertical ? margin.horizontal() : margin.vertical());
            int anchor = this.crossAnchor(child, vertical);
            if (vertical) {
                int x = Anchor.x(anchor, originX + margin.left(), room, cross[i]);
                child.layout(x, cursor + margin.top(), cross[i], main[i]);
                cursor += main[i] + margin.vertical() + spacing;
            } else {
                int y = Anchor.y(anchor, originY + margin.top(), room, cross[i]);
                child.layout(cursor + margin.left(), y, main[i], cross[i]);
                cursor += main[i] + margin.horizontal() + spacing;
            }

            // BETWEEN DISTRIBUTES EXTRA GAP EVENLY BETWEEN CHILDREN, NONE AT THE EDGES
            if (justify == Justify.BETWEEN && count > 1 && betweenBudget > 0 && i < count - 1) {
                int gaps = count - 1;
                cursor += betweenBudget * (i + 1) / gaps - betweenBudget * i / gaps;
            }
        }
    }

    // CROSS-AXIS PLACEMENT: CHILD alignMask FIRST, THEN CONTAINER DEFAULT
    private int crossAnchor(Element child, boolean vertical) {
        int crossBits = vertical
                ? Anchor.START | Anchor.END | Anchor.CENTER_H | Anchor.STRETCH_H
                : Anchor.TOP | Anchor.BOTTOM | Anchor.CENTER_V | Anchor.STRETCH_V;

        int childAlign = child.alignMask();
        if ((childAlign & crossBits) != 0) return childAlign;

        if ((containerAlign & crossBits) != 0) return containerAlign;

        // DEFAULT: START ON THE CROSS AXIS
        return vertical ? Anchor.START : Anchor.TOP;
    }

    // ---- SCROLL RENDERING ---------------------------------------------------------------

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        if (!scrollX && !scrollY) {
            super.draw(graphics, mouseX, mouseY, partial);
            return;
        }

        // enableScissor ISN'T POSE-TRANSFORMED IN 1.21.1: PROJECT TO THE OUTER (SCALED) BOX BY HAND
        int right = scale() == 1f ? bounds.right() : bounds.x + Math.round(bounds.width * scale());
        int bottom = scale() == 1f ? bounds.bottom() : bounds.y + Math.round(bounds.height * scale());
        graphics.enableScissor(bounds.x, bounds.y, right, bottom);
        super.draw(graphics, mouseX, mouseY, partial);
        graphics.disableScissor();

        if (scrollY) this.drawScrollbar(graphics, true);
        if (scrollX) this.drawScrollbar(graphics, false);
    }

    private void drawScrollbar(GuiGraphics graphics, boolean vertical) {
        int maxScroll = vertical ? maxScrollY() : maxScrollX();
        if (maxScroll <= 0) return;

        ScrollTheme style = theme().scroll();
        int offset = vertical ? scrollOffsetY : scrollOffsetX;

        // THE RAIL SPANS THE BORDER BOX EDGE-TO-EDGE, IGNORING PADDING
        int border = borderWidth();
        int barX = bounds.x + border;
        int barY = bounds.y + border;
        int barW = Math.max(0, bounds.width - border * 2);
        int barH = Math.max(0, bounds.height - border * 2);

        // WHEN BOTH AXES SCROLL THE CROSS BAR EATS INTO THIS ONE'S RAIL
        int railLength = vertical ? barH : barW;
        if (scrollX && vertical) railLength = Math.max(0, railLength - style.width());
        if (scrollY && !vertical) railLength = Math.max(0, railLength - style.width());

        // THUMB PROPORTIONAL TO THE VIEWPORT/CONTENT RATIO OVER THE RAIL LENGTH
        int viewport = vertical ? contentHeight() : contentWidth();
        if (scrollY && !vertical) viewport = Math.max(0, viewport - style.width());
        if (scrollX && vertical) viewport = Math.max(0, viewport - style.width());
        int contentLen = vertical ? contentLengthY : contentLengthX;
        int thumb = Math.max(8, (int) ((long) railLength * viewport / Math.max(1, contentLen)));
        int thumbOffset = maxScroll > 0 ? (railLength - thumb) * offset / maxScroll : 0;

        if (vertical) {
            int x = barX + barW - style.width();
            style.track().draw(graphics, x, barY, style.width(), railLength);
            style.thumb().draw(graphics, x, barY + thumbOffset, style.width(), thumb);
        } else {
            int y = barY + barH - style.width();
            style.track().draw(graphics, barX, y, railLength, style.width());
            style.thumb().draw(graphics, barX + thumbOffset, y, thumb, style.width());
        }
    }

    // ---- SCROLL INPUT -------------------------------------------------------------------

    @Override
    public boolean scroll(double mouseX, double mouseY, double amount) {
        // CHILDREN FIRST: A NESTED SCROLLABLE OR A WIDGET THAT CONSUMES SCROLL WINS
        if (super.scroll(mouseX, mouseY, amount)) return true;
        if (!scrollX && !scrollY) return false;

        // WHEEL DRIVES VERTICAL WHEN scrollY IS ON, HORIZONTAL WHEN scrollX IS THE ONLY AXIS
        if (scrollY) {
            if (maxScrollY() == 0) return false;
            this.scrollOffsetY = Math.clamp(scrollOffsetY - (int) (amount * 10), 0, maxScrollY());
            this.arrange();
            return true;
        }
        if (maxScrollX() == 0) return false;
        this.scrollOffsetX = Math.clamp(scrollOffsetX - (int) (amount * 10), 0, maxScrollX());
        this.arrange();
        return true;
    }

    @Override
    public boolean mouseDown(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int border = borderWidth();
            int barRight = bounds.x + bounds.width - border;
            int barBottom = bounds.y + bounds.height - border;
            // THUMB DRAG ON THE VERTICAL SCROLLBAR (FLUSH AGAINST THE BORDER, IGNORING PADDING)
            if (scrollY && maxScrollY() > 0 && mouseX >= barRight - theme().scroll().width()) {
                this.draggingY = true;
                this.dragOriginY = mouseY;
                this.dragScrollY = scrollOffsetY;
                return true;
            }
            // THUMB DRAG ON THE HORIZONTAL SCROLLBAR (FLUSH AGAINST THE BORDER, IGNORING PADDING)
            if (scrollX && maxScrollX() > 0 && mouseY >= barBottom - theme().scroll().width()) {
                this.draggingX = true;
                this.dragOriginX = mouseX;
                this.dragScrollX = scrollOffsetX;
                return true;
            }
        }
        return super.mouseDown(mouseX, mouseY, button);
    }

    @Override
    public void mouseMove(double mouseX, double mouseY) {
        if (draggingY) {
            this.thumbDrag(mouseY, dragOriginY, dragScrollY, true);
            return;
        }
        if (draggingX) {
            this.thumbDrag(mouseX, dragOriginX, dragScrollX, false);
            return;
        }
        super.mouseMove(mouseX, mouseY);
    }

    private void thumbDrag(double cursor, double origin, int startScroll, boolean vertical) {
        // RAIL MATCHES THE BORDER BOX, CONSISTENT WITH drawScrollbar
        int border = borderWidth();
        int railLength = vertical
                ? Math.max(0, bounds.height - border * 2)
                : Math.max(0, bounds.width - border * 2);
        if (scrollX && vertical) railLength = Math.max(0, railLength - theme().scroll().width());
        if (scrollY && !vertical) railLength = Math.max(0, railLength - theme().scroll().width());

        int viewport = vertical ? contentHeight() : contentWidth();
        if (scrollY && !vertical) viewport = Math.max(0, viewport - scrollBarWidth());
        if (scrollX && vertical) viewport = Math.max(0, viewport - scrollBarWidth());
        int contentLen = vertical ? contentLengthY : contentLengthX;
        int maxScroll = vertical ? maxScrollY() : maxScrollX();
        int thumb = Math.max(8, (int) ((long) railLength * viewport / Math.max(1, contentLen)));
        int span = Math.max(1, railLength - thumb);
        int next = Math.clamp(startScroll + (int) ((cursor - origin) * maxScroll / span), 0, maxScroll);
        if (vertical) this.scrollOffsetY = next;
        else this.scrollOffsetX = next;
        this.arrange();
    }

    @Override
    public void mouseUp(double mouseX, double mouseY, int button) {
        this.draggingX = false;
        this.draggingY = false;
        super.mouseUp(mouseX, mouseY, button);
    }

    // ---- FILL DISTRIBUTION AND SHRINKING ------------------------------------------------

    // HANDS LEFTOVER TO FILL CHILDREN BY WEIGHT, RETRYING WHAT THE CAPS REJECT
    private void share(List<Element> shown, int[] main, int[] cross, int leftover, int space, boolean vertical) {
        for (int pass = 0; pass < 3 && leftover > 0; pass++) {
            float weights = 0f;
            for (int i = 0; i < shown.size(); i++) {
                Element child = shown.get(i);
                if (expandable(child, vertical) && !capped(child, main[i], cross[i], space, vertical)) {
                    weights += child.weight();
                }
            }
            if (weights <= 0f) return;

            int budget = leftover;
            int last = -1;
            for (int i = 0; i < shown.size() && leftover > 0; i++) {
                Element child = shown.get(i);
                if (!expandable(child, vertical) || capped(child, main[i], cross[i], space, vertical)) continue;
                int share = Math.min(leftover, Math.round(budget * (child.weight() / weights)));
                int before = main[i];
                main[i] = vertical
                        ? child.clampHeight(before + share, cross[i], space)
                        : child.clampWidth(before + share, space);
                leftover -= main[i] - before;
                last = i;
            }

            // ROUNDING LEAVES A PIXEL OR TWO UNCLAIMED WHENEVER THE LEFTOVER DOES NOT DIVIDE
            // EVENLY. THE LAST CHILD THAT GREW TAKES THEM, SO THE ROW ENDS EXACTLY ON ITS EDGE
            if (leftover > 0 && last != -1) {
                Element child = shown.get(last);
                int before = main[last];
                main[last] = vertical
                        ? child.clampHeight(before + leftover, cross[last], space)
                        : child.clampWidth(before + leftover, space);
                leftover -= main[last] - before;
            }
        }
    }

    // TAKES SPACE BACK FROM THE LARGEST CHILDREN UNTIL THE ROW FITS
    private void shrink(List<Element> shown, int[] main, int[] cross, int excess, int space, boolean vertical) {
        for (int pass = 0; pass < 3 && excess > 0; pass++) {
            int shrinkable = 0;
            for (int i = 0; i < shown.size(); i++) {
                Element child = shown.get(i);
                int min = vertical ? child.minHeight(cross[i], space) : child.minWidth(space);
                if (main[i] > min) shrinkable++;
            }
            if (shrinkable == 0) return;

            int each = Math.max(1, excess / shrinkable);
            for (int i = 0; i < shown.size() && excess > 0; i++) {
                Element child = shown.get(i);
                int before = main[i];
                int target = Math.max(0, before - Math.min(each, excess));
                main[i] = vertical
                        ? child.clampHeight(target, cross[i], space)
                        : child.clampWidth(target, space);
                excess -= before - main[i];
            }
        }
    }

    private static boolean expandable(Element child, boolean vertical) {
        return (vertical ? child.height() : child.width()) == Element.FILL;
    }

    private boolean capped(Element child, int size, int cross, int space, boolean vertical) {
        int max = vertical ? child.maxHeight(cross, space) : child.maxWidth(space);
        return max != -1 && size >= max;
    }
}
