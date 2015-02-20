package org.hidetake.groovy.ssh.operation

import groovy.util.logging.Slf4j
import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.core.settings.LoggingMethod
import org.hidetake.groovy.ssh.extension.settings.LocalPortForwardSettings
import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.extension.settings.RemotePortForwardSettings
import org.hidetake.groovy.ssh.interaction.Stream
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.hidetake.groovy.ssh.connection.Connection
import org.hidetake.groovy.ssh.interaction.Engine
import org.hidetake.groovy.ssh.interaction.InteractionHandler
import org.hidetake.groovy.ssh.interaction.LineOutputStream

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

/**
 * Default implementation of {@link Operations}.
 *
 * Operations should follow the logging convention, that is,
 * it should write a log as DEBUG on beginning of an operation,
 * it should write a log as INFO on success of an operation,
 * but it does not need to write a log if it is an internal operation.

 * @author Hidetake Iwata
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
            case LoggingMethod.slf4j:
                standardOutput.listenLogging { String m -> log.info("${remote.name}|$m") }
                break
            case LoggingMethod.stdout:
                standardOutput.listenLogging { String m -> System.out.println("${remote.name}|$m") }
                break
        }

        if (settings.outputStream) {
            standardOutput.linkStream(settings.outputStream)
        }

        if (settings.interaction) {
            def delegate = new InteractionHandler(standardInput)
            def rules = delegate.evaluate(settings.interaction)
            def engine = new Engine(delegate)
            engine.alterInteractionRules(rules)
            engine.attach(standardOutput, Stream.StandardOutput)
        }

        try {
            channel.connect()
            log.info("Shell #${channel.id} has been started")
            while (!channel.closed) {
                sleep(100)
            }

            int exitStatus = channel.exitStatus
            log.info("Shell #${channel.id} returned exit status $exitStatus")

            if (exitStatus != 0 && !settings.ignoreError) {
                throw new BadExitStatusException("Shell #${channel.id} returned exit status $exitStatus", exitStatus)
            }
        } finally {
            channel.disconnect()
        }
    }

    @Override
    String execute(OperationSettings settings, String command, Closure callback) {
        log.debug("Executing the command ($command) with $settings")

        def channel = connection.createExecutionChannel(command, settings)
        def standardInput = channel.outputStream
        def standardOutput = new LineOutputStream(settings.encoding)
        def standardError = new LineOutputStream(settings.encoding)
        channel.outputStream = standardOutput
        channel.errStream = standardError

        switch (settings.logging) {
            case LoggingMethod.slf4j:
                standardOutput.listenLogging { String m -> log.info("${remote.name}|$m") }
                standardError.listenLogging  { String m -> log.error("${remote.name}|$m") }
                break
            case LoggingMethod.stdout:
                standardOutput.listenLogging { String m -> System.out.println("${remote.name}|$m") }
                standardError.listenLogging  { String m -> System.err.println("${remote.name}|$m") }
                break
        }

        if (settings.outputStream) {
            standardOutput.linkStream(settings.outputStream)
        }
        if (settings.errorStream) {
            standardError.linkStream(settings.errorStream)
        }

        if (settings.interaction) {
            def delegate = new InteractionHandler(standardInput)
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
            log.info("Command #${channel.id} started ($command)")
            while (!channel.closed) {
                sleep(100)
            }

            int exitStatus = channel.exitStatus
            log.info("Command #${channel.id} returned exit status $exitStatus")

            if (exitStatus != 0 && !settings.ignoreError) {
                throw new BadExitStatusException("Command #${channel.id} returned exit status $exitStatus", exitStatus)
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
        log.debug("Executing the command ($command) in background with $settings")

        def channel = connection.createExecutionChannel(command, settings)
        def standardInput = channel.outputStream
        def standardOutput = new LineOutputStream(settings.encoding)
        def standardError = new LineOutputStream(settings.encoding)
        channel.outputStream = standardOutput
        channel.errStream = standardError

        switch (settings.logging) {
            case LoggingMethod.slf4j:
                standardOutput.listenLogging { String m -> log.info("${remote.name}|$m") }
                standardError.listenLogging  { String m -> log.error("${remote.name}|$m") }
                break
            case LoggingMethod.stdout:
                standardOutput.listenLogging { String m -> System.out.println("${remote.name}|$m") }
                standardError.listenLogging  { String m -> System.err.println("${remote.name}|$m") }
                break
        }

        if (settings.outputStream) {
            standardOutput.linkStream(settings.outputStream)
        }
        if (settings.errorStream) {
            standardError.linkStream(settings.errorStream)
        }

        if (settings.interaction) {
            def delegate = new InteractionHandler(standardInput)
            def rules = delegate.evaluate(settings.interaction)
            def engine = new Engine(delegate)
            engine.alterInteractionRules(rules)
            engine.attach(standardOutput, Stream.StandardOutput)
            engine.attach(standardError, Stream.StandardError)
        }

        def lines = [] as List<String>
        standardOutput.listenLine { String line -> lines << line }

        channel.connect()
        log.info("Command #${channel.id} started in background ($command)")

        connection.whenClosed(channel) {
            int exitStatus = channel.exitStatus
            log.info("Command #${channel.id} returned exit status $exitStatus")

            if (exitStatus != 0 && !settings.ignoreError) {
                throw new BadExitStatusException("Command #${channel.id} returned exit status $exitStatus", exitStatus)
            }

            def result = lines.join(Utilities.eol())
            callback?.call(result)
        }
    }

    @Override
    int forwardLocalPort(LocalPortForwardSettings settings) {
        log.debug("Requesting port forwarding from " +
                  "local (${settings.bind}:${settings.port}) to remote (${settings.host}:${settings.hostPort})")
        int port = connection.forwardLocalPort(settings)
        log.info("Enabled port forwarding from " +
                 "local (${settings.bind}:${port}) to remote (${settings.host}:${settings.hostPort})")
        port
    }

    @Override
    void forwardRemotePort(RemotePortForwardSettings settings) {
        log.debug("Requesting port forwarding from " +
                  "remote (${settings.bind}:${settings.port}) to local (${settings.host}:${settings.hostPort})")
        connection.forwardRemotePort(settings)
        log.info("Enabled port forwarding from " +
                 "remote (${settings.bind}:${settings.port}) to local (${settings.host}:${settings.hostPort})")
    }

    @Override
    def sftp(Closure closure) {
        def channel = connection.createSftpChannel()
        try {
            channel.connect()
            log.debug("SFTP #${channel.id} started")
            callWithDelegate(closure, new SftpOperations(channel))
        } finally {
            channel.disconnect()
            log.debug("SFTP #${channel.id} closed")
        }
    }
}
