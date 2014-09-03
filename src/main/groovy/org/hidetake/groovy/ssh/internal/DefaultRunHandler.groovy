package org.hidetake.groovy.ssh.internal

import org.hidetake.groovy.ssh.api.CompositeSettings
import org.hidetake.groovy.ssh.api.Remote
import org.hidetake.groovy.ssh.api.RunHandler
import org.hidetake.groovy.ssh.internal.connection.ConnectionManager
import org.hidetake.groovy.ssh.internal.connection.ConnectionService
import org.hidetake.groovy.ssh.internal.session.SessionService

import static org.hidetake.groovy.ssh.internal.util.ClosureUtil.callWithDelegate

/**
 * A runner for {@link RunHandler}.
 *
 * @author hidetake.org
 */
class DefaultRunHandler implements RunHandler {
    /**
     * One time settings.
     * This overrides global settings.
     */
    private final CompositeSettings settings = new CompositeSettings()

    private final List<Map> sessions = []

    @Override
    void settings(Closure closure) {
        assert closure, 'closure must be given'
        callWithDelegate(closure, settings)
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
        remoteProperties.each { k, v -> remote[k as String] = v }
        session(remote, closure)
    }

    Object run(CompositeSettings globalSettings) {
        def merged = CompositeSettings.DEFAULT + globalSettings + settings

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
