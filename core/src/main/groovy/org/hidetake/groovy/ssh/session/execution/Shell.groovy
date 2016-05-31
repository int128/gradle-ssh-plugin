package org.hidetake.groovy.ssh.session.execution

import org.hidetake.groovy.ssh.operation.ShellSettings
import org.hidetake.groovy.ssh.session.BadExitStatusException
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
     * @param map shell settings
     * @return output value of the command
     */
    void shell(HashMap map) {
        assert map != null, 'map must not be null'

        def settings = new ShellSettings.With(mergedSettings, new ShellSettings.With(map))
        def operation = operations.shell(settings)

        def exitStatus = operation.startSync()
        if (exitStatus != 0 && !settings.ignoreError) {
            throw new BadExitStatusException("Shell returned exit status $exitStatus", exitStatus)
        }
    }
}