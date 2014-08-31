package org.hidetake.groovy.ssh.internal.session

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.api.OperationSettings
import org.hidetake.groovy.ssh.api.Remote
import org.hidetake.groovy.ssh.api.session.SessionHandler
import org.hidetake.groovy.ssh.internal.connection.ConnectionManager
import org.hidetake.groovy.ssh.internal.operation.OperationService

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
