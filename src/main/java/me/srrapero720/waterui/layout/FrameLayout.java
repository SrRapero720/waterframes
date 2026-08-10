package me.srrapero720.waterui.layout;

import me.srrapero720.waterui.core.Anchor;
import me.srrapero720.waterui.core.Spacing;
import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.core.AbstractParent;

/** Stacks every child in the same rectangle, placed by its own anchor. Used for overlays. */
public class FrameLayout extends AbstractParent {

    @Override
    protected int prefContentWidth(int available) {
        int width = 0;
        for (Element child: children) {
            if (child.visible) width = Math.max(width, child.layoutWidth(available) + child.margin().horizontal());
        }
        return width;
    }

    @Override
    protected int prefContentHeight(int width, int available) {
        int height = 0;
        for (Element child: children) {
            if (child.visible) height = Math.max(height, child.layoutHeight(width, available) + child.margin().vertical());
        }
        return height;
    }

    @Override
    protected int minContentWidth(int available) {
        int width = 0;
        for (Element child: children) {
            if (child.visible) width = Math.max(width, child.minWidth(available) + child.margin().horizontal());
        }
        return width;
    }

    @Override
    protected int minContentHeight(int width, int available) {
        int height = 0;
        for (Element child: children) {
            if (child.visible) height = Math.max(height, child.minHeight(width, available) + child.margin().vertical());
        }
        return height;
    }

    @Override
    protected void arrange() {
        int x = contentX();
        int y = contentY();
        int width = contentWidth();
        int height = contentHeight();

        for (Element child: children) {
            if (!child.visible) continue;
            Spacing margin = child.margin();
            int anchor = child.anchor();
            int roomX = width - margin.horizontal();
            int roomY = height - margin.vertical();

            int childWidth = (anchor & Anchor.STRETCH_H) != 0 || child.width() == Element.FILL
                    ? roomX : Math.min(roomX, child.layoutWidth(roomX));
            int childHeight = (anchor & Anchor.STRETCH_V) != 0 || child.height() == Element.FILL
                    ? roomY : Math.min(roomY, child.layoutHeight(childWidth, roomY));

            child.layout(Anchor.x(anchor, x + margin.left(), roomX, childWidth),
                    Anchor.y(anchor, y + margin.top(), roomY, childHeight), childWidth, childHeight);
        }
    }
}
