package org.hidetake.groovy.ssh.session.execution

import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.operation.CommandSettings
import org.hidetake.groovy.ssh.operation.Operations
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.hidetake.groovy.ssh.session.SessionExtension

/**
 * Provides the blocking command execution.
 * Each method blocks until channel is closed.
 *
 * @author Hidetake Iwata
 */
trait Command implements SessionExtension {
    void execute(HashMap map = [:], String commandLine, Closure callback) {
        assert callback, 'callback must be given'
        callback.call(execute(map, commandLine))
    }

    void execute(HashMap map = [:], List<String> commandLineArgs, Closure callback) {
        assert callback, 'callback must be given'
        callback.call(execute(map, commandLineArgs))
    }

    String execute(HashMap map = [:], String commandLine) {
        assert commandLine, 'commandLine must be given'
        assert map != null, 'map must not be null'
        def settings = new CommandSettings.With(mergedSettings, new CommandSettings.With(map))
        Helper.execute(operations, settings, commandLine)
    }

    String execute(HashMap map = [:], List<String> commandLineArgs) {
        assert commandLineArgs, 'commandLineArgs must be given'
        assert map != null, 'map must not be null'
        execute(map, Escape.escape(commandLineArgs))
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

            def exitStatus = operation.execute()
            if (exitStatus != 0 && !settings.ignoreError) {
                throw new BadExitStatusException("Command returned exit status $exitStatus: $commandLine", exitStatus)
            }
            lines.join(Utilities.eol())
        }
    }
}