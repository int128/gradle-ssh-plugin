package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.Session
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.session.forwarding.LocalPortForwardSettings
import org.hidetake.groovy.ssh.session.forwarding.RemotePortForwardSettings

/**
 * A connected SSH connection.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Connection {
    final Remote remote
    final Session session

    /**
     * Constructor
     * @param remote1
     * @param session1 connected session
     * @return an instance
     */
    def Connection(Remote remote1, Session session1) {
        remote = remote1
        session = session1
        assert remote
        assert session
    }

    /**
     * Create an execution channel.
     *
     * @return a channel
     */
    ChannelExec createExecutionChannel() {
        session.openChannel('exec') as ChannelExec
    }

    /**
     * Create a shell channel.
     *
     * @param operationSettings
     * @return a channel
     */
    ChannelShell createShellChannel() {
        session.openChannel('shell') as ChannelShell
    }

    /**
     * Create a SFTP channel.
     *
     * @return a channel
     */
    ChannelSftp createSftpChannel() {
        session.openChannel('sftp') as ChannelSftp
    }

    /**
     * Set up local port forwarding.
     *
     * @param settings
     * @return local port
     */
    int forwardLocalPort(LocalPortForwardSettings settings) {
        session.setPortForwardingL(settings.bind, settings.port, settings.host, settings.hostPort)
    }

    /**
     * Set up remote port forwarding.
     *
     * @param settings
     */
    void forwardRemotePort(RemotePortForwardSettings settings) {
        session.setPortForwardingR(settings.bind, settings.port, settings.host, settings.hostPort)
    }

    /**
     * Cleanup the connection and all channels.
     */
    void close() {
        session.disconnect()
        log.info("Disconnected from $remote")
    }

    @Override
    String toString() {
        "Connection[$remote]"
    }
}
