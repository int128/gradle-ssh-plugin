package org.hidetake.groovy.ssh.operation

import com.jcraft.jsch.ChannelShell
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.connection.Connection
import org.hidetake.groovy.ssh.core.settings.LoggingMethod
import org.hidetake.groovy.ssh.interaction.Interaction

/**
 * A shell operation.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Shell implements Operation {
    private final Connection connection
    private final ChannelShell channel
    private final OutputStream standardInput
    private final LineOutputStream standardOutput

    def Shell(Connection connection1, CommandSettings settings) {
        connection = connection1
        channel = connection.createShellChannel()
        standardInput = channel.outputStream
        standardOutput = new LineOutputStream(settings.encoding)
        channel.outputStream = standardOutput

        switch (settings.logging) {
            case LoggingMethod.slf4j:
                standardOutput.listenLogging { String m -> log.info("$connection.remote.name#$channel.id|$m") }
                break
            case LoggingMethod.stdout:
                standardOutput.listenLogging { String m -> System.out.println("$connection.remote.name#$channel.id|$m") }
                break
        }

        if (settings.outputStream) {
            standardOutput.pipe(settings.outputStream)
        }
        if (settings.interaction) {
            Interaction.enable(settings.interaction, standardInput, standardOutput)
        }
    }

    @Override
    int startSync() {
        channel.connect()
        try {
            log.info("Started shell $connection.remote.name#$channel.id")
            while (!channel.closed) {
                sleep(100)
            }
            int exitStatus = channel.exitStatus
            if (exitStatus == 0) {
                log.info("Success shell $connection.remote.name#$channel.id")
            } else {
                log.error("Failed shell $connection.remote.name#$channel.id with status $exitStatus")
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
                log.info("Success shell $connection.remote.name#$channel.id")
            } else {
                log.error("Failed shell $connection.remote.name#$channel.id with status $exitStatus")
            }
            closure.call(exitStatus)
        }
        channel.connect()
        log.info("Started shell $connection.remote.name#$channel.id")
    }

    @Override
    void onEachLineOfStandardOutput(Closure closure) {
        standardOutput.listenLine(closure)
    }
}
