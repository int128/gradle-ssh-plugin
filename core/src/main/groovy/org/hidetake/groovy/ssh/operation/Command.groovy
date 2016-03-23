package org.hidetake.groovy.ssh.operation

import com.jcraft.jsch.ChannelExec
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.connection.Connection
import org.hidetake.groovy.ssh.core.settings.LoggingMethod
import org.hidetake.groovy.ssh.interaction.Interaction

/**
 * A command operation.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Command implements Operation {
    private final Connection connection
    private final ChannelExec channel
    private final String commandLine
    private final OutputStream standardInput
    private final LineOutputStream standardOutput
    private final LineOutputStream standardError

    def Command(Connection connection1, CommandSettings settings, String commandLine1) {
        connection = connection1
        commandLine = commandLine1

        channel = connection.createExecutionChannel()
        channel.command = commandLine
        channel.pty = settings.pty

        standardInput = channel.outputStream
        standardOutput = new LineOutputStream(settings.encoding)
        standardError = new LineOutputStream(settings.encoding)
        channel.outputStream = standardOutput
        channel.errStream = standardError

        switch (settings.logging) {
            case LoggingMethod.slf4j:
                standardOutput.listenLogging { String m -> log.info("$connection.remote.name#$channel.id|$m") }
                standardError.listenLogging { String m -> log.error("$connection.remote.name#$channel.id|$m") }
                break
            case LoggingMethod.stdout:
                standardOutput.listenLogging { String m -> System.out.println("$connection.remote.name#$channel.id|$m") }
                standardError.listenLogging { String m -> System.err.println("$connection.remote.name#$channel.id|$m") }
                break
        }

        if (settings.outputStream) {
            standardOutput.pipe(settings.outputStream)
        }
        if (settings.errorStream) {
            standardError.pipe(settings.errorStream)
        }
        if (settings.interaction) {
            Interaction.enable(settings.interaction, standardInput, standardOutput, standardError)
        }
    }

    @Override
    int startSync() {
        channel.connect()
        log.info("Started command $connection.remote.name#$channel.id: $commandLine")
        try {
            while (!channel.closed) {
                sleep(100)
            }
            int exitStatus = channel.exitStatus
            if (exitStatus == 0) {
                log.info("Success command $connection.remote.name#$channel.id: $commandLine")
            } else {
                log.error("Failed command $connection.remote.name#$channel.id with status $exitStatus: $commandLine")
            }
            exitStatus
        } finally {
            channel.disconnect()
        }
    }

    @Override
    void startAsync(Closure closure) {
        connection.whenClosed(channel) {
            int exitStatus = channel.exitStatus
            if (exitStatus == 0) {
                log.info("Success command $connection.remote.name#$channel.id: $commandLine")
            } else {
                log.error("Failed command $connection.remote.name#$channel.id with status $exitStatus: $commandLine")
            }
            closure.call(exitStatus)
        }
        channel.connect()
        log.info("Started command $connection.remote.name#$channel.id: $commandLine")
    }

    @Override
    void onEachLineOfStandardOutput(Closure closure) {
        standardOutput.listenLine(closure)
    }
}
