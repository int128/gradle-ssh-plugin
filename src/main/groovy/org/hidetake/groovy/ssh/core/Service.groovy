package org.hidetake.groovy.ssh.core

import groovy.transform.TupleConstructor
import org.hidetake.groovy.ssh.api.CompositeSettings
import org.hidetake.groovy.ssh.api.Proxy
import org.hidetake.groovy.ssh.api.Remote
import org.hidetake.groovy.ssh.api.RemoteContainer
import org.hidetake.groovy.ssh.api.util.NamedObjectMap
import org.hidetake.groovy.ssh.internal.DefaultRemoteContainer
import org.hidetake.groovy.ssh.internal.session.SessionExecutor
import org.hidetake.groovy.ssh.internal.util.DefaultNamedObjectMap
import org.hidetake.groovy.ssh.internal.util.NamedObjectMapBuilder

import static org.hidetake.groovy.ssh.internal.util.ClosureUtil.callWithDelegate

/**
 * An entry point of SSH service.
 *
 * @author Hidetake Iwata
 */
@TupleConstructor
class Service {
    final SessionExecutor sessionExecutor = new SessionExecutor()

    /**
     * Container of remote hosts.
     */
    final RemoteContainer remotes = new DefaultRemoteContainer()

    /**
     * Container of proxy hosts.
     */
    final NamedObjectMap<Proxy> proxies = new DefaultNamedObjectMap<Proxy>()

    /**
     * Global settings.
     */
    final CompositeSettings settings = new CompositeSettings()

    /**
     * Configure the container of remote hosts.
     *
     * @param closure
     */
    void remotes(Closure closure) {
        assert closure, 'closure must be given'
        def builder = new NamedObjectMapBuilder(Remote, remotes)
        callWithDelegate(closure, builder)
    }

    /**
     * Configure the container of proxy hosts.
     *
     * @param closure
     */
    void proxies(Closure closure) {
        assert closure, 'closure must be given'
        def builder = new NamedObjectMapBuilder(Proxy, proxies)
        callWithDelegate(closure, builder)
    }

    /**
     * Configure global settings.
     *
     * @param closure
     */
    void settings(@DelegatesTo(CompositeSettings) Closure closure) {
        assert closure, 'closure must be given'
        callWithDelegate(closure, settings)
    }

    /**
     * Run a closure.
     *
     * @param closure
     * @return returned value of the last session
     */
    def run(@DelegatesTo(RunHandler) Closure closure) {
        assert closure, 'closure must be given'
        def handler = new RunHandler()
        callWithDelegate(closure, handler)

        def results = sessionExecutor.execute(CompositeSettings.DEFAULT + settings + handler.settings, handler.sessions)
        results.empty ? null : results.last()
    }
}
