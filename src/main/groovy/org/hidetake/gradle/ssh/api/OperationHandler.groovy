package org.hidetake.gradle.ssh.api

/**
 * Handler for a operation closure.
 * 
 * @author hidetake.org
 *
 */
interface OperationHandler {
	/**
	 * Performs an execution operation.
	 * This method blocks until channel is closed.
	 *
	 * @param command
	 */
	void execute(String command)

	/**
	 * Performs an execution operation.
	 * This method returns immediately and executes the command concurrently.
	 *
	 * @param command
	 */
	void executeBackground(String command)

	/**
	 * Performs a GET operation.
	 * This method blocks until channel is closed.
	 *
	 * @param remote
	 * @param local
	 */
	void get(String remote, String local)

	/**
	 * Performs a PUT operation.
	 * This method blocks until channel is closed.
	 *
	 * @param local
	 * @param remote
	 */
	void put(String local, String remote)
}
