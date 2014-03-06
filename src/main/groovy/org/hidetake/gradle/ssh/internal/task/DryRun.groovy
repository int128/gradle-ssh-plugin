package org.hidetake.gradle.ssh.internal.task

import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.task.Executor
import org.hidetake.gradle.ssh.internal.operation.Handler
import org.hidetake.gradle.ssh.internal.operation.OperationProxy

/**
 * A dry run implementation of executor.
 *
 * @author hidetake.org
 */
@Singleton
class DryRun implements Executor {
    protected Handler handler = [:] as Handler

    @Override
    void execute(SshSettings sshSettings, List<SessionSpec> sessionSpecs) {
        sessionSpecs.each { spec ->
            new OperationProxy(handler).with(spec.operationClosure)
        }
    }
}
