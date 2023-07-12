package me.srrapero720.waterframes;

import me.srrapero720.waterframes.common.blocks.Frame;
import me.srrapero720.waterframes.common.blocks.Projector;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.creativecore.common.network.CreativePacket;

import java.util.function.Supplier;

@Mod(WaterFrames.ID)
public class WaterFrames {
    public static final String ID = "waterframes";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    private static final Marker MARKER = MarkerFactory.getMarker("Bootstrap");
    private static final CreativeNetwork NETWORK = new CreativeNetwork("2.0", LogManager.getLogger(ID), new ResourceLocation(ID, "main"));

    public static IEventBus bus() { return FMLJavaModLoadingContext.get().getModEventBus(); }
    public WaterFrames() { WFRegistry.init(); }


    public static Frame getFrame() { return WFRegistry.FRAME.get(); }
    public static Projector getProjector() { return WFRegistry.PROJECTOR.get(); }

    public static void sendPacketToClient(CreativePacket packet, Level level, BlockPos pos) {
        NETWORK.sendToClient(packet, level, pos);
    }

    static <T extends CreativePacket> void registerPacket(Class<T> packetBase, Supplier<T> packet) {
        NETWORK.registerType(packetBase, packet);
    }

    // Util
    @OnlyIn(Dist.CLIENT)
    public static float toDeltaFrames() { return Minecraft.getInstance().isPaused() ? 1.0F : Minecraft.getInstance().getFrameTime(); }
}