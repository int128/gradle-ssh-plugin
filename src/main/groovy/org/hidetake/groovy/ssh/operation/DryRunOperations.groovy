package org.hidetake.groovy.ssh.operation

import org.hidetake.groovy.ssh.extension.settings.LocalPortForwardSettings
import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.extension.settings.RemotePortForwardSettings

/**
 * Dry-run implementation of {@link Operations}.
 *
 * @author hidetake.org
 */
class DryRunOperations implements Operations {
    final Remote remote

    def DryRunOperations(Remote remote1) {
        remote = remote1
        assert remote
    }

    @Override
    void shell(OperationSettings settings) {
    }

    @Override
    String execute(OperationSettings settings, String command, Closure callback) {
        callback?.call('')
        ''
    }

    @Override
    void executeBackground(OperationSettings settings, String command, Closure callback) {
        callback?.call('')
    }

    @Override
    int forwardLocalPort(LocalPortForwardSettings settings) {
        0
    }

    @Override
    void forwardRemotePort(RemotePortForwardSettings settings) {
    }

    @Override
    def sftp(Closure closure) {
    }
}
