package org.hidetake.gradle.ssh.extension

import groovy.util.logging.Slf4j
import org.codehaus.groovy.tools.Utilities
import org.hidetake.gradle.ssh.plugin.Remote
import org.hidetake.gradle.ssh.plugin.session.SessionHandler

/**
 * An extension class of sudo command execution.
 *
 * @author hidetake.org
 */
@Category(SessionHandler)
@Slf4j
class SudoExecution {
    /**
     * Support class for sudo interaction.
     */
    private static class InteractionSupport {
        final String commandWithSudo
        final HashMap settings = [:]

        private final lines = []

        /**
         * Constructor.
         *
         * @param command will be prepended with <code>sudo</code>
         * @param remote should be given to provide the password
         */
        def InteractionSupport(String command, Remote remote, Map givenSettings = [:]) {
            def prompt = UUID.randomUUID().toString()
            def interaction = {
                when(partial: prompt, from: standardOutput) {
                    log.info("Sending password for sudo authentication")
                    standardInput << remote.password << '\n'

                    when(nextLine: _, from: standardOutput) {
                        when(nextLine: 'Sorry, try again.') {
                            throw new RuntimeException("Sudo authentication failed")
                        }
                        when(line: _, from: standardOutput) {
                            lines << it
                        }
                    }
                }
                when(line: _, from: standardOutput) {
                    lines << it
                }
            }

            commandWithSudo = "sudo -S -p '$prompt' $command"

            settings << givenSettings << [interaction: interaction]
        }

        String getResult() {
            lines.join(Utilities.eol())
        }
    }

    /**
     * Performs a sudo operation, explicitly providing password for the sudo user.
     * This method blocks until channel is closed.
     *
     * @param command
     * @return output value of the command
     */
    String executeSudo(String command) {
        assert command, 'command must be given'
        log.info("Execute a command ($command) with sudo support")
        def support = new InteractionSupport(command, remote)
        execute(support.settings, support.commandWithSudo)
        support.result
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
        log.info("Execute a command ($command) with sudo support and settings ($settings)")
        def support = new InteractionSupport(command, remote, settings)
        execute(support.settings, support.commandWithSudo)
        support.result
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
        log.info("Execute a command ($command) with sudo support")
        def support = new InteractionSupport(command, remote)
        execute(support.settings, support.commandWithSudo)
        callback(support.result)
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
        log.info("Execute a command ($command) with sudo support and settings ($settings)")
        def support = new InteractionSupport(command, remote, settings)
        execute(support.settings, support.commandWithSudo)
        callback(support.result)
    }
}
