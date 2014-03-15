package org.hidetake.gradle.ssh.api

/**
 * Service to execute a SSH task specified by {@link SshSettings}.
 *
 * @see SshSettings
 * @author hidetake.org
 *
 */
interface SshService {
    /**
     * Executes a SSH task.
     *
     * @param sshSettings ssh settings
     */
    void execute(SshSettings sshSettings)
}
