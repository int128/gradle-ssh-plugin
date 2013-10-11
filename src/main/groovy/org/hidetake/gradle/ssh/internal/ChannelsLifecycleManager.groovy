package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.Channel
import org.hidetake.gradle.ssh.api.OperationEventListener
import org.hidetake.gradle.ssh.api.SessionSpec

/**
 * Event listener for lifecycle management of unmanaged channels.
 *
 * <p>A channel has state of following:</p>
 * <ol>
 * <li>pending: command is running (not closed)</li>
 * <li>closed: command has been finished (closed and exit status is not -1)</li>
 * <li>disconnected: {@link Channel#disconnect()} has been called (closed and exit status is -1)</li>
 * </ol>
 *
 * @author hidetake.org
 *
 */
class ChannelsLifecycleManager implements OperationEventListener {
    final channels = [] as List<Channel>

    /**
     * Wait for pending channels.
     *
     * @param closedChannelHandler callback handler for closed channel
     */
    void waitForPending(Closure closedChannelHandler) {
        def pendingChannels = new ArrayList<Channel>(channels)
        while (!pendingChannels.empty) {
            def closedChannels = pendingChannels.findAll { it.closed }
            closedChannels.each(closedChannelHandler)
            pendingChannels.removeAll(closedChannels)
            sleep(100)
        }
    }

    /**
     * Validates exit status of channels.
     *
     * @see ExitStatusValidator#validate(Channel)
     */
    void validateExitStatus() {
        channels.each { ExitStatusValidator.validate(it) }
    }

    /**
     * Disconnect all channels.
     */
    void disconnect() {
        channels.each { it.disconnect() }
    }

    @Override
    void unmanagedChannelConnected(Channel channel, SessionSpec spec) {
        channels.add(channel)
    }

    @Override
    void beginOperation(String operation, Object... args) {
    }

    @Override
    void managedChannelConnected(Channel channel, SessionSpec spec) {
    }

    @Override
    void managedChannelClosed(Channel channel, SessionSpec spec) {
    }
}
