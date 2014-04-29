package org.hidetake.gradle.ssh.internal.session

import org.hidetake.gradle.ssh.internal.connection.ConnectionManager
import org.hidetake.gradle.ssh.internal.operation.OperationService
import org.hidetake.gradle.ssh.plugin.OperationSettings
import org.hidetake.gradle.ssh.plugin.Remote
import org.hidetake.gradle.ssh.plugin.session.SessionHandler

@Singleton(lazy = true)
class SessionService {
    SessionHandler createDelegate(Remote remote, OperationSettings operationSettings, ConnectionManager connectionManager) {
        def operationService = OperationService.instance
        def operations = operationService.create(remote, operationSettings, connectionManager)
        new DefaultSessionHandler(operations, operationSettings)
    }
}
