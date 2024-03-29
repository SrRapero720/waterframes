package me.srrapero720.waterframes.client.display;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class DisplayControl {
    private static final Marker IT = MarkerManager.getMarker("DisplayControl");
    private static final Integer DEFAULT_SIZE = 32;
    static final int SYNC_TIME = 1500;

    private static volatile TextureDisplay[] displays = new TextureDisplay[DEFAULT_SIZE];
    private static int position = 0;
    private static boolean checkSize = false;

    public static void add(TextureDisplay display) {
        if (checkSize) {
            if (((float) position / displays.length) <= 0.25f) {
                TextureDisplay[] freshMeal = new TextureDisplay[displays.length / 2]; // free unused memory
                System.arraycopy(displays, 0, freshMeal, 0, position);
                displays = freshMeal;
            }
            checkSize = false;
        }

        if (position >= displays.length) { // position never should be major than length
            TextureDisplay[] freshMeal = new TextureDisplay[displays.length * 2];
            position = copyData$resetPosition(displays, freshMeal);
            displays = freshMeal;
            checkSize = true;
        }

        displays[position++] = display;
    }

    public static void pause() {
        for (int i = 0; i < position; i++) {
            if (displays[i] != null) displays[i].pause();
        }
    }

    public static void resume() {
        for (int i = 0; i < position; i++) {
            if (displays[i] != null) displays[i].resume();
        }
    }

    public static void remove(int i) {
        if (i > displays.length) return; // 'i' cannot be over position
        displays[i] = null;
    }

    public static void remove(TextureDisplay obj) {
        if (obj == null) return; // null cannot be removed, duh
        for (int i = 0; i < position; i++) {
            if (obj == displays[i]) {
                displays[i] = null;
                break;
            }
        }
    }

    public static void release() {
        for (int i = 0; i < position; i++) {
            if (displays[i] != null) {
                displays[i].release();
                displays[i] = null;
            }
        }

        displays = new TextureDisplay[DEFAULT_SIZE];
        position = 0;
    }

    private static int copyData$resetPosition(TextureDisplay[] current, TextureDisplay[] target) {
        int freshPosition = 0;
        for (int i = 0; i < current.length; i++) { // tries to ignore all null pos to stack all instanced objects, spend less memory
            if (current[i] != null) {
                target[freshPosition++] = current[i];
                current[i] = null;
            }
        }
        return freshPosition;
    }
}