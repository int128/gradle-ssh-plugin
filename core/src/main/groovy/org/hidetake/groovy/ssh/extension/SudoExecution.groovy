package org.hidetake.groovy.ssh.extension

import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.session.SessionExtension
import org.slf4j.LoggerFactory

/**
 * An extension class of sudo command execution.
 *
 * @author Hidetake Iwata
 */
trait SudoExecution implements SessionExtension {
    /**
     * Performs a sudo operation, explicitly providing password for the sudo user.
     * This method blocks until channel is closed.
     *
     * @param command
     * @return output value of the command
     */
    String executeSudo(String command) {
        assert command, 'command must be given'
        executeSudoInternal(command, remote, [:])
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
        executeSudoInternal(command, remote, settings)
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
        def result = executeSudoInternal(command, remote, [:])
        callback(result)
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
        def result = executeSudoInternal(command, remote, settings)
        callback(result)
    }

    /**
     * Performs a sudo operation.
     *
     * @param command will be prepended with <code>sudo</code>
     * @param remote should be given to provide the password
     * @param givenSettings
     */
    private String executeSudoInternal(String command, Remote remote, Map givenSettings) {
        final log = LoggerFactory.getLogger(SudoExecution)
        final prompt = UUID.randomUUID().toString()
        final lines = []

        final settings = [:] << givenSettings << [interaction: {
            when(partial: prompt, from: standardOutput) {
                log.info('Providing the password for sudo authentication')
                standardInput << remote.password << '\n'

                when(nextLine: _, from: standardOutput) {
                    when(nextLine: 'Sorry, try again.') {
                        throw new RuntimeException('sudo authentication failed')
                    }
                    when(line: _, from: standardOutput) {
                        log.info('sudo authentication passed')
                        lines << it
                    }
                }
            }
            when(line: _, from: standardOutput) {
                lines << it
            }
        }]

        log.info('Waiting for the password prompt of sudo')
        execute(settings, "sudo -S -p '$prompt' $command")

        lines.join(Utilities.eol())
    }
}
