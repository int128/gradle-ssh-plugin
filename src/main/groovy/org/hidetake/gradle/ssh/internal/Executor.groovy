package org.hidetake.gradle.ssh.internal

import org.gradle.api.GradleException
import org.hidetake.gradle.ssh.OperationHandler
import org.hidetake.gradle.ssh.SessionSpec
import org.hidetake.gradle.ssh.SshSpec

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

/**
 * Executes a SSH task.
 * 
 * @author hidetake.org
 *
 */
class Executor {
	protected Closure<JSch> createJSchInstance = { new JSch() }

	/**
	 * Executes SSH.
	 * 
	 * @param sshSpec
	 */
	void execute(SshSpec sshSpec) {
		if (sshSpec.dryRun) {
			dryRun(sshSpec)
		} else {
			run(sshSpec)
		}
	}

	/**
	 * Opens sessions and performs each operations.
	 *
	 * @param sshSpec
	 */
	void run(SshSpec sshSpec) {
		JSch jsch = createJSchInstance()
		jsch.config.putAll(sshSpec.config)

		Map<SessionSpec, Session> sessions = [:]
		try {
			sshSpec.sessionSpecs.each { spec ->
				def session = jsch.getSession(spec.remote.user, spec.remote.host)
				session.identityRepository.add(spec.remote.identity.bytes)
				session.connect()
				sessions.put(spec, session)
			}

			def unmanagedChannelsManager = new UnmanagedChannelsManager()
			try {
				sessions.each { spec, session ->
					def handler = new DefaultOperationHandler(spec, session)
					handler.listeners.add(unmanagedChannelsManager)
					handler.with(spec.operationClosure)
				}
				while (unmanagedChannelsManager.pending) {
					Thread.sleep(500L)
				}
				def errorChannels = unmanagedChannelsManager.errorChannels
				if (errorChannels.size() > 0) {
					throw new GradleException(errorChannels.collect {
						"Channel #${it.id} returned status ${it.exitStatus}"}.join('\n'))
				}
			} finally {
				unmanagedChannelsManager.disconnect()
			}
		} finally {
			sessions.each { spec, session -> session.disconnect() }
		}
	}

	/**
	 * Opens sessions but performs no operation.
	 * 
	 * @param sshSpec
	 */
	void dryRun(SshSpec sshSpec) {
		def handler = new OperationHandler() {
			@Override
			void execute(String command) {
				// TODO: logger.warn()
			}
			@Override
			void executeBackground(String command) {
				// TODO: logger.warn()
			}
			@Override
			void get(String remote, String local) {
				// TODO: logger.warn()
			}
			@Override
			void put(String local, String remote) {
				// TODO: logger.warn()
			}
		}
		sshSpec.sessionSpecs.each { spec ->
			handler.with(spec.operationClosure)
		}
	}
}
