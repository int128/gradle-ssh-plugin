package org.hidetake.gradle.ssh.plugin

import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.session.Sessions

import static org.gradle.util.ConfigureUtil.configure

/**
 * A delegate class of ssh task.
 *
 * @author hidetake.org
 */
class SshTaskDelegate {
    /**
     * Task specific settings.
     * This overrides global settings.
     */
    protected final GlobalSettings globalSettings = new GlobalSettings()

    /**
     * Configure task specific settings.
     *
     * @param closure closure for {@link GlobalSettings}
     */
    void ssh(Closure closure) {
        assert closure, 'closure must be given'
        configure(closure, globalSettings)
    }

    /**
     * Sessions.
     */
    protected final Sessions sessions = Sessions.factory.create()

    /**
     * Add a session.
     *
     * @param remote the {@link Remote}
     * @param closure closure for {@link org.hidetake.gradle.ssh.api.session.SessionHandler} (run in execution phase)
     */
    void session(Remote remote, Closure closure) {
        assert remote, 'remote must be given'
        assert remote.host, "host must be given for the remote ${remote.name}"
        assert closure, 'closure must be given'
        sessions.add(remote, closure)
    }

    /**
     * Add sessions.
     *
     * @param remotes collection of {@link Remote}s
     * @param closure closure for {@link org.hidetake.gradle.ssh.api.session.SessionHandler} (run in execution phase)
     */
    void session(Collection<Remote> remotes, Closure closure) {
        assert remotes, 'at least one remote must be given'
        remotes.each { remote -> session(remote, closure) }
    }
}
