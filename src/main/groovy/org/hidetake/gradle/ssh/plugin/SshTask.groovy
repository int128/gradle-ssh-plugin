package org.hidetake.gradle.ssh.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.hidetake.gradle.ssh.internal.SshTaskService

/**
 * A SSH task for Gradle.
 *
 * @author hidetake.org
 */
class SshTask extends DefaultTask {
    @Delegate
    private final SshTaskHandler sshTaskHandler = SshTaskService.instance.createDelegate()

    @TaskAction
    void perform() {
        sshTaskHandler.execute(project.extensions.ssh as CompositeSettings)
    }
}
