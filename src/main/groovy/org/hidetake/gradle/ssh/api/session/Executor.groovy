package org.hidetake.gradle.ssh.api.session

import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshSettings

/**
 * A task executor.
 */
interface Executor {
    /**
     * Instantiate a task and execute it.
     *
     * @param sshSettings
     * @param sessionSpecs
     */
    void execute(SshSettings sshSettings, List<SessionSpec> sessionSpecs)
}
