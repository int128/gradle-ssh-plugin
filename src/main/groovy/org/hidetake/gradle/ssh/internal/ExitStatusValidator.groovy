package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.Channel
import org.hidetake.gradle.ssh.api.OperationEventListener
import org.hidetake.gradle.ssh.api.SessionSpec

/**
 * An event listener to validate exit status of the channel.
 * This class supports only managed channels.
 *
 * @author hidetake.org
 *
 */
class ExitStatusValidator implements OperationEventListener {
    @Override
    void beginOperation(String operation, Object... args) {
    }

    @Override
    void unmanagedChannelConnected(Channel channel, SessionSpec spec) {
    }

    @Override
    void managedChannelConnected(Channel channel, SessionSpec spec) {
    }

    @Override
    void managedChannelClosed(Channel channel, SessionSpec spec) {
        channelClosed(channel)
    }

    /**
     * Notifies that the channel has been closed.
     * This method raises exception if exit status is greater that zero.
     *
     * @param channel
     */
    void channelClosed(Channel channel) {
        if (channel.exitStatus > 0) {
            throw new IllegalStateException("Channel #${channel.id} returned exit status ${channel.exitStatus}")
        }
    }
}
