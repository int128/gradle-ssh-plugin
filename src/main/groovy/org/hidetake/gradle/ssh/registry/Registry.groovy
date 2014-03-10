package org.hidetake.gradle.ssh.registry

import org.hidetake.gradle.ssh.api.operation.OperationsFactory
import org.hidetake.gradle.ssh.api.session.Executor
import org.hidetake.gradle.ssh.api.session.SessionHandlerFactory
import org.hidetake.gradle.ssh.internal.operation.DefaultOperations
import org.hidetake.gradle.ssh.internal.session.DefaultExecutor
import org.hidetake.gradle.ssh.internal.session.SessionDelegateFactory

@Singleton
class Registry extends AbstractRegistry {
    @Override
    void wire() {
        this[Executor] = DefaultExecutor.instance
        this[SessionHandlerFactory] = SessionDelegateFactory.instance

        factory(OperationsFactory, DefaultOperations)
    }
}
