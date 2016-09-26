package eu.codetopic.anty.lejos.utils;

public final class Utils {

    private Utils() {
    }

    public static boolean stopThread(Thread thread) {
        boolean caughtInterrupt = false;
        thread.interrupt();
        while (thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                caughtInterrupt = true;
                thread.interrupt();
            }
        }
        return caughtInterrupt;
    }

    public static void sleep(long ms) {
        tryIt(() -> Thread.sleep(ms), t -> {
            if (t instanceof InterruptedException) Thread.currentThread().interrupt();
            else t.printStackTrace();
        });
    }

    public static void waitWhile(While doWhile) {
        while (doWhile.doWhile()) Thread.yield();
    }

    public static void tryIt(TryCase tryCase) {
        tryIt(tryCase, Throwable::printStackTrace);
    }

    public static void tryIt(TryCase tryCase, OnException ex) {
        try {
            tryCase.tryIt();
        } catch (Throwable t) {
            ex.exception(t);
        }
    }

    public interface TryCase {
        void tryIt() throws Throwable;
    }

    public interface OnException {
        void exception(Throwable t);
    }

    public interface While {
        boolean doWhile();
    }
}
