package me.srrapero720.waterframes.client.display;

public class DisplayControl {
    public static final Registry REGISTRY = new Registry();

    public static final int SYNC_TIME = 1500;

    public static void release() { REGISTRY.release(); }
    public static void add(TextureDisplay display) { REGISTRY.add(display); }

    public static int tickTime = 0;
    public static void tick() {
        tickTime++;
        if (tickTime == Integer.MAX_VALUE) tickTime = 0;
    }

    public static int getTickTime() { return tickTime; }

    public static void pause() {
        REGISTRY.pauseAll();
    }

    public static class Registry {
        private static final int DEFAULT_SIZE = 32;

        protected transient TextureDisplay[] data;
        protected int size = 0;
        protected boolean nextOpCheckSize = false;

        public Registry() {
            this.data = new TextureDisplay[DEFAULT_SIZE];
        }

        public void add(TextureDisplay obj) {
            if (nextOpCheckSize) {
                if (((float) size / data.length) <= 0.25f) {
                    TextureDisplay[] temp = data;
                    data = new TextureDisplay[temp.length / 2];
                    System.arraycopy(temp, 0, data, 0, data.length);
                }
                nextOpCheckSize = false;
            }

            if (size == data.length) {
                TextureDisplay[] temp = data;
                data = new TextureDisplay[temp.length * 2];
                size = reduceOf(temp, data);
                nextOpCheckSize = true;
            }

            data[size] = obj;
            size++;
        }

        public int size() { return size; }

        public void pauseAll() {
            for (int i = 0; i < size; i++) {
                if (size >= data.length) break; //FIXME: That wasn't should happen
                if (data[i] != null) data[i].pause();
            }
        }
        public void resumeAll() {
            for (int i = 0; i < size; i++) {
                if (size >= data.length) break; //FIXME: That wasn't should happen
                if (data[i] != null) data[i].resume();
            }
        }
        public void remove(int i) { data[i] = null; }
        public void remove(TextureDisplay obj) {
            if (obj == null) return; // null cannot be removed, duh
            for (int i = 0; i < size; i++) {
                if (i >= data.length) break; //FIXME: That wasn't should happen
                if (obj == data[i]) {
                    data[i] = null;
                    break;
                }
            }
        }

        public void release() {
            for (int i = 0; i < size; i++) {
                if (i >= data.length) break;  //FIXME: That wasn't should happen
                if (data[i] != null) {
                    data[i].release(false);
                    data[i] = null;
                }
            }

            this.data = new TextureDisplay[DEFAULT_SIZE];
        }

        static int reduceOf(TextureDisplay[] data, TextureDisplay[] target) {
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