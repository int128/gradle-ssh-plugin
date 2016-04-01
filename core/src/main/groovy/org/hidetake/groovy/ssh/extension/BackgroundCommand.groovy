package org.hidetake.groovy.ssh.extension

import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.operation.CommandSettings
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.hidetake.groovy.ssh.session.SessionExtension

/**
 * Provides the non-blocking command execution.
 *
 * @author Hidetake Iwata
 */
trait BackgroundCommand implements SessionExtension {
    /**
     * Performs an execution operation.
     * This method returns immediately and executes the commandLine concurrently.
     *
     * @param commandLine
     */
    void executeBackground(String commandLine) {
        assert commandLine, 'commandLine must be given'
        executeBackground([:], commandLine)
    }

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the commandLine concurrently.
     *
     * @param commandLine
     * @param callback closure called with an output value of the commandLine
     */
    void executeBackground(String commandLine, Closure callback) {
        assert commandLine, 'commandLine must be given'
        assert callback, 'callback must be given'
        executeBackground([:], commandLine, callback)
    }

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the commandLine concurrently.
     *
     * @param map execution settings
     * @param commandLine
     */
    void executeBackground(HashMap map, String commandLine) {
        assert commandLine, 'commandLine must be given'
        assert map != null, 'map must not be null'

        def settings = new CommandSettings.With(mergedSettings, new CommandSettings.With(map))
        def command = operations.command(settings, commandLine)

        command.startAsync { int exitStatus ->
            if (exitStatus != 0 && !settings.ignoreError) {
                throw new BadExitStatusException("Command returned exit status $exitStatus: $commandLine", exitStatus)
            }
        }
    }

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the commandLine concurrently.
     *
     * @param map execution settings
     * @param commandLine
     * @param callback closure called with an output value of the commandLine
     */
    void executeBackground(HashMap map, String commandLine, Closure callback) {
        assert commandLine, 'commandLine must be given'
        assert callback, 'callback must be given'
        assert map != null, 'map must not be null'

        def settings = new CommandSettings.With(mergedSettings, new CommandSettings.With(map))
        def operation = operations.command(settings, commandLine)

        def lines = [] as List<String>
        operation.onEachLineOfStandardOutput { String line -> lines << line }

        operation.startAsync { int exitStatus ->
            if (exitStatus != 0 && !settings.ignoreError) {
                throw new BadExitStatusException("Command returned exit status $exitStatus: $commandLine", exitStatus)
            }
            callback.call(lines.join(Utilities.eol()))
        }
    }
}
