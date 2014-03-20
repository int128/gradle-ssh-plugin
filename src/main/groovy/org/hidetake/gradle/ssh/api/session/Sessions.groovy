package org.hidetake.gradle.ssh.api.session

import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SshSettings

/**
 * A list of session.
 *
 * @author hidetake.org
 */
interface Sessions {
    /**
     * A factory of {@link Sessions}.
     */
    interface Factory {
        Sessions create()
    }

    /**
     * Add a session.
     *
     * @param remote
     * @param closure
     */
    void add(Remote remote, Closure closure)

    /**
     * Execute sessions.
     *
     * @param sshSettings
     */
    void execute(SshSettings sshSettings)
}
