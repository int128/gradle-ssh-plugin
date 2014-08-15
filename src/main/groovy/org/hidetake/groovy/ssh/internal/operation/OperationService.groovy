package org.hidetake.groovy.ssh.internal.operation

import org.hidetake.groovy.ssh.api.OperationSettings
import org.hidetake.groovy.ssh.api.Remote
import org.hidetake.groovy.ssh.api.operation.Operations
import org.hidetake.groovy.ssh.internal.connection.ConnectionManager

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
