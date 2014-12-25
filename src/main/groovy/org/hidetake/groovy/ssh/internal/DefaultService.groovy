package org.hidetake.groovy.ssh.internal

import groovy.transform.TupleConstructor
import org.hidetake.groovy.ssh.api.CompositeSettings
import org.hidetake.groovy.ssh.api.Proxy
import org.hidetake.groovy.ssh.api.Remote
import org.hidetake.groovy.ssh.api.RemoteContainer
import org.hidetake.groovy.ssh.api.Service
import org.hidetake.groovy.ssh.api.util.NamedObjectMap
import org.hidetake.groovy.ssh.internal.session.DefaultSessionExecutor
import org.hidetake.groovy.ssh.internal.session.SessionExecutor
import org.hidetake.groovy.ssh.internal.util.DefaultNamedObjectMap
import org.hidetake.groovy.ssh.internal.util.NamedObjectMapBuilder

import static org.hidetake.groovy.ssh.internal.util.ClosureUtil.callWithDelegate

/**
 * A default implementation of {@link Service}.
 *
 * @author Hidetake Iwata
 */
@TupleConstructor
class DefaultService implements Service {
    final SessionExecutor sessionExecutor = new DefaultSessionExecutor()

    final RemoteContainer remotes = new DefaultRemoteContainer()

    final NamedObjectMap<Proxy> proxies = new DefaultNamedObjectMap<Proxy>()

    final CompositeSettings settings = new CompositeSettings()

    @Override
    void remotes(Closure closure) {
        assert closure, 'closure must be given'
        def builder = new NamedObjectMapBuilder(Remote, remotes)
        callWithDelegate(closure, builder)
    }

    @Override
    void proxies(Closure closure) {
        assert closure, 'closure must be given'
        def builder = new NamedObjectMapBuilder(Proxy, proxies)
        callWithDelegate(closure, builder)
    }

    @Override
    void settings(Closure closure) {
        assert closure, 'closure must be given'
        callWithDelegate(closure, settings)
    }

    @Override
    def run(Closure closure) {
        assert closure, 'closure must be given'
        def handler = new DefaultRunHandler()
        callWithDelegate(closure, handler)

        def results = sessionExecutor.execute(CompositeSettings.DEFAULT + settings + handler.settings, handler.sessions)
        results.empty ? null : results.last()
    }
}
