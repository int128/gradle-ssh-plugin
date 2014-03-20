package org.hidetake.gradle.ssh.registry

import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.session.SessionHandler
import org.hidetake.gradle.ssh.api.session.Sessions
import org.hidetake.gradle.ssh.internal.operation.DefaultOperations
import org.hidetake.gradle.ssh.internal.session.DefaultSessions
import org.hidetake.gradle.ssh.internal.session.SessionDelegate
import org.hidetake.gradle.ssh.ssh.api.ConnectionManager
import org.hidetake.gradle.ssh.ssh.internal.DefaultConnectionManager

@Singleton
class Registry extends AbstractRegistry {
    @Override
    void wire() {
        factory(Sessions.Factory, DefaultSessions)
        factory(SessionHandler.Factory, SessionDelegate)
        factory(Operations.Factory, DefaultOperations)
        factory(ConnectionManager.Factory, DefaultConnectionManager)
    }
}
