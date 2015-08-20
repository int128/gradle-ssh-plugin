package org.hidetake.groovy.ssh.operation

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.extension.settings.LocalPortForwardSettings
import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.extension.settings.RemotePortForwardSettings

/**
 * Dry-run implementation of {@link Operations}.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class DryRunOperations implements Operations {
    final Remote remote

    def DryRunOperations(Remote remote1) {
        remote = remote1
        assert remote
    }

    @Override
    void shell(OperationSettings settings) {
        log.info("[dry-run] Executing a shell")
    }

    @Override
    String execute(OperationSettings settings, String command, Closure callback) {
        log.info("[dry-run] Executing the command ($command)")
        callback?.call('')
        ''
    }

    @Override
    void executeBackground(OperationSettings settings, String command, Closure callback) {
        log.info("[dry-run] Executing the command in background ($command)")
        callback?.call('')
    }

    @Override
    int forwardLocalPort(LocalPortForwardSettings settings) {
        log.info("[dry-run] Requesting port forwarding from " +
                 "local (${settings.bind}:${settings.port}) to remote (${settings.host}:${settings.hostPort})")
        0
    }

    @Override
    void forwardRemotePort(RemotePortForwardSettings settings) {
        log.info("Requesting port forwarding from " +
                 "remote (${settings.bind}:${settings.port}) to local (${settings.host}:${settings.hostPort})")
    }

    @Override
    def sftp(Closure closure) {
        log.info("[dry-run] Requesting SFTP subsystem")
    }
}
