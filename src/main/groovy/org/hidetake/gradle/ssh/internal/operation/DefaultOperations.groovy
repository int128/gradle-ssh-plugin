package org.hidetake.gradle.ssh.internal.operation

import groovy.util.logging.Slf4j
import org.codehaus.groovy.tools.Utilities
import org.gradle.api.logging.Logging
import org.hidetake.gradle.ssh.internal.connection.Connection
import org.hidetake.gradle.ssh.internal.interaction.Engine
import org.hidetake.gradle.ssh.internal.interaction.InteractionDelegate
import org.hidetake.gradle.ssh.internal.interaction.LineOutputStream
import org.hidetake.gradle.ssh.plugin.OperationSettings
import org.hidetake.gradle.ssh.plugin.Remote
import org.hidetake.gradle.ssh.plugin.interaction.Stream
import org.hidetake.gradle.ssh.plugin.session.BadExitStatusException

/**
 * Default implementation of {@link Operations}.
 *
 * @author hidetake.org
 */
@Slf4j
class DefaultOperations implements Operations {
    final Remote remote

    private final Connection connection

    def DefaultOperations(Connection connection1) {
        connection = connection1
        remote = connection.remote
        assert connection
        assert remote
    }

    @Override
    void shell(OperationSettings settings) {
        log.debug("Executing a shell with $settings")

        def channel = connection.createShellChannel(settings)
        def standardInput = channel.outputStream
        def standardOutput = new LineOutputStream(settings.encoding)
        channel.outputStream = standardOutput

        if (settings.logging) {
            def logger = Logging.getLogger(DefaultOperations)
            standardOutput.listenLogging { String m -> logger.log(settings.outputLogLevel, m) }
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

            int exitStatus = channel.exitStatus
            log.info("Channel #${channel.id} has been closed with exit status $exitStatus")
            if (exitStatus != 0) {
                throw new BadExitStatusException("Shell returned exit status $exitStatus", exitStatus)
            }
        } finally {
            channel.disconnect()
        }
    }

    @Override
    String execute(OperationSettings settings, String command, Closure callback) {
        log.debug("Executing a command ($command) with $settings")

        def channel = connection.createExecutionChannel(command, settings)
        def standardInput = channel.outputStream
        def standardOutput = new LineOutputStream(settings.encoding)
        def standardError = new LineOutputStream(settings.encoding)
        channel.outputStream = standardOutput
        channel.errStream = standardError

        if (settings.logging) {
            def logger = Logging.getLogger(DefaultOperations)
            standardOutput.listenLogging { String m -> logger.log(settings.outputLogLevel, m) }
            standardError.listenLogging { String m -> logger.log(settings.errorLogLevel, m) }
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
        standardOutput.listenLine { String line -> lines << line }

        try {
            channel.connect()
            log.info("Channel #${channel.id} has been opened")
            while (!channel.closed) {
                sleep(100)
            }

            int exitStatus = channel.exitStatus
            log.info("Channel #${channel.id} has been closed with exit status $exitStatus")
            if (exitStatus != 0) {
                throw new BadExitStatusException("Command returned exit status $exitStatus", exitStatus)
            }

            def result = lines.join(Utilities.eol())
            callback?.call(result)
            result
        } finally {
            channel.disconnect()
        }
    }

    @Override
    void executeBackground(OperationSettings settings, String command, Closure callback) {
        log.debug("Executing a command ($command) in background with $settings")

        def channel = connection.createExecutionChannel(command, settings)
        def standardInput = channel.outputStream
        def standardOutput = new LineOutputStream(settings.encoding)
        def standardError = new LineOutputStream(settings.encoding)
        channel.outputStream = standardOutput
        channel.errStream = standardError

        if (settings.logging) {
            def logger = Logging.getLogger(DefaultOperations)
            standardOutput.listenLogging { String m -> logger.log(settings.outputLogLevel, m) }
            standardError.listenLogging { String m -> logger.log(settings.errorLogLevel, m) }
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

        def lines = [] as List<String>
        standardOutput.listenLine { String line -> lines << line }

        connection.whenClosed(channel) {
            int exitStatus = channel.exitStatus
            log.info("Channel #${channel.id} has been closed with exit status $exitStatus")
            if (exitStatus != 0) {
                throw new BadExitStatusException("Command returned exit status $exitStatus", exitStatus)
            }

            def result = lines.join(Utilities.eol())
            callback?.call(result)
        }
    }

    @Override
    void sftp(Closure closure) {
        def channel = connection.createSftpChannel()
        try {
            channel.connect()
            log.info("SFTP Channel #${channel.id} has been opened")

            closure.delegate = new DefaultSftpOperations(channel)
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.call()

            log.info("SFTP Channel #${channel.id} has been closed")
        } finally {
            channel.disconnect()
        }
    }
}
