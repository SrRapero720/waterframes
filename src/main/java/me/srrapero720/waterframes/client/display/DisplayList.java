package me.srrapero720.waterframes.client.display;

import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WaterFrames.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
@OnlyIn(Dist.CLIENT)
public class DisplayList {
    public static final Integer DEFAULT_SIZE = 32;

    private static volatile Display[] displays = new Display[DEFAULT_SIZE];
    private static int position = 0;
    private static boolean checkSize = false;
    private static boolean paused;

    public static void add(Display display) {
        if (checkSize) {
            if (((float) position / displays.length) <= 0.25f) {
                Display[] freshMeal = new Display[displays.length / 2]; // free unused memory
                System.arraycopy(displays, 0, freshMeal, 0, position);
                displays = freshMeal;
            }
            checkSize = false;
        }

        if (position >= displays.length) { // position never should be major than length
            Display[] freshMeal = new Display[displays.length * 2];
            position = copyData$resetPosition(displays, freshMeal);
            displays = freshMeal;
            checkSize = true;
        }

        // pause the display when the pause event is fired even if was too late
        if (paused) display.setPauseMode(true);

        displays[position++] = display;
    }

    public static void pause() {
        paused = true;
        for (int i = 0; i < position; i++) {
            if (displays[i] != null) displays[i].setPauseMode(true);
        }
    }

    public static void resume() {
        paused = false;
        for (int i = 0; i < position; i++) {
            if (displays[i] != null) displays[i].setPauseMode(false);
        }
    }

    public static void remove(int i) {
        if (i > displays.length) return; // 'i' cannot be over position
        displays[i] = null;
    }

    public static void remove(Display obj) {
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

        displays = new Display[DEFAULT_SIZE];
        position = 0;
    }

    private static int copyData$resetPosition(Display[] current, Display[] target) {
        int freshPosition = 0;
        for (int i = 0; i < current.length; i++) { // tries to ignore all null pos to stack all instanced objects, spend less memory
            if (current[i] != null) {
                target[freshPosition++] = current[i];
                current[i] = null;
            }
        }
        return freshPosition;
    }

    @SubscribeEvent
    public static void onUnloadingLevel(LevelEvent.Unload event) {
        LevelAccessor level = event.getLevel();
        if (level != null && level.isClientSide()) DisplayList.release();
    }

    // @SubscribeEvent
    public static void onClientPause(/*ClientPauseChangeEvent.Post event*/boolean paused) {
        if (/*event.isPaused()*/paused) DisplayList.pause();
        // else DisplayList.resume();
    }
}