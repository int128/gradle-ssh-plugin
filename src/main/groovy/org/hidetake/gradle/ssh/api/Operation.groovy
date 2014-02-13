package org.hidetake.gradle.ssh.api

/**
 * Handler for a operation closure.
 *
 * @author hidetake.org
 *
 */
interface Operation {
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
     * @param settings shell settings
     * @param interactions closure including interactions
     * @return output value of the command
     */
    void shell(HashMap settings, Closure interactions)

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
     * @param settings execution settings
     * @param command
     * @return output value of the command
     */
    String execute(HashMap settings, String command)

    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param settings execution settings
     * @param command
     * @param interactions closure including interactions
     * @return output value of the command
     */
    String execute(HashMap settings, String command, Closure interactions)

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
     * @param settings execution settings
     * @param command
     */
    String executeSudo(HashMap settings, String command)

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
     * @param settings execution settings
     * @param command
     */
    CommandContext executeBackground(HashMap settings, String command)

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
