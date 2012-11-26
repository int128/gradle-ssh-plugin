package org.hidetake.gradle.ssh.api

/**
 * Specification of a session.
 *
 * @author hidetake.org
 *
 */
class SessionSpec {
	/**
	 * Remote.
	 */
	final Remote remote

	/**
	 * Closure for {@link OperationHandler}.
	 */
	final Closure operationClosure

	/**
	 * Constructor.
	 * 
	 * @param remote
	 * @param operationClosure
	 */
	SessionSpec(Remote remote, Closure operationClosure) {
		this.remote = remote
		this.operationClosure = operationClosure
	}
}
