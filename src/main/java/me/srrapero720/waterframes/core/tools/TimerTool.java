package me.srrapero720.waterframes.core.tools;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class TimerTool {
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