package org.hidetake.gradle.ssh.internal.session

import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.operation.ExecutionSettings
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.operation.ShellSettings
import org.hidetake.gradle.ssh.api.session.SessionHandler

@TupleConstructor
@Slf4j
class SessionDelegate implements SessionHandler {
    static final NULL_CLOSURE = {}

    final Operations operations = null

    @Override
    Remote getRemote() {
        operations?.remote
    }

    @Override
    void shell(HashMap settings, Closure closure) {
        log.info("Execute a shell with settings ($settings)")
        operations?.shell(new ShellSettings(settings), closure)
    }

    @Override
    void shell(Closure closure) {
        log.info("Execute a shell")
        operations?.shell(ShellSettings.DEFAULT, closure)
    }

    @Override
    String execute(String command) {
        log.info("Execute a command ($command)")
        operations?.execute(ExecutionSettings.DEFAULT, command, NULL_CLOSURE)
    }

    @Override
    String execute(String command, Closure closure) {
        log.info("Execute a command ($command) with interactions")
        operations?.execute(ExecutionSettings.DEFAULT, command, closure)
    }

    @Override
    String execute(HashMap settings, String command) {
        log.info("Execute a command ($command) with settings ($settings)")
        operations?.execute(new ExecutionSettings(settings), command, NULL_CLOSURE)
    }

    @Override
    String execute(HashMap settings, String command, Closure closure) {
        log.info("Execute a command ($command) with settings ($settings) and interactions")
        operations?.execute(new ExecutionSettings(settings), command, closure)
    }

    @Override
    String executeSudo(String command) {
        log.info("Execute a command ($command) with sudo support")
        operations?.executeSudo(ExecutionSettings.DEFAULT, command)
    }

    @Override
    String executeSudo(HashMap settings, String command) {
        log.info("Execute a command ($command) with sudo support and settings ($settings)")
        operations?.executeSudo(new ExecutionSettings(settings), command)
    }

    @Override
    void executeBackground(String command) {
        log.info("Execute a command ($command) in background")
        operations?.executeBackground(ExecutionSettings.DEFAULT, command)
    }

    @Override
    void executeBackground(HashMap settings, String command) {
        log.info("Execute a command ($command) with settings ($settings) in background")
        operations?.executeBackground(new ExecutionSettings(settings), command)
    }

    @Override
    void get(String remote, String local) {
        log.info("Get a remote file ($remote) to local ($local)")
        operations?.get(remote, local)
    }

    @Override
    void put(String local, String remote) {
        log.info("Put a local file ($local) to remote ($remote)")
        operations?.put(local, remote)
    }
}
