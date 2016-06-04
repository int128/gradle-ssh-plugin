package org.hidetake.groovy.ssh.session.execution

import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.operation.CommandSettings
import org.hidetake.groovy.ssh.operation.Operations
import org.hidetake.groovy.ssh.session.BadExitStatusException
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
     * @param commandLine
     * @return output value of the commandLine
     */
    String execute(String commandLine) {
        assert commandLine, 'commandLine must be given'
        execute([:], commandLine)
    }

    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param commandLine
     * @param callback closure called with an output value of the commandLine
     * @return output value of the commandLine
     */
    void execute(String commandLine, Closure callback) {
        assert commandLine, 'commandLine must be given'
        assert callback, 'callback must be given'
        execute([:], commandLine, callback)
    }

    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param map execution settings
     * @param commandLine
     * @param callback closure called with an output value of the commandLine
     * @return output value of the commandLine
     */
    void execute(HashMap map, String commandLine, Closure callback) {
        assert commandLine, 'commandLine must be given'
        assert callback, 'callback must be given'
        assert map != null, 'settings must not be null'
        callback.call(execute(map, commandLine))
    }

    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param map execution settings
     * @param commandLine
     * @return output value of the commandLine
     */
    String execute(HashMap map, String commandLine) {
        assert commandLine, 'commandLine must be given'
        assert map != null, 'map must not be null'
        def settings = new CommandSettings.With(mergedSettings, new CommandSettings.With(map))
        Helper.execute(operations, settings, commandLine)
    }

    private static class Helper {
        static execute(Operations operations, CommandSettings settings, String commandLine) {
            def operation = operations.command(settings, commandLine)

            def lines = [] as List<String>
            operation.addInteraction {
                when(line: _, from: standardOutput) { String line ->
                    lines.add(line)
                }
            }

            def exitStatus = operation.startSync()
            if (exitStatus != 0 && !settings.ignoreError) {
                throw new BadExitStatusException("Command returned exit status $exitStatus: $commandLine", exitStatus)
            }
            lines.join(Utilities.eol())
        }
    }
}