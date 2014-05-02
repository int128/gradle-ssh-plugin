package org.hidetake.gradle.ssh.internal.operation

import org.hidetake.gradle.ssh.internal.connection.Connection
import org.hidetake.gradle.ssh.plugin.Remote

/**
 * Default implementation of {@link Operations.Factory}.
 *
 * @author hidetake.org
 */
@Singleton
class DefaultOperationsFactory implements Operations.Factory {
    @Override
    Operations create(Connection connection) {
        new DefaultOperations(connection)
    }

    @Override
    Operations create(Remote remote) {
        new DryRunOperations(remote)
    }
}
