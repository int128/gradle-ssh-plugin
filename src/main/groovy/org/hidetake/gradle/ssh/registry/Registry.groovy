package org.hidetake.gradle.ssh.registry

import org.hidetake.gradle.ssh.internal.DefaultSshTaskHandler
import org.hidetake.gradle.ssh.internal.connection.ConnectionManager
import org.hidetake.gradle.ssh.internal.connection.DefaultConnectionManager
import org.hidetake.gradle.ssh.internal.operation.DefaultOperationsFactory
import org.hidetake.gradle.ssh.internal.operation.DefaultSftpHandler
import org.hidetake.gradle.ssh.internal.operation.Operations
import org.hidetake.gradle.ssh.internal.operation.SftpHandler
import org.hidetake.gradle.ssh.internal.session.DefaultSessionHandler
import org.hidetake.gradle.ssh.internal.session.DefaultSessions
import org.hidetake.gradle.ssh.internal.session.Sessions
import org.hidetake.gradle.ssh.plugin.SshTaskHandler
import org.hidetake.gradle.ssh.plugin.session.SessionHandler

/**
 * A component registry.
 *
 * @author hidetake.org
 */
@Singleton
class Registry {
    @SuppressWarnings("GroovyUnusedDeclaration")
    @Delegate
    private final RegistrySupport registrySupport = new RegistrySupport()

    private Registry() {
        factory(SshTaskHandler.Factory) >> DefaultSshTaskHandler

        factory(Sessions.Factory) >> DefaultSessions
        factory(SessionHandler.Factory) >> DefaultSessionHandler
        factory(ConnectionManager.Factory) >> DefaultConnectionManager

        factory(SftpHandler.Factory) >> DefaultSftpHandler

        singleton(Operations.Factory) >> DefaultOperationsFactory.instance
    }
}
