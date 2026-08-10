package me.srrapero720.waterui.layout;

import me.srrapero720.waterui.core.Anchor;
import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.core.AbstractParent;
import me.srrapero720.waterui.core.Spacing;

import java.util.ArrayList;
import java.util.List;

/**
 * Cell lattice: children fill cells left-to-right, top-to-bottom. Each cell is free 2D space,
 * so the child places itself with its {@code anchor()} mask.
 */
public class Grid extends AbstractParent {
    private int columns = 1;
    private int rows;
    private int spacing;
    private Justify justify = Justify.START;

    public Grid() {}

    public Grid columns(int columns) {
        this.columns = Math.max(1, columns);
        return this;
    }

    public Grid rows(int rows) {
        this.rows = Math.max(0, rows);
        return this;
    }

    public Grid spacing(int spacing) {
        this.spacing = spacing;
        return this;
    }

    public Grid justify(Justify justify) {
        this.justify = justify;
        return this;
    }

    public int columns() { return columns; }
    public int rows() { return rows; }
    public int spacing() { return spacing; }
    public Justify justify() { return justify; }

    private int rowCount() {
        List<Element> shown = this.shown();
        if (rows > 0) return rows;
        return Math.max(1, (shown.size() + columns - 1) / columns);
    }

    private List<Element> shown() {
        List<Element> shown = new ArrayList<>(children.size());
        for (Element child: children) if (child.visible) shown.add(child);
        return shown;
    }

    @Override
    protected int prefContentWidth(int available) {
        List<Element> shown = this.shown();
        if (shown.isEmpty()) return 0;
        int cellWidth = widestCell(shown, available);
        return cellWidth * columns + Math.max(0, columns - 1) * spacing;
    }

    @Override
    protected int prefContentHeight(int width, int available) {
        List<Element> shown = this.shown();
        if (shown.isEmpty()) return 0;
        int cellWidth = cellWidth(width);
        int[] rowHeights = rowHeights(shown, cellWidth, available);
        int total = 0;
        for (int h: rowHeights) total += h;
        return total + Math.max(0, rowHeights.length - 1) * spacing;
    }

    @Override
    protected int minContentWidth(int available) {
        return prefContentWidth(available);
    }

    @Override
    protected int minContentHeight(int width, int available) {
        return prefContentHeight(width, available);
    }

    // CELL WIDTH FROM THE CONTENT BOX WIDTH, DEDUCTING SPACING BETWEEN COLUMNS
    private int cellWidth(int boxWidth) {
        return Math.max(0, (boxWidth - Math.max(0, columns - 1) * spacing) / columns);
    }

    // WIDEST PREFERRED CELL ACROSS ALL CHILDREN, USED FOR PREFERRED WIDTH MEASUREMENT
    private int widestCell(List<Element> shown, int available) {
        int widest = 0;
        for (Element child: shown) {
            widest = Math.max(widest, child.layoutWidth(available) + child.margin().horizontal());
        }
        return widest;
    }

    // HEIGHT OF EACH ROW: TALLEST CELL IN THAT ROW
    private int[] rowHeights(List<Element> shown, int cellWidth, int available) {
        int needed = Math.max(1, (shown.size() + columns - 1) / columns);
        int rCount = rows > 0 ? Math.max(rows, needed) : needed;
        int[] heights = new int[rCount];
        for (int i = 0; i < shown.size() && i / columns < rCount; i++) {
            Element child = shown.get(i);
            int row = i / columns;
            Spacing margin = child.margin();
            int innerW = Math.max(0, cellWidth - margin.horizontal());
            int h = child.layoutHeight(innerW, available) + margin.vertical();
            heights[row] = Math.max(heights[row], h);
        }
        return heights;
    }

    @Override
    protected void arrange() {
        List<Element> shown = this.shown();
        if (shown.isEmpty()) return;

        int originX = contentX();
        int originY = contentY();
        int boxWidth = contentWidth();
        int boxHeight = contentHeight();
        int cellW = cellWidth(boxWidth);
        // GROW TO FIT: AN EXPLICIT rows IS A MINIMUM, NOT A CAP — EXTRA CHILDREN GET THEIR OWN ROWS
        int needed = Math.max(1, (shown.size() + columns - 1) / columns);
        int rCount = rows > 0 ? Math.max(rows, needed) : needed;
        int[] rowH = rowHeights(shown, cellW, boxHeight);

        // TOTAL HEIGHT USED BY ROWS + SPACING
        int totalH = 0;
        for (int h: rowH) totalH += h;
        totalH += Math.max(0, rCount - 1) * spacing;

        // JUSTIFY: DISTRIBUTE LEFTOVER HEIGHT AMONG ROWS
        int leftover = Math.max(0, boxHeight - totalH);
        int justifyOffset = 0;
        if (leftover > 0) {
            justifyOffset = switch (justify) {
                case CENTER -> leftover / 2;
                case END -> leftover;
                default -> 0;
            };
        }

        int cursorY = originY + justifyOffset;
        for (int row = 0; row < rCount; row++) {
            // BETWEEN GAP FOR THIS ROW
            if (justify == Justify.BETWEEN && rCount > 1 && leftover > 0 && row > 0) {
                cursorY += leftover * row / (rCount - 1) - leftover * (row - 1) / (rCount - 1);
            }

            int cursorX = originX;
            for (int col = 0; col < columns; col++) {
                int idx = row * columns + col;
                if (idx >= shown.size()) break;

                Element child = shown.get(idx);
                Spacing margin = child.margin();
                int anchor = child.anchor();

                int roomX = cellW - margin.horizontal();
                int roomY = rowH[row] - margin.vertical();

                // INSIDE A CELL THE CHILD PLACES BY ITS ANCHOR MASK (FREE 2D SPACE)
                int childWidth = (anchor & Anchor.STRETCH_H) != 0 || child.width() == Element.FILL
                        ? roomX : Math.min(roomX, child.layoutWidth(roomX));
                int childHeight = (anchor & Anchor.STRETCH_V) != 0 || child.height() == Element.FILL
                        ? roomY : Math.min(roomY, child.layoutHeight(childWidth, roomY));

                child.layout(
                        Anchor.x(anchor, cursorX + margin.left(), roomX, childWidth),
                        Anchor.y(anchor, cursorY + margin.top(), roomY, childHeight),
                        childWidth, childHeight);

                cursorX += cellW + spacing;
            }
            cursorY += rowH[row] + spacing;
        }
    }
}
