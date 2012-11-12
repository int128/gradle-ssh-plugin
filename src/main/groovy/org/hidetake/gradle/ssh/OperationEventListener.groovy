package org.hidetake.gradle.ssh

import com.jcraft.jsch.Channel

/**
 * Event listener for operation closure.
 * 
 * @author hidetake.org
 *
 */
interface OperationEventListener {
	/**
	 * Notifies that an unmanaged channel has been connected.
	 * You should disconnect the channel finally.
	 * 
	 * @param channel
	 * @param spec
	 */
	void unmanagedChannelConnected(Channel channel, SessionSpec spec)

	/**
	 * Notifies that an managed channel has been connected.
	 * 
	 * @param channel
	 * @param spec
	 */
	void managedChannelConnected(Channel channel, SessionSpec spec)

	/**
	 * Notifies that an unmanaged channel has been closed.
	 * You can get exit status of the channel.
	 * 
	 * @param channel
	 * @param spec
	 */
	void managedChannelClosed(Channel channel, SessionSpec spec)
}
