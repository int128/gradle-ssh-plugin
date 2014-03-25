package org.hidetake.gradle.ssh.internal.operation

import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.codehaus.groovy.tools.Utilities
import org.gradle.api.logging.Logging
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.operation.ExecutionSettings
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.operation.SftpHandler
import org.hidetake.gradle.ssh.api.operation.ShellSettings
import org.hidetake.gradle.ssh.api.operation.interaction.Stream
import org.hidetake.gradle.ssh.api.ssh.Connection
import org.hidetake.gradle.ssh.internal.operation.interaction.Engine
import org.hidetake.gradle.ssh.internal.operation.interaction.InteractionDelegate
import org.hidetake.gradle.ssh.internal.operation.interaction.LineOutputStream
import org.hidetake.gradle.ssh.registry.Registry

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
    void shell(ShellSettings settings) {
        def channel = connection.createShellChannel(settings)

        def standardInput = channel.outputStream
        def standardOutput = new LineOutputStream(sshSettings.encoding)
        channel.outputStream = standardOutput

        if (settings.logging) {
            def logger = Logging.getLogger(DefaultOperations)
            standardOutput.loggingListeners.add { String m -> logger.log(sshSettings.outputLogLevel, m) }
        }

        if (settings.interaction) {
            def delegate = new InteractionDelegate(standardInput)
            def rules = delegate.evaluate(settings.interaction)
            def engine = new Engine(delegate)
            engine.alterInteractionRules(rules)
            engine.attach(standardOutput, Stream.StandardOutput)
        }

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
    String execute(ExecutionSettings settings, String command) {
        def channel = connection.createExecutionChannel(command, settings)

        def standardInput = channel.outputStream
        def standardOutput = new LineOutputStream(sshSettings.encoding)
        def standardError = new LineOutputStream(sshSettings.encoding)
        channel.outputStream = standardOutput
        channel.errStream = standardError

        if (settings.logging) {
            def logger = Logging.getLogger(DefaultOperations)
            standardOutput.loggingListeners.add { String m -> logger.log(sshSettings.outputLogLevel, m) }
            standardError.loggingListeners.add { String m -> logger.log(sshSettings.outputLogLevel, m) }
        }

        if (settings.interaction) {
            def delegate = new InteractionDelegate(standardInput)
            def rules = delegate.evaluate(settings.interaction)
            def engine = new Engine(delegate)
            engine.alterInteractionRules(rules)
            engine.attach(standardOutput, Stream.StandardOutput)
            engine.attach(standardError, Stream.StandardError)
        }

        def lines = [] as List<String>
        standardOutput.lineListeners.add { String line -> lines << line }

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
    void executeBackground(ExecutionSettings settings, String command) {
        def channel = connection.createExecutionChannel(command, settings)

        def standardInput = channel.outputStream
        def standardOutput = new LineOutputStream(sshSettings.encoding)
        def standardError = new LineOutputStream(sshSettings.encoding)
        channel.outputStream = standardOutput
        channel.errStream = standardError

        if (settings.logging) {
            def logger = Logging.getLogger(DefaultOperations)
            standardOutput.loggingListeners.add { String m -> logger.log(sshSettings.outputLogLevel, m) }
            standardError.loggingListeners.add { String m -> logger.log(sshSettings.outputLogLevel, m) }
        }

        if (settings.interaction) {
            def delegate = new InteractionDelegate(standardInput)
            def rules = delegate.evaluate(settings.interaction)
            def engine = new Engine(delegate)
            engine.alterInteractionRules(rules)
            engine.attach(standardOutput, Stream.StandardOutput)
            engine.attach(standardError, Stream.StandardError)
        }

        channel.connect()
        log.info("Channel #${channel.id} has been opened")

        connection.whenClosed(channel) {
            log.info("Channel #${channel.id} has been closed with exit status ${channel.exitStatus}")
            channel.disconnect()
        }
    }

    @Override
    void sftp(Closure closure) {
        def channel = connection.createSftpChannel()
        try {
            channel.connect()
            log.info("SFTP Channel #${channel.id} has been opened")

            closure.delegate = Registry.instance[SftpHandler.Factory].create(channel)
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.call()

            log.info("SFTP Channel #${channel.id} has been closed")
        } finally {
            channel.disconnect()
        }
    }
}
