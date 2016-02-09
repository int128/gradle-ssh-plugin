package org.hidetake.groovy.ssh.extension

import org.hidetake.groovy.ssh.session.SessionExtension

/**
 * An extension class of sudo command execution.
 *
 * @author Hidetake Iwata
 */
trait Sudo implements SessionExtension {
    /**
     * Performs a sudo operation, explicitly providing password for the sudo user.
     * This method blocks until channel is closed.
     *
     * @param command
     * @return output value of the command
     */
    String executeSudo(String command) {
        assert command, 'command must be given'
        def interaction = new SudoInteraction()
        execute(interaction.settings(remote.password), interaction.command(command))
        interaction.text
    }

    /**
     * Performs a sudo operation, explicitly providing password for the sudo user.
     * This method blocks until channel is closed.
     *
     * @param settings execution settings
     * @param command
     * @return output value of the command
     */
    String executeSudo(HashMap settings, String command) {
        assert command, 'command must be given'
        assert settings != null, 'settings must not be null'
        def interaction = new SudoInteraction()
        execute(interaction.settings(remote.password, settings), interaction.command(command))
        interaction.text
    }

    /**
     * Performs a sudo operation, explicitly providing password for the sudo user.
     * This method blocks until channel is closed.
     *
     * @param command
     * @param callback closure called with an output value of the command
     */
    void executeSudo(String command, Closure callback) {
        assert command, 'command must be given'
        assert callback, 'callback must be given'
        def interaction = new SudoInteraction()
        execute(interaction.settings(remote.password), interaction.command(command))
        callback(interaction.text)
    }

    /**
     * Performs a sudo operation, explicitly providing password for the sudo user.
     * This method blocks until channel is closed.
     *
     * @param settings execution settings
     * @param command
     * @param callback closure called with an output value of the command
     */
    void executeSudo(HashMap settings, String command, Closure callback) {
        assert command, 'command must be given'
        assert callback, 'callback must be given'
        assert settings != null, 'settings must not be null'
        def interaction = new SudoInteraction()
        execute(interaction.settings(remote.password, settings), interaction.command(command))
        callback(interaction.text)
    }
}
