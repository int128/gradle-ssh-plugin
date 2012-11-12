package org.hidetake.gradle.ssh.internal

import org.hidetake.gradle.ssh.OperationEventListener
import org.hidetake.gradle.ssh.OperationHandler
import org.hidetake.gradle.ssh.SessionSpec

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.Session

/**
 * This class handles SSH operations described in the closure.
 * 
 * @author hidetake.org
 *
 */
class DefaultOperationHandler implements OperationHandler {
	protected final SessionSpec spec
	protected final Session session

	/**
	 * Event listeners.
	 */
	final List<OperationEventListener> listeners = []

	/**
	 * Constructor.
	 * 
	 * @param session
	 */
	DefaultOperationHandler(SessionSpec spec, Session session) {
		this.spec = spec
		this.session = session
	}

	@Override
	void execute(String command) {
		ChannelExec channel = session.openChannel('exec')
		channel.command = command
		channel.inputStream = null
		channel.setOutputStream(System.out, true)
		channel.setErrStream(System.err, true)
		try {
			channel.connect()
			listeners*.managedChannelConnected(channel, spec)
			while (!channel.closed) {
				Thread.sleep(500L)
			}
			listeners*.managedChannelClosed(channel, spec)
		} finally {
			channel.disconnect()
		}
	}

	@Override
	void executeBackground(String command) {
		ChannelExec channel = session.openChannel('exec')
		channel.command = command
		channel.inputStream = null
		channel.setOutputStream(System.out, true)
		channel.setErrStream(System.err, true)
		channel.connect()
		listeners*.unmanagedChannelConnected(channel, spec)
	}

	@Override
	void get(String remote, String local) {
		ChannelSftp channel = session.openChannel('sftp')
		try {
			channel.connect()
			listeners*.managedChannelConnected(channel, spec)
			channel.get(remote, local)
			listeners*.managedChannelClosed(channel, spec)
		} finally {
			channel.disconnect()
		}
	}

	@Override
	void put(String local, String remote) {
		ChannelSftp channel = session.openChannel('sftp')
		try {
			channel.connect()
			listeners*.managedChannelConnected(channel, spec)
			channel.put(local, remote, ChannelSftp.OVERWRITE)
			listeners*.managedChannelClosed(channel, spec)
		} finally {
			channel.disconnect()
		}
	}
}
