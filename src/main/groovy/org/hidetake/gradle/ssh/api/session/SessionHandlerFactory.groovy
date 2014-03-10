package org.hidetake.gradle.ssh.api.session

import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.internal.session.ChannelManager
import org.hidetake.gradle.ssh.internal.session.SessionManager

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
     * @param sessionManager
     * @param channelManager
     * @param sshSettings
     * @return an instance
     */
    SessionHandler create(Remote remote, SessionManager sessionManager, ChannelManager channelManager, SshSettings sshSettings)
}
