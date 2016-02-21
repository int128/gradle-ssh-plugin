package org.hidetake.groovy.ssh.operation

/**
 * An operation such as a command or shell execution.
 *
 * @author Hidetake Iwata
 */
interface Operation {
    /**
     * Start the operation synchronously.
     * @return exit status
     */
    int startSync()

    /**
     * Start the operation asynchronously.
     * @param closure callback
     */
    void startAsync(Closure closure)

    /**
     * Register a callback on each line of the standard output.
     * @param closure a callback
     */
    void onEachLineOfStandardOutput(Closure closure)
}
