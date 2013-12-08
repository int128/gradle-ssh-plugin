package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.Session
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.codehaus.groovy.tools.Utilities
import org.hidetake.gradle.ssh.api.*

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
    final SessionLifecycleManager globalLifecycleManager

    @Override
    Remote getRemote() {
        sessionSpec.remote
    }

    @Override
    String execute(Map<String, Object> options, String command, Closure interactions) {
        log.info("Executing command: ${command}")

        def lifecycleManager = new SessionLifecycleManager()
        try {
            def channel = session.openChannel('exec') as ChannelExec
            channel.command = command
            options.each { k, v -> channel[k] = v }

            def context = DefaultCommandContext.create(channel, sshSpec.encoding)
            context.enableLogging(sshSpec.outputLogLevel, sshSpec.errorLogLevel)

            def lines = [] as List<String>
            context.standardOutput.lineListeners.add { String line -> lines << line }

            interactions.delegate = context
            interactions.resolveStrategy = Closure.DELEGATE_FIRST
            interactions()

            lifecycleManager << context
            context.channel.connect()
            log.info("Channel #${context.channel.id} has been opened")
            lifecycleManager.waitForPending()
            log.info("Channel #${context.channel.id} has been closed with exit status ${context.channel.exitStatus}")
            lifecycleManager.validateExitStatus()
            lines.join(Utilities.eol())
        } finally {
            lifecycleManager.disconnect()
        }
    }

    @Override
    String executeSudo(Map<String, Object> options, String command) {
        log.info("Executing command with sudo support: ${command}")

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
        log.info("Executing command in background: ${command}")

        def channel = session.openChannel('exec') as ChannelExec
        channel.command = command
        options.each { k, v -> channel[k] = v }

        def context = DefaultCommandContext.create(channel, sshSpec.encoding)
        context.enableLogging(sshSpec.outputLogLevel, sshSpec.errorLogLevel)

        globalLifecycleManager << context
        context.channel.connect()
        log.info("Channel #${context.channel.id} has been opened")

        context
    }

    @Override
    void get(Map<String, Object> options, String remote, String local) {
        log.info("Get: ${remote} -> ${local}")
        def channel = session.openChannel('sftp') as ChannelSftp
        options.each { k, v -> channel[k] = v }
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
        log.info("Put: ${local} -> ${remote}")
        def channel = session.openChannel('sftp') as ChannelSftp
        options.each { k, v -> channel[k] = v }
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
