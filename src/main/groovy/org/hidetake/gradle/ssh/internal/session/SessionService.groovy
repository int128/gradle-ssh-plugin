package org.hidetake.gradle.ssh.internal.session

import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.internal.connection.ConnectionManager
import org.hidetake.gradle.ssh.internal.operation.OperationService
import org.hidetake.gradle.ssh.plugin.OperationSettings
import org.hidetake.gradle.ssh.plugin.Remote
import org.hidetake.gradle.ssh.plugin.session.SessionHandler

@Singleton(lazy = true)
@Slf4j
class SessionService {
    SessionHandler createDelegate(Remote remote, OperationSettings operationSettings, ConnectionManager connectionManager) {
        def operationService = OperationService.instance
        def operations = operationService.create(remote, operationSettings, connectionManager)

        def handler = new DefaultSessionHandler(operations, operationSettings)
        log.debug("Mixin extensions: ${operationSettings.extensions}")
        handler.metaClass.mixin(operationSettings.extensions)
        handler
    }
}
