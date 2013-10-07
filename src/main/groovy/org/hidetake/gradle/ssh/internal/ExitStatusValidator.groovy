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
        validate(channel)
    }

    /**
     * Validates exit status of the channel is zero.
     * This method must be called before channel is disconnected.
     *
     * @param channel
     */
    static void validate(Channel channel) {
        assert channel.exitStatus != -1, 'validate should be called before disconnect'
        if (channel.exitStatus > 0) {
            throw new RuntimeException("Channel #${channel.id} returned exit status ${channel.exitStatus}")
        }
    }
}
