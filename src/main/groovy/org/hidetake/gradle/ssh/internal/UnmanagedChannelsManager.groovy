package org.hidetake.gradle.ssh.internal

import org.hidetake.gradle.ssh.OperationEventListener
import org.hidetake.gradle.ssh.SessionSpec

import com.jcraft.jsch.Channel

class UnmanagedChannelsManager implements OperationEventListener {
	final List<Channel> channels = []

	/**
	 * Returns true if there are pending channels.
	 * Also should return false if contains no channel.
	 * 
	 * @return
	 */
	boolean isPending() {
		channels.find { !it.closed } != null
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
	void managedChannelConnected(Channel channel, SessionSpec spec) {
	}

	@Override
	void managedChannelClosed(Channel channel, SessionSpec spec) {
	}
}
