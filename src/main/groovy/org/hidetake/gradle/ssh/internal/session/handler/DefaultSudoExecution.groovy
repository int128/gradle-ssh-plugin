package org.hidetake.gradle.ssh.internal.session.handler

import groovy.util.logging.Slf4j
import org.codehaus.groovy.tools.Utilities
import org.hidetake.gradle.ssh.api.operation.ExecutionSettings
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.session.handler.SudoExecution

/**
 * A default implementation of {@link SudoExecution}.
 *
 * @author hidetake.org
 */
@Slf4j
class DefaultSudoExecution implements SudoExecution {
    @Override
    String executeSudo(String command) {
        log.info("Execute a command ($command) with sudo support")
        executeSudoInternal(ExecutionSettings.DEFAULT, command)
    }

    @Override
    String executeSudo(HashMap settings, String command) {
        log.info("Execute a command ($command) with sudo support and settings ($settings)")
        executeSudoInternal(new ExecutionSettings(settings), command)
    }

    @Override
    void executeSudo(String command, Closure callback) {
        log.info("Execute a command ($command) with sudo support")
        def result = executeSudoInternal(ExecutionSettings.DEFAULT, command)
        callback(result)
    }

    @Override
    void executeSudo(HashMap settings, String command, Closure callback) {
        log.info("Execute a command ($command) with sudo support and settings ($settings)")
        def result = executeSudoInternal(new ExecutionSettings(settings), command)
        callback(result)
    }

    private executeSudoInternal(ExecutionSettings settings, String command) {
        assert operations instanceof Operations

        def prompt = UUID.randomUUID().toString()
        def lines = [] as List<String>
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

        operations.execute(settings + [interaction: interaction], "sudo -S -p '$prompt' $command")

        lines.join(Utilities.eol())
    }
}
