package org.hidetake.gradle.ssh.api.operation

import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.ssh.api.Connection

/**
 * A factory interface of {@link Operations}.
 *
 * @author hidetake.org
 */
interface OperationsFactory {
    /**
     * Create an instance.
     *
     * @param connection
     * @param sshSettings
     * @return an instance
     */
    Operations create(Connection connection, SshSettings sshSettings)
}
