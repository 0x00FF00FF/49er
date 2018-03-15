package org.rares.miner49er.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author rares
 * @since 05.10.2017
 */

public class NumberUtils {
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    private static final AtomicInteger projectsIds = new AtomicInteger(Integer.MAX_VALUE);
    private static final AtomicInteger elevationDegree = new AtomicInteger(21);

    public static int getRandomInt(int min, int max) {
        Random r = new Random();
        return r.nextInt(max - min) + min;
    }

    /**
     * Generate a value suitable for use in {@link android.view.View#setId(int)}.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    /**
     * Generates a new project id. Project ids go down from {@link Integer#MAX_VALUE}
     * @return a newly generated id for projects.
     */
    public static int getNextProjectId() {
        return projectsIds.getAndDecrement();
    }


    /**
     * Static method that returns a lower elevation value each time it's called.
     * @return an elevation level, but lower than the one returned previously.
     */
    public static int getNextElevation(){
        return elevationDegree.decrementAndGet();
    }

    public static int getCurrentElevation(){
        return elevationDegree.get();
    }
}
