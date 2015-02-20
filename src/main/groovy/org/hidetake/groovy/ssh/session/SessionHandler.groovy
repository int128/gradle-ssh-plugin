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
 * @author Hidetake Iwata
 */
@Slf4j
class SessionHandler implements CoreExtensions {
    private final Operations operations

    private final OperationSettings operationSettings

    static def create(Operations operations, OperationSettings operationSettings) {
        def handler = new SessionHandler(operations, operationSettings)
        if (operationSettings.extensions) {
            log.debug("Applying extensions: ${operationSettings.extensions}")
            handler.withTraits(operationSettings.extensions as Class[])
        } else {
            handler
        }
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
