package org.hidetake.gradle.ssh.api.session

import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.registry.Registry

/**
 * A core handler for session closure.
 *
 * @author hidetake.org
 */
interface SessionHandler {
    /**
     * A factory of {@link SessionHandler}.
     */
    interface Factory {
        SessionHandler create(Operations operations)
    }

    final factory = Registry.instance[Factory]

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
     * @param settings execution settings
     * @param command
     * @return output value of the command
     */
    String execute(HashMap settings, String command)

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
}
