package org.hidetake.groovy.ssh.session.execution

import org.hidetake.groovy.ssh.session.SessionExtension

/**
 * An extension class of sudo command execution.
 * Each method performs a sudo operation, explicitly providing password for the sudo user.
 * It blocks until channel is closed.
 *
 * @author Hidetake Iwata
 */
trait Sudo implements SessionExtension {

    String executeSudo(HashMap settings = [:], String commandLine) {
        assert commandLine, 'commandLine must be given'
        assert settings != null, 'settings must not be null'
        def helper = new SudoHelper(operations, mergedSettings, new SudoHelper.SudoCommandSettings(settings))
        helper.execute(commandLine)
    }

    String executeSudo(HashMap settings = [:], List<String> commandLineArgs) {
        executeSudo(settings, Escape.escape(commandLineArgs))
    }

    void executeSudo(HashMap settings = [:], String commandLine, Closure callback) {
        assert commandLine, 'commandLine must be given'
        assert callback, 'callback must be given'
        assert settings != null, 'settings must not be null'
        callback.call(executeSudo(settings, commandLine))
    }

    void executeSudo(HashMap settings = [:], List<String> commandLineArgs, Closure callback) {
        executeSudo(settings, Escape.escape(commandLineArgs), callback)
    }

}
