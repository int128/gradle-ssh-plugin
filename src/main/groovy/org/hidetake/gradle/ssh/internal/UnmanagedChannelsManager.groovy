package org.hidetake.gradle.ssh.internal

import org.hidetake.gradle.ssh.api.OperationEventListener
import org.hidetake.gradle.ssh.api.SessionSpec

import com.jcraft.jsch.Channel

/**
 * Event listener for lifecycle management of unmanaged channels.
 * 
 * @author hidetake.org
 *
 */
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
	 * Returns channels which returned error status.
	 * 
	 * @return
	 */
	Collection<Channel> getErrorChannels() {
		channels.findAll { it.exitStatus > 0 }
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
