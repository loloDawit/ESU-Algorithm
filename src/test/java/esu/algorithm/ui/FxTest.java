package esu.algorithm.ui;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Class FxTest
 *
 * Enough of JavaFX to build real controls and look at them, without showing a
 * window or pulling in a UI-driving framework.
 *
 * The views had no automated coverage at all, and every visual bug this
 * project shipped reached a person before it reached a test. What those bugs
 * had in common was not a mishandled click: it was the wrong nodes being
 * built, or built with the wrong style. That is what this can see.
 *
 * What it cannot see is whether the result looks good. Screenshots still
 * answer that.
 */
public abstract class FxTest {

    private static boolean started;

    /** Starts the toolkit once for the whole run. */
    protected static synchronized void startToolkit() {
        if (started) {
            return;
        }
        CountDownLatch ready = new CountDownLatch(1);
        try {
            Platform.startup(ready::countDown);
        } catch (IllegalStateException alreadyRunning) {
            ready.countDown();
        }
        try {
            assertTrue(ready.await(30, TimeUnit.SECONDS),
                    "the JavaFX toolkit did not start");
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            fail("interrupted waiting for the toolkit");
        }
        Platform.setImplicitExit(false);
        started = true;
    }

    /**
     * Run something on the JavaFX thread and hand back what it made.
     *
     * @param <T>   what is being built
     * @param build builds it, on the right thread
     * @return the result
     */
    protected static <T> T onFxThread(Supplier<T> build) {
        startToolkit();
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                result.set(build.get());
            } catch (Throwable thrown) {
                failure.set(thrown);
            } finally {
                done.countDown();
            }
        });
        try {
            assertTrue(done.await(30, TimeUnit.SECONDS),
                    "nothing came back from the JavaFX thread");
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            fail("interrupted waiting for the JavaFX thread");
        }
        if (failure.get() != null) {
            throw new AssertionError("failed on the JavaFX thread", failure.get());
        }
        return result.get();
    }

    /**
     * Put a control in a scene and resolve its styles, so that what the tests
     * read is what a user would have seen.
     *
     * @param root  the control under test
     * @param dark  true to layer the dark stylesheet over the base one
     * @return the same control, styled and laid out
     */
    protected static Parent style(Parent root, boolean dark) {
        Scene scene = new Scene(root, 900, 700);
        scene.getStylesheets().add(
                FxTest.class.getResource("esu.css").toExternalForm());
        if (dark) {
            scene.getStylesheets().add(
                    FxTest.class.getResource("dark.css").toExternalForm());
        }
        root.applyCss();
        root.layout();
        return root;
    }
}
