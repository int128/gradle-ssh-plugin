package org.hidetake.gradle.ssh.plugin

import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.session.Sessions
import org.hidetake.gradle.ssh.registry.Registry

/**
 * A delegate class of ssh task.
 *
 * @author hidetake.org
 */
class SshTaskDelegate {
    /**
     * Delegate of task specific settings.
     * This overrides global settings.
     */
    @Delegate
    final SshSettings sshSettings = new SshSettings()

    /**
     * Sessions.
     */
    final Sessions sessions = Registry.instance[Sessions.Factory].create()

    /**
     * Add a session.
     *
     * @param remote the {@link Remote}
     * @param closure closure for {@link org.hidetake.gradle.ssh.api.session.SessionHandler} (run in execution phase)
     */
    void session(Remote remote, Closure closure) {
        assert remote, 'remote should be set'
        assert remote.user, "user of remote ${remote.name} should be set"
        assert remote.host, "host of remote ${remote.name} should be set"
        assert closure, 'closure should be set'
        sessions.add(remote, closure)
    }

    /**
     * Add sessions.
     *
     * @param remotes collection of {@link Remote}s
     * @param closure closure for {@link org.hidetake.gradle.ssh.api.session.SessionHandler} (run in execution phase)
     */
    void session(Collection<Remote> remotes, Closure closure) {
        assert remotes, 'remotes should contain at least one'
        assert closure, 'closure should be set'
        remotes.each { Remote remote -> session(remote, closure) }
    }
}
