package me.srrapero720.waterframes.watercore_supplier;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThreadUtil {
    private static Thread THREADLG = null;
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
    private static final Thread.UncaughtExceptionHandler EXCEPTION_HANDLER = (t, e) -> System.out.println("Fatal exception on ThreadUtils - " + e);

    public static void printStackTrace(Exception e) { e.printStackTrace(); }

    public static <T> T tryAndReturn(ReturnableRunnable<T> runnable, T defaultVar) {
        return tryAndReturn(runnable, null, defaultVar);
    }

    public static <T> T tryAndReturn(ReturnableRunnable<T> runnable, @Nullable CatchRunnable catchRunnable, T defaultVar) {
        return tryAndReturn(runnable, catchRunnable, null, defaultVar);
    }

    public static <T> T tryAndReturn(ReturnableRunnable<T> runnable, @Nullable CatchRunnable catchRunnable, @Nullable ReturnableFinallyRunnable<T> finallyRunnable, T defaultVar) {
        var returned = defaultVar;
        try { return returned = runnable.run(defaultVar);
        } catch (Exception exception) {
            if (catchRunnable != null) catchRunnable.run(exception);
            return defaultVar;
        } finally { if (finallyRunnable != null) finallyRunnable.run(returned); }
    }

    public static void trySimple(SimpleTryRunnable runnable) { try { runnable.run(); } catch (Exception ignored) {} }
    public static void trySimple(SimpleTryRunnable runnable, CatchRunnable catchRunnable) { try { runnable.run(); } catch (Exception e) { catchRunnable.run(e); } }

    public static void threadTry(@NotNull TryRunnable toTry, @Nullable CatchRunnable toCatch, @Nullable FinallyRunnable toFinally) {
        threadTryArgument(null, (object -> toTry.run()), toCatch, (object -> { if (toFinally != null) toFinally.run(); }));
    }

    public static void threadNonDaeomTry(@NotNull TryRunnable toTry, @Nullable CatchRunnable toCatch, @Nullable FinallyRunnable toFinally) {
        threadTryArgument(null, (object -> toTry.run()), toCatch, (object -> { if (toFinally != null) toFinally.run(); }));
    }

    public static Thread thread(Runnable runnable) {
        var thread = new Thread(runnable);
        thread.setName("WATERCoRE-" + String.valueOf(Math.random() * 100).replace(".", "-"));
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(EXCEPTION_HANDLER);
        thread.start();
        return thread;
    }

    public static Thread threadNonDaemon(Runnable runnable) {
        var thread = new Thread(runnable);
        thread.setName("WATERCoRE-" + String.valueOf(Math.random() * 100).replace(".", "-"));
        thread.setDaemon(false);
        thread.setUncaughtExceptionHandler(EXCEPTION_HANDLER);
        thread.start();
        return thread;
    }

    public static <T> void threadTryArgument(T object, TryRunnableWithArgument<T> toTry, @Nullable CatchRunnable toCatch, @Nullable FinallyRunnableWithArgument<T> toFinally) {
        thread(() -> {
            try { toTry.run(object);
            } catch (Exception e) { if (toCatch != null) toCatch.run(e);
            } finally { if (toFinally != null) toFinally.run(object); }
        });
    }

    public static <T> void threadNonDaemonTryArgument(T object, TryRunnableWithArgument<T> toTry, @Nullable CatchRunnable toCatch, @Nullable FinallyRunnableWithArgument<T> toFinally) {
        threadNonDaemon(() -> {
            try { toTry.run(object);
            } catch (Exception e) { if (toCatch != null) toCatch.run(e);
            } finally { if (toFinally != null) toFinally.run(object); }
        });
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public static void threadLogger() {
        threadLoggerKill();
        THREADLG = thread(() -> {
            while (true) {
                trySimple(ThreadUtil::showThreads);
                trySimple(() -> Thread.sleep(2000));
                if (THREADLG == null) throw new IllegalStateException("Thread logger was lost");
            }
        });
    }

    public static void threadLoggerKill() {
        trySimple(() -> {
            if (threadLoggerEnabled()) THREADLG.interrupt();
            THREADLG = null;
            System.gc();
        });
    }

    public static boolean threadLoggerEnabled() { return THREADLG != null && !THREADLG.isInterrupted(); }

    public static void showThreads() {
        LOGGER.info("{}\t{}\t{}\t{}\n", "Name", "State", "Priority", "isDaemon");
        for (var t: Thread.getAllStackTraces().keySet())
            LOGGER.info("{}\t{}\t{}\t{}\n", t.getName(), t.getState(), t.getPriority(), t.isDaemon());
    }

    public interface ReturnableRunnable<T> { T run(T defaultVar) throws Exception; }
    public interface ReturnableFinallyRunnable<T> { void run(@NotNull T returnedVar); }
    public interface SimpleTryRunnable { void run() throws Exception; }

    public interface TryRunnableWithArgument<T> {  void run(T object) throws Exception; }
    public interface FinallyRunnableWithArgument<T> { void run(T object); }

    public interface TryRunnable {  void run() throws Exception; }
    public interface CatchRunnable {  void run(Exception e); }
    public interface FinallyRunnable { void run(); }
}
