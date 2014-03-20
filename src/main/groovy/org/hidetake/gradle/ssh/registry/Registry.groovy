package org.hidetake.gradle.ssh.registry

import org.hidetake.gradle.ssh.api.operation.OperationsFactory
import org.hidetake.gradle.ssh.api.session.SessionHandlerFactory
import org.hidetake.gradle.ssh.api.session.SessionsFactory
import org.hidetake.gradle.ssh.internal.operation.DefaultOperations
import org.hidetake.gradle.ssh.internal.session.DefaultSessions
import org.hidetake.gradle.ssh.internal.session.SessionDelegate
import org.hidetake.gradle.ssh.ssh.api.ConnectionManagerFactory
import org.hidetake.gradle.ssh.ssh.internal.DefaultConnectionManagerFactory

@Singleton
class Registry extends AbstractRegistry {
    @Override
    void wire() {
        this[ConnectionManagerFactory] = DefaultConnectionManagerFactory.instance

        factory(SessionsFactory, DefaultSessions)
        factory(SessionHandlerFactory, SessionDelegate)
        factory(OperationsFactory, DefaultOperations)
    }
}
