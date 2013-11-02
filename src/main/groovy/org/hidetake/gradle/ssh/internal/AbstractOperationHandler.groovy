package org.hidetake.gradle.ssh.internal

import org.hidetake.gradle.ssh.api.CommandContext
import org.hidetake.gradle.ssh.api.OperationHandler

abstract class AbstractOperationHandler implements OperationHandler {
    @Override
    String execute(String command) {
        execute([:], command)
    }

    @Override
    String execute(String command, Closure interactions) {
        execute([:], command, interactions)
    }

    @Override
    String execute(Map<String, Object> options, String command) {
        execute([:], command, {})
    }

    @Override
    String executeSudo(String command) {
        executeSudo([:], command)
    }

    @Override
    CommandContext executeBackground(String command) {
        executeBackground([:], command)
    }

    @Override
    void get(String remote, String local) {
        get([:], remote, local)
    }

    @Override
    void put(String local, String remote) {
        put([:], local, remote)
    }
}
