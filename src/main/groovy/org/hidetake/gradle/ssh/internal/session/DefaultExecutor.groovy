package org.hidetake.gradle.ssh.internal.session

import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.session.Executor
import org.hidetake.gradle.ssh.api.session.SessionHandlerFactory
import org.hidetake.gradle.ssh.registry.Registry
import org.hidetake.gradle.ssh.ssh.api.ConnectionManagerFactory

/**
 * A default implementation of executor.
 *
 * @author hidetake.org
 */
@Singleton
@Slf4j
class DefaultExecutor implements Executor {
    @Override
    void execute(SshSettings sshSettings, List<SessionSpec> sessionSpecs) {
        def connectionManager = Registry.instance[ConnectionManagerFactory].create(sshSettings)
        try {
            sessionSpecs.collect { sessionSpec ->
                def factory = Registry.instance[SessionHandlerFactory]
                def sessionHandler = factory.create(sessionSpec.remote, connectionManager, sshSettings)
                sessionSpec.operationClosure.delegate = sessionHandler
                sessionSpec.operationClosure.resolveStrategy = Closure.DELEGATE_FIRST
                sessionSpec.operationClosure
            }.each {
                it.call()
            }

            while (connectionManager.anyPending) {
                sleep(100)
            }
            if (connectionManager.anyError) {
                throw new RuntimeException('At least one session finished with error')
            }
        } finally {
            connectionManager.cleanup()
        }
    }
}
