package org.hidetake.groovy.ssh.extension

import groovy.util.logging.Slf4j
import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.operation.Operations
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
        Internal.sudo(operations, operationSettings + new OperationSettings(), command)
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
        Internal.sudo(operations, operationSettings + new OperationSettings(settings), command)
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
        callback.call(Internal.sudo(operations, operationSettings + new OperationSettings(), command))
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
        callback.call(Internal.sudo(operations, operationSettings + new OperationSettings(settings), command))
    }


    @Slf4j
    private static class Internal {
        static sudo(Operations operations, OperationSettings settings, String command) {
            final prompt = UUID.randomUUID().toString()
            final lines = []
            final interationSettings = new OperationSettings(interaction: {
                when(partial: prompt, from: standardOutput) {
                    log.info("Providing password for sudo prompt on $operations.remote.name")
                    standardInput << operations.remote.password << '\n'

                    when(nextLine: _, from: standardOutput) {
                        when(nextLine: 'Sorry, try again.') {
                            log.error("Failed sudo authentication on $operations.remote.name")
                            throw new RuntimeException('sudo authentication failed')
                        }
                        when(line: _, from: standardOutput) {
                            log.info("Success sudo authentication on $operations.remote.name")
                            lines << it
                        }
                    }
                }
                when(line: _, from: standardOutput) {
                    lines << it
                }
            })

            final sudoCommand = "sudo -S -p '$prompt' $command"
            operations.execute(settings + interationSettings, sudoCommand)

            lines.join(Utilities.eol())
        }
    }
}
