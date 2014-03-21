package org.hidetake.gradle.ssh.internal.session

import groovy.transform.TupleConstructor
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.session.SessionHandler
import org.hidetake.gradle.ssh.api.session.Sessions
import org.hidetake.gradle.ssh.ssh.api.ConnectionManager

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

        /**
         * Establish a connection if dry run is turned off.
         *
         * @param connectionManager
         * @param sshSettings
         * @return
         */
        EstablishedSession establish(ConnectionManager connectionManager, SshSettings sshSettings) {
            if (sshSettings.dryRun) {
                new EstablishedSession(this)
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
            if (connectionManager.anyError) {
                throw new RuntimeException('At least one session finished with error')
            }
        } finally {
            connectionManager.cleanup()
        }
    }
}
