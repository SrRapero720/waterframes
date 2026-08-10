package me.srrapero720.waterui;

import me.srrapero720.waterui.format.UIBindings;
import me.srrapero720.waterui.format.UIRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Entry point of WaterUI, a self-contained {@code .ui} widget toolkit shipped as its own client
 * library mod. It registers its built-in elements on the mod event bus and otherwise knows nothing
 * about the mod that hosts it: documents, themes and atlases are supplied by whoever embeds it.
 */
@Mod(WaterUI.ID)
public class WaterUI {
    public static final String ID = "waterui";
    public static final Logger LOGGER = LogManager.getLogger("WaterUI");

    public WaterUI(IEventBus bus) {
        bus.addListener(WaterUI::onClientSetup);
    }

    // THE ELEMENT CATALOG IS CLIENT STATE; THE SETUP EVENT NEVER FIRES ON A SERVER, SO bootstrap() RIDES IT
    private static void onClientSetup(FMLClientSetupEvent event) {
        bootstrap();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    /** Registers the built-in element catalog and scans host bindings so {@code .ui} documents inflate. Idempotent. */
    public static void bootstrap() {
        UIRegistry.bootstrap();
        UIBindings.bootstrap();
    }
}
