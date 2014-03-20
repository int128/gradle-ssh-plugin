package org.hidetake.gradle.ssh.ssh.internal

import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.ssh.api.ConnectionManager
import org.hidetake.gradle.ssh.ssh.api.ConnectionManagerFactory

/**
 * A default implementation of {@link ConnectionManagerFactory}.
 *
 * @author hidetake.org
 */
@Singleton
class DefaultConnectionManagerFactory implements ConnectionManagerFactory {
    @Override
    ConnectionManager create(SshSettings sshSettings) {
        new DefaultConnectionManager(sshSettings)
    }
}
