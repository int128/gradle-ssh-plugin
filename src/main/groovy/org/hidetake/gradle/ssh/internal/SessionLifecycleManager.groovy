package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.Channel
import groovy.util.logging.Slf4j

/**
 * Event listener for lifecycle management of commands.
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
class SessionLifecycleManager {
    final contexts = [] as List<DefaultCommandContext>

    /**
     * Add a context to be managed.
     *
     * @param context
     * @return this
     */
    def leftShift(DefaultCommandContext context) {
        contexts << context
        this
    }

    /**
     * Wait for pending channels.
     *
     * @param closedCommandHandler callback handler for closed command
     */
    void waitForPending(Closure closedCommandHandler = {}) {
        def pendingCommands = new ArrayList<DefaultCommandContext>(contexts)
        while (!pendingCommands.empty) {
            def closedCommands = pendingCommands.findAll { it.channel.closed }
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
        def errors = contexts.findAll { it.channel.exitStatus != 0 }
        if (errors.size() > 1) {
            errors.each { log.error("Channel #${it.channel.id} finished with exit status ${it.channel.exitStatus}") }
            throw new RuntimeException("${errors.size()} channels returned error exit status")
        } else if (errors.size() == 1) {
            def e = errors.first()
            throw new RuntimeException( "Channel #${e.channel.id} finished with exit status ${e.channel.exitStatus}")
        }
    }

    /**
     * Disconnect all channels.
     */
    void disconnect() {
        contexts.each { it.channel.disconnect() }
    }
}
