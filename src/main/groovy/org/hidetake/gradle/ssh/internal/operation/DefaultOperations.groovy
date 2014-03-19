package org.hidetake.gradle.ssh.internal.operation

import com.jcraft.jsch.ChannelSftp
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.codehaus.groovy.tools.Utilities
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.operation.ExecutionSettings
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.operation.ShellSettings
import org.hidetake.gradle.ssh.ssh.api.Connection

/**
 * Default implementation of {@link org.hidetake.gradle.ssh.api.operation.Operations}.
 *
 * @author hidetake.org
 */
@TupleConstructor
@Slf4j
class DefaultOperations implements Operations {
    final Connection connection
    final SshSettings sshSettings

    @Override
    Remote getRemote() {
        connection.remote
    }

    @Override
    void shell(ShellSettings settings, Closure closure) {
        def channel = connection.createShellChannel(settings)
        def context = ShellDelegate.create(channel, sshSettings.encoding)
        if (settings.logging) {
            context.enableLogging(sshSettings.outputLogLevel)
        }

        closure.delegate = context
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()

        try {
            channel.connect()
            log.info("Channel #${channel.id} has been opened")
            while (!channel.closed) {
                sleep(100)
            }
            log.info("Channel #${channel.id} has been closed with exit status ${channel.exitStatus}")
            if (channel.exitStatus != 0) {
                throw new RuntimeException("Shell session finished with exit status ${channel.exitStatus}")
            }
        } finally {
            channel.disconnect()
        }
    }

    @Override
    String execute(ExecutionSettings settings, String command, Closure closure) {
        def channel = connection.createExecutionChannel(command, settings)
        def context = ExecutionDelegate.create(channel, sshSettings.encoding)
        if (settings.logging) {
            context.enableLogging(sshSettings.outputLogLevel, sshSettings.errorLogLevel)
        }

        def lines = [] as List<String>
        context.standardOutput.lineListeners.add { String line -> lines << line }

        closure.delegate = context
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()

        try {
            channel.connect()
            log.info("Channel #${channel.id} has been opened")
            while (!channel.closed) {
                sleep(100)
            }
            log.info("Channel #${channel.id} has been closed with exit status ${channel.exitStatus}")
            if (channel.exitStatus != 0) {
                throw new RuntimeException(
                    "Command ($command) execution session finished with exit status ${channel.exitStatus}")
            }
            lines.join(Utilities.eol())
        } finally {
            channel.disconnect()
        }
    }

    @Override
    String executeSudo(ExecutionSettings settings, String command) {
        def prompt = UUID.randomUUID().toString()
        def lines = [] as List<String>
        execute(settings, "sudo -S -p '$prompt' $command") {
            interaction {
                when(partial: prompt, from: standardOutput) {
                    log.info("Sending password for sudo authentication on channel #${channel.id}")
                    standardInput << remote.password << '\n'

                    when(nextLine: _, from: standardOutput) {
                        when(nextLine: 'Sorry, try again.') {
                            throw new RuntimeException("Sudo authentication failed on channel #${channel.id}")
                        }
                        when(line: _, from: standardOutput) {
                            lines << it
                        }
                    }
                }
                when(line: _, from: standardOutput) {
                    lines << it
                }
            }
        }

        lines.join(Utilities.eol())
    }

    @Override
    void executeBackground(ExecutionSettings settings, String command) {
        def channel = connection.createExecutionChannel(command, settings)
        def context = ExecutionDelegate.create(channel, sshSettings.encoding)
        if (settings.logging) {
            context.enableLogging(sshSettings.outputLogLevel, sshSettings.errorLogLevel)
        }

        channel.connect()
        log.info("Channel #${channel.id} has been opened")

        connection.whenClosed(channel) {
            log.info("Channel #${channel.id} has been closed with exit status ${channel.exitStatus}")
            channel.disconnect()
        }
    }

    @Override
    void get(String remote, String local) {
        def channel = connection.createSftpChannel()
        try {
            channel.connect()
            log.info("Channel #${channel.id} has been opened")
            channel.get(remote, local)
            log.info("Channel #${channel.id} has been closed with exit status ${channel.exitStatus}")
        } finally {
            channel.disconnect()
        }
    }

    @Override
    void put(String local, String remote) {
        def channel = connection.createSftpChannel()
        try {
            channel.connect()
            log.info("Channel #${channel.id} has been opened")
            channel.put(local, remote, ChannelSftp.OVERWRITE)
            log.info("Channel #${channel.id} has been closed with exit status ${channel.exitStatus}")
        } finally {
            channel.disconnect()
        }
    }
}
