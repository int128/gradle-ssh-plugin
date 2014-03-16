package org.hidetake.gradle.ssh.internal.session

import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.operation.OperationsFactory
import org.hidetake.gradle.ssh.api.session.SessionHandler
import org.hidetake.gradle.ssh.api.session.SessionHandlerFactory
import org.hidetake.gradle.ssh.registry.Registry
import org.hidetake.gradle.ssh.ssh.api.ConnectionManager

/**
 * A factory of {@link SessionDelegate}.
 *
 * @author hidetake.org
 */
@Singleton
class SessionDelegateFactory implements SessionHandlerFactory {
    @Override
    SessionHandler create(Remote remote, ConnectionManager connectionFactory, SshSettings sshSettings) {
        def operationsFactory = Registry.instance[OperationsFactory]

        if (sshSettings.dryRun) {
            new SessionDelegate()
        } else {
            def connection = connectionFactory.establish(remote)
            def operations = operationsFactory.create(connection, sshSettings)
            new SessionDelegate(operations)
        }
    }
}
