package org.hidetake.groovy.ssh.interaction

/**
 * An exception thrown if one or more exceptions occurred while stream interaction.
 *
 * @author Hidetake Iwata
 */
class InteractionException extends RuntimeException {
    final List<Throwable> exceptions

    def InteractionException(Throwable exception) {
        super("Error while stream interaction: $exception", exception)
        this.exceptions = [exception]
    }

    def InteractionException(Throwable... exceptions) {
        super("Error while stream interaction: $exceptions")
        this.exceptions = exceptions
    }
}
