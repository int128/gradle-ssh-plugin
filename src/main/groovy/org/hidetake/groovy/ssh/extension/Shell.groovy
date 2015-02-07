package org.hidetake.groovy.ssh.extension

import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.session.SessionExtension
import org.slf4j.LoggerFactory

trait Shell implements SessionExtension {
    private static final log = LoggerFactory.getLogger(Shell)

    /**
     * Performs a shell operation.
     * This method blocks until channel is closed.
     *
     * @param settings shell settings
     * @return output value of the command
     */
    void shell(HashMap settings) {
        assert settings != null, 'settings must not be null'
        log.info("Execute a shell with settings ($settings)")
        operations.shell(operationSettings + new OperationSettings(settings))
    }
}