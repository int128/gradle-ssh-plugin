package org.hidetake.gradle.ssh.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.hidetake.gradle.ssh.api.SshSettings

/**
 * A SSH task for Gradle.
 *
 * @author hidetake.org
 */
class SshTask extends DefaultTask {
    @SuppressWarnings("GroovyUnusedDeclaration")
    @Delegate
    protected final SshTaskDelegate sshTaskDelegate = new SshTaskDelegate()

    @TaskAction
    void perform() {
        def convention = project.convention.getPlugin(SshPluginConvention)
        def mergedSettings = SshSettings.computeMerged(sshSettings, convention.ssh)
        sessions.execute(mergedSettings)
    }
}
