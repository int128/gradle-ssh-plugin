package org.hidetake.gradle.ssh.plugin

import org.hidetake.groovy.ssh.api.CompositeSettings
import org.hidetake.groovy.ssh.api.Remote
import org.hidetake.groovy.ssh.api.session.SessionHandler

/**
 * An interface delegating into {@link SshTask}.
 *
 * @author hidetake.org
 */
interface SshTaskHandler {
    /**
     * Configure task specific settings.
     *
     * @param closure closure for {@link org.hidetake.groovy.ssh.api.CompositeSettings}
     */
    void ssh(@DelegatesTo(CompositeSettings) Closure closure)

    /**
     * Add a session.
     *
     * @param remote the {@link org.hidetake.groovy.ssh.api.Remote}
     * @param closure closure for {@link org.hidetake.groovy.ssh.api.session.SessionHandler} (run in execution phase)
     */
    void session(Remote remote, @DelegatesTo(SessionHandler) Closure closure)

    /**
     * Add sessions.
     *
     * @param remotes collection of {@link Remote}s
     * @param closure closure for {@link org.hidetake.groovy.ssh.api.session.SessionHandler} (run in execution phase)
     */
    void session(Collection<Remote> remotes, @DelegatesTo(SessionHandler) Closure closure)

    /**
     * Add a session.
     * This method creates a {@link Remote} instance and add a session with it.
     *
     * @param remoteProperties properties of a {@link org.hidetake.groovy.ssh.api.Remote}
     * @param closure closure for {@link org.hidetake.groovy.ssh.api.session.SessionHandler} (run in execution phase)
     */
    void session(Map remoteProperties, @DelegatesTo(SessionHandler) Closure closure)

    /**
     * Execute the task.
     * This method should be called by plugin internally.
     *
     * @param globalSettings
     * @return returned value of the last session
     */
    Object execute(CompositeSettings globalSettings)
}
