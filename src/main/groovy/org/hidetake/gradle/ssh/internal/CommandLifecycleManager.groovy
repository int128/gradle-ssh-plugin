package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.Channel

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
class CommandLifecycleManager {
    final contexts = [] as List<CommandContext>

    /**
     * Add a context to be managed.
     *
     * @param context
     * @return this
     */
    def leftShift(CommandContext context) {
        contexts << context
        this
    }

    /**
     * Wait for pending channels.
     *
     * @param closedCommandHandler callback handler for closed command
     */
    void waitForPending(Closure closedCommandHandler = {}) {
        def pendingCommands = new ArrayList<CommandContext>(contexts)
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
        contexts.each {
            def status = it.channel.exitStatus
            assert status != -1, 'method should be called before disconnect'
            if (status > 0) {
                throw new RuntimeException("Channel #${it.channel.id} returned exit status ${status}")
            }
        }
    }

    /**
     * Disconnect all channels.
     */
    void disconnect() {
        contexts.each { it.channel.disconnect() }
    }
}
