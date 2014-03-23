package org.hidetake.gradle.ssh.internal.operation

import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.ssh.Connection

/**
 * Default implementation of {@link Operations.Factory}.
 *
 * @author hidetake.org
 */
@Singleton
class DefaultOperationsFactory implements Operations.Factory {
    @Override
    Operations create(Connection connection, SshSettings sshSettings) {
        new DefaultOperations(connection, sshSettings)
    }

    @Override
    Operations create(Remote remote) {
        new DryRunOperations(remote)
    }
}
