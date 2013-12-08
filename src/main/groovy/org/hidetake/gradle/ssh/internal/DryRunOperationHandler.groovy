package org.hidetake.gradle.ssh.internal

import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.CommandContext
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
class DryRunOperationHandler extends AbstractOperationHandler {
    final SessionSpec spec

    @Override
    Remote getRemote() {
        spec.remote
    }

    @Override
    String execute(Map<String, Object> options, String command, Closure interactions) {
        log.info("Executing command: ${command}")
        null
    }

    @Override
    String executeSudo(Map options, String command) {
        log.info("Executing command with sudo support: ${command}")
        null
    }

    @Override
    CommandContext executeBackground(Map<String, Object> options, String command) {
        log.info("Executing command in background: ${command}")
        new DryRunCommandContext()
    }

    @Override
    void get(Map<String, Object> options, String remote, String local) {
        log.info("Get: ${remote} -> ${local}")
    }

    @Override
    void put(Map<String, Object> options, String local, String remote) {
        log.info("Put: ${local} -> ${remote}")
    }
}
