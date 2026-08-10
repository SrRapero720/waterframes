package me.srrapero720.waterui.layout;

import me.srrapero720.waterui.WaterUI;
import me.srrapero720.waterui.core.DragThumb;
import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.format.DataContext;
import me.srrapero720.waterui.format.UIBuilder;
import me.srrapero720.waterui.format.UIContract;
import me.srrapero720.waterui.format.UIDocument;
import me.srrapero720.waterui.format.UILoader;
import me.srrapero720.waterui.theme.Drawable;
import me.srrapero720.waterui.theme.Face;
import me.srrapero720.waterui.widget.Text;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;

/**
 * A vertical list whose rows are inflated from a shared {@code .ui} template, one per submitted key
 * (UI-SPEC.md §10). The template is a LAZY document path, not an import: its per-row {@code var}s never
 * aggregate into the screen's contract (§7). It is parsed and its {@link UIContract.Skeleton} baked ONCE
 * when the list binds; each row then gets its own {@link UIContract} over that skeleton plus a
 * {@link DataContext} whose host is the row model, while events resolve against the screen-level host (§12).
 * <p>
 * {@link #submit(List)} diffs by key equality: a known key reuses its live row, a new key inflates one, a
 * dropped key is disposed, then the rows are reordered to match — moved, never recreated, so capture state
 * and media players survive. The row cache is an opaque {@link RowStore} the host owns, so a fresh list
 * instance re-adopts the surviving rows across a screen re-inflate; the list is never a panel disposable.
 * <p>
 * Drag-to-reorder is list-owned (§10.6): the template's {@code dragThumbId} carrier runs the session,
 * {@code groupBy} makes token-sharing rows travel as one block, and the drop notifies the host through
 * {@code onListMove}/{@code onListSwap}/{@code onListDelete} in row-index space.
 */
public class ParentList extends ParentLinear {
    private static final Marker IT = MarkerManager.getMarker(ParentList.class.getSimpleName());
    // CURSOR DISTANCE PAST THE CONTENT SIDES THAT ARMS THE DRAG-OUT DELETE, WHEN ENABLED
    private static final int OUT_ZONE = 20;

    /** Drop semantics of the integrated drag (§10.6): displace the other rows, or exchange with the target. */
    public enum Reorder { MOVE, SWAP }

    /** A two-index list notification: a move's {@code (origin, target)} or a swap's pair (§10.6). */
    public interface MoveListener { void moved(int origin, int target); }

    private ResourceLocation entryDoc;
    private Object eventHost;
    private RowBinder binder;

    // DECLARABLE DRAG BEHAVIOUR (§10.6): BLOCK TOKEN SOURCE, DROP SEMANTICS AND THE DRAG-OUT DELETE GATE
    private String groupVar;
    private Reorder reorder = Reorder.MOVE;
    private boolean dragOut;

    // DECLARABLE LIST NOTIFICATIONS (§10.6): STRUCTURAL CHANGES THE ELEMENT INITIATES, INDEX-BASED
    private MoveListener onListMove;
    private MoveListener onListSwap;
    private IntConsumer onListDelete;

    // LIVE DRAG SESSION (§10.6): THE LIST OWNS GESTURE, OVERLAY, GAP AND AUTO-SCROLL; THE DROP NOTIFIES
    private ListRow dragRow;
    private int dragOrigin;
    private double dragX, dragY;
    private int scrollDir;

    // COMPILED ONCE WHEN THE LIST FIRST BINDS ITS TEMPLATE; broken LATCHES A BAD TEMPLATE SO IT NEVER RETRIES (§13.4)
    private UIDocument template;
    private UIContract.Skeleton skeleton;
    private boolean broken;

    private RowStore store = new RowStore();

    public ParentList() {
        super(Orientation.VERTICAL);
    }

    // ---- BINDING API -------------------------------------------------------------------

    /** Sets the LAZY template document path; recompiled on the next {@link #submit(List)} (§10). */
    public ParentList entry(ResourceLocation entryDoc) {
        this.entryDoc = entryDoc;
        this.skeleton = null;
        this.template = null;
        this.broken = false;
        return this;
    }

    /** The host object row events resolve against; defaults to the screen host at inflate (§12). */
    public ParentList eventHost(Object eventHost) {
        this.eventHost = eventHost;
        return this;
    }

    /** Supplies each row's baked values and model object, keyed by the submitted key (§10). */
    public ParentList binder(RowBinder binder) {
        this.binder = binder;
        return this;
    }

    /** Names the baked-map key whose value groups rows into drag blocks; token-sharing neighbours travel whole (§10.6). */
    public ParentList groupBy(String bakedKey) {
        this.groupVar = bakedKey;
        return this;
    }

    /** Drop semantics of the integrated drag: {@code MOVE} displaces (default), {@code SWAP} exchanges (§10.6). */
    public ParentList reorder(Reorder reorder) {
        this.reorder = reorder;
        return this;
    }

    /** Enables drag-out delete: dropping a row past the list's sides removes it and fires {@code onListDelete} (§10.6). */
    public ParentList dragOut(boolean dragOut) {
        this.dragOut = dragOut;
        return this;
    }

    /** Fired on a drop that left the dragged block at a new index: {@code (origin, target)} in row space (§10.6). */
    public ParentList onListMove(MoveListener listener) {
        this.onListMove = listener;
        return this;
    }

    /** Fired when two blocks exchanged positions, by a SWAP drop or {@link #swap(int, int)} (§10.6). */
    public ParentList onListSwap(MoveListener listener) {
        this.onListSwap = listener;
        return this;
    }

    /** Fired when a block was removed, by a drag-out drop or {@link #delete(int)} (§10.6). */
    public ParentList onListDelete(IntConsumer listener) {
        this.onListDelete = listener;
        return this;
    }

    /** The opaque row cache; the host keeps it across a screen re-inflate and re-adopts it with {@link #rows(RowStore)}. */
    public RowStore rows() {
        return store;
    }

    /** The live row element for {@code key}, or null: the host reads its bounds or dresses it through {@link ListRow#element}. */
    public ListRow row(Object key) {
        return store.rows.get(key);
    }

    /** The row at visual {@code index}, or null; with {@link ListRow#element(String)} the entry API replaces global ids. */
    public ListRow row(int index) {
        return index >= 0 && index < children.size() && children.get(index) instanceof ListRow row ? row : null;
    }

    /** The submitted keys in current visual order; the host mirrors its data from this after a move (§10.6). */
    public List<Object> order() {
        List<Object> keys = new ArrayList<>(children.size());
        for (Element child: children) if (child instanceof ListRow row) keys.add(row.key);
        return keys;
    }

    /** Whether a drag session is live; hosts gate list-rebuilding work on it (§10.6). */
    public boolean dragging() {
        return dragRow != null;
    }

    // TEMPLATE IDS REPEAT ACROSS ROWS, SO THEY STAY ENCAPSULATED (§10.6): A SCREEN-LEVEL LOOKUP
    // NEVER DESCENDS INTO THE ROWS; ENTRY ELEMENTS RESOLVE THROUGH row(...).element(...) INSTEAD
    @Override
    public Element get(ResourceLocation id) {
        return null;
    }

    /** Re-adopts a surviving cache: the rows keep their contexts, players and disposables; a {@link #submit(List)} re-attaches them. */
    public void rows(RowStore store) {
        this.store = store != null ? store : new RowStore();
        // A RE-INIT MAY HAVE CUT A DRAG SHORT: DROP STALE VISUAL, hidden AND CAPTURE STATE ON THE ADOPTED ROWS
        for (ListRow row: this.store.rows.values()) {
            row.dragging = false;
            row.hidden = false;
            row.dropCapture();
            if (row.thumb != null) row.thumb.reset();
        }
    }

    // ---- SURFACE STATE (§9): SINGLE SELECTION / SINGLE PLAYING ROW ----------------------

    /** Marks {@code key}'s row as selected (accent border) and clears the rest; null clears all. */
    public void selected(Object key) {
        for (Map.Entry<Object, ListRow> e: store.rows.entrySet()) e.getValue().selected = key != null && key.equals(e.getKey());
    }

    /** Marks {@code key}'s row as the one on air (selection face) and clears the rest; null clears all. */
    public void playing(Object key) {
        for (Map.Entry<Object, ListRow> e: store.rows.entrySet()) e.getValue().playing = key != null && key.equals(e.getKey());
    }

    // ---- DIFFING ROWS AGAINST THE SUBMITTED KEYS ---------------------------------------

    /**
     * Reconciles the rows to {@code keys} by key equality: reuse a known row, inflate a new one, dispose a
     * dropped one, then reorder to the submitted order. Rows MOVE, never rebuild, so players and capture state
     * survive. A broken template shows one error row and no-ops; a row that fails to build is dropped alone.
     */
    public void submit(List<?> keys) {
        if (!this.ensureTemplate()) return;

        // INFLATE ROWS FOR NEW KEYS; A PER-ROW FAILURE DROPS ONLY THAT ROW (§13.4)
        for (Object key: keys) {
            if (store.rows.containsKey(key)) continue;
            ListRow row = this.buildRow(key);
            if (row != null) store.rows.put(key, row);
        }

        // DISPOSE ROWS WHOSE KEY LEFT THE SUBMISSION, DETACHING THEM FROM THE TREE FIRST (SET LOOKUP, M3)
        Set<Object> keep = new HashSet<>(keys);
        var it = store.rows.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, ListRow> e = it.next();
            if (keep.contains(e.getKey())) continue;
            ListRow row = e.getValue();
            if (row == dragRow) this.abandon();
            if (row.parent() == this) this.remove(row);
            row.disposeRow();
            it.remove();
        }

        // ATTACH ANY NEW OR RE-ADOPTED ROW, THEN MOVE IT TO ITS SUBMITTED SLOT ONLY WHEN IT DRIFTED (M3)
        for (int i = 0; i < keys.size(); i++) {
            ListRow row = store.rows.get(keys.get(i));
            if (row == null) continue;
            if (row.parent() != this) this.add(row);
            if (children.indexOf(row) != i) this.reorder(row, i);
        }
    }

    // PARSES AND VALIDATES THE TEMPLATE ONCE (§10); ON FAILURE LATCHES broken AND SHOWS ONE ERROR ROW (§13.4)
    private boolean ensureTemplate() {
        if (skeleton != null) return true;
        if (broken) return false;
        if (entryDoc == null) { this.markBroken("no template document set"); return false; }
        try {
            UIDocument doc = UILoader.document(entryDoc);
            if (doc == null) { this.markBroken("template not found: " + entryDoc); return false; }
            // SKELETON WALKS THE IMPORT CHAIN AND VALIDATES JAVA IMPORTS AND CYCLES A SINGLE TIME
            UIContract.Skeleton baked = UIContract.skeleton(doc);
            this.template = doc;
            this.skeleton = baked;
            WaterUI.LOGGER.debug(IT, "template {} compiled", entryDoc);
            return true;
        } catch (RuntimeException ex) {
            this.markBroken(String.valueOf(ex.getMessage()));
            return false;
        }
    }

    // ONE RED ROW INSIDE THE LIST'S BOUNDS PLUS ONE LOG LINE, MATCHING THE §13.4 SURFACE; THE LIST SURVIVES.
    // SURVIVING ROWS ARE DISPOSED HERE: A BROKEN RE-INIT MUST NOT KEEP ORPHAN MEDIA SESSIONS ALIVE (L1)
    private void markBroken(String message) {
        this.broken = true;
        this.abandon();
        WaterUI.LOGGER.error(IT, "ParentList template '{}' is broken: {}", entryDoc, message);
        this.clear();
        store.dispose();
        Text error = new Text().text(Component.literal("LIST ERROR: " + message));
        error.color = 0xFFFF5555;
        this.add(error);
    }

    // BUILDS ONE ROW OVER THE SHARED SKELETON: A FRESH CONTRACT (BAKED VALUES) AND A FRESH CONTEXT (MODEL HOST)
    private ListRow buildRow(Object key) {
        if (broken) return null;
        try {
            Map<String, Object> baked = binder != null ? binder.baked(key) : Map.of();
            Object model = binder != null ? binder.model(key) : null;
            UIContract contract = UIContract.of(skeleton, template.source(), baked);
            ListRow row = new ListRow(key);
            row.ns = template.source().getNamespace();
            row.group = groupVar != null ? baked.get(groupVar) : null;
            row.data.host(model);
            Element carrier = UIBuilder.inflateRow(template, row, contract, row.data, eventHost, row::disposable);
            // §10.1 BINDS THE TEMPLATE TOO (L9): FILL ON THE SCROLLED AXIS WOULD SIZE AN UNBOUNDED ROW
            if ((scrollsY() && row.height() == FILL) || (scrollsX() && row.width() == FILL)) {
                this.markBroken("row template declares FILL on the scrolled axis");
                return null;
            }
            if (carrier != null) attachThumb(row, carrier);
            return row;
        } catch (RuntimeException ex) {
            WaterUI.LOGGER.error(IT, "ParentList row '{}' failed to build: {}", key, ex.getMessage());
            return null;
        }
    }

    // ---- INTEGRATED DRAG (§10.6): SESSION DRIVEN BY THE TEMPLATE'S dragThumbId CARRIER --------

    // WEARS THE THUMB ON THE DECLARED CARRIER; A SHORT TAP FIRES THE CARRIER'S OWN DECLARED onClick
    // (E.G. SELECT). GRAB/SLIDE/DROP ROUTE THROUGH THE ROW'S CURRENT LIST, NEVER A CAPTURED ONE:
    // A RE-ADOPTED ROW (RE-INIT) MUST DRIVE THE LIVE LIST, NOT THE DETACHED ONE THAT BUILT IT
    private static void attachThumb(ListRow row, Element carrier) {
        DragThumb thumb = new DragThumb()
                .onGrab((x, y) -> { if (row.parent() instanceof ParentList list) list.grab(row, x, y); })
                .onSlide((x, y) -> { if (row.parent() instanceof ParentList list) list.slide(x, y); })
                .onDrop(() -> { if (row.parent() instanceof ParentList list) list.drop(); })
                .onTap(carrier::performClick);
        carrier.thumb(thumb);
        row.thumb = thumb;
    }

    private void grab(ListRow row, double x, double y) {
        this.dragRow = row;
        this.dragOrigin = this.blockStart(children.indexOf(row));
        this.dragX = x;
        this.dragY = y;
        row.dragging = true;
        row.hidden = true;
        // FLOATING COPY AT THE CURSOR; THE HIDDEN SELF KEEPS THE GAP THAT RELOCATES WHILE SLIDING
        this.floatDragged(row, (int) (x - row.bounds.x), (int) (y - row.bounds.y));
    }

    private void slide(double x, double y) {
        if (dragRow == null) return;
        this.dragX = x;
        this.dragY = y;
        this.retarget(y);
        // AUTO-SCROLL DIRECTION WHILE THE CURSOR SITS NEAR AN EDGE; APPLIED ON THE TICK
        int edge = 15;
        int top = contentY();
        int bottom = top + contentHeight();
        this.scrollDir = y < top + edge ? -1 : y > bottom - edge ? 1 : 0;
    }

    // THE GAP RELOCATES LIVE IN MOVE MODE: THE DRAGGED BLOCK SLIDES TO THE SLOT COUNTED BY THE ROW
    // MIDPOINTS ABOVE THE CURSOR. VISUAL ONLY — DATA MOVES WHEN THE DROP NOTIFIES AND THE HOST MIRRORS
    private void retarget(double mouseY) {
        if (reorder == Reorder.SWAP || this.outZone()) return;
        int start = this.blockStart(children.indexOf(dragRow));
        List<Element> block = new ArrayList<>(children.subList(start, this.blockEnd(start)));
        List<Element> order = new ArrayList<>(children);
        order.removeAll(block);
        int slot = 0;
        for (Element child: order) {
            if (!child.visible) continue;
            if (mouseY > child.bounds.y + child.bounds.height / 2.0) slot++;
        }
        // SNAP FORWARD PAST A SPLIT: A SLOT LANDING INSIDE ANOTHER BLOCK'S RUN WOULD TEAR IT APART
        while (slot > 0 && slot < order.size()
                && order.get(slot - 1) instanceof ListRow before && before.group != null
                && order.get(slot) instanceof ListRow after && before.group.equals(after.group)) slot++;
        if (slot == start) return;
        order.addAll(Math.min(slot, order.size()), block);
        this.arrange(order);
    }

    private void drop() {
        ListRow row = this.dragRow;
        if (row == null) return;
        this.dragRow = null;
        this.scrollDir = 0;
        row.dragging = false;
        row.hidden = false;
        this.floatDragged(null, 0, 0);

        if (dragOut && this.outZone()) {
            this.delete(children.indexOf(row));
            return;
        }
        if (reorder == Reorder.SWAP) {
            // EXCHANGE WITH THE BLOCK UNDER THE CURSOR; A DROP OVER NOTHING OR OVER ITSELF IS A NO-OP
            int target = this.rowAt(dragY, row);
            if (target >= 0) this.swap(children.indexOf(row), target);
            return;
        }
        int target = this.blockStart(children.indexOf(row));
        if (target != dragOrigin && onListMove != null) onListMove.moved(dragOrigin, target);
    }

    // A ROW DISPOSED MID-DRAG OR A BROKEN REBUILD ABANDONS THE SESSION WITHOUT NOTIFYING
    private void abandon() {
        ListRow row = this.dragRow;
        if (row == null) return;
        this.dragRow = null;
        this.scrollDir = 0;
        row.dragging = false;
        row.hidden = false;
        this.floatDragged(null, 0, 0);
    }

    // WHETHER THE CURSOR SITS PAST THE CONTENT SIDES FAR ENOUGH TO ARM THE DRAG-OUT DELETE
    private boolean outZone() {
        return dragOut && (dragX < contentX() - OUT_ZONE || dragX > contentX() + contentWidth() + OUT_ZONE);
    }

    // VISIBLE ROW WHOSE BOUNDS HOLD mouseY, OUTSIDE THE DRAGGED BLOCK; -1 WHEN NOTHING QUALIFIES
    private int rowAt(double mouseY, ListRow dragged) {
        int start = this.blockStart(children.indexOf(dragged));
        int end = this.blockEnd(start);
        for (int i = 0; i < children.size(); i++) {
            if (i >= start && i < end) continue;
            Element child = children.get(i);
            if (!child.visible || !(child instanceof ListRow)) continue;
            if (mouseY >= child.bounds.y && mouseY < child.bounds.y + child.bounds.height) return i;
        }
        return -1;
    }

    /** Exchanges the blocks holding {@code a} and {@code b} and fires {@code onListSwap}; the host mirrors its data (§10.6). */
    public void swap(int a, int b) {
        if (a < 0 || b < 0 || a >= children.size() || b >= children.size()) return;
        int aStart = this.blockStart(a);
        int bStart = this.blockStart(b);
        if (aStart == bStart) return;
        int lo = Math.min(aStart, bStart), hi = Math.max(aStart, bStart);
        int loEnd = this.blockEnd(lo), hiEnd = this.blockEnd(hi);
        if (loEnd > hi) return;
        List<Element> order = new ArrayList<>(children.size());
        order.addAll(children.subList(0, lo));
        order.addAll(children.subList(hi, hiEnd));
        order.addAll(children.subList(loEnd, hi));
        order.addAll(children.subList(lo, loEnd));
        order.addAll(children.subList(hiEnd, children.size()));
        this.arrange(order);
        if (onListSwap != null) onListSwap.moved(aStart, bStart);
    }

    /** Removes the block holding {@code index} — its rows dispose — and fires {@code onListDelete}; the host drops the keys (§10.6). */
    public void delete(int index) {
        if (index < 0 || index >= children.size() || !(children.get(index) instanceof ListRow)) return;
        int start = this.blockStart(index);
        int end = this.blockEnd(start);
        if (dragRow != null && children.indexOf(dragRow) >= start && children.indexOf(dragRow) < end) this.abandon();
        for (int i = end - 1; i >= start; i--) {
            if (children.get(i) instanceof ListRow row) {
                this.remove(row);
                store.rows.remove(row.key);
                row.disposeRow();
            }
        }
        if (onListDelete != null) onListDelete.accept(start);
    }

    // MOVES CHILDREN TO MATCH order THROUGH reorder(), THE SAME NON-REBUILDING PATH submit USES
    private void arrange(List<Element> order) {
        for (int i = 0; i < order.size(); i++) {
            Element child = order.get(i);
            if (children.indexOf(child) != i) this.reorder(child, i);
        }
    }

    // ---- DRAG BLOCKS (§10.6): CONTIGUOUS ROWS SHARING A NON-NULL groupBy TOKEN TRAVEL AS ONE ----

    private Object groupOf(int index) {
        return children.get(index) instanceof ListRow row ? row.group : null;
    }

    private int blockStart(int index) {
        Object token = this.groupOf(index);
        if (token == null) return index;
        while (index > 0 && token.equals(this.groupOf(index - 1))) index--;
        return index;
    }

    // EXCLUSIVE END OF THE BLOCK STARTING AT start
    private int blockEnd(int start) {
        Object token = this.groupOf(start);
        if (token == null) return start + 1;
        int end = start + 1;
        while (end < children.size() && token.equals(this.groupOf(end))) end++;
        return end;
    }

    // ---- LIFECYCLE ---------------------------------------------------------------------

    // ROWS JUST OUTSIDE THE VIEWPORT STILL TICK, SO A SCROLL NEVER POPS A COLD ROW IN
    private static final int OVERSCAN = 32;

    @Override
    public void tick() {
        // AUTO-SCROLL A DRAG NEAR THE EDGES: ONE STEP PER TICK EVEN WITH THE CURSOR STILL, THEN
        // RETARGET AT THE HELD CURSOR SO THE GAP FOLLOWS THE ROWS SLIDING UNDER IT
        if (dragRow != null && scrollDir != 0) {
            this.scrollBy(0, scrollDir * 5);
            this.retarget(dragY);
        }
        // VIEWPORT CULLING (H2): OFF-SCREEN ROWS NEITHER TICK NOR DRAIN, SO THEIR MEDIA SESSIONS
        // OPEN LAZILY ON SCROLL INSTEAD OF ALL AT ONCE; THE SCREEN DRAINS ONLY ITS OWN CONTEXT (§5.5)
        int top = contentY() - OVERSCAN;
        int bottom = contentY() + contentHeight() + OVERSCAN;
        for (Element child: children) {
            if (!child.visible) continue;
            if (child.bottom() < top || child.bounds.y > bottom) continue;
            child.tick();
        }
        for (ListRow row: store.rows.values()) {
            if (row.parent() != this || !row.visible) continue;
            if (row.bottom() < top || row.bounds.y > bottom) continue;
            row.data.tick();
        }
    }

    @Override
    public void dispose() {
        // SAFETY NET ONLY: THE LIST IS NEVER A PANEL DISPOSABLE, SO A SCREEN RE-INFLATE LEAVES ROW PLAYERS ALIVE (S7)
        this.abandon();
        store.dispose();
    }

    // ---- ROW CACHE ---------------------------------------------------------------------

    /** Opaque row cache the host owns; survives a screen re-inflate so the fresh list re-adopts its rows. */
    public static final class RowStore {
        private final Map<Object, ListRow> rows = new LinkedHashMap<>();

        /** Host belt for a store no live list adopted (a broken re-init); disposing twice is a no-op. */
        public void dispose() {
            for (ListRow row: rows.values()) row.disposeRow();
            rows.clear();
        }
    }

    /** Supplies the per-row build inputs: the baked variable map and the model the row's live {@code var}s read. */
    public interface RowBinder {
        Map<String, Object> baked(Object key);
        Object model(Object key);
    }

    // ---- ONE INFLATED ROW --------------------------------------------------------------

    /**
     * One templated row: a horizontal container carrying the inflated template subtree, its own live-binding
     * {@link DataContext} and its own disposable sink. Its painted surface is state-driven per frame (§13.3):
     * dragging shows the accent, the playing row the selection face, otherwise the field face; a selected row
     * frames itself with the accent border. Its template ids resolve through {@link #element(String)} only.
     */
    public static final class ListRow extends ParentLinear {
        final Object key;
        private final DataContext data = new DataContext();
        private List<Element> disposables;
        boolean selected, playing, dragging;
        DragThumb thumb;
        // DRAG-BLOCK TOKEN AND THE TEMPLATE NAMESPACE FOR element(String) LOOKUPS
        Object group;
        String ns;

        ListRow(Object key) {
            super(Orientation.HORIZONTAL);
            this.key = key;
            this.face = Face.NESTED;
            // OWN FRAME WIDTH SO THE SELECTION BORDER SHOWS EVEN ON A THEME WHOSE ROLE HAS NO BORDER
            this.border(1);
        }

        /** The template element with {@code id} inside THIS row; bare paths take the template's namespace (§10.6). */
        public Element element(String id) {
            int colon = id.indexOf(':');
            ResourceLocation ref = colon >= 0
                    ? ResourceLocation.fromNamespaceAndPath(id.substring(0, colon), id.substring(colon + 1))
                    : ResourceLocation.fromNamespaceAndPath(ns, id);
            return this.get(ref);
        }

        void disposable(Element element) {
            if (disposables == null) disposables = new ArrayList<>();
            this.disposables.add(element);
        }

        // RELEASES THIS ROW'S BUILDER-OWNED RESOURCES AND STOPS ITS TICKED EVALUATORS (§13.3)
        void disposeRow() {
            if (disposables != null) {
                for (Element e: disposables) e.dispose();
                disposables.clear();
            }
            this.data.reset();
            if (thumb != null) thumb.reset();
        }

        @Override
        public void dispose() {
            // SAFETY NET WHEN THE WHOLE TREE IS DISPOSED (SCREEN CLOSE)
            this.disposeRow();
        }

        @Override
        protected Drawable faceDisplay() {
            if (dragging) return theme().accent();
            if (playing) return theme().selection();
            return theme().field(hovering);
        }

        @Override
        protected Drawable borderDisplay() {
            return selected ? theme().accentBorder() : super.borderDisplay();
        }
    }
}
