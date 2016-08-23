package org.hidetake.groovy.ssh.operation

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.connection.Connection
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.session.forwarding.LocalPortForwardSettings
import org.hidetake.groovy.ssh.session.forwarding.RemotePortForwardSettings
import org.hidetake.groovy.ssh.session.transfer.FileTransferSettings

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

/**
 * Default implementation of {@link Operations}.
 *
 * Operations should follow the logging convention, that is,
 * it should write a log as DEBUG on beginning of an operation,
 * it should write a log as INFO on success of an operation,
 * but it does not need to write a log if it is an internal operation.

 * @author Hidetake Iwata
 */
@Slf4j
class DefaultOperations implements Operations {
    final Remote remote

    private final Connection connection

    def DefaultOperations(Connection connection1) {
        connection = connection1
        remote = connection.remote
        assert connection
        assert remote
    }

    @Override
    Operation shell(ShellSettings settings) {
        log.debug("Executing shell on $remote: $settings")
        new Shell(connection, settings)
    }

    @Override
    Operation command(CommandSettings settings, String commandLine) {
        log.debug("Executing command on $remote: $commandLine: $settings")
        new Command(connection, settings, commandLine)
    }

    @Override
    int forwardLocalPort(LocalPortForwardSettings settings) {
        log.debug("Requesting local port forwarding on $remote with ${new LocalPortForwardSettings.With(settings)}")
        int port = connection.forwardLocalPort(settings)
        log.info("Enabled local port forwarding on $remote with ${new LocalPortForwardSettings.With(settings)}")
        port
    }

    @Override
    void forwardRemotePort(RemotePortForwardSettings settings) {
        log.debug("Requesting remote port forwarding on $remote with ${new RemotePortForwardSettings.With(settings)}")
        connection.forwardRemotePort(settings)
        log.info("Enabled remote port forwarding from on $remote with ${new RemotePortForwardSettings.With(settings)}")
    }

    @Override
    def <T> T sftp(FileTransferSettings settings, @DelegatesTo(SftpOperations) Closure<T> closure) {
        log.debug("Requesting SFTP subsystem on $remote")
        def channel = connection.createSftpChannel()
        channel.connect(settings.timeoutSec * 1000)
        try {
            log.debug("Started SFTP $remote.name#$channel.id")
            def result = callWithDelegate(closure, new SftpOperations(remote, channel))
            log.debug("Finished SFTP $remote.name#$channel.id")
            result
        } finally {
            channel.disconnect()
        }
    }
}
