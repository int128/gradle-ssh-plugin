package org.hidetake.groovy.ssh.core

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.container.ContainerBuilder
import org.hidetake.groovy.ssh.core.container.ProxyContainer
import org.hidetake.groovy.ssh.core.container.RemoteContainer
import org.hidetake.groovy.ssh.core.settings.CompositeSettings
import org.hidetake.groovy.ssh.core.settings.GlobalSettings
import org.hidetake.groovy.ssh.session.SessionTask

import java.util.concurrent.ForkJoinPool

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

/**
 * An entry point of SSH service.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Service {
    /**
     * Container of remote hosts.
     */
    final remotes = new RemoteContainer()

    /**
     * Container of proxy hosts.
     */
    final proxies = new ProxyContainer()

    /**
     * Global settings.
     */
    final settings = new GlobalSettings()

    /**
     * Configure the container of remote hosts.
     *
     * @param closure
     */
    void remotes(Closure closure) {
        assert closure, 'closure must be given'
        def builder = new ContainerBuilder(remotes)
        callWithDelegate(closure, builder)
    }

    /**
     * Configure the container of proxy hosts.
     *
     * @param closure
     */
    void proxies(Closure closure) {
        assert closure, 'closure must be given'
        def builder = new ContainerBuilder(proxies)
        callWithDelegate(closure, builder)
    }

    /**
     * Configure global settings.
     *
     * @param closure
     */
    void settings(@DelegatesTo(GlobalSettings) Closure closure) {
        assert closure, 'closure must be given'
        callWithDelegate(closure, settings)
    }

    /**
     * Run a closure.
     *
     * @param closure
     * @return null if no session, a result of last session otherwise
     */
    def run(@DelegatesTo(RunHandler) Closure closure) {
        assert closure, 'closure must be given'
        def handler = new RunHandler()
        callWithDelegate(closure, handler)

        log.debug("Using default settings: $CompositeSettings.With.DEFAULT")
        log.debug("Using global settings: $settings")
        log.debug("Using per-service settings: $handler.settings")

        def pool = new ForkJoinPool(10,
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            null,
            true /* use FIFO mode */)
        def futures = handler.sessions.collect { session ->
            pool.submit(new SessionTask(session, settings, handler.settings))
        }
        pool.shutdown()

        def results = futures*.get()
        switch (results.size()) {
            case 0: return null
            case 1: return results.last()
            default: return results.last()
        }
    }
}
