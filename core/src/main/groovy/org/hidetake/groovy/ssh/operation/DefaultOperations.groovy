package org.hidetake.groovy.ssh.operation

import groovy.util.logging.Slf4j
import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.connection.Connection
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.settings.LoggingMethod
import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.extension.settings.LocalPortForwardSettings
import org.hidetake.groovy.ssh.extension.settings.RemotePortForwardSettings
import org.hidetake.groovy.ssh.interaction.Interaction
import org.hidetake.groovy.ssh.session.BadExitStatusException

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
        log.debug("Executing shell on $remote: $settings")

        def channel = connection.createShellChannel(settings)
        def standardInput = channel.outputStream
        def standardOutput = new LineOutputStream(settings.encoding)
        channel.outputStream = standardOutput

        enableLogging(settings.logging, channel.id, standardOutput)

        if (settings.outputStream) {
            standardOutput.pipe(settings.outputStream)
        }
        if (settings.interaction) {
            Interaction.enable(settings.interaction, standardInput, standardOutput)
        }

        channel.connect()
        try {
            log.info("Started shell $remote.name#$channel.id")
            while (!channel.closed) {
                sleep(100)
            }

            int exitStatus = channel.exitStatus
            if (exitStatus == 0) {
                log.info("Success shell $remote.name#$channel.id")
            } else {
                log.error("Failed shell $remote.name#$channel.id with status $exitStatus")
                if (!settings.ignoreError) {
                    throw new BadExitStatusException("Shell $remote.name#$channel.id returned status $exitStatus", exitStatus)
                }
            }
        } finally {
            channel.disconnect()
        }
    }

    @Override
    String execute(OperationSettings settings, String command, Closure callback) {
        log.debug("Executing command on $remote: $command: $settings")

        def channel = connection.createExecutionChannel(command, settings)
        def standardInput = channel.outputStream
        def standardOutput = new LineOutputStream(settings.encoding)
        def standardError = new LineOutputStream(settings.encoding)
        channel.outputStream = standardOutput
        channel.errStream = standardError

        enableLogging(settings.logging, channel.id, standardOutput, standardError)

        if (settings.outputStream) {
            standardOutput.pipe(settings.outputStream)
        }
        if (settings.errorStream) {
            standardError.pipe(settings.errorStream)
        }
        if (settings.interaction) {
            Interaction.enable(settings.interaction, standardInput, standardOutput, standardError)
        }

        def lines = [] as List<String>
        standardOutput.listenLine { String line -> lines << line }

        channel.connect()
        try {
            log.info("Started command $remote.name#$channel.id: $command")
            while (!channel.closed) {
                sleep(100)
            }

            int exitStatus = channel.exitStatus
            if (exitStatus == 0) {
                log.info("Success command $remote.name#$channel.id: $command")
            } else {
                log.error("Failed command $remote.name#$channel.id with status $exitStatus: $command")
                if (!settings.ignoreError) {
                    throw new BadExitStatusException(
                            "Command $remote.name#$channel.id returned exit status $exitStatus: $command", exitStatus)
                }
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
        log.debug("Executing command on $remote: $command: $settings")

        def channel = connection.createExecutionChannel(command, settings)
        def standardInput = channel.outputStream
        def standardOutput = new LineOutputStream(settings.encoding)
        def standardError = new LineOutputStream(settings.encoding)
        channel.outputStream = standardOutput
        channel.errStream = standardError

        enableLogging(settings.logging, channel.id, standardOutput, standardError)

        if (settings.outputStream) {
            standardOutput.pipe(settings.outputStream)
        }
        if (settings.errorStream) {
            standardError.pipe(settings.errorStream)
        }
        if (settings.interaction) {
            Interaction.enable(settings.interaction, standardInput, standardOutput, standardError)
        }

        def lines = [] as List<String>
        standardOutput.listenLine { String line -> lines << line }

        channel.connect()
        log.info("Started command $remote.name#$channel.id: $command")

        connection.whenClosed(channel) {
            int exitStatus = channel.exitStatus
            if (exitStatus == 0) {
                log.info("Success command $remote.name#$channel.id: $command")
            } else {
                log.error("Failed command $remote.name#$channel.id with status $exitStatus: $command")
                if (!settings.ignoreError) {
                    throw new BadExitStatusException(
                            "Command $remote.name#$channel.id returned exit status $exitStatus: $command", exitStatus)
                }
            }

            def result = lines.join(Utilities.eol())
            callback?.call(result)
        }
    }

    @Override
    int forwardLocalPort(LocalPortForwardSettings settings) {
        log.debug("Requesting port forwarding " +
                  "from $settings.bind:$settings.port " +
                  "to $remote.name [$settings.host:$settings.hostPort]")
        int port = connection.forwardLocalPort(settings)
        log.info("Enabled port forwarding " +
                 "from $settings.bind:$settings.port " +
                 "to $remote.name [$settings.host:$settings.hostPort]")
        port
    }

    @Override
    void forwardRemotePort(RemotePortForwardSettings settings) {
        log.debug("Requesting port forwarding " +
                  "from $remote.name [$settings.bind:$settings.port] " +
                  "to $settings.host:$settings.hostPort")
        connection.forwardRemotePort(settings)
        log.info("Enabled port forwarding from " +
                 "from $remote.name [$settings.bind:$settings.port] " +
                 "to $settings.host:$settings.hostPort")
    }

    @Override
    def sftp(Closure closure) {
        log.debug("Requesting SFTP subsystem on $remote")
        def channel = connection.createSftpChannel()
        channel.connect()
        try {
            log.debug("Started SFTP $remote.name#$channel.id")
            callWithDelegate(closure, new SftpOperations(channel))
            log.debug("Finished SFTP $remote.name#$channel.id")
        } finally {
            channel.disconnect()
        }
    }

    private void enableLogging(LoggingMethod loggingMethod,
                               int channelId,
                               LineOutputStream standardOutput,
                               LineOutputStream standardError = null) {
        switch (loggingMethod) {
            case LoggingMethod.slf4j:
                standardOutput.listenLogging { String m -> log.info("$remote.name#$channelId|$m") }
                standardError?.listenLogging { String m -> log.error("$remote.name#$channelId|$m") }
                break
            case LoggingMethod.stdout:
                standardOutput.listenLogging { String m -> System.out.println("$remote.name#$channelId|$m") }
                standardError?.listenLogging { String m -> System.err.println("$remote.name#$channelId|$m") }
                break
        }
    }
}
