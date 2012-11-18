package org.hidetake.gradle.ssh.internal

import org.hidetake.gradle.ssh.api.OperationEventListener
import org.hidetake.gradle.ssh.api.OperationHandler
import org.hidetake.gradle.ssh.api.SessionSpec

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.Session

/**
 * Default implementation of {@link OperationHandler}.
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
	 * @param spec
	 * @param session
	 */
	DefaultOperationHandler(SessionSpec spec, Session session) {
		this.spec = spec
		this.session = session
	}

	@Override
	void execute(String command) {
		listeners*.beginOperation('execute', command)
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
		listeners*.beginOperation('executeBackground', command)
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
		listeners*.beginOperation('get', remote, local)
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
		listeners*.beginOperation('put', remote, local)
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
