package org.hidetake.gradle.ssh.internal

import org.hidetake.gradle.ssh.api.SshService
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.internal.operation.Handler
import org.hidetake.gradle.ssh.internal.operation.OperationProxy

/**
 * Dry run implementation of {@link SshService}.
 *
 * @author hidetake.org
 *
 */
@Singleton
class DryRunSshService implements SshService {
    protected handler = [:] as Handler

    @Override
    void execute(SshSettings sshSettings) {
        assert sshSettings.dryRun == Boolean.TRUE, 'dryRun should be true'

        sshSettings.sessionSpecs.each { spec ->
            new OperationProxy(handler, spec.remote).with(spec.operationClosure)
        }
    }
}
