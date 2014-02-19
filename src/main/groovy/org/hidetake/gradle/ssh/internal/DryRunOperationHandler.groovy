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
    void shell(Map<String, Object> options, Closure interactions) {
        log.info("Execute a shell with options ($options)")
    }

    @Override
    String execute(Map<String, Object> options, String command, Closure interactions) {
        log.info("Execute a command (${command}) with options ($options)")
        null
    }

    @Override
    String executeSudo(Map options, String command) {
        log.info("Execute a command ($command) with sudo support and options ($options)")
        null
    }

    @Override
    CommandContext executeBackground(Map<String, Object> options, String command) {
        log.info("Execute a command ($command) with options ($options) in background")
        new DryRunCommandContext()
    }

    @Override
    void get(Map<String, Object> options, String remote, String local) {
        log.info("Get a remote file (${remote}) to local (${local})")
    }

    @Override
    void put(Map<String, Object> options, String local, String remote) {
        log.info("Put a local file (${local}) to remote (${remote})")
    }

    @Override
    int forwardLocalPortTo(String remoteHost, int remotePort) {
        log.info("Start local port fowarding to remote ($remoteHost:$remotePort)")
        0
    }
}
