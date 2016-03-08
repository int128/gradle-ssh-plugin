package org.hidetake.groovy.ssh.operation

import com.jcraft.jsch.ChannelExec
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.connection.Connection
import org.hidetake.groovy.ssh.core.settings.LoggingMethod
import org.hidetake.groovy.ssh.interaction.InteractionHandler
import org.hidetake.groovy.ssh.interaction.Interactions
import org.hidetake.groovy.ssh.interaction.Stream

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
    private final Interactions interactions

    def Command(Connection connection1, CommandSettings settings, String commandLine1) {
        connection = connection1
        commandLine = commandLine1

        channel = connection.createExecutionChannel()
        channel.command = commandLine
        channel.pty = settings.pty
        channel.agentForwarding = settings.agentForwarding

        interactions = new Interactions(channel.outputStream, channel.inputStream, channel.errStream, settings.encoding)
        if (settings.outputStream) {
            interactions.pipe(Stream.StandardOutput, settings.outputStream)
        }
        if (settings.errorStream) {
            interactions.pipe(Stream.StandardError, settings.errorStream)
        }
        if (settings.logging == LoggingMethod.slf4j) {
            def log = Command.log  // workaround for mock injection in test code
            interactions.add {
                when(line: _, from: standardOutput) {
                    log.info("$connection.remote.name#$channel.id|$it")
                }
                when(line: _, from: standardError) {
                    log.error("$connection.remote.name#$channel.id|$it")
                }
            }
        } else if (settings.logging == LoggingMethod.stdout) {
            interactions.add {
                when(line: _, from: standardOutput) {
                    System.out.println("$connection.remote.name#$channel.id|$it")
                }
                when(line: _, from: standardError) {
                    System.err.println("$connection.remote.name#$channel.id|$it")
                }
            }
        }
        if (settings.interaction) {
            interactions.add(settings.interaction)
        }
    }

    @Override
    int startSync() {
        channel.connect()
        log.info("Started command $connection.remote.name#$channel.id: $commandLine")
        try {
            interactions.start()
            interactions.waitForEndOfStream()
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
            interactions.waitForEndOfStream()
            int exitStatus = channel.exitStatus
            if (exitStatus == 0) {
                log.info("Success command $connection.remote.name#$channel.id: $commandLine")
            } else {
                log.error("Failed command $connection.remote.name#$channel.id with status $exitStatus: $commandLine")
            }
            closure.call(exitStatus)
        }
        channel.connect()
        interactions.start()
        log.info("Started command $connection.remote.name#$channel.id: $commandLine")
    }

    @Override
    void addInteraction(@DelegatesTo(InteractionHandler) Closure closure) {
        interactions.add(closure)
    }
}
