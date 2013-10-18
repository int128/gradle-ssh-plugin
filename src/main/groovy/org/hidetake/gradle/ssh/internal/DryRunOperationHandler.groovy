package org.hidetake.gradle.ssh.internal

import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.CommandPromise
import org.hidetake.gradle.ssh.api.OperationHandler
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SessionSpec

/**
 * Null implementation of {@link OperationHandler} for dry-run.
 *
 * @author hidetake.org
 *
 */
@TupleConstructor
@Slf4j
class DryRunOperationHandler implements OperationHandler {
    final SessionSpec spec

    @Override
    Remote getRemote() {
        spec.remote
    }

    @Override
    String execute(String command) {
        execute([:], command)
    }

    @Override
    CommandPromise executeBackground(String command) {
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

    @Override
    String execute(Map<String, Object> options, String command) {
        log.info("Executing command: ${command}")
        null
    }

    @Override
    CommandPromise executeBackground(Map<String, Object> options, String command) {
        log.info("Executing command in background: ${command}")
        new CommandContext()
    }

    @Override
    void get(Map<String, Object> options, String remote, String local) {
        log.info("Get: ${remote} -> ${local}")
    }

    @Override
    void put(Map<String, Object> options, String local, String remote) {
        log.info("Put: ${local} -> ${remote}")
    }

    @Override
    String executeSudo(String command) {
        executeSudo([:], command)
    }

    @Override
    String executeSudo(Map options, String command) {
        log.info("Executing command with sudo support: ${command}")
        null
    }
}
