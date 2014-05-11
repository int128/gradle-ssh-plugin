package org.hidetake.gradle.ssh.plugin.session

import org.hidetake.gradle.ssh.plugin.Remote

/**
 * A core handler for session closure.
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
     * @return output value of the command
     */
    void shell(HashMap settings)

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
     * @param callback closure called with an output value of the command
     * @return output value of the command
     */
    void execute(String command, Closure callback)

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
     * @param callback closure called with an output value of the command
     * @return output value of the command
     */
    void execute(HashMap settings, String command, Closure callback)

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
     * @param command
     * @param callback closure called with an output value of the command
     */
    void executeBackground(String command, Closure callback)

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the command concurrently.
     *
     * @param settings execution settings
     * @param command
     */
    void executeBackground(HashMap settings, String command)

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the command concurrently.
     *
     * @param settings execution settings
     * @param command
     * @param callback closure called with an output value of the command
     */
    void executeBackground(HashMap settings, String command, Closure callback)
}
