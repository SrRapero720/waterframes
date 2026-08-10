package me.srrapero720.waterframes.client.ui.screen;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterui.screen.Dialog;
import me.srrapero720.waterui.format.UIVar;
import me.srrapero720.waterui.theme.Palette;
import me.srrapero720.waterui.widget.*;
import net.minecraft.network.chat.Component;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.watermedia.api.network.NetworkAPI;
import org.watermedia.api.platform.internal.WaterPlatform;
import org.watermedia.api.network.NetworkServer;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * Sends a local file to the WaterMedia relay and hands back the url it answers with. The picker
 * is native and blocking, so it runs on its own thread; a running upload cannot be dismissed.
 */
public class UploadScreen extends Dialog {
    private static final int WIDTH = 212;
    private static final int HEIGHT = 102;
    private static final String[] FILTERS = { "*.mp4", "*.mkv", "*.webm", "*.mov", "*.mp3", "*.ogg", "*.wav", "*.png", "*.jpg", "*.jpeg", "*.gif" };

    private final Consumer<String> onUploaded;

    // RESOLVED FROM dialogs/upload.ui ON EVERY init(); THE FORMAT OWNS THE CHROME, JAVA ONLY WIRES BEHAVIOUR
    private Text title;
    private Button confirm;
    private InputText path;
    // TRUE ONLY AFTER init() RESOLVED EVERY WIDGET, SO A BROKEN DOCUMENT NEVER NPEs A LATER PATH (H4)
    private boolean wired;

    // WRITTEN BY THE PICKER THREAD, READ ON THE TICK
    private volatile String chosen;
    private volatile boolean browsing;

    private NetworkServer.UploadStatus status;
    private String uploaded;
    private boolean failed;

    public UploadScreen(Consumer<String> onUploaded) {
        super(WaterFrames.asResource("dialogs/upload"), WIDTH, HEIGHT);
        this.onUploaded = onUploaded;
        // ONE-LINE GATES REGISTER FOR THE INSTANCE'S LIFE (§5.6); THE STATEFUL confirm_enabled AND THE
        // HOT progress_value KEEP THEIR ANNOTATED METHODS
        this.registerVar("path_visible", () -> status == null || failed);
        this.registerVar("progress_visible", () -> status != null && !failed);
        this.registerVar("browse_enabled", () -> !browsing && status == null);
        this.registerVar("path_enabled", () -> !failed);
        this.registerUIEvent("confirm", () -> { if (wired) this.confirm(); });
    }

    // ---- LIVE BINDINGS (resolved by var name from dialogs/upload.ui, §5) -----------------

    @UIVar("confirm_enabled")
    public boolean confirmEnabled() {
        if (failed) return true;
        if (status == null) return path != null && !path.text().isBlank();
        return status.failed() || status.completed();
    }

    @UIVar("progress_value")
    public long progressValue() {
        return status == null ? 0 : (long) status.percentage();
    }

    @Override
    public void init() {
        this.wired = false;

        this.title = (Text) this.get(WaterFrames.asResource("upload/title"));
        this.confirm = (Button) this.get(WaterFrames.asResource("upload/confirm"));
        this.path = (InputText) this.get(WaterFrames.asResource("upload/path"));
        Button browse = (Button) this.get(WaterFrames.asResource("upload/browse"));
        ProgressBar progress = (ProgressBar) this.get(WaterFrames.asResource("upload/progress"));
        // A BROKEN dialogs/upload.ui LEAVES A LOOKUP NULL; NAME THE FIRST MISSING id AND SKIP THE WIRING (S7)
        String missing = title == null ? "upload/title" : confirm == null ? "upload/confirm" : path == null ? "upload/path"
                : progress == null ? "upload/progress" : browse == null ? "upload/browse" : null;
        if (missing != null) {
            WaterFrames.LOGGER.error("[WaterFrames] upload screen missing id '{}', skipping wiring", missing);
            return;
        }
        this.wired = true;

        // init() RE-RUNS ON RESIZE: THE REBUILT CHROME MUST REGAIN THE STATE ITS UPLOAD LIFECYCLE IMPLIES
        if (failed) {
            this.title.text(translatable("waterframes.gui.upload.failed"));
            this.title.color = Palette.RED_BORDER;
            this.path.text(status != null && status.error() != null ? status.error() : "");
            this.confirm.text(Component.translatable("waterframes.gui.upload.accept"));
        } else if (uploaded != null) {
            this.confirm.text(Component.translatable("waterframes.gui.upload.add"));
        }
    }

    // BROWSE BUTTON HANDLER (onClick=browse): NATIVE DIALOGS BLOCK THEIR CALLER, SO NEVER THE RENDER THREAD
    public void browse(String id) {
        if (!wired || browsing || status != null) return;
        this.browsing = true;
        Thread thread = new Thread(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer filters = stack.mallocPointer(FILTERS.length);
                for (String filter: FILTERS) filters.put(stack.UTF8(filter));
                filters.flip();

                this.chosen = TinyFileDialogs.tinyfd_openFileDialog(
                        translate("waterframes.gui.upload.title"), null, filters,
                        translate("waterframes.gui.upload.filter"), false);
            } catch (Exception ignored) {
                // NO NATIVE DIALOG HERE; THE PATH CAN STILL BE TYPED OR DROPPED IN
            } finally {
                this.browsing = false;
            }
        }, "waterframes-file-chooser");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void filesDropped(List<Path> files) {
        if (!wired || status != null || files.isEmpty()) return;
        this.path.text(files.get(0).toString());
    }

    // CONFIRM BUTTON (onClick=confirm, REGISTERED IN init): IT UPLOADS FIRST AND ADDS THE FINISHED URL AFTER
    private void confirm() {
        if (failed) {
            this.close();
            return;
        }
        if (uploaded != null) {
            this.onUploaded.accept(uploaded);
            this.close();
            return;
        }
        if (status != null) return;

        File file = new File(path.text().trim());
        if (!file.isFile()) return;
        this.status = NetworkAPI.upload(file);
        this.dirty();
    }

    /** An upload in flight has nowhere to go back to, so the panel stays put until it lands. */
    @UIVar("closeable")
    @Override
    public boolean closeable() {
        return status == null || status.completed() || status.failed();
    }

    @Override
    public void tick() {
        super.tick();
        if (!wired) return;
        String picked = this.chosen;
        if (picked != null) {
            this.path.text(picked);
            this.chosen = null;
        }

        if (status == null) return;

        // A FAILED UPLOAD: SAY SO ONCE AND LET THE SAME BUTTON DISMISS THE PANEL
        if (status.failed()) {
            if (failed) return;
            this.failed = true;
            this.title.text(translatable("waterframes.gui.upload.failed"));
            this.title.color = Palette.RED_BORDER;
            this.path.text(status.error() == null ? "" : status.error());
            this.confirm.text(Component.translatable("waterframes.gui.upload.accept"));
            this.dirty();
            return;
        }

        if (!status.completed()) return;

        if (uploaded == null) {
            this.uploaded = NetworkAPI.PROTOCOL_WATER + "://" + WaterPlatform.HOST_REMOTE + "/" + status.id();
            this.confirm.text(Component.translatable("waterframes.gui.upload.add"));
        }
    }
}
