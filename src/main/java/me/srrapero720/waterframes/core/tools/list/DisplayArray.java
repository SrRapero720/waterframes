package me.srrapero720.waterframes.core.tools.list;

import me.srrapero720.waterframes.api.display.IDisplay;

import java.util.Arrays;

public class DisplayArray {
    protected transient IDisplay[] data;
    protected int size = 0;
    protected boolean nextOpCheckSize = false;

    public DisplayArray() {
        this.data = new IDisplay[1024];
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

    public int size() {
        return size;
    }

    public void pauseAll() {
        for (int i = 0; i < size; i++) if (data[i] != null) data[i].pause();
    }

    public void resumeAll() {
        for (int i = 0; i < size; i++) if (data[i] != null) data[i].resume();
    }


    public void remove(int i) {
        data[i] = null;
    }

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
                data[i].release();
                data[i] = null;
            }
        }

        this.data = new IDisplay[1024];
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