package me.srrapero720.vlctool;

import me.srrapero720.vlctool.strategy.LinuxNativeDiscoveryStrategyFixed;
import me.srrapero720.vlctool.strategy.WindowsNativeDiscoveryStrategyFixed;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.watercore_supplier.ThreadUtil;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;

import java.lang.reflect.Field;

public class VLCDiscovery extends Thread {
    public enum VLCState { FAILED, UNLOADED, LOADING, READY }
    private static volatile VLCState state = VLCState.UNLOADED;
    private static volatile NativeDiscovery discovery;
    public static volatile MediaPlayerFactory factory;

    // MISSING
    private static Field searchPaths;
    private static Field libraries;

    public static void setState(VLCState state) { VLCDiscovery.state = state; }
    public static boolean isLoading() { return state.equals(VLCState.LOADING); }
    public static boolean isReady() { return state.equals(VLCState.READY); }

    public static boolean isReadyOrRequest() {
        if (isReady()) return true;
        if (!isLoading()) {
            setState(VLCState.LOADING);
            new VLCDiscovery();
        }
        return false;
    }

    public VLCDiscovery() {
        this.setName("WF/VLCDiscover");
        this.setDaemon(true);
        this.start();
    }

    @Override
    public synchronized void run() {
        super.run();
        if (isReady()) return;

        ThreadUtil.trySimple(() -> {
            var windows = new WindowsNativeDiscoveryStrategyFixed();
            var linux = new LinuxNativeDiscoveryStrategyFixed();
            discovery = new NativeDiscovery(windows, linux) {
                @Override
                protected void onFailed(String path, NativeDiscoveryStrategy strategy) {
                    WaterFrames.LOGGER.info("Failed to load VLC in '{}' using '{}' stop searching", path, strategy.getClass().getSimpleName());
                    super.onFailed(path, strategy);
                }

                @Override
                protected void onNotFound() {
                    WaterFrames.LOGGER.info("Could not find VLC in any of the given paths");
                    super.onNotFound();
                }
            };
            if (discovery.discover()) {
                setState(VLCState.READY);
                factory = new MediaPlayerFactory("--quiet");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> factory.release()));
                WaterFrames.LOGGER.info("Loaded VLC in '{}'", discovery.discoveredPath());
            } else WaterFrames.LOGGER.info("Failed to load VLC");

        }, (e) -> {
            setState(VLCState.FAILED);
            WaterFrames.LOGGER.error("Failed to load VLC", e);
        });
    }
}
