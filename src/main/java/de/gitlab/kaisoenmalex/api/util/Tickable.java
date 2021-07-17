package de.gitlab.kaisoenmalex.api.util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Alexander Ley
 * @version 1.4
 *
 * Represents an Object with Timer
 */
public abstract class Tickable {

    private boolean isRunning = false;
    private boolean isReady = true;

    protected Timer timer = new Timer();
    protected TimerTask task = new TimerTask() {
        @Override
        public void run() {
            tick();
        }
    };

    /**
     * Tick Loop
     */
    public abstract void tick();

    public boolean isRunning(){
        return isRunning;
    }

    public boolean isReady() {
        return isReady;
    }

    /**
     * Starts the Timer
     * @param delay Start delay.
     * @param period Timer period.
     */
    public void start(long delay, long period){
        timer.schedule(task, delay, period);
        isRunning = true;
        isReady = false;
    }

    /**
     * Stops the Timer and makes the timer ready for next start.
     */
    public void stop() {
        timer.cancel();
        isRunning = false;
    }

    /**
     * Tries to refresh timer object, so that it can start again.
     * It is only refresh-able if the timer was at least one time started and stopped.
     * Note Refreshing timer lets application open.
     */
    public void refresh(){
        if (!isRunning) {
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    tick();
                }
            };
            isReady = true;
        }
    }
}
