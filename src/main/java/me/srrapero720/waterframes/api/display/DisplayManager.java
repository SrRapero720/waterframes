package me.srrapero720.waterframes.api.display;

import net.minecraft.client.Minecraft;

public class DisplayManager {
    public static final Registry REGISTRY = new Registry();

    public static final int SYNC_TIME = 1500;
    public static boolean IS_PAUSED = false;

    public static void release() { REGISTRY.release(); }
    public static void add(IDisplay display) { REGISTRY.add(display); }

    public static void tick() {
        boolean paused = Minecraft.getInstance().isPaused();
        if (IS_PAUSED != paused && (IS_PAUSED = paused)) {
            synchronized (REGISTRY) {
                REGISTRY.pauseAll();
            }
        }
    }

    public static class Registry {
        private static final int DEFAULT_SIZE = 32;

        protected transient IDisplay[] data;
        protected int size = 0;
        protected boolean nextOpCheckSize = false;

        public Registry() {
            this.data = new IDisplay[DEFAULT_SIZE];
        }

        public void add(IDisplay obj) {
            if (nextOpCheckSize) {
                if (((float) size / data.length) <= 0.25f) {
                    IDisplay[] temp = data;
                    data = new IDisplay[temp.length / 2];
                    System.arraycopy(temp, 0, data, 0, data.length);
                }
                nextOpCheckSize = false;
            }

            if (size == data.length) {
                IDisplay[] temp = data;
                data = new IDisplay[temp.length * 2];
                size = reduceOf(temp, data);
                nextOpCheckSize = true;
            }

            data[size] = obj;
            size++;
        }

        public int size() { return size; }

        public void pauseAll() { for (int i = 0; i < size; i++) if (data[i] != null) data[i].pause(); }
        public void resumeAll() { for (int i = 0; i < size; i++) if (data[i] != null) data[i].resume(); }
        public void remove(int i) { data[i] = null; }
        public void remove(IDisplay obj) {
            if (obj == null) return; // null cannot be removed, duh
            for (int i = 0; i < size; i++) {
                if (obj == data[i]) {
                    data[i] = null;
                    break;
                }
            }
        }

        public void release() {
            for (int i = 0; i < size; i++) {
                if (data[i] != null) {
                    data[i].release(false);
                    data[i] = null;
                }
            }

            this.data = new IDisplay[DEFAULT_SIZE];
        }

        static int reduceOf(IDisplay[] data, IDisplay[] target) {
            int size = 0;
            for (int i = 0; i < data.length; i++) {
                if (data[i] != null) {
                    target[size] = data[i];
                    data[i] = null;
                    size++;
                }
            }
            return size;
        }
    }
}