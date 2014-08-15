package org.hidetake.groovy.ssh.internal.operation

import org.hidetake.groovy.ssh.api.OperationSettings
import org.hidetake.groovy.ssh.api.Remote
import org.hidetake.groovy.ssh.api.operation.Operations

/**
 * Dry-run implementation of {@link org.hidetake.groovy.ssh.api.operation.Operations}.
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
    void sftp(Closure closure) {
    }
}
