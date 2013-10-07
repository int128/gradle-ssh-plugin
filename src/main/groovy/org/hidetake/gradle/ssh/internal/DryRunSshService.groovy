package org.hidetake.gradle.ssh.internal

import org.gradle.api.logging.LogLevel
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
        assert sshSpec.logger != null, 'default logger should be set by convention'

        def operationEventLogger = new OperationEventLogger(sshSpec.logger, LogLevel.WARN)
        sshSpec.sessionSpecs.each { spec ->
            def handler = new DryRunOperationHandler(spec, [operationEventLogger])
            println handler
            handler.with(spec.operationClosure)
        }
    }
}
