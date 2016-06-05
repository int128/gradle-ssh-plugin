package org.hidetake.groovy.ssh.session.execution

import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.operation.CommandSettings
import org.hidetake.groovy.ssh.operation.Operations
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.hidetake.groovy.ssh.session.SessionExtension

/**
 * Provides the non-blocking command execution.
 * Each method returns immediately and executes the commandLine concurrently.
 *
 * @author Hidetake Iwata
 */
trait BackgroundCommand implements SessionExtension {
    void executeBackground(String commandLine) {
        executeBackground([:], commandLine)
    }

    void executeBackground(List<String> commandLineArgs) {
        executeBackground([:], commandLineArgs)
    }

    void executeBackground(String commandLine, Closure callback) {
        executeBackground([:], commandLine, callback)
    }

    void executeBackground(List<String> commandLineArgs, Closure callback) {
        executeBackground([:], commandLineArgs, callback)
    }

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

    void executeBackground(HashMap map, List<String> commandLineArgs) {
        executeBackground(map, Escape.escape(commandLineArgs))
    }

    void executeBackground(HashMap map, String commandLine, Closure callback) {
        assert commandLine, 'commandLine must be given'
        assert callback, 'callback must be given'
        assert map != null, 'map must not be null'
        def settings = new CommandSettings.With(mergedSettings, new CommandSettings.With(map))
        Helper.execute(operations, settings, commandLine, callback)
    }

    void executeBackground(HashMap map, List<String> commandLineArgs, Closure callback) {
        executeBackground(map, Escape.escape(commandLineArgs), callback)
    }

    private static class Helper {
        static execute(Operations operations, CommandSettings settings, String commandLine, Closure callback) {
            def operation = operations.command(settings, commandLine)

            def lines = [] as List<String>
            operation.addInteraction {
                when(line: _, from: standardOutput) { String line ->
                    lines.add(line)
                }
            }

            operation.startAsync { int exitStatus ->
                if (exitStatus != 0 && !settings.ignoreError) {
                    throw new BadExitStatusException("Command returned exit status $exitStatus: $commandLine", exitStatus)
                }
                callback.call(lines.join(Utilities.eol()))
            }
        }
    }
}
