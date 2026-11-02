package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.core.Popup;
import me.srrapero720.waterui.theme.Drawable;
import me.srrapero720.waterui.theme.Face;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Closed field showing the selected option, opening a list of text options over every layer on click.
 * Options come from a fixed list of anything printable, an enum class or a live supplier read on every
 * draw; picking one reports its index and keeps it as the local selection unless a live index outranks it.
 */
public class Dropdown extends Element implements Popup {
    // ROWS SHOWN AT ONCE BEFORE THE LIST SCROLLS, AND THE CHEVRON GLYPH BOX
    private static final int MAX_ROWS = 8;
    private static final int CHEVRON = 5;
    private static final int ROW_PAD = 2;

    private Supplier<List<?>> options = List::of;
    // THE LIST AS LAST PULLED FROM THE SUPPLIER: ONCE PER TICK, NOT PER DRAW, SINCE A HOST LIST MAY ALLOCATE
    private List<?> shown = List.of();
    private Component placeholder = Component.empty();
    private float textScale = 1f;
    // LIVE SELECTION WHEN SET; THE LOCAL PICK OTHERWISE (-1 IS NONE)
    private IntSupplier source;
    private int selected = -1;
    private IntConsumer onSelect;

    // OPEN LIST STATE: SCROLL OFFSET IN ROWS AND THE BOX IT WAS LAST PAINTED IN, FOR HIT-TESTING
    private boolean open;
    private int scroll;
    private int listX, listY, listWidth, listHeight, rows;

    public Dropdown() {
        this.face = Face.CLICKABLE;
    }

    /** Fixed options, shown through {@code toString()} ({@link Component}s through their string). */
    public Dropdown options(List<?> options) {
        List<?> fixed = new ArrayList<>(options);
        this.options = () -> fixed;
        this.refresh();
        return this;
    }

    public Dropdown options(Object... options) {
        return this.options(Arrays.asList(options));
    }

    /** Every constant of {@code type}, in declaration order. */
    public Dropdown options(Class<? extends Enum<?>> type) {
        return this.options(Arrays.asList(type.getEnumConstants()));
    }

    /** Live options, pulled once per tick; the selection clamps to the list it sees. */
    public Dropdown source(Supplier<List<?>> options) {
        this.options = options;
        this.refresh();
        return this;
    }

    // A CHANGED LIST REMEASURES, SO THE FIELD ALWAYS FITS ITS LONGEST OPTION
    private void refresh() {
        List<?> fresh = options.get();
        if (fresh == null) fresh = List.of();
        if (fresh.equals(shown)) return;
        this.shown = new ArrayList<>(fresh);
        this.dirty();
    }

    /** Live selected index, read on every draw; while set it outranks the local pick. */
    public Dropdown selected(IntSupplier source) {
        this.source = source;
        return this;
    }

    public Dropdown selected(int index) {
        this.selected = index;
        return this;
    }

    /** Text shown while nothing is selected. */
    public Dropdown placeholder(Component placeholder) {
        this.placeholder = placeholder;
        this.dirty();
        return this;
    }

    public Dropdown textScale(float textScale) {
        this.textScale = textScale;
        this.dirty();
        return this;
    }

    /** Called with the index of the option picked from the list. */
    public Dropdown onSelect(IntConsumer onSelect) {
        this.onSelect = onSelect;
        return this;
    }

    public List<?> options() {
        return shown;
    }

    /** Selected index clamped to the current options, -1 when none or empty. */
    public int selected() {
        int size = this.options().size();
        int index = source != null ? source.getAsInt() : selected;
        return size == 0 || index < 0 ? -1 : Math.min(index, size - 1);
    }

    public boolean open() {
        return open;
    }

    // ---- MEASURE ----------------------------------------------------------------------

    private static String label(Object option) {
        return option instanceof Component c ? c.getString() : String.valueOf(option);
    }

    private int textWidth(String text) {
        return Math.round(font().width(text) * textScale);
    }

    private int rowHeight() {
        return Math.round(GLYPH_HEIGHT * textScale) + ROW_PAD * 2;
    }

    @Override
    protected int prefContentWidth(int available) {
        // WIDE ENOUGH FOR THE LONGEST OPTION SO THE FIELD NEVER RESIZES AS THE PICK CHANGES
        int widest = textWidth(placeholder.getString());
        for (Object option: this.options()) widest = Math.max(widest, textWidth(label(option)));
        return widest + ROW_PAD + CHEVRON;
    }

    @Override
    protected int prefContentHeight(int width, int available) {
        return Math.round(font().lineHeight * textScale);
    }

    // ---- FIELD -------------------------------------------------------------------------

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        int index = this.selected();
        String text = index < 0 ? placeholder.getString() : label(this.options().get(index));
        int color = index < 0 && enabled ? theme().textDisabled() : textColor();
        int room = contentWidth() - CHEVRON - ROW_PAD;
        this.drawText(graphics, font().plainSubstrByWidth(text, Math.max(0, Math.round(room / textScale))), contentX(), contentY(), contentHeight(), color);
        this.drawChevron(graphics, contentX() + contentWidth() - CHEVRON, contentY() + (contentHeight() - 3) / 2, open);
    }

    // TEXT CENTRED ON THE ROW HEIGHT, SCALED THROUGH THE POSE WHEN NOT 1:1
    private void drawText(GuiGraphics graphics, String text, int x, int y, int height, int color) {
        int glyph = Math.round(GLYPH_HEIGHT * textScale);
        int top = y + Math.max(0, (height - glyph + 1) / 2);
        if (textScale == 1f) {
            graphics.drawString(font(), text, x, top, color, shadow());
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(x, top, 0);
        graphics.pose().scale(textScale, textScale, 1f);
        graphics.drawString(font(), text, 0, 0, color, shadow());
        graphics.pose().popPose();
    }

    // 5x3 TRIANGLE OUT OF FILLS: NO ATLAS AND NO FONT GLYPH DEPENDENCY
    private void drawChevron(GuiGraphics graphics, int x, int y, boolean up) {
        int color = textColor();
        for (int row = 0; row < 3; row++) {
            int inset = up ? 2 - row : row;
            graphics.fill(x + inset, y + row, x + CHEVRON - inset, y + row + 1, color);
        }
    }

    @Override
    public boolean mouseDown(double mouseX, double mouseY, int button) {
        if (!enabled || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        // AN OPEN LIST NEVER GETS HERE: THE SCREEN ROUTES THE PRESS TO THE POPUP FIRST
        if (this.options().isEmpty()) return true;
        playClick();
        this.open = true;
        // START THE LIST ON THE SELECTION SO A LONG LIST OPENS WHERE THE PICK IS
        int index = this.selected();
        this.scroll = index < 0 ? 0 : Math.max(0, Math.min(index - MAX_ROWS / 2, this.options().size() - MAX_ROWS));
        this.openPopup(this);
        return true;
    }

    @Override
    public void tick() {
        this.refresh();
        // THE FIELD CAN VANISH UNDER ITS OWN LIST (TAB SWITCH, LIVE visible/enabled): THE LIST GOES WITH IT
        if (open && (!visible || hidden || !enabled)) this.closePopup(this);
    }

    @Override
    public void dispose() {
        if (open) this.closePopup(this);
    }

    // ---- LIST (POPUP) ------------------------------------------------------------------

    @Override
    public void paintPopup(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        List<?> list = this.options();
        if (list.isEmpty()) {
            this.closePopup(this);
            return;
        }
        int rowHeight = this.rowHeight();
        int border = this.borderWidth();
        this.rows = Math.min(MAX_ROWS, list.size());
        this.scroll = Math.max(0, Math.min(scroll, list.size() - rows));
        this.listWidth = bounds.width;
        for (Object option: list) this.listWidth = Math.max(listWidth, textWidth(label(option)) + (border + ROW_PAD) * 2);
        this.listHeight = rows * rowHeight + border * 2;
        this.listX = bounds.x;
        // BELOW THE FIELD, OR ABOVE IT WHEN THE WINDOW HAS NO ROOM BELOW
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        this.listY = bounds.bottom() + listHeight <= screenHeight || bounds.y - listHeight < 0 ? bounds.bottom() : bounds.y - listHeight;

        Drawable outline = this.borderDisplay();
        if (outline != null) outline.draw(graphics, listX, listY, listWidth, listHeight);
        theme().field(false).draw(graphics, listX + border, listY + border, listWidth - border * 2, listHeight - border * 2);

        int selected = this.selected();
        int hover = this.rowAt(mouseX, mouseY);
        for (int row = 0; row < rows; row++) {
            int index = scroll + row;
            int y = listY + border + row * rowHeight;
            // THE PICK KEEPS THE SELECTION TINT, THE CURSOR THE HOVER ONE; HOVER WINS ON THE SAME ROW
            if (index == hover) theme().fieldHover().draw(graphics, listX + border, y, listWidth - border * 2, rowHeight);
            else if (index == selected) theme().selection().draw(graphics, listX + border, y, listWidth - border * 2, rowHeight);
            this.drawText(graphics, label(list.get(index)), listX + border + ROW_PAD, y, rowHeight, theme().text());
        }
        // SCROLL HINTS: A CHEVRON AT THE EDGE WITH MORE ROWS PAST IT
        int hintX = listX + listWidth - border - CHEVRON - 1;
        if (scroll > 0) this.drawChevron(graphics, hintX, listY + border + 1, true);
        if (scroll + rows < list.size()) this.drawChevron(graphics, hintX, listY + listHeight - border - 4, false);
    }

    // ROW INDEX UNDER A SCREEN POINT, -1 OUTSIDE THE LIST
    private int rowAt(double x, double y) {
        int border = this.borderWidth();
        if (x < listX + border || x >= listX + listWidth - border || y < listY + border || y >= listY + listHeight - border) return -1;
        int row = (int) ((y - listY - border) / this.rowHeight());
        return row < rows ? scroll + row : -1;
    }

    @Override
    public boolean pressPopup(double mouseX, double mouseY, int button) {
        int index = this.rowAt(mouseX, mouseY);
        if (index < 0) return false;
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return true;
        playClick();
        this.selected = index;
        this.closePopup(this);
        if (onSelect != null) onSelect.accept(index);
        return true;
    }

    @Override
    public boolean scrollPopup(double mouseX, double mouseY, double amount) {
        if (mouseX < listX || mouseX >= listX + listWidth || mouseY < listY || mouseY >= listY + listHeight) return false;
        this.scroll = Math.max(0, Math.min(scroll - (int) Math.signum(amount), this.options().size() - rows));
        return true;
    }

    @Override
    public void closedPopup() {
        this.open = false;
    }
}
