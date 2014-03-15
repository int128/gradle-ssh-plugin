package org.hidetake.gradle.ssh.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.session.Executor
import org.hidetake.gradle.ssh.registry.Registry

/**
 * A SSH task for Gradle.
 *
 * @author hidetake.org
 */
class SshTask extends DefaultTask {
    private final executor = Registry.instance[Executor]

    @Delegate
    protected final SshTaskDelegate sshTaskDelegate = new SshTaskDelegate()

    @TaskAction
    void perform() {
        def convention = project.convention.getPlugin(SshPluginConvention)
        def mergedSettings = SshSettings.computeMerged(sshSettings, convention.ssh)
        executor.execute(mergedSettings, sshTaskDelegate.sessionSpecs)
    }
}
