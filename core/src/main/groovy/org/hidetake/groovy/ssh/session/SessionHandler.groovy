package org.hidetake.groovy.ssh.session

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.settings.CompositeSettings
import org.hidetake.groovy.ssh.operation.Operations
import org.hidetake.groovy.ssh.operation.SftpOperations

/**
 * A handler of {@link org.hidetake.groovy.ssh.core.RunHandler#session(Remote, groovy.lang.Closure)}.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class SessionHandler implements SessionExtensions {
    final Operations operations

    /**
     * Settings with default, global, per-service and per-remote.
     */
    final CompositeSettings mergedSettings

    static def create(Operations operations, CompositeSettings mergedSettings) {
        def handler = new SessionHandler(operations, mergedSettings)
        mergedSettings.extensions.inject(handler) { applied, extension ->
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

    private def SessionHandler(Operations operations1, CompositeSettings mergedSettings1) {
        operations = operations1
        mergedSettings = mergedSettings1
    }

    @Override
    Remote getRemote() {
        operations.remote
    }

    @Override
    def <T> T sftp(@DelegatesTo(SftpOperations) Closure<T> closure) {
        assert closure, 'closure must be given'
        operations.sftp(mergedSettings, closure)
    }
}
