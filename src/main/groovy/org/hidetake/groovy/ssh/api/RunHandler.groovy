package org.hidetake.groovy.ssh.api

import org.hidetake.groovy.ssh.api.session.SessionHandler

/**
 * An interface delegating into {@link Service#run(groovy.lang.Closure)}.
 *
 * @author hidetake.org
 */
interface RunHandler {
    /**
     * Configure one time settings.
     *
     * @param closure closure for {@link org.hidetake.groovy.ssh.api.CompositeSettings}
     */
    void settings(@DelegatesTo(CompositeSettings) Closure closure)

    /**
     * Add a session.
     *
     * @param remote the {@link Remote}
     * @param closure closure for {@link org.hidetake.groovy.ssh.api.session.SessionHandler}
     */
    void session(Remote remote, @DelegatesTo(SessionHandler) Closure closure)

    /**
     * Add sessions.
     *
     * @param remotes collection of {@link Remote}s
     * @param closure closure for {@link org.hidetake.groovy.ssh.api.session.SessionHandler}
     */
    void session(Collection<Remote> remotes, @DelegatesTo(SessionHandler) Closure closure)

    /**
     * Add a session.
     * This method creates a {@link Remote} instance and add a session with it.
     *
     * @param remoteProperties properties of a {@link org.hidetake.groovy.ssh.api.Remote}
     * @param closure closure for {@link org.hidetake.groovy.ssh.api.session.SessionHandler}
     */
    void session(Map remoteProperties, @DelegatesTo(SessionHandler) Closure closure)
}
