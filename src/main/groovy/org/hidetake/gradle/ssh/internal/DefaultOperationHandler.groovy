package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.Session
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.codehaus.groovy.tools.Utilities
import org.hidetake.gradle.ssh.api.*
import org.hidetake.gradle.ssh.api.operation.ExecutionSettings
import org.hidetake.gradle.ssh.api.operation.ShellSettings
import org.hidetake.gradle.ssh.internal.session.ChannelManager

/**
 * Default implementation of {@link OperationHandler}.
 *
 * @author hidetake.org
 *
 */
@TupleConstructor
@Slf4j
class DefaultOperationHandler extends AbstractOperationHandler {
    final SshSpec sshSpec
    final SessionSpec sessionSpec
    final Session session
    final ChannelManager globalChannelManager

    @Override
    Remote getRemote() {
        sessionSpec.remote
    }

    @Override
    void shell(Map<String, Object> options, Closure interactions) {
        log.info("Execute a shell with options ($options)")

        def settings = new ShellSettings(logging: options.logging)
        def channelManager = new ChannelManager()
        try {
            def channel = session.openChannel('shell') as ChannelShell

            // TODO: removed in v0.3.0
            def remainingOptions = options.findAll { !(it.key in ['logging']) }
            remainingOptions.each { k, v ->
                channel[k] = v
                log.warn("Deprecated: JSch option `$k` will be no longer supported in v0.3.0")
            }

            def context = DefaultShellContext.create(channel, sshSpec.encoding)
            if (settings.logging) {
                context.enableLogging(sshSpec.outputLogLevel)
            }

            interactions.delegate = context
            interactions.resolveStrategy = Closure.DELEGATE_FIRST
            interactions()

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
    String execute(Map<String, Object> options, String command, Closure interactions) {
        log.info("Execute a command (${command}) with options ($options)")

        def settings = new ExecutionSettings(pty: options.pty, logging: options.logging)
        def channelManager = new ChannelManager()
        try {
            def channel = session.openChannel('exec') as ChannelExec
            channel.command = command
            channel.pty = settings.pty

            // TODO: removed in v0.3.0
            def remainingOptions = options.findAll { !(it.key in ['pty', 'logging']) }
            remainingOptions.each { k, v ->
                channel[k] = v
                log.warn("Deprecated: JSch option `$k` will be no longer supported in v0.3.0")
            }

            def context = DefaultCommandContext.create(channel, sshSpec.encoding)
            if (settings.logging) {
                context.enableLogging(sshSpec.outputLogLevel, sshSpec.errorLogLevel)
            }

            def lines = [] as List<String>
            context.standardOutput.lineListeners.add { String line -> lines << line }

            interactions.delegate = context
            interactions.resolveStrategy = Closure.DELEGATE_FIRST
            interactions()

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
    String executeSudo(Map<String, Object> options, String command) {
        log.info("Execute a command ($command) with sudo support and options ($options)")

        def prompt = UUID.randomUUID().toString()
        def lines = [] as List<String>
        execute(options, "sudo -S -p '$prompt' $command") {
            interaction {
                when(partial: prompt, from: standardOutput) {
                    log.info("Sending password for sudo authentication on channel #${channel.id}")
                    standardInput << sessionSpec.remote.password << '\n'

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
    CommandContext executeBackground(Map<String, Object> options, String command) {
        log.info("Execute a command ($command) in background")

        def settings = new ExecutionSettings(pty: options.pty, logging: options.logging)
        def channel = session.openChannel('exec') as ChannelExec
        channel.command = command
        channel.pty = settings.pty

        // TODO: removed in v0.3.0
        def remainingOptions = options.findAll { !(it.key in ['pty', 'logging']) }
        remainingOptions.each { k, v ->
            channel[k] = v
            log.warn("Deprecated: JSch option `$k` will be no longer supported in v0.3.0")
        }

        def context = DefaultCommandContext.create(channel, sshSpec.encoding)
        if (settings.logging) {
            context.enableLogging(sshSpec.outputLogLevel, sshSpec.errorLogLevel)
        }

        globalChannelManager.add(channel)
        channel.connect()
        log.info("Channel #${channel.id} has been opened")

        context
    }

    @Override
    void get(Map<String, Object> options, String remote, String local) {
        log.info("Get a remote file (${remote}) to local (${local})")
        def channel = session.openChannel('sftp') as ChannelSftp

        // TODO: removed in v0.3.0
        options.each { k, v ->
            channel[k] = v
            log.warn("Deprecated: JSch option `$k` will be no longer supported in v0.3.0")
        }

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
    void put(Map<String, Object> options, String local, String remote) {
        log.info("Put a local file (${local}) to remote (${remote})")
        def channel = session.openChannel('sftp') as ChannelSftp

        // TODO: removed in v0.3.0
        options.each { k, v ->
            channel[k] = v
            log.warn("Deprecated: JSch option `$k` will be no longer supported in v0.3.0")
        }

        try {
            channel.connect()
            log.info("Channel #${channel.id} has been opened")
            channel.put(local, remote, ChannelSftp.OVERWRITE)
            log.info("Channel #${channel.id} has been closed with exit status ${channel.exitStatus}")
        } finally {
            channel.disconnect()
        }
    }

    @Override
    int forwardLocalPortTo(String remoteHost, int remotePort) {
        def localPort = session.setPortForwardingL(0, remoteHost, remotePort)
        log.info("Started port fowarding from local port $localPort to remote ($remoteHost:$remotePort)")
        localPort
    }
}
