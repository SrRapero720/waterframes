package me.srrapero720.waterui.core;

import me.srrapero720.waterui.WaterUI;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

/**
 * Hardware mouse cursor of the game window. Widgets claim a shape on every frame they want it;
 * the end-of-frame sweep hands the arrow back when nobody claimed, so a widget that vanished
 * mid-gesture never leaves a stale hand behind. The hand cursors are built once, lazily, from
 * the embedded pixmaps below — no asset pipeline involved.
 */
public final class Cursors {
    private static final Marker IT = MarkerManager.getMarker(Cursors.class.getSimpleName());

    private Cursors() {}

    public enum Shape { ARROW, GRAB, GRABBING }

    private static final int SIZE = 16;

    // EMBEDDED PIXMAPS: '#' OUTLINE, 'o' FILL, ANYTHING ELSE TRANSPARENT
    private static final String[] GRAB_ART = {
            "................",
            ".....##.##......",
            "....#oo#oo##....",
            "....#oo#oo#o#...",
            ".##.#oo#oo#o#...",
            "#oo##ooooooo#...",
            "#ooo#ooooooo#...",
            ".#oo#ooooooo#...",
            ".#ooooooooooo#..",
            "..#oooooooooo#..",
            "..#ooooooooo#...",
            "...#oooooooo#...",
            "...#oooooooo#...",
            "....#ooooooo#...",
            "....#########...",
            "................",
    };

    private static final String[] GRABBING_ART = {
            "................",
            "................",
            "................",
            "....##.##.##....",
            "...#oo#oo#oo##..",
            "..##oooooooo#o#.",
            ".#oo#ooooooooo#.",
            ".#ooo#oooooooo#.",
            ".#oooooooooooo#.",
            "..#ooooooooooo#.",
            "..#oooooooooo#..",
            "...#ooooooooo#..",
            "...#oooooooo#...",
            "....########....",
            "................",
            "................",
    };

    private static long grab;
    private static long grabbing;
    private static Shape current = Shape.ARROW;
    private static boolean claimed;

    /** Asks for {@code shape} until the end of this frame; the last claim before the sweep wins. */
    public static void claim(Shape shape) {
        claimed = true;
        set(shape);
    }

    /** End-of-frame sweep, run by the screen: no claim arrived, so the arrow returns. */
    public static void frame() {
        if (!claimed) set(Shape.ARROW);
        claimed = false;
    }

    /** Hard reset for screen teardown, whatever was claimed. */
    public static void reset() {
        claimed = false;
        set(Shape.ARROW);
    }

    private static void set(Shape shape) {
        if (shape == current) return;
        current = shape;
        long window = Minecraft.getInstance().getWindow().getWindow();
        GLFW.glfwSetCursor(window, switch (shape) {
            case ARROW -> 0L;
            case GRAB -> grab != 0 ? grab : (grab = cursor(GRAB_ART));
            case GRABBING -> grabbing != 0 ? grabbing : (grabbing = cursor(GRABBING_ART));
        });
    }

    // BUILT ON FIRST USE, ON THE RENDER THREAD; GLFW COPIES THE PIXELS SO THE BUFFER IS FREED HERE
    private static long cursor(String[] art) {
        if (art.length < SIZE) throw new IllegalArgumentException(
                "Cursor pixmap has " + art.length + " rows, expected " + SIZE);
        ByteBuffer pixels = MemoryUtil.memAlloc(SIZE * SIZE * 4);
        for (String row: art) {
            if (row.length() < SIZE) throw new IllegalArgumentException(
                    "Cursor pixmap row has " + row.length() + " columns, expected " + SIZE);
            for (int x = 0; x < SIZE; x++) {
                char c = row.charAt(x);
                int argb = c == '#' ? 0xFF000000 : c == 'o' ? 0xFFFFFFFF : 0;
                pixels.put((byte) (argb >> 16)).put((byte) (argb >> 8)).put((byte) argb).put((byte) (argb >>> 24));
            }
        }
        pixels.flip();
        GLFWImage image = GLFWImage.malloc();
        image.set(SIZE, SIZE, pixels);
        long handle = GLFW.glfwCreateCursor(image, SIZE / 2, SIZE / 2);
        if (handle == 0) WaterUI.LOGGER.warn(IT, "GLFW failed to create a hardware cursor, falling back to the arrow");
        image.free();
        MemoryUtil.memFree(pixels);
        return handle;
    }
}
