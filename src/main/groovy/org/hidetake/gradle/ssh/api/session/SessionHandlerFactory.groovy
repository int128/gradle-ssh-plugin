package org.hidetake.gradle.ssh.api.session

import org.hidetake.gradle.ssh.api.operation.Operations

/**
 * A factory interface of {@link SessionHandler}.
 *
 * @author hidetake.org
 */
interface SessionHandlerFactory {
    /**
     * Create an instance with null operations.
     *
     * @return an instance
     */
    SessionHandler create()

    /**
     * Create an instance.
     *
     * @param operations
     * @return an instance
     */
    SessionHandler create(Operations operations)
}
