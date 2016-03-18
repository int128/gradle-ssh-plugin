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
        operationSettings.extensions.inject(handler) { applied, extension ->
            if (extension instanceof Class) {
                log.debug("Applying extension: $extension")
                applied.withTraits(extension)
            } else if (extension instanceof Map<String, Closure>) {
                extension.each { String name, Closure implementation ->
                    log.debug("Applying extension method: $name")
                    applied.metaClass[name] = implementation
                }
                applied
            } else {
                log.error("Ignored unknown extension: $extension")
                applied
            }
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
    def <T> T sftp(@DelegatesTo(SftpOperations) Closure<T> closure) {
        assert closure, 'closure must be given'
        operations.sftp(closure)
    }
}
