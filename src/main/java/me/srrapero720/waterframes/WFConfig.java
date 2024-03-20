package me.srrapero720.waterframes;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class WFConfig {
//    private static final ForgeConfigSpec server_spec;
//    private static final ForgeConfigSpec client_spec;

    // RENDERING
    private static ForgeConfigSpec.DoubleValue maxWidth;
    private static ForgeConfigSpec.DoubleValue maxHeight;
    private static ForgeConfigSpec.IntValue maxRenderDistance;
    private static ForgeConfigSpec.IntValue maxProjectionDistance;
    // MULTIMEDIA
    private static ForgeConfigSpec.IntValue maxVolumeDistance;
    private static ForgeConfigSpec.IntValue maxVolume;
    private static ForgeConfigSpec.BooleanValue useVideoLan;
    private static ForgeConfigSpec.BooleanValue keepsRendering;
    // BEHAVIOR
    private static ForgeConfigSpec.BooleanValue useRedstone;
    private static ForgeConfigSpec.EnumValue<DisplayConfig.RedstoneMode> redstoneMode;
    private static ForgeConfigSpec.BooleanValue overridingPlayback;
    // PERMISSIONS
    private static ForgeConfigSpec.BooleanValue usableOnAdventure;
    private static ForgeConfigSpec.BooleanValue usableOnSurvival;
    private static ForgeConfigSpec.BooleanValue usableForAny;
    private static ForgeConfigSpec.BooleanValue useWhitelist;
    private static ForgeConfigSpec.ConfigValue<List<String>> whitelist;

    // OVERRIDES (client)
//    private final static ForgeConfigSpec.BooleanValue overrideServerConfig;
    private static ForgeConfigSpec.BooleanValue overrideUseVideolan;
    private static ForgeConfigSpec.BooleanValue overrideKeepsRendering;
}
