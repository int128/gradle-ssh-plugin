package org.hidetake.groovy.ssh.internal.operation

import groovy.util.logging.Slf4j
import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.api.OperationSettings
import org.hidetake.groovy.ssh.api.Remote
import org.hidetake.groovy.ssh.api.interaction.Stream
import org.hidetake.groovy.ssh.api.operation.Operations
import org.hidetake.groovy.ssh.api.session.BadExitStatusException
import org.hidetake.groovy.ssh.internal.connection.Connection
import org.hidetake.groovy.ssh.internal.interaction.Engine
import org.hidetake.groovy.ssh.internal.interaction.InteractionDelegate
import org.hidetake.groovy.ssh.internal.interaction.LineOutputStream

import static org.hidetake.groovy.ssh.internal.util.ClosureUtil.callWithDelegate

/**
 * Default implementation of {@link org.hidetake.groovy.ssh.api.operation.Operations}.
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

        switch (settings.logging) {
            case OperationSettings.Logging.slf4j:
                standardOutput.listenLogging { String m -> log.info(m) }
                break
            case OperationSettings.Logging.stdout:
                standardOutput.listenLogging { String m -> System.out.println(m) }
                break
        }

        if (settings.outputStream) {
            standardOutput.linkStream(settings.outputStream)
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

        switch (settings.logging) {
            case OperationSettings.Logging.slf4j:
                standardOutput.listenLogging { String m -> log.info(m) }
                standardError.listenLogging  { String m -> log.error(m) }
                break
            case OperationSettings.Logging.stdout:
                standardOutput.listenLogging { String m -> System.out.println(m) }
                standardError.listenLogging  { String m -> System.err.println(m) }
                break
        }

        if (settings.outputStream) {
            standardOutput.linkStream(settings.outputStream)
        }
        if (settings.errorStream) {
            standardError.linkStream(settings.errorStream)
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

        switch (settings.logging) {
            case OperationSettings.Logging.slf4j:
                standardOutput.listenLogging { String m -> log.info(m) }
                standardError.listenLogging  { String m -> log.error(m) }
                break
            case OperationSettings.Logging.stdout:
                standardOutput.listenLogging { String m -> System.out.println(m) }
                standardError.listenLogging  { String m -> System.err.println(m) }
                break
        }

        if (settings.outputStream) {
            standardOutput.linkStream(settings.outputStream)
        }
        if (settings.errorStream) {
            standardError.linkStream(settings.errorStream)
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

            callWithDelegate(closure, new DefaultSftpOperations(channel))

            log.info("SFTP Channel #${channel.id} has been closed")
        } finally {
            channel.disconnect()
        }
    }
}
