package org.hidetake.gradle.ssh

/**
 * Specification of a session.
 *
 *
 * @author hidetake.org
 *
 */
interface SessionSpec {
	/**
	 * Returns the remote.
	 *
	 * @return
	 */
	Remote getRemote()

	/**
	 * Returns closure for {@link OperationSpec}.
	 *
	 * @return
	 */
	Closure getOperationClosure()
}
