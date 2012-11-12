package org.hidetake.gradle.ssh

import com.jcraft.jsch.Channel;

class UnmanagedChannels implements OperationEventListener {
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
