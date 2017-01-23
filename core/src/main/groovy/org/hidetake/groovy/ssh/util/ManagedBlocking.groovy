package org.hidetake.groovy.ssh.util

import java.util.concurrent.ForkJoinPool

/**
 * A convenient class of {@link ForkJoinPool.ManagedBlocker}.
 *
 * @author Hidetake Iwata
 */
class ManagedBlocking {
    /**
     * Wait until condition is satisfied.
     *
     * @param intervalMillis polling interval
     * @param condition returns true if polling is completed
     */
    static void until(long intervalMillis = 100L, Closure<Boolean> condition) {
        ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker() {
            @Override
            boolean block() throws InterruptedException {
                if (!isReleasable()) {
                    sleep(intervalMillis)
                }
                isReleasable()
            }

            @Override
            boolean isReleasable() {
                condition.call()
            }
        })
    }

    /**
     * Wait given time.
     * @param millis
     */
    static void sleep(long millis) {
        def started = System.currentTimeMillis()
        until {
            (System.currentTimeMillis() - started) > millis
        }
    }
}
