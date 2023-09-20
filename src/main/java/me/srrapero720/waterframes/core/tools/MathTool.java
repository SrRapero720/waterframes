package me.srrapero720.waterframes.core.tools;

public class MathTool {
    private static final long negativeZeroDoubleBits = Float.floatToRawIntBits(-0.0f);
    public static double min(float a, float b) {
        if (a != a)
            return a;   // a is NaN
        if ((a == 0.0f) &&
                (b == 0.0f) &&
                (Float.floatToRawIntBits(b) == negativeZeroDoubleBits)) {
            // Raw conversion ok since NaN can't map to -0.0.
            return b;
        }
        return (a <= b) ? a : b;
    }
}