package org.hidetake.gradle.ssh.api.ssh

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.ChannelShell
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.operation.ExecutionSettings
import org.hidetake.gradle.ssh.api.operation.ShellSettings

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
     * @param executionSettings
     * @return a channel
     */
    ChannelExec createExecutionChannel(String command, ExecutionSettings executionSettings)

    /**
     * Create a shell channel.
     *
     * @param shellSettings
     * @return a channel
     */
    ChannelShell createShellChannel(ShellSettings shellSettings)

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
     * This method throws a {@link BackgroundCommandException} if any closure returns an exception.
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
     * Cleanup all channels.
     */
    void cleanup()
}
