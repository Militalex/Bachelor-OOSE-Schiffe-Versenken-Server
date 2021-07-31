package de.gitlab.kaisoenmalex.api.util;

import java.util.function.Consumer;

/**
 * This class is used to perform an action a specific amount of time later.
 *
 * @author Alexander Ley
 * @version 1.1
 */
public class RunLater extends Tickable{

    private final int timeOut;
    private final Consumer<Void> consumer;

    private int counter = 0;

    /**
     * @param timeOut in seconds
     */
    public RunLater(int timeOut, Consumer<Void> consumer) {
        this.timeOut = timeOut;
        this.consumer = consumer;

        start(0, 1000);
    }

    @Override
    public void tick() {
        if (counter >= timeOut){
            consumer.accept(null);
            forceStop();
            return;
        }
        counter++;
    }
}
