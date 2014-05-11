package org.hidetake.gradle.ssh.internal.operation

import org.hidetake.gradle.ssh.internal.connection.ConnectionManager
import org.hidetake.gradle.ssh.plugin.OperationSettings
import org.hidetake.gradle.ssh.plugin.Remote

@Singleton(lazy = true)
class OperationService {
    Operations create(Remote remote, OperationSettings operationSettings, ConnectionManager connectionManager) {
        if (operationSettings.dryRun) {
            new DryRunOperations(remote)
        } else {
            new DefaultOperations(connectionManager.connect(remote))
        }
    }
}
