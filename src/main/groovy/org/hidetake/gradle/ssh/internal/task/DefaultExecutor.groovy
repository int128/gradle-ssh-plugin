package org.hidetake.gradle.ssh.internal.task

import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.task.Executor

/**
 * An executor which delegates dry run or wet run.
 *
 * @author hidetake.org
 */
@Singleton
class DefaultExecutor implements Executor {
    @Override
    void execute(SshSettings sshSettings, List<SessionSpec> sessionSpecs) {
        if (sshSettings.dryRun) {
            DryRun.instance.execute(sshSettings, sessionSpecs)
        } else  {
            WetRun.instance.execute(sshSettings, sessionSpecs)
        }
    }
}
