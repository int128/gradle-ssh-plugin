package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.Channel
import org.hidetake.gradle.ssh.api.OperationEventListener
import org.hidetake.gradle.ssh.api.SessionSpec

/**
 * Event listener for lifecycle management of unmanaged channels.
 *
 * @author hidetake.org
 *
 */
class ChannelsLifecycleManager implements OperationEventListener {
    final channels = [] as List<Channel>

    /**
     * Wait for pending channels.
     *
     * <p>A channel has following state:</p>
     * <ol>
     * <li>pending: execution is running (not closed)</li>
     * <li>closed: execution has been finished (closed and exit status is not -1)</li>
     * <li>disconnected: {@link Channel#disconnect()} has been called (closed and exit status is -1)</li>
     * </ol>
     */
    void waitForPending() {
        def pendingFilter = { Channel channel -> !channel.closed }
        def closedFilter = { Channel channel -> channel.closed && channel.exitStatus != -1 }
        while (channels.find(pendingFilter) != null) {
            channels.findAll(closedFilter).each { channel ->
                ExitStatusValidator.validate(channel)
                channel.disconnect()
            }
            sleep(500L)
        }
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
