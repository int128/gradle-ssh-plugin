package org.hidetake.gradle.ssh

/**
 * Handler for the session closure.
 * 
 * @author hidetake.org
 *
 */
interface OperationHandler {
	/**
	 * Perform an execution operation.
	 * This method blocks until channel is closed.
	 *
	 * @param command
	 */
	void execute(String command)

	/**
	 * Perform an execution operation.
	 * This method returns immediately and executes the command concurrently.
	 *
	 * @param command
	 */
	void executeBackground(String command)

	/**
	 * Perform a GET operation.
	 * This method blocks until channel is closed.
	 *
	 * @param remote
	 * @param local
	 */
	void get(String remote, String local)

	/**
	 * Perform a PUT operation.
	 * This method blocks until channel is closed.
	 *
	 * @param local
	 * @param remote
	 */
	void put(String local, String remote)
}
