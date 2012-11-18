package org.hidetake.gradle.ssh.api

import com.jcraft.jsch.Channel

/**
 * Event listener for a operation closure.
 * 
 * @author hidetake.org
 *
 */
interface OperationEventListener {
	/**
	 * Notifies that operation has been started.
	 * 
	 * @param operation
	 */
	void beginOperation(String operation, Object... args)

	/**
	 * Notifies that an unmanaged channel has been connected.
	 * You should disconnect the channel later (typically do it in <code>finally</code> block).
	 * 
	 * @param channel
	 * @param spec
	 */
	void unmanagedChannelConnected(Channel channel, SessionSpec spec)

	/**
	 * Notifies that a managed channel has been connected.
	 * 
	 * @param channel
	 * @param spec
	 */
	void managedChannelConnected(Channel channel, SessionSpec spec)

	/**
	 * Notifies that a managed channel has been closed.
	 * You can get exit status of the channel.
	 * 
	 * @param channel
	 * @param spec
	 */
	void managedChannelClosed(Channel channel, SessionSpec spec)
}
