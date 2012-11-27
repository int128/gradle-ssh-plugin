package org.hidetake.gradle.ssh.internal

import org.hidetake.gradle.ssh.api.OperationEventListener
import org.hidetake.gradle.ssh.api.SessionSpec

import com.jcraft.jsch.Channel

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
		if (channel.exitStatus > 0) {
			throw new IllegalStateException("Channel #${channel.id} returned exit status ${channel.exitStatus}")
		}
	}
}
