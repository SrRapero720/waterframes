/**
 * WaterUI, a {@code .ui} widget toolkit shipped as its own client library. Self contained,
 * retained mode and vanilla friendly: it draws through {@code GuiGraphics}, so text, fills
 * and icons ride the game's own batches.
 *
 * <h2>Tree</h2>
 * Everything on screen is an {@link me.srrapero720.waterui.core.Element}.
 * Containers extend {@link me.srrapero720.waterui.core.AbstractParent}:
 * {@link me.srrapero720.waterui.layout.ParentLinear} stacks children on one
 * axis (and scrolls them in place when enabled),
 * {@link me.srrapero720.waterui.layout.FrameLayout} overlays them, and
 * {@link me.srrapero720.waterui.layout.Grid} arranges them in a cell lattice.
 * A {@link me.srrapero720.waterui.screen.WaterScreen} is a
 * vanilla Screen that owns a {@link me.srrapero720.waterui.screen.Panel} root
 * (the themed content slab) and hosts a stack of
 * {@link me.srrapero720.waterui.screen.Dialog} overlays over it.
 *
 * <h2>Layout</h2>
 * One value per axis: an exact outer size in pixels, {@code FILL} to share the parent's
 * leftover by weight, or {@code CONTAIN} to wrap the content. Minimums are never declared,
 * they are measured from the content itself. Border and padding inset the content instead of
 * growing the box; only {@code CONTAIN} adds them on top of the measure. Layout is lazy: any
 * change calls {@code dirty()} and the hosting screen reflows the whole tree once, before the
 * next frame.
 *
 * <h2>Theme</h2>
 * A {@link me.srrapero720.waterui.theme.Theme} carries every colour, one
 * {@link me.srrapero720.waterui.theme.ElementTheme} per visual role, picked by
 * the {@link me.srrapero720.waterui.theme.Face} each widget declares, and one
 * dedicated theme per control that paints parts a role cannot describe: the switch, the
 * slider, the progress bars and the scrollbar. Themes inherit down the tree from the screen;
 * an element can pin its own theme, or override a single knob: {@code border},
 * {@code padding}, {@code margin}, {@code shadow}, {@code face(Drawable)} and
 * {@code outline(Drawable)}. Widgets that change surface by state override
 * {@code faceDisplay()} or {@code borderDisplay()} instead.
 *
 * <h2>Input</h2>
 * The hosting screen routes mouse, keys and scroll to the top layer only. Containers walk
 * their children back to front; the deepest hovered element that claims the event wins.
 * Focus follows the click and lands on elements reporting {@code focusable()}.
 */
package me.srrapero720.waterui;
