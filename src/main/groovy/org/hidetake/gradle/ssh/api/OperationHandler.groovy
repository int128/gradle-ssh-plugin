package org.hidetake.gradle.ssh.api

/**
 * Handler for a operation closure.
 *
 * @author hidetake.org
 *
 */
interface OperationHandler {
    /**
     * Returns remote host for current operation.
     *
     * @return the remote host
     */
    Remote getRemote()

    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param command
     * @return output value of the command
     */
    String execute(String command)

    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param options properties to configure the channel
     * @param command
     * @return output value of the command
     */
    String execute(Map options, String command)

    /**
     * Performs a sudo operation, explicitly providing password for the sudo user.
     * This method blocks until channel is closed
     *
     * @param command
     */
    String executeSudo(String command)

    /**
     * Performs a sudo operation, explicitly providing password for the sudo user.
     * This method blocks until channel is closed
     *
     * @param options properties to configure the channel
     * @param command
     */
    String executeSudo(Map options, String command)

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the command concurrently.
     *
     * @param command
     */
    void executeBackground(String command)

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the command concurrently.
     *
     * @param options properties to configure the channel
     * @param command
     */
    void executeBackground(Map options, String command)

    /**
     * Performs a GET operation.
     * This method blocks until channel is closed.
     *
     * @param remote
     * @param local
     */
    void get(String remote, String local)

    /**
     * Performs a GET operation.
     * This method blocks until channel is closed.
     *
     * @param options properties to configure the channel
     * @param remote
     * @param local
     */
    void get(Map options, String remote, String local)

    /**
     * Performs a PUT operation.
     * This method blocks until channel is closed.
     *
     * @param local
     * @param remote
     */
    void put(String local, String remote)

    /**
     * Performs a PUT operation.
     * This method blocks until channel is closed.
     *
     * @param options properties to configure the channel
     * @param local
     * @param remote
     */
    void put(Map options, String local, String remote)
}
