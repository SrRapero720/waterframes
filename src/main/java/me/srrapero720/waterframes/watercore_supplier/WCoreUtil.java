package me.srrapero720.waterframes.watercore_supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URL;

public class WCoreUtil {
    public static int toTicks(final double sec) { return (int) (sec * 20); }

    @Contract(pure = true)
    @OnlyIn(Dist.CLIENT)
    public static Minecraft mine() { return Minecraft.getInstance(); }

    @OnlyIn(Dist.CLIENT)
    public static float toDeltaFrames() { return mine().isPaused() ? 1.0F : mine().getFrameTime(); }

    public static int fixAngle(final int input) {
        var angle = input;

        if (angle >= 0 && angle <= 45) angle = 0;
        else if (angle >= 45 && angle <= 90) angle = 90;
        else if (angle >= 90 && angle <= 135) angle = 90;
        else if (angle >= 135 && angle <= 180) angle = 180;
        else if (angle >= -180 && angle <= -135) angle = -180;
        else if (angle >= -135 && angle <= -90) angle = -90;
        else if (angle >= -90 && angle <= -45) angle = -90;
        else if (angle >= -45 && angle <= 0) angle = 0;

        return angle;
    }


    @SuppressWarnings("ConstantValue")
    public static boolean isLong(String s) { return ThreadUtil.tryAndReturn((def) -> Long.valueOf(s) != null, false); }
    @SuppressWarnings("ConstantValue")
    public static boolean isFloat(String s) { return ThreadUtil.tryAndReturn((def) -> Float.valueOf(s) != null, false); }
    @SuppressWarnings("ConstantValue")
    public static boolean isInt(String s) { return ThreadUtil.tryAndReturn((def) -> Integer.valueOf(s) != null, false); }

    /* THANKS STACKOVERFLOW
    * https://stackoverflow.com/questions/5051395/java-float-123-129456-to-123-12-without-rounding
    */
    public static float twoDecimal(double number) { return twoDecimal(Double.toString(number)); }
    public static float twoDecimal(float number) { return twoDecimal(Float.toString(number)); }
    public static float twoDecimal(String number) {
        StringBuilder sbFloat = new StringBuilder(number);
        int start = sbFloat.indexOf(".");
        if (start < 0) {
            return Float.parseFloat(sbFloat.toString());
        }
        int end = start+3;
        if((end)>(sbFloat.length()-1)) end = sbFloat.length();

        String twoPlaces = sbFloat.substring(start, end);
        sbFloat.replace(start, sbFloat.length(), twoPlaces);
        return Float.parseFloat(sbFloat.toString());
    }
}
