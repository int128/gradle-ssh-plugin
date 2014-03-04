package org.hidetake.gradle.ssh

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.hidetake.gradle.ssh.api.SshService
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.internal.DefaultSshService
import org.hidetake.gradle.ssh.internal.DryRunSshService

/**
 * SSH task.
 *
 * @see SshService
 * @author hidetake.org
 *
 */
class SshTask extends DefaultTask {
    protected service = DefaultSshService.instance
    protected dryRunService = DryRunSshService.instance

    /**
     * Delegate of task specific settings.
     * This overrides global settings.
     */
    @Delegate
    final SshSettings sshSettings = new SshSettings()

    @TaskAction
    void perform() {
        def convention = project.convention.getPlugin(SshPluginConvention)
        def merged = SshSettings.computeMerged(sshSettings, convention.sshSettings)
        if (merged.dryRun) {
            dryRunService.execute(merged)
        } else {
            service.execute(merged)
        }
    }
}
