/*
 * Stepping the search forward on a timer.
 */
package esu.algorithm.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Class Playback
 *
 * Runs an action repeatedly at a chosen rate, and stops when the action says
 * there is nothing left to do.
 *
 * Pulled out of the window so that nothing else has to know a Timeline is
 * involved, and so the rules about stopping it — on reset, on load, on
 * closing — live in one place rather than being remembered at each call.
 */
public class Playback {

    /** What to do on each tick. */
    public interface Tick {
        /**
         * @return false when there is nothing left to do, which stops playback
         */
        boolean advance();
    }

    private final Tick tick;
    private final Runnable onStop;
    private Timeline timeline;
    private double stepsPerSecond = 6;

    /**
     * @param tick   performed on every tick
     * @param onStop run whenever playback stops, for whatever reason
     */
    public Playback(Tick tick, Runnable onStop) {
        this.tick = tick;
        this.onStop = onStop;
    }

    /** Start, or restart at the current rate. */
    public void start() {
        stop();
        timeline = new Timeline(new KeyFrame(
                Duration.seconds(1.0 / stepsPerSecond), event -> {
            if (!tick.advance()) {
                stop();
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /** Stop. Safe to call when nothing is playing. */
    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
            onStop.run();
        }
    }

    public boolean isPlaying() {
        return timeline != null;
    }

    /**
     * Change the rate, restarting only if something is already playing.
     *
     * @param stepsPerSecond ticks per second
     */
    public void setRate(double stepsPerSecond) {
        this.stepsPerSecond = stepsPerSecond;
        if (isPlaying()) {
            start();
        }
    }
}
