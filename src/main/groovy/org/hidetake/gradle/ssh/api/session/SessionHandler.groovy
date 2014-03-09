package org.hidetake.gradle.ssh.api.session

import org.hidetake.gradle.ssh.api.Remote

/**
 * Handler of operations in a session closure.
 *
 * @author hidetake.org
 */
interface SessionHandler {
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
     * @param closure closure for {@link org.hidetake.gradle.ssh.api.operation.ShellHandler}
     * @return output value of the command
     */
    void shell(HashMap settings, Closure closure)

    /**
     * Performs a shell operation.
     * This method blocks until channel is closed.
     *
     * @param closure closure for {@link org.hidetake.gradle.ssh.api.operation.ShellHandler}
     * @return output value of the command
     */
    void shell(Closure closure)

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
     * @param closure closure for {@link org.hidetake.gradle.ssh.api.operation.ExecutionHandler}
     * @return output value of the command
     */
    String execute(String command, Closure closure)

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
     * @param closure closure for {@link org.hidetake.gradle.ssh.api.operation.ExecutionHandler}
     * @return output value of the command
     */
    String execute(HashMap settings, String command, Closure closure)

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
    void executeBackground(String command)

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the command concurrently.
     *
     * @param settings execution settings
     * @param command
     */
    void executeBackground(HashMap settings, String command)

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
