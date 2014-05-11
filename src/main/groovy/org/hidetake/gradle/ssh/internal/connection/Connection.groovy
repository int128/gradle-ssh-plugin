package org.hidetake.gradle.ssh.internal.connection

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.ChannelShell
import org.hidetake.gradle.ssh.plugin.OperationSettings
import org.hidetake.gradle.ssh.plugin.Remote

/**
 * A SSH connection.
 *
 * @author hidetake.org
 */
interface Connection {
    /**
     * Return the remote host.
     *
     * @return the remote host
     */
    Remote getRemote()

    /**
     * Create an execution channel.
     *
     * @param command
     * @param operationSettings
     * @return a channel
     */
    ChannelExec createExecutionChannel(String command, OperationSettings operationSettings)

    /**
     * Create a shell channel.
     *
     * @param operationSettings
     * @return a channel
     */
    ChannelShell createShellChannel(OperationSettings operationSettings)

    /**
     * Create a SFTP channel.
     *
     * @return a channel
     */
    ChannelSftp createSftpChannel()

    /**
     * Register a closure called when the channel is closed.
     *
     * @param channel the channel
     * @param closure callback closure
     */
    void whenClosed(Channel channel, Closure closure)

    /**
     * Execute registered closures.
     * This method throws a {@link org.hidetake.gradle.ssh.plugin.session.BackgroundCommandException} if any closure returns an exception.
     *
     * @see #whenClosed(com.jcraft.jsch.Channel, groovy.lang.Closure)
     */
    void executeCallbackForClosedChannels()

    /**
     * Return if any channel is pending.
     *
     * @return true if at least one is pending
     */
    boolean isAnyPending()

    /**
     * Cleanup the connection and all channels.
     */
    void close()
}
