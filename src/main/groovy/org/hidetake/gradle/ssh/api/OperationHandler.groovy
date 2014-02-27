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
     * Performs a shell operation.
     * This method blocks until channel is closed.
     *
     * @param options properties to configure the channel
     * @param interactions closure including interactions
     * @return output value of the command
     */
    void shell(Map<String, Object> options, Closure interactions)

    /**
     * Performs a shell operation.
     * This method blocks until channel is closed.
     *
     * @param interactions closure including interactions
     * @return output value of the command
     */
    void shell(Closure interactions)

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
     * @param command
     * @param interactions closure including interactions
     * @return output value of the command
     */
    String execute(String command, Closure interactions)

    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param options properties to configure the channel
     * @param command
     * @return output value of the command
     */
    String execute(Map<String, Object> options, String command)

    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param options properties to configure the channel
     * @param command
     * @param interactions closure including interactions
     * @return output value of the command
     */
    String execute(Map<String, Object> options, String command, Closure interactions)

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
    String executeSudo(Map<String, Object> options, String command)

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the command concurrently.
     *
     * @param command
     */
    CommandContext executeBackground(String command)

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the command concurrently.
     *
     * @param options properties to configure the channel
     * @param command
     */
    CommandContext executeBackground(Map<String, Object> options, String command)

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
    void get(Map<String, Object> options, String remote, String local)

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
    void put(Map<String, Object> options, String local, String remote)

    /**
     * Enables local port forwarding.
     *
     * @param remoteHost remote host
     * @param remotePort remote port
     * @return local port (automatically assigned)
     */
    int forwardLocalPortTo(String remoteHost, int remotePort)
}
