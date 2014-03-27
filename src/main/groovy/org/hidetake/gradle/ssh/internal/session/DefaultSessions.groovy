package org.hidetake.gradle.ssh.internal.session

import groovy.transform.TupleConstructor
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.session.SessionHandler
import org.hidetake.gradle.ssh.api.session.Sessions
import org.hidetake.gradle.ssh.api.ssh.ConnectionManager

/**
 * A default implementation of {@link Sessions}.
 *
 * @author hidetake.org
 */
class DefaultSessions implements Sessions {
    @TupleConstructor
    static class Session {
        final Remote remote
        final Closure closure

        EstablishedSession establish(ConnectionManager connectionManager, SshSettings sshSettings) {
            if (sshSettings.dryRun) {
                def operations = Operations.factory.create(remote)
                new EstablishedSession(this, operations)
            } else {
                def connection = connectionManager.establish(remote)
                def operations = Operations.factory.create(connection, sshSettings)
                new EstablishedSession(this, operations)
            }
        }
    }

    @TupleConstructor
    static class EstablishedSession {
        final Session session
        final Operations operations

        void execute() {
            session.closure.delegate = SessionHandler.factory.create(operations)
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
    void execute(SshSettings sshSettings) {
        def connectionManager = ConnectionManager.factory.create(sshSettings)
        try {
            sessions*.establish(connectionManager, sshSettings)*.execute()

            connectionManager.waitForPending()
        } finally {
            connectionManager.cleanup()
        }
    }
}
