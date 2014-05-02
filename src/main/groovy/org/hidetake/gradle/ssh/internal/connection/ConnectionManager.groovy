package org.hidetake.gradle.ssh.internal.connection

import org.hidetake.gradle.ssh.plugin.ConnectionSettings
import org.hidetake.gradle.ssh.plugin.Remote
import org.hidetake.gradle.ssh.registry.Registry

/**
 * A factory of {@link Connection}.
 *
 * @author hidetake.org
 */
interface ConnectionManager {
    /**
     * A factory of {@link ConnectionManager}.
     */
    interface Factory {
        ConnectionManager create(ConnectionSettings connectionSettings)
    }

    final factory = Registry.instance[Factory]

    /**
     * Establish a connection.
     *
     * @param remote the remote host
     * @return a connection
     */
    Connection establish(Remote remote)

    /**
     * Wait for pending sessions.
     * This method throws a {@link org.hidetake.gradle.ssh.plugin.session.BackgroundCommandException} if any error occurs.
     */
    void waitForPending()

    /**
     * Cleanup all connections.
     */
    void cleanup()
}
