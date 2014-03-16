package org.hidetake.gradle.ssh.api.session

import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.ssh.api.ConnectionManager

/**
 * A factory interface of {@link SessionHandler}.
 *
 * @author hidetake.org
 */
interface SessionHandlerFactory {
    /**
     * Create an instance.
     *
     * @param remote
     * @param connectionFactory
     * @param sshSettings
     * @return an instance
     */
    SessionHandler create(Remote remote, ConnectionManager connectionFactory, SshSettings sshSettings)
}
