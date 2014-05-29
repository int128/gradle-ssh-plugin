package org.hidetake.gradle.ssh.internal

import org.hidetake.gradle.ssh.internal.connection.ConnectionManager
import org.hidetake.gradle.ssh.internal.connection.ConnectionService
import org.hidetake.gradle.ssh.internal.session.SessionService
import org.hidetake.gradle.ssh.plugin.CompositeSettings
import org.hidetake.gradle.ssh.plugin.Remote
import org.hidetake.gradle.ssh.plugin.SshTaskHandler

import static org.gradle.util.ConfigureUtil.configure
import static org.hidetake.gradle.ssh.util.ClosureUtil.callWithDelegate

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
    private final CompositeSettings taskSpecificSettings = new CompositeSettings()

    private final List<Map> sessions = []

    @Override
    void ssh(Closure closure) {
        assert closure, 'closure must be given'
        configure(closure, taskSpecificSettings)
    }

    @Override
    void session(Remote remote, Closure closure) {
        assert remote, 'remote must be given'
        assert remote.host, "host must be given for the remote ${remote.name}"
        assert closure, 'closure must be given'
        sessions.add(remote: remote, closure: closure)
    }

    @Override
    void session(Collection<Remote> remotes, Closure closure) {
        assert remotes, 'at least one remote must be given'
        remotes.each { remote -> session(remote, closure) }
    }

    @Override
    void session(Map remoteProperties, Closure closure) {
        assert remoteProperties, 'properties of a remote must be given'
        assert remoteProperties.host, 'host must be given for the remote'
        def remote = new Remote(remoteProperties.host as String)
        remoteProperties.each { String k, Object v -> remote[k] = v }
        session(remote, closure)
    }

    @Override
    Object execute(CompositeSettings globalSettings) {
        def merged = CompositeSettings.DEFAULT + globalSettings + taskSpecificSettings

        def connectionService = ConnectionService.instance
        def sessionService = SessionService.instance

        connectionService.withManager(merged.connectionSettings) { ConnectionManager manager ->
            sessions.each { session ->
                session.delegate = sessionService.createDelegate(
                        session.remote as Remote, merged.operationSettings, manager)
            }
            sessions.collect { session ->
                callWithDelegate(session.closure as Closure, session.delegate)
            }.last()
        }
    }
}
