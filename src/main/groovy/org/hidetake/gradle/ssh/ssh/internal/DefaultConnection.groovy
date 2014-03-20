package org.hidetake.gradle.ssh.ssh.internal

import com.jcraft.jsch.*
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.operation.ExecutionSettings
import org.hidetake.gradle.ssh.api.operation.ShellSettings
import org.hidetake.gradle.ssh.ssh.api.Connection

/**
 * A default implementation of SSH connection.
 *
 * @author hidetake.org
 */
@TupleConstructor
@Slf4j
class DefaultConnection implements Connection {
    final Remote remote
    final Session session
    final List<Channel> channels = []
    final List<Closure> whenClosedClosures = []

    @Override
    ChannelExec createExecutionChannel(String command, ExecutionSettings executionSettings) {
        def channel = session.openChannel('exec') as ChannelExec
        channel.command = command
        channel.pty = executionSettings.pty
        channels.add(channel)
        channel
    }

    @Override
    ChannelShell createShellChannel(ShellSettings shellSettings) {
        def channel = session.openChannel('shell') as ChannelShell
        channels.add(channel)
        channel
    }

    @Override
    ChannelSftp createSftpChannel() {
        def channel = session.openChannel('sftp') as ChannelSftp
        channels.add(channel)
        channel
    }

    @Override
    void whenClosed(Channel channel, Closure closure) {
        boolean executed = false
        whenClosedClosures.add { ->
            if (!executed && channel.closed) {
                closure(channel)
                executed = true
            }
        }
    }

    @Override
    void executeWhenClosedClosures() {
        whenClosedClosures*.call()
    }

    @Override
    boolean isAnyPending() {
        channels.any { channel -> !channel.closed }
    }

    @Override
    boolean isAnyError() {
        channels.findAll { it instanceof ChannelExec || it instanceof ChannelShell }
                .any { channel -> channel.exitStatus != 0 }
    }

    @Override
    void cleanup() {
        whenClosedClosures*.call()
        channels*.disconnect()
        channels.clear()
    }
}
