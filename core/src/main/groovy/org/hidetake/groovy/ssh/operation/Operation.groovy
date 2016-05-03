package org.hidetake.groovy.ssh.operation

import org.hidetake.groovy.ssh.interaction.InteractionHandler

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
     * Adds an interaction.
     *
     * @param closure definition of interaction
     */
    void addInteraction(@DelegatesTo(InteractionHandler) Closure closure)
}
