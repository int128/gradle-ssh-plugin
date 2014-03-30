package org.hidetake.gradle.ssh.api.session.handler

/**
 * Handler for sudo execution.
 *
 * @author hidetake.org
 */
interface SudoExecution {
    /**
     * Performs a sudo operation, explicitly providing password for the sudo user.
     * This method blocks until channel is closed.
     *
     * @param command
     * @return output value of the command
     */
    String executeSudo(String command)

    /**
     * Performs a sudo operation, explicitly providing password for the sudo user.
     * This method blocks until channel is closed.
     *
     * @param settings execution settings
     * @param command
     * @return output value of the command
     */
    String executeSudo(HashMap settings, String command)

    /**
     * Performs a sudo operation, explicitly providing password for the sudo user.
     * This method blocks until channel is closed.
     *
     * @param command
     * @param callback closure called with an output value of the command
     */
    void executeSudo(String command, Closure callback)

    /**
     * Performs a sudo operation, explicitly providing password for the sudo user.
     * This method blocks until channel is closed.
     *
     * @param settings execution settings
     * @param command
     * @param callback closure called with an output value of the command
     */
    void executeSudo(HashMap settings, String command, Closure callback)
}
