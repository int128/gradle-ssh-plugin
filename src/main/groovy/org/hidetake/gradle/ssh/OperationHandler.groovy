package org.hidetake.gradle.ssh

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.Session

/**
 * This class handles SSH operations described in the closure.
 * 
 * @author hidetake.org
 *
 */
class OperationHandler implements OperationSpec {
	/**
	 * SSH session
	 */
	protected final Session session

	/**
	 * channels generated in this context
	 */
	protected final List<Channel> channels = []

	/**
	 * Constructor.
	 * 
	 * @param session
	 */
	OperationHandler(Session session) {
		this.session = session
	}

	@Override
	void execute(String command) {
		ChannelExec channel = session.openChannel('exec')
		channels.add(channel)
		channel.command = command
		channel.inputStream = null
		channel.setOutputStream(System.out, true)
		channel.setErrStream(System.err, true)
		channel.connect()
	}

	@Override
	void get(String remote, String local) {
		ChannelSftp channel = session.openChannel('sftp')
		channels.add(channel)
		try {
			channel.get(remote, local)
		} finally {
			channel.close()
		}
	}

	@Override
	void put(String local, String remote) {
		ChannelSftp channel = session.openChannel('sftp')
		channels.add(channel)
		try {
			channel.put(local, remote, ChannelSftp.OVERWRITE)
		} finally {
			channel.close()
		}
	}
}
