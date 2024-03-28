package me.srrapero720.waterframes.util.exceptions;

import me.srrapero720.waterframes.WaterFrames;

public class IllegalModException extends RuntimeException {
    private static final String MSG = "§fMod §6%s §fis not compatible with §e%s §fbecause §c%s §fplease remove it";
    private static final String MSG_REASON = "§fMod §6%s §fis not compatible with §e%s §fbecause §c%s §fuse §a%s §finstead";

    private IllegalModException(String msg) {
        super(msg);
    }

    public IllegalModException(String modid, String reason) {
        this(String.format(MSG, modid, WaterFrames.ID, reason));
    }

    public IllegalModException(String modid, String reason, String alternatives) {
        this(String.format(MSG_REASON, modid, WaterFrames.ID, reason, alternatives));
    }
}
