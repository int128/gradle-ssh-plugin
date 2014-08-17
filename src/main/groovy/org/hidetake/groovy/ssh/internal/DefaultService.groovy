package org.hidetake.groovy.ssh.internal

import org.hidetake.gradle.ssh.internal.SshTaskService
import org.hidetake.groovy.ssh.api.CompositeSettings
import org.hidetake.groovy.ssh.api.Proxy
import org.hidetake.groovy.ssh.api.Remote
import org.hidetake.groovy.ssh.api.RemoteContainer
import org.hidetake.groovy.ssh.api.Service
import org.hidetake.groovy.ssh.api.util.NamedObjectMap
import org.hidetake.groovy.ssh.internal.util.DefaultNamedObjectMap
import org.hidetake.groovy.ssh.internal.util.NamedObjectMapBuilder

import static org.hidetake.groovy.ssh.internal.util.ClosureUtil.callWithDelegate

/**
 * A default implementation of {@link Service}.
 *
 * @author Hidetake Iwata
 */
class DefaultService implements Service {
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
    Object run(Closure closure) {
        assert closure, 'closure must be given'
        SshTaskService.instance.execute(settings, closure)
    }
}
