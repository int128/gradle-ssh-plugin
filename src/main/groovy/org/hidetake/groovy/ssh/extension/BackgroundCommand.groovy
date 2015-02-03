package org.hidetake.groovy.ssh.extension

import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.session.SessionExtension
import org.slf4j.LoggerFactory

trait BackgroundCommand implements SessionExtension {
    private static final log = LoggerFactory.getLogger(BackgroundCommand)

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the command concurrently.
     *
     * @param command
     */
    void executeBackground(String command) {
        assert command, 'command must be given'
        log.info("Execute a command ($command) in background")
        operations.executeBackground(operationSettings, command, null)
    }

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the command concurrently.
     *
     * @param command
     * @param callback closure called with an output value of the command
     */
    void executeBackground(String command, Closure callback) {
        assert command, 'command must be given'
        assert callback, 'callback must be given'
        log.info("Execute a command ($command) with callback in background")
        operations.executeBackground(operationSettings, command, callback)
    }

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the command concurrently.
     *
     * @param settings execution settings
     * @param command
     */
    void executeBackground(HashMap settings, String command) {
        assert command, 'command must be given'
        assert settings != null, 'settings must not be null'
        log.info("Execute a command ($command) with settings ($settings) in background")
        operations.executeBackground(operationSettings + new OperationSettings(settings), command, null)
    }

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the command concurrently.
     *
     * @param settings execution settings
     * @param command
     * @param callback closure called with an output value of the command
     */
    void executeBackground(HashMap settings, String command, Closure callback) {
        assert command, 'command must be given'
        assert callback, 'callback must be given'
        assert settings != null, 'settings must not be null'
        log.info("Execute a command ($command) with settings ($settings) and callback in background")
        operations.executeBackground(operationSettings + new OperationSettings(settings), command, callback)
    }
}
