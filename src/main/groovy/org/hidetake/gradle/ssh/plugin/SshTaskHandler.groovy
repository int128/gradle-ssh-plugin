package org.hidetake.gradle.ssh.plugin

import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.registry.Registry

/**
 * An interface delegating into {@link SshTask}.
 *
 * @author hidetake.org
 */
interface SshTaskHandler {
    /**
     * A factory of {@link SshTaskHandler}.
     */
    interface Factory {
        SshTaskHandler create()
    }

    final factory = Registry.instance[Factory]

    /**
     * Configure task specific settings.
     *
     * @param closure closure for {@link GlobalSettings}
     */
    void ssh(Closure closure)

    /**
     * Add a session.
     *
     * @param remote the {@link org.hidetake.gradle.ssh.api.Remote}
     * @param closure closure for {@link org.hidetake.gradle.ssh.api.session.SessionHandler} (run in execution phase)
     */
    void session(Remote remote, Closure closure)

    /**
     * Add sessions.
     *
     * @param remotes collection of {@link Remote}s
     * @param closure closure for {@link org.hidetake.gradle.ssh.api.session.SessionHandler} (run in execution phase)
     */
    void session(Collection<Remote> remotes, Closure closure)

    /**
     * Execute the task.
     * This method should be called by plugin internally.
     *
     * @param globalSettings
     */
    void execute(GlobalSettings globalSettings)
}
