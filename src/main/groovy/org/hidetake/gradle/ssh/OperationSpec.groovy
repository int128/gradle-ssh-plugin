package org.hidetake.gradle.ssh

/**
 * Specification of session operations.
 * 
 * @author hidetake.org
 *
 */
interface OperationSpec {
	/**
	 * Perform an execution operation.
	 *
	 * @param command
	 */
	void execute(String command)

	/**
	 * Perform a GET operation.
	 *
	 * @param remote
	 * @param local
	 */
	void get(String remote, String local)

	/**
	 * Perform a PUT operation.
	 *
	 * @param local
	 * @param remote
	 */
	void put(String local, String remote)
}
