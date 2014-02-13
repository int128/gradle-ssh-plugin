package org.hidetake.gradle.ssh.internal.operation

import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.CommandContext
import org.hidetake.gradle.ssh.api.Operation
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.operation.ExecutionSettings
import org.hidetake.gradle.ssh.api.operation.ShellSettings

@TupleConstructor
@Slf4j
class OperationProxy implements Operation {
    static final NULL_CLOSURE = {}

    final Handler handler
    final Remote remote

    @Override
    void shell(HashMap settings, Closure interactions) {
        log.info("Execute a shell with settings ($settings)")
        handler.shell(new ShellSettings(settings), interactions)
    }

    @Override
    void shell(Closure interactions) {
        log.info("Execute a shell")
        handler.shell(ShellSettings.DEFAULT, interactions)
    }

    @Override
    String execute(String command) {
        log.info("Execute a command ($command)")
        handler.execute(ExecutionSettings.DEFAULT, command, NULL_CLOSURE)
    }

    @Override
    String execute(String command, Closure interactions) {
        log.info("Execute a command ($command) with interactions")
        handler.execute(ExecutionSettings.DEFAULT, command, interactions)
    }

    @Override
    String execute(HashMap settings, String command) {
        log.info("Execute a command ($command) with settings ($settings)")
        handler.execute(new ExecutionSettings(settings), command, NULL_CLOSURE)
    }

    @Override
    String execute(HashMap settings, String command, Closure interactions) {
        log.info("Execute a command ($command) with settings ($settings) and interactions")
        handler.execute(new ExecutionSettings(settings), command, interactions)
    }

    @Override
    String executeSudo(String command) {
        log.info("Execute a command ($command) with sudo support")
        handler.executeSudo(ExecutionSettings.DEFAULT, command)
    }

    @Override
    String executeSudo(HashMap settings, String command) {
        log.info("Execute a command ($command) with sudo support and settings ($settings)")
        handler.executeSudo(new ExecutionSettings(settings), command)
    }

    @Override
    CommandContext executeBackground(String command) {
        log.info("Execute a command ($command) in background")
        handler.executeBackground(ExecutionSettings.DEFAULT, command)
    }

    @Override
    CommandContext executeBackground(HashMap settings, String command) {
        log.info("Execute a command ($command) with settings ($settings) in background")
        handler.executeBackground(new ExecutionSettings(settings), command)
    }

    @Override
    void get(String remote, String local) {
        log.info("Get a remote file ($remote) to local ($local)")
        handler.get(remote, local)
    }

    @Override
    void put(String local, String remote) {
        log.info("Put a local file ($local) to remote ($remote)")
        handler.put(local, remote)
    }
}
