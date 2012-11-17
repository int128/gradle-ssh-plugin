package org.hidetake.gradle.ssh

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
	Remote remote

	/**
	 * Closure for {@link OperationHandler}.
	 */
	Closure operationClosure
}
