package me.srrapero720.waterui.core;

import me.srrapero720.waterui.WaterUI;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.ArrayList;
import java.util.List;

/** An element that owns children. Subclasses only decide how to measure and place them. */
public abstract class AbstractParent extends Element {
    private static final Marker IT = MarkerManager.getMarker(AbstractParent.class.getSimpleName());
    protected final List<Element> children = new ArrayList<>();

    // CHILD THAT CONSUMED THE LAST PRESS; MOVES AND THE RELEASE ROUTE TO IT ALONE
    private Element captured;

    public AbstractParent add(Element child) {
        child.parent = this; // IMPLICIT NPE CHECK
        this.children.add(child);
        // A NEW CHILD INVALIDATES THE ANCESTOR MEASURE CACHES AND QUEUES THE REFLOW (M2)
        this.dirty();
        return this;
    }

    // clear/remove DO NOT CASCADE dispose: HOST-CACHED ELEMENTS (ParentList ROWS AND THEIR
    // STORES) OWN THEIR LIFECYCLE ACROSS REBUILDS AND DISPOSE THEMSELVES WHEN THE HOST DROPS THEM
    public void clear() {
        for (Element child: children) child.parent = null;
        this.children.clear();
        this.captured = null;
        this.dirty();
    }

    @Override
    void measureDirty() {
        super.measureDirty();
        for (Element child: children) child.measureDirty();
    }

    /** Drops the press capture of this subtree, for a host-cached branch whose release can no longer arrive. */
    public void dropCapture() {
        this.captured = null;
        for (Element child: children) if (child instanceof AbstractParent parent) parent.dropCapture();
    }

    // SEE clear(): DETACH ONLY, NO DISPOSE
    public void remove(Element child) {
        child.parent = null;
        this.children.remove(child);
        if (captured == child) this.captured = null;
        this.dirty();
    }

    @Override
    public void dispose() {
        if (!children.isEmpty()) WaterUI.LOGGER.trace(IT, "disposing {} child element(s)", children.size());
        for (Element child: children) child.dispose();
    }

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        for (Element child: children) child.render(graphics, mouseX, mouseY, partial);
    }

    // THE CONSUMING CHILD CAPTURES THE POINTER: ITS DRAG AND ITS RELEASE STAY ITS OWN INSTEAD
    // OF BROADCASTING, SO A PRESS ON ONE SLIDER NEVER ENDS OR FEEDS A SIBLING'S DRAG
    @Override
    public boolean mouseDown(double mouseX, double mouseY, int button) {
        for (int i = children.size() - 1; i >= 0; i--) {
            Element child = children.get(i);
            if (child.hovered(mouseX, mouseY) && child.press(child.toLocalX(mouseX), child.toLocalY(mouseY), button)) {
                this.captured = child;
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseUp(double mouseX, double mouseY, int button) {
        // CLEARED BEFORE DELIVERY: THE RELEASE MAY REBUILD THE TREE UNDER THIS VERY PARENT
        Element target = captured;
        this.captured = null;
        if (target != null) target.release(target.toLocalX(mouseX), target.toLocalY(mouseY), button);
    }

    @Override
    public void mouseMove(double mouseX, double mouseY) {
        if (captured != null) {
            captured.move(captured.toLocalX(mouseX), captured.toLocalY(mouseY));
            return;
        }
        for (Element child: children) child.move(child.toLocalX(mouseX), child.toLocalY(mouseY));
    }

    @Override
    public boolean scroll(double mouseX, double mouseY, double amount) {
        for (int i = children.size() - 1; i >= 0; i--) {
            Element child = children.get(i);
            if (child.hovered(mouseX, mouseY) && child.wheel(child.toLocalX(mouseX), child.toLocalY(mouseY), amount)) return true;
        }
        return false;
    }

    @Override
    public void tick() {
        for (Element child: children) {
            if (!child.visible) continue;
            child.tick();
        }
    }

    /** Deepest visible element under the cursor, used for tooltips and focus. */
    public Element pick(double mouseX, double mouseY) {
        for (int i = children.size() - 1; i >= 0; i--) {
            Element child = children.get(i);
            if (!child.hovered(mouseX, mouseY)) continue;
            if (child instanceof AbstractParent group) {
                Element deeper = group.pick(child.toLocalX(mouseX), child.toLocalY(mouseY));
                if (deeper != null) return deeper;
            }
            return child;
        }
        return null;
    }

    /** Direct children are matched before descending, so a shallow id wins over a deeper duplicate. */
    public Element get(ResourceLocation id) {
        for (Element child: children) {
            if (id.equals(child.id())) return child;
        }
        for (Element child: children) {
            if (child instanceof AbstractParent group) {
                Element found = group.get(id);
                if (found != null) return found;
            }
        }
        return null;
    }

    /** Deepest visible element under the cursor that accepts file drops. */
    public Element pickForFileDrop(double mouseX, double mouseY) {
        for (int i = children.size() - 1; i >= 0; i--) {
            Element child = children.get(i);
            if (!child.hovered(mouseX, mouseY)) continue;
            if (child instanceof AbstractParent group) {
                Element deeper = group.pickForFileDrop(child.toLocalX(mouseX), child.toLocalY(mouseY));
                if (deeper != null) return deeper;
            }
            if (child.acceptsFileDrop()) return child;
        }
        return null;
    }

    /** Moves an attached child to {@code index} without detaching it, so capture and state survive. */
    public void reorder(Element child, int index) {
        if (!children.remove(child)) return;
        this.children.add(Math.clamp(index, 0, children.size()), child);
        this.dirty();
    }
}
