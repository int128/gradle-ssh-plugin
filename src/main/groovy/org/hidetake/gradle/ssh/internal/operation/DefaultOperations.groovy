package org.hidetake.gradle.ssh.internal.operation

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.Session
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.codehaus.groovy.tools.Utilities
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.operation.ExecutionSettings
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.operation.ShellSettings
import org.hidetake.gradle.ssh.internal.session.ChannelManager

/**
 * Default implementation of {@link org.hidetake.gradle.ssh.api.operation.Operations}.
 *
 * @author hidetake.org
 */
@TupleConstructor
@Slf4j
class DefaultOperations implements Operations {
    final Remote remote
    final Session session
    final ChannelManager globalChannelManager
    final SshSettings sshSettings

    @Override
    void shell(ShellSettings settings, Closure closure) {
        def channelManager = new ChannelManager()
        try {
            def channel = session.openChannel('shell') as ChannelShell
            def context = ShellDelegate.create(channel, sshSettings.encoding)
            if (settings.logging) {
                context.enableLogging(sshSettings.outputLogLevel)
            }

            closure.delegate = context
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure()

            channel.connect()
            channelManager.add(channel)
            log.info("Channel #${channel.id} has been opened")
            channelManager.waitForPending()
            log.info("Channel #${channel.id} has been closed with exit status ${channel.exitStatus}")
            channelManager.validateExitStatus()
        } finally {
            channelManager.disconnect()
        }
    }

    @Override
    String execute(ExecutionSettings settings, String command, Closure closure) {
        def channelManager = new ChannelManager()
        try {
            def channel = session.openChannel('exec') as ChannelExec
            channel.command = command
            channel.pty = settings.pty

            def context = ExecutionDelegate.create(channel, sshSettings.encoding)
            if (settings.logging) {
                context.enableLogging(sshSettings.outputLogLevel, sshSettings.errorLogLevel)
            }

            def lines = [] as List<String>
            context.standardOutput.lineListeners.add { String line -> lines << line }

            closure.delegate = context
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure()

            channel.connect()
            channelManager.add(channel)
            log.info("Channel #${channel.id} has been opened")
            channelManager.waitForPending()
            log.info("Channel #${channel.id} has been closed with exit status ${channel.exitStatus}")
            channelManager.validateExitStatus()
            lines.join(Utilities.eol())
        } finally {
            channelManager.disconnect()
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
        def channel = session.openChannel('exec') as ChannelExec
        channel.command = command
        channel.pty = settings.pty

        def context = ExecutionDelegate.create(channel, sshSettings.encoding)
        if (settings.logging) {
            context.enableLogging(sshSettings.outputLogLevel, sshSettings.errorLogLevel)
        }

        globalChannelManager.add(channel)
        channel.connect()
        log.info("Channel #${channel.id} has been opened")
    }

    @Override
    void get(String remote, String local) {
        def channel = session.openChannel('sftp') as ChannelSftp
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
        def channel = session.openChannel('sftp') as ChannelSftp
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
