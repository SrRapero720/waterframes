
package me.srrapero720.waterframes.common.item.data;

public record RemoteData(String dimension, int x, int y, int z) {

    public int[] getPos() {
        return new int[] { x, y, z };
    }
}
