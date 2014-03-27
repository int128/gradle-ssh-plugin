package org.hidetake.gradle.ssh.internal.session

import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.operation.ExecutionSettings
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.operation.ShellSettings
import org.hidetake.gradle.ssh.api.session.SessionHandler
import org.hidetake.gradle.ssh.internal.session.handler.DefaultFileTransfer
import org.hidetake.gradle.ssh.internal.session.handler.DefaultSudoExecution

/**
 * A default implementation of {@link SessionHandler}.
 *
 * @author hidetake.org
 */
@TupleConstructor
@Slf4j
@Mixin(DefaultSudoExecution)
@Mixin(DefaultFileTransfer)
class SessionDelegate implements SessionHandler {
    final Operations operations

    @Override
    Remote getRemote() {
        operations.remote
    }

    @Override
    void shell(HashMap settings) {
        log.info("Execute a shell with settings ($settings)")
        operations.shell(new ShellSettings(settings))
    }

    @Override
    String execute(String command) {
        log.info("Execute a command ($command)")
        operations.execute(ExecutionSettings.DEFAULT, command)
    }

    @Override
    void execute(String command, Closure callback) {
        log.info("Execute a command ($command)")
        operations.execute(new ExecutionSettings(callback: callback), command)
    }

    @Override
    String execute(HashMap settings, String command) {
        log.info("Execute a command ($command) with settings ($settings)")
        operations.execute(new ExecutionSettings(settings), command)
    }

    @Override
    void execute(HashMap settings, String command, Closure callback) {
        log.info("Execute a command ($command) with settings ($settings)")
        operations.execute(new ExecutionSettings(settings) + [callback: callback], command)
    }

    @Override
    void executeBackground(String command) {
        log.info("Execute a command ($command) in background")
        operations.executeBackground(ExecutionSettings.DEFAULT, command)
    }

    @Override
    void executeBackground(String command, Closure callback) {
        log.info("Execute a command ($command) in background")
        operations.executeBackground(new ExecutionSettings(callback: callback), command)
    }

    @Override
    void executeBackground(HashMap settings, String command) {
        log.info("Execute a command ($command) with settings ($settings) in background")
        operations.executeBackground(new ExecutionSettings(settings), command)
    }

    @Override
    void executeBackground(HashMap settings, String command, Closure callback) {
        log.info("Execute a command ($command) with settings ($settings) in background")
        operations.executeBackground(new ExecutionSettings(settings) + [callback: callback], command)
    }
}
