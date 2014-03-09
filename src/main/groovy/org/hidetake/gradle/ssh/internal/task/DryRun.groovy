package org.hidetake.gradle.ssh.internal.task

import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.task.Executor
import org.hidetake.gradle.ssh.internal.session.SessionDelegate

/**
 * A dry run implementation of executor.
 *
 * @author hidetake.org
 */
@Singleton
class DryRun implements Executor {
    protected Operations handler = [:] as Operations

    @Override
    void execute(SshSettings sshSettings, List<SessionSpec> sessionSpecs) {
        sessionSpecs.each { spec ->
            new SessionDelegate(handler).with(spec.operationClosure)
        }
    }
}
