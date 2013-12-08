package org.hidetake.gradle.ssh.internal

import org.hidetake.gradle.ssh.api.SshService
import org.hidetake.gradle.ssh.api.SshSpec

/**
 * Dry run implementation of {@link SshService}.
 *
 * @author hidetake.org
 *
 */
@Singleton
class DryRunSshService implements SshService {
    @Override
    void execute(SshSpec sshSpec) {
        assert sshSpec.dryRun == Boolean.TRUE, 'dryRun should be true'

        sshSpec.sessionSpecs.each { spec ->
            def handler = new DryRunOperationHandler(spec)
            handler.with(spec.operationClosure)
        }
    }
}
