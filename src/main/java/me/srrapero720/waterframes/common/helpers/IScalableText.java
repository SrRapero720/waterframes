package me.srrapero720.waterframes.common.helpers;

public interface IScalableText {
    void wf$setScale(float scale);
    float wf$getScale();

    static void setScale(Object o, float scale) {
        if (o instanceof IScalableText scalableText) {
            scalableText.wf$setScale(scale);
        }
    }
}