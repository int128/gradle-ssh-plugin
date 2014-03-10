package org.hidetake.gradle.ssh.api.operation

import com.jcraft.jsch.Session
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.internal.session.ChannelManager

/**
 * A factory interface of {@link Operations}.
 *
 * @author hidetake.org
 */
interface OperationsFactory {
    /**
     * Create an instance.
     *
     * @param remote
     * @param session
     * @param channelManager
     * @param sshSettings
     * @return an instance
     */
    Operations create(Remote remote, Session session, ChannelManager channelManager, SshSettings sshSettings)
}
