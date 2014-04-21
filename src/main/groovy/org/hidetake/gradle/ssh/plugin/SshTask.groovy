package org.hidetake.gradle.ssh.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.hidetake.gradle.ssh.api.operation.OperationSettings
import org.hidetake.gradle.ssh.api.ssh.ConnectionSettings

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
        sshTaskDelegate.sessions.execute(
                ConnectionSettings.DEFAULT
                        + convention.globalSettings.connectionSettings
                        + sshTaskDelegate.globalSettings.connectionSettings,
                OperationSettings.DEFAULT
                        + convention.globalSettings.operationSettings
                        + sshTaskDelegate.globalSettings.operationSettings
        )
    }
}
