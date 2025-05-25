package org.hidetake.groovy.ssh.operation

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.session.forwarding.LocalPortForwardSettings
import org.hidetake.groovy.ssh.session.forwarding.RemotePortForwardSettings
import org.hidetake.groovy.ssh.session.transfer.FileTransferSettings

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
    Operation shell(ShellSettings settings) {
        log.info("Executing shell on $remote")
        new DryRunOperation()
    }

    @Override
    Operation command(CommandSettings settings, String commandLine) {
        log.info("Executing command on $remote: $commandLine")
        new DryRunOperation()
    }

    @Override
    int forwardLocalPort(LocalPortForwardSettings settings) {
        log.info("Requesting local port forwarding on $remote with ${new LocalPortForwardSettings.With(settings)}")
        0
    }

    @Override
    void forwardRemotePort(RemotePortForwardSettings settings) {
        log.info("Requesting remote port forwarding on $remote with ${new RemotePortForwardSettings.With(settings)}")
    }

    @Override
    def <T> T sftp(FileTransferSettings settings, @DelegatesTo(SftpOperations) Closure<T> closure) {
        log.info("Requesting SFTP subsystem on $remote")
        null
    }
}
