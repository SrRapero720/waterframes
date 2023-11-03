package me.srrapero720.waterframes.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class FrameTools {
    private static final Gson GSON = new Gson();
    private static final Marker IT = MarkerManager.getMarker("Tools");
    private static final long negativeZeroDoubleBits = Float.floatToRawIntBits(-0.0f);

    public static boolean isUrlValid(String url) {
        try { new URL(url); return true; } catch (Exception ignored) { return false; }
    }

    @OnlyIn(Dist.CLIENT)
    public static @NotNull String patchUrl(@NotNull String url) {
        return url.replace("minecraft://", ("file:///" + FMLPaths.GAMEDIR.get().toAbsolutePath()).replace("\\", "/") + "/");
    }

    public static List<String> readStringList(String path) {
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(readResource(Thread.currentThread().getContextClassLoader(), path)))) {
            result.addAll(GSON.fromJson(reader, new TypeToken<List<String>>() {
            }.getType()));
        } catch (Exception e) {
            LOGGER.fatal(IT, "Exception trying to read JSON from {}", path, e);
        }

        return result;
    }

    public static InputStream readResource(ClassLoader loader, String source) {
        InputStream is = loader.getResourceAsStream(source);
        if (is == null && source.startsWith("/")) is = loader.getResourceAsStream(source.substring(1));
        return is;
    }

    public static double minFloat(float a, float b) {
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

    public static long floorMod(long x, long y) {
        try {
            final long r = x % y;
            // if the signs are different and modulo not zero, adjust result
            if ((x ^ y) < 0 && r != 0) {
                return r + y;
            }
            return r;
        } catch (ArithmeticException e) {
            return 0;
        }
    }

    public static int floorVolume(Vec3d pos, int volume, int min, int max) {
        assert Minecraft.getInstance().player != null;
        double distance = (int) pos.distance(Minecraft.getInstance().player.getPosition(deltaFrames()));
        if (min > max) {
            int temp = max;
            max = min;
            min = temp;
        }

        if (distance > min)
            volume = (distance > max) ? 0 : (int) (volume * (1 - ((distance - min) / (max - min))));
        return volume;
    }

    public static int floorVolume(Vec3d pos, Direction direction, float offSet, int volume, int min, int max) {
        assert Minecraft.getInstance().player != null;
        pos = new Vec3d(pos.toBlockPos().relative(direction, (int) offSet));
        double distance = pos.distance(Minecraft.getInstance().player.getPosition(deltaFrames()));
        if (min > max) {
            int temp = max;
            max = min;
            min = temp;
        }

        if (distance > min)
            volume = (distance > max) ? 0 : (int) (volume * (1 - ((distance - min) / (max - min))));

        return volume;
    }

    // IS LOADED SOMETHING
    public static boolean isLoadingMod(String id) {
        ModList list;
        return (list = ModList.get()) != null ? list.isLoaded(id) : FMLLoader.getLoadingModList().getModFileById(id) != null;
    }
    public static boolean isPackageLoaded(String id) { return Thread.currentThread().getContextClassLoader().getDefinedPackage(id) != null; }

    @OnlyIn(Dist.CLIENT)
    public static float deltaFrames() { return Minecraft.getInstance().isPaused() ? 1.0F : Minecraft.getInstance().getFrameTime(); }

    public static String timestamp(long time) {
        if (time < 3600000) {
            long minutos = TimeUnit.MILLISECONDS.toMinutes(time);
            long segundos = TimeUnit.MILLISECONDS.toSeconds(time) -
                    TimeUnit.MINUTES.toSeconds(minutos);
            return String.format("%02d:%02d", minutos, segundos);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            return sdf.format(time);
        }
    }
}