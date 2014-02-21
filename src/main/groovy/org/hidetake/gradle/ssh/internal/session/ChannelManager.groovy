package org.hidetake.gradle.ssh.internal.session

import com.jcraft.jsch.Channel
import groovy.util.logging.Slf4j

/**
 * Channel lifecycle manager.
 *
 * <p>A command context has state of following:</p>
 * <ol>
 * <li>pending: command is running (not closed)</li>
 * <li>closed: command has been finished (closed and exit status is not -1)</li>
 * <li>disconnected: {@link Channel#disconnect()} has been called (closed and exit status is -1)</li>
 * </ol>
 *
 * @author hidetake.org
 *
 */
@Slf4j
class ChannelManager {
    final channels = [] as List<Channel>

    /**
     * Add a channel to be managed.
     *
     * @param channel
     */
    void add(Channel channel) {
        channels.add(channel)
    }

    /**
     * Wait for pending channels.
     *
     * @param closedCommandHandler callback handler for closed command
     */
    void waitForPending(Closure closedCommandHandler = { Channel c -> }) {
        def pendingCommands = new ArrayList<Channel>(channels)
        while (!pendingCommands.empty) {
            def closedCommands = pendingCommands.findAll { it.closed }
            closedCommands.each(closedCommandHandler)
            pendingCommands.removeAll(closedCommands)
            sleep(100)
        }
    }

    /**
     * Validates exit status of channels.
     * This method must be called before any channel is disconnected.
     */
    void validateExitStatus() {
        def errors = channels.findAll { it.exitStatus != 0 }
        if (errors.size() > 1) {
            errors.each { log.error("Channel #${it.id} finished with exit status ${it.exitStatus}") }
            throw new RuntimeException("${errors.size()} channels returned error exit status")
        } else if (errors.size() == 1) {
            def e = errors.first()
            throw new RuntimeException("Channel #${e.id} finished with exit status ${e.exitStatus}")
        }
    }

    /**
     * Disconnect all channels.
     */
    void disconnect() {
        channels.each { it.disconnect() }
    }
}
