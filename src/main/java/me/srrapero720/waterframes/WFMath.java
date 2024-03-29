package me.srrapero720.waterframes;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import team.creative.creativecore.common.util.math.vec.Vec3d;

public class WFMath {
    private static final long negativeZeroDoubleBits = Float.floatToRawIntBits(-0.0f);

    public static short minShort(short a, short b) {
        return (a <= b) ? a : b;
    }

    public static float minFloat(float a, float b) {
        if (a != a) return a;   // a is NaN
        if ((a == 0.0f) && (b == 0.0f) && (Float.floatToRawIntBits(b) == negativeZeroDoubleBits)) {
            // Raw conversion ok since NaN can't map to -0.0.
            return b;
        }
        return (a <= b) ? a : b;
    }

    public static long floorMod(long x, long y) {
        try {
            final long r = x % y;
            // if the signs are different and modulo not zero, adjust result
            if ((x ^ y) < 0 && r != 0) return r + y;
            return r;
        } catch (ArithmeticException e) {
            return 0;
        }
    }

    public static int floorVolume(BlockPos pos, int volume, int min, int max) {
        assert Minecraft.getInstance().player != null;
        Position position = Minecraft.getInstance().player.getPosition(WaterFrames.deltaFrames());
        double distance = Math.sqrt(pos.distToLowCornerSqr(position.x(), position.y(), position.z()));
        if (min > max) {
            int temp = max;
            max = min;
            min = temp;
        }

        if (distance > min)
            volume = (distance > max + 1) ? 0 : (int) (volume * (1 - ((distance - min) / ((1 + max) - min))));
        return volume;
    }
}
