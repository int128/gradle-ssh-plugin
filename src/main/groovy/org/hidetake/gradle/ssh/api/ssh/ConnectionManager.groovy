package org.hidetake.gradle.ssh.api.ssh

import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SshSettings
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
        ConnectionManager create(SshSettings sshSettings)
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
     * This method throws a {@link BackgroundCommandException} if any error occurs.
     */
    void waitForPending()

    /**
     * Cleanup all connections.
     */
    void cleanup()
}
