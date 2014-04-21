package org.hidetake.gradle.ssh.internal.session

import groovy.transform.TupleConstructor
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
    @TupleConstructor
    static class Session {
        final Remote remote
        final Closure closure

        EstablishedSession establish(ConnectionManager connectionManager, OperationSettings settings) {
            if (settings.dryRun) {
                def operations = Operations.factory.create(remote)
                new EstablishedSession(this, operations)
            } else {
                def connection = connectionManager.establish(remote)
                def operations = Operations.factory.create(connection)
                new EstablishedSession(this, operations)
            }
        }
    }

    @TupleConstructor
    static class EstablishedSession {
        final Session session
        final Operations operations

        void execute(OperationSettings settings) {
            session.closure.delegate = SessionHandler.factory.create(operations, settings)
            session.closure.resolveStrategy = Closure.DELEGATE_FIRST
            session.closure.call()
        }
    }

    final List<Session> sessions = []

    @Override
    void add(Remote remote, Closure closure) {
        sessions.add(new Session(remote, closure))
    }

    @Override
    void execute(ConnectionSettings connectionSettings, OperationSettings operationSettings) {
        def connectionManager = ConnectionManager.factory.create(connectionSettings)
        try {
            sessions*.establish(connectionManager, operationSettings)*.execute(operationSettings)

            connectionManager.waitForPending()
        } finally {
            connectionManager.cleanup()
        }
    }
}
