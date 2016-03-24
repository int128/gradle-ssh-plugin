package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.*
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.extension.settings.LocalPortForwardSettings
import org.hidetake.groovy.ssh.extension.settings.RemotePortForwardSettings
import org.hidetake.groovy.ssh.session.BackgroundCommandException
import org.hidetake.groovy.ssh.session.BadExitStatusException

/**
 * A SSH connection.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Connection {
    final Remote remote

    private final Session session
    private final List<Channel> channels = []
    private final List<Closure> callbackForClosedChannels = []

    def Connection(Remote remote1, Session session1) {
        remote = remote1
        session = session1
        assert remote
        assert session
    }

    /**
     * Create an execution channel.
     *
     * @param command
     * @param operationSettings
     * @return a channel
     */
    ChannelExec createExecutionChannel(String command, OperationSettings operationSettings) {
        def channel = session.openChannel('exec') as ChannelExec
        channel.command = command
        channel.pty = operationSettings.pty
        channels.add(channel)
        channel
    }

    /**
     * Create a shell channel.
     *
     * @param operationSettings
     * @return a channel
     */
    ChannelShell createShellChannel(OperationSettings operationSettings) {
        def channel = session.openChannel('shell') as ChannelShell
        channels.add(channel)
        channel
    }

    /**
     * Create a SFTP channel.
     *
     * @return a channel
     */
    ChannelSftp createSftpChannel() {
        def channel = session.openChannel('sftp') as ChannelSftp
        channels.add(channel)
        channel
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
     * Register a closure called when the channel is closed.
     *
     * @param channel the channel
     * @param closure callback closure
     */
    void whenClosed(Channel channel, Closure closure) {
        boolean executed = false
        callbackForClosedChannels.add { ->
            if (!executed && channel.closed) {
                executed = true
                closure(channel)
            }
        }
    }

    /**
     * Execute registered closures.
     * This method throws a {@link BackgroundCommandException} if any closure returns an exception.
     *
     * @see #whenClosed(com.jcraft.jsch.Channel, groovy.lang.Closure)
     */
    void executeCallbackForClosedChannels() {
        List<Exception> exceptions = []
        callbackForClosedChannels.each { callback ->
            try {
                callback.call()
            } catch (Exception e) {
                exceptions.add(e)
                if (e instanceof BadExitStatusException) {
                    log.error("${e.class.name}: ${e.localizedMessage}")
                } else {
                    log.error("Error occurred on $remote", e)
                }
            }
        }
        if (!exceptions.empty) {
            throw new BackgroundCommandException(exceptions)
        }
    }

    /**
     * Return if any channel is pending.
     *
     * @return true if at least one is pending
     */
    boolean isAnyPending() {
        channels.any { channel -> channel.connected && !channel.closed }
    }

    /**
     * Cleanup the connection and all channels.
     */
    void close() {
        try {
            channels*.disconnect()
            channels.clear()
        } finally {
            session.disconnect()
        }
    }
}
