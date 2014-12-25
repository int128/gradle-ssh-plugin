package org.hidetake.groovy.ssh.internal

import org.hidetake.groovy.ssh.api.CompositeSettings
import org.hidetake.groovy.ssh.api.Remote
import org.hidetake.groovy.ssh.api.RunHandler
import org.hidetake.groovy.ssh.internal.session.Plan

import static org.hidetake.groovy.ssh.internal.util.ClosureUtil.callWithDelegate

/**
 * A handler for {@link RunHandler}.
 *
 * @author hidetake.org
 */
class DefaultRunHandler implements RunHandler {
    final CompositeSettings settings = new CompositeSettings()

    final List<Plan> sessions = []

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
        sessions.add(new Plan(remote, closure))
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

    @Override
    void session(Object[] args) {
        if (args.last() instanceof Closure) {
            def remotes = args.take(args.length - 1)
            def closure = args.last()
            remotes.each { remote -> session(remote as Remote, closure as Closure) }
        } else {
            throw new IllegalArgumentException('''session() allows following arguments:
session(remote) {}
session(remote1, remote2, ...) {}
session([remote1, remote2, ...]) {}
session(host: 'myHost', user: 'myUser', ...) {}''')
        }
    }
}
