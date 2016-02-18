package org.hidetake.groovy.ssh.extension

import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.session.SessionExtension

/**
 * Provides the blocking command execution.
 *
 * @author Hidetake Iwata
 */
trait Command implements SessionExtension {
    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param command
     * @return output value of the command
     */
    String execute(String command) {
        assert command, 'command must be given'
        operations.execute(operationSettings, command)
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
        callback.call(operations.execute(operationSettings, command))
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
        operations.execute(operationSettings + new OperationSettings(settings), command)
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
        callback.call(operations.execute(operationSettings + new OperationSettings(settings), command))
    }
}