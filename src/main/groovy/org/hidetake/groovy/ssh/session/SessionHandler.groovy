package org.hidetake.groovy.ssh.session

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.extension.CoreExtensions
import org.hidetake.groovy.ssh.operation.Operations
import org.hidetake.groovy.ssh.operation.SftpOperations

/**
 * A handler of {@link org.hidetake.groovy.ssh.core.RunHandler#session(Remote, groovy.lang.Closure)}.
 *
 * @author hidetake.org
 */
@Slf4j
class SessionHandler implements SessionExtension {
    private final Operations operations

    private final OperationSettings operationSettings

    static def create(Operations operations, OperationSettings operationSettings) {
        log.debug("Extensions: ${operationSettings.extensions}")
        def handler = new SessionHandler(operations, operationSettings)
        handler.withTraits(CoreExtensions).withTraits(operationSettings.extensions as Class[])
    }

    private def SessionHandler(Operations operations1, OperationSettings operationSettings1) {
        operations = operations1
        operationSettings = operationSettings1
    }

    @Override
    Operations getOperations() {
        operations
    }

    @Override
    OperationSettings getOperationSettings() {
        operationSettings
    }

    @Override
    Remote getRemote() {
        operations.remote
    }

    @Override
    def sftp(@DelegatesTo(SftpOperations) Closure closure) {
        assert closure, 'closure must be given'
        log.info("Execute a SFTP subsystem")
        operations.sftp(closure)
    }
}
