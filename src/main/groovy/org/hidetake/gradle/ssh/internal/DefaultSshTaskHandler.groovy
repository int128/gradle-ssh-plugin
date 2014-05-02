package org.hidetake.gradle.ssh.internal

import org.hidetake.gradle.ssh.internal.session.Sessions
import org.hidetake.gradle.ssh.plugin.*

import static org.gradle.util.ConfigureUtil.configure

/**
 * A delegate class of ssh task.
 *
 * @author hidetake.org
 */
class DefaultSshTaskHandler implements SshTaskHandler {
    /**
     * Task specific settings.
     * This overrides global settings.
     */
    private final GlobalSettings taskSpecificSettings = new GlobalSettings()

    /**
     * Sessions.
     */
    private final Sessions sessions = Sessions.factory.create()

    void ssh(Closure closure) {
        assert closure, 'closure must be given'
        configure(closure, taskSpecificSettings)
    }

    void session(Remote remote, Closure closure) {
        assert remote, 'remote must be given'
        assert remote.host, "host must be given for the remote ${remote.name}"
        assert closure, 'closure must be given'
        sessions.add(remote, closure)
    }

    void session(Collection<Remote> remotes, Closure closure) {
        assert remotes, 'at least one remote must be given'
        remotes.each { remote -> session(remote, closure) }
    }

    void execute(GlobalSettings globalSettings) {
        sessions.execute(
                ConnectionSettings.DEFAULT
                        + globalSettings.connectionSettings
                        + taskSpecificSettings.connectionSettings,
                OperationSettings.DEFAULT
                        + globalSettings.operationSettings
                        + taskSpecificSettings.operationSettings
        )
    }
}
