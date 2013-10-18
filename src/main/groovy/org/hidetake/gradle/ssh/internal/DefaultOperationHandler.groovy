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
class DefaultOperationHandler implements OperationHandler {
    final SshSpec sshSpec
    final SessionSpec sessionSpec
    final Session session
    final CommandLifecycleManager commandLifecycleManager

    @Override
    Remote getRemote() {
        sessionSpec.remote
    }

    @Override
    String execute(String command) {
        execute([:], command)
    }

    @Override
    String execute(Map<String, Object> options, String command) {
        log.info("Executing command: ${command}")

        def manager = new CommandLifecycleManager()
        try {
            def context = invokeCommand(options, command)
            log.info("Channel #${context.channel.id} has been opened")

            manager << context
            manager.waitForPending()
            log.info("Channel #${context.channel.id} has been closed with exit status ${context.channel.exitStatus}")

            manager.validateExitStatus()
            context.standardOutput.lines.join(Utilities.eol())
        } finally {
            manager.disconnect()
        }
    }

    @Override
    String executeSudo(String command) {
        executeSudo([:], command)
    }

    @Override
    String executeSudo(Map<String, Object> options, String command) {
        log.info("Executing command with sudo support: ${command}")

        def channel = session.openChannel('exec') as ChannelExec
        channel.command = "sudo -S -p '' $command"
        options.each { k, v -> channel[k] = v }

        def outputLogger = new LoggingOutputStream(sshSpec.outputLogLevel, sshSpec.encoding)
        def errorLogger = new LoggingOutputStream(sshSpec.errorLogLevel, sshSpec.encoding)
        channel.outputStream = outputLogger
        channel.errStream = errorLogger

        // filter password and check authentication failure.
        boolean authenticationFailed = false
        int lineNumber = 0
        outputLogger.filter = { String line ->
            // usually password or messages appear within a few lines.
            if (++lineNumber < 5) {
                if (line.contains('try again')) {
                    // this closure runs in I/O thread, so needs to notify failure to main thread.
                    authenticationFailed = true
                }
                !line.contains(sessionSpec.remote.password)
            } else {
                true
            }
        }

        try {
            channel.connect()
            log.info("Channel #${channel.id} has been opened")
            channel.outputStream.withWriter(sshSpec.encoding) {
                it << sessionSpec.remote.password << '\n'
            }
            while (!channel.closed) {
                if (authenticationFailed) {
                    throw new RuntimeException('Unable to execute sudo command. Wrong username/password')
                }
                sleep(100)
            }
            log.info("Channel #${channel.id} has been closed with exit status ${channel.exitStatus}")
        } finally {
            channel.disconnect()
            outputLogger.close()
            errorLogger.close()
        }

        outputLogger.lines.join(Utilities.eol())
    }

    @Override
    CommandPromise executeBackground(String command) {
        executeBackground([:], command)
    }

    @Override
    CommandPromise executeBackground(Map<String, Object> options, String command) {
        log.info("Executing command in background: ${command}")

        def context = invokeCommand(options, command)
        log.info("Channel #${context.channel.id} has been opened")
        commandLifecycleManager << context
        context
    }

    protected CommandContext invokeCommand(Map<String, Object> options, String command) {
        def channel = session.openChannel('exec') as ChannelExec
        channel.command = command
        options.each { k, v -> channel[k] = v }

        def context = new CommandContext(channel,
                new LoggingOutputStream(sshSpec.outputLogLevel, sshSpec.encoding),
                new LoggingOutputStream(sshSpec.errorLogLevel, sshSpec.encoding))
        channel.outputStream = context.standardOutput
        channel.errStream = context.standardError

        channel.connect()
        context
    }

    @Override
    void get(String remote, String local) {
        get([:], remote, local)
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
    void put(String local, String remote) {
        put([:], local, remote)
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
