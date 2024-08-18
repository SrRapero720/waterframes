package me.srrapero720.waterframes.common.compat.creativecore;

public interface IScalableText {
    void wf$setScale(float scale);
    float wf$getScale();

    static void setScale(Object o, float scale) {
        if (o instanceof IScalableText scalableText) {
            scalableText.wf$setScale(scale);
        }
    }
}