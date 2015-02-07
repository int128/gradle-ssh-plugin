package org.hidetake.groovy.ssh.extension

import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.session.SessionExtension
import org.slf4j.LoggerFactory

trait Command implements SessionExtension {
    private static final log = LoggerFactory.getLogger(Command)

    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param command
     * @return output value of the command
     */
    String execute(String command) {
        assert command, 'command must be given'
        log.info("Execute a command ($command)")
        operations.execute(operationSettings, command, null)
    }

    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param command
     * @param callback closure called with an output value of the command
     * @return output value of the command
     */
    void execute(String command, Closure callback) {
        assert command, 'command must be given'
        assert callback, 'callback must be given'
        log.info("Execute a command ($command) with callback")
        operations.execute(operationSettings, command, callback)
    }

    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param settings execution settings
     * @param command
     * @return output value of the command
     */
    String execute(HashMap settings, String command) {
        assert command, 'command must be given'
        assert settings != null, 'settings must not be null'
        log.info("Execute a command ($command) with settings ($settings)")
        operations.execute(operationSettings + new OperationSettings(settings), command, null)
    }

    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param settings execution settings
     * @param command
     * @param callback closure called with an output value of the command
     * @return output value of the command
     */
    void execute(HashMap settings, String command, Closure callback) {
        assert command, 'command must be given'
        assert callback, 'callback must be given'
        assert settings != null, 'settings must not be null'
        log.info("Execute a command ($command) with settings ($settings) and callback")
        operations.execute(operationSettings + new OperationSettings(settings), command, callback)
    }
}