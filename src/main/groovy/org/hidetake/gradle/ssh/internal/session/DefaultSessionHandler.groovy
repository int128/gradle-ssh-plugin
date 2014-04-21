package org.hidetake.gradle.ssh.internal.session

import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.operation.OperationSettings
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.session.SessionHandler
import org.hidetake.gradle.ssh.internal.session.handler.DefaultFileTransfer
import org.hidetake.gradle.ssh.internal.session.handler.DefaultSudoExecution

/**
 * A default implementation of {@link SessionHandler}.
 *
 * @author hidetake.org
 */
@Slf4j
@Mixin(DefaultSudoExecution)
@Mixin(DefaultFileTransfer)
class DefaultSessionHandler implements SessionHandler {
    private final Operations operations
    private final OperationSettings operationSettings

    def DefaultSessionHandler(Operations operations1, OperationSettings operationSettings1) {
        operations = operations1
        operationSettings = operationSettings1
    }

    @Override
    Remote getRemote() {
        operations.remote
    }

    @Override
    void shell(HashMap settings = [:]) {
        log.info("Execute a shell with settings ($settings)")
        operations.shell(operationSettings + new OperationSettings(settings))
    }

    @Override
    String execute(String command) {
        log.info("Execute a command ($command)")
        operations.execute(operationSettings, command, null)
    }

    @Override
    void execute(String command, Closure callback) {
        log.info("Execute a command ($command) with callback")
        operations.execute(operationSettings, command, callback)
    }

    @Override
    String execute(HashMap settings, String command) {
        log.info("Execute a command ($command) with settings ($settings)")
        operations.execute(operationSettings + new OperationSettings(settings), command, null)
    }

    @Override
    void execute(HashMap settings, String command, Closure callback) {
        log.info("Execute a command ($command) with settings ($settings) and callback")
        operations.execute(operationSettings + new OperationSettings(settings), command, callback)
    }

    @Override
    void executeBackground(String command) {
        log.info("Execute a command ($command) in background")
        operations.executeBackground(operationSettings, command, null)
    }

    @Override
    void executeBackground(String command, Closure callback) {
        log.info("Execute a command ($command) with callback in background")
        operations.executeBackground(operationSettings, command, callback)
    }

    @Override
    void executeBackground(HashMap settings, String command) {
        log.info("Execute a command ($command) with settings ($settings) in background")
        operations.executeBackground(operationSettings + new OperationSettings(settings), command, null)
    }

    @Override
    void executeBackground(HashMap settings, String command, Closure callback) {
        log.info("Execute a command ($command) with settings ($settings) and callback in background")
        operations.executeBackground(operationSettings + new OperationSettings(settings), command, callback)
    }
}
