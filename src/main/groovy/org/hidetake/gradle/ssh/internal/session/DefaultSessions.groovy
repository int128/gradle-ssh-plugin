package org.hidetake.gradle.ssh.internal.session

import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.operation.OperationSettings
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.session.SessionHandler
import org.hidetake.gradle.ssh.api.session.Sessions
import org.hidetake.gradle.ssh.api.ssh.ConnectionManager
import org.hidetake.gradle.ssh.api.ssh.ConnectionSettings

/**
 * A default implementation of {@link Sessions}.
 *
 * @author hidetake.org
 */
@Slf4j
class DefaultSessions implements Sessions {
    private static class Session {
        private final Remote remote
        private final Closure closure

        def Session(Remote remote1, Closure closure1) {
            remote = remote1
            closure = closure1
        }

        def establish(ConnectionManager connectionManager, OperationSettings operationSettings) {
            def operations = establishInternal(operationSettings.dryRun, connectionManager)
            closure.delegate = SessionHandler.factory.create(operations, operationSettings)
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure
        }

        private establishInternal(boolean dryRun, ConnectionManager connectionManager) {
            if (dryRun) {
                Operations.factory.create(remote)
            } else {
                Operations.factory.create(connectionManager.establish(remote))
            }
        }
    }

    private final List<Session> sessions = []

    @Override
    void add(Remote remote, Closure closure) {
        sessions.add(new Session(remote, closure))
    }

    @Override
    void execute(ConnectionSettings connectionSettings, OperationSettings operationSettings) {
        def connectionManager = ConnectionManager.factory.create(connectionSettings)
        try {
            sessions*.establish(connectionManager, operationSettings)*.call()

            connectionManager.waitForPending()
        } finally {
            connectionManager.cleanup()
        }
    }
}
