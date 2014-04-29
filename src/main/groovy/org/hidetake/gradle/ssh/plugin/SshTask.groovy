package org.hidetake.gradle.ssh.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

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
        sshTaskDelegate.execute(project.extensions.ssh)
    }
}
