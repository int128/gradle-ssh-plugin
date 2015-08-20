package org.hidetake.groovy.ssh.extension

import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.session.SessionExtension

/**
 * Provides the shell execution.
 *
 * @author Hidetake Iwata
 */
trait Shell implements SessionExtension {
    /**
     * Performs a shell operation.
     * This method blocks until channel is closed.
     *
     * @param settings shell settings
     * @return output value of the command
     */
    void shell(HashMap settings) {
        assert settings != null, 'settings must not be null'
        operations.shell(operationSettings + new OperationSettings(settings))
    }
}