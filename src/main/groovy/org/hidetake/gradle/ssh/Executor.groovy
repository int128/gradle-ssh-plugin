package org.hidetake.gradle.ssh

import com.jcraft.jsch.Channel
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

/**
 * Session manager.
 * 
 * @author hidetake.org
 *
 */
class Executor {
	final List<Session> sessions = []
	final List<Channel> channels = []

	/**
	 * Open a session and perform operations.
	 *
	 * @param spec
	 */
	void execute(SshSpec spec) {
		def jsch = new JSch()
		jsch.config.putAll(spec.config)
		try {
			spec.sessionSpecs.each { sessionSpec ->
				def session = jsch.getSession(sessionSpec.remote.user, sessionSpec.remote.host)
				sessions.add(session)
				session.identityRepository.add(sessionSpec.remote.identity.bytes)
				session.connect()

				def handler = new OperationHandler(session)
				handler.with(sessionSpec.operationClosure)
				channels.addAll(handler.channels)
			}
			waitForAll()
		} finally {
			dispose()
		}
	}

	/**
	 * Wait for all channels until closed.
	 */
	void waitForAll() {
		while (!allClosed) {
			Thread.sleep(500L)
		}
	}

	/**
	 * Returns true if all channels are closed.
	 * 
	 * @return
	 */
	boolean isAllClosed() {
		channels.find { !it.closed } == null
	}

	/**
	 * Returns channels which returned error status.
	 * 
	 * @return
	 */
	List<Channel> getErrorChannels() {
		channels.findAll { !(it.exitStatus == 0 || it.exitStatus == -1) }
	}

	/**
	 * Disposes sessions.
	 */
	void dispose() {
		sessions.each { it.disconnect() }
	}
}
