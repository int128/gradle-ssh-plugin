package org.hidetake.gradle.ssh.internal.session.handler

import groovy.util.logging.Slf4j
import org.codehaus.groovy.tools.Utilities
import org.hidetake.gradle.ssh.api.operation.OperationSettings
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
        assert operationSettings instanceof OperationSettings
        executeSudoInternal(command, operationSettings)
    }

    @Override
    String executeSudo(HashMap settings, String command) {
        log.info("Execute a command ($command) with sudo support and settings ($settings)")
        assert operationSettings instanceof OperationSettings
        executeSudoInternal(command, operationSettings + new OperationSettings(settings))
    }

    @Override
    void executeSudo(String command, Closure callback) {
        log.info("Execute a command ($command) with sudo support")
        assert operationSettings instanceof OperationSettings
        def result = executeSudoInternal(command, operationSettings)
        callback(result)
        result
    }

    @Override
    void executeSudo(HashMap settings, String command, Closure callback) {
        log.info("Execute a command ($command) with sudo support and settings ($settings)")
        assert operationSettings instanceof OperationSettings
        def result = executeSudoInternal(command, operationSettings + new OperationSettings(settings))
        callback(result)
        result
    }

    private executeSudoInternal(String command, OperationSettings settings) {
        log.debug("Executing sudo ($command) with $settings")
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

        operations.execute(settings + new OperationSettings(interaction: interaction),
                "sudo -S -p '$prompt' $command", null)

        lines.join(Utilities.eol())
    }
}
