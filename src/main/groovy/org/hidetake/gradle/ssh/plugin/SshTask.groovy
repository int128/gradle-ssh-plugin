package org.hidetake.gradle.ssh.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * A SSH task for Gradle.
 *
 * @author hidetake.org
 */
class SshTask extends DefaultTask {
    @Delegate
    private final SshTaskHandler sshTaskHandler = factory.create()

    @TaskAction
    void perform() {
        sshTaskHandler.execute(project.extensions.ssh)
    }
}
