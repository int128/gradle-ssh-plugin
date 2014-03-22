package org.hidetake.gradle.ssh.api.session.handler

/**
 * Handler for sudo execution.
 *
 * @author hidetake.org
 */
interface SudoExecution {
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
}
