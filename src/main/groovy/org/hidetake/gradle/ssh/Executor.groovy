package org.hidetake.gradle.ssh

import java.util.Map;

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

/**
 * Executes a SSH task.
 * 
 * @author hidetake.org
 *
 */
@Singleton
class Executor {
	protected Closure<JSch> createJSchInstance = { new JSch() }

	/**
	 * Opens sessions and performs each operations.
	 *
	 * @param sshSpec
	 */
	void execute(SshSpec sshSpec) {
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

			def unmanagedChannels = new UnmanagedChannels()
			sessions.each { spec, session ->
				def evaluator = new OperationClosureEvaluator(spec, session)
				evaluator.listeners.add(unmanagedChannels)
				evaluator.with(spec.operationClosure)
			}

			while (unmanagedChannels.pending) {
				Thread.sleep(500L)
			}
		} finally {
			sessions.each { spec, session -> session.disconnect() }
		}
	}
}
