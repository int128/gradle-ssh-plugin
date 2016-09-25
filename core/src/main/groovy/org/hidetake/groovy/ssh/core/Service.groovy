package org.hidetake.groovy.ssh.core

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.container.ContainerBuilder
import org.hidetake.groovy.ssh.core.container.ProxyContainer
import org.hidetake.groovy.ssh.core.container.RemoteContainer
import org.hidetake.groovy.ssh.core.settings.CompositeSettings
import org.hidetake.groovy.ssh.core.settings.GlobalSettings
import org.hidetake.groovy.ssh.session.SessionTask

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

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
     * @return null if no session, a result if one session is given, a list of results otherwise
     */
    def run(@DelegatesTo(RunHandler) Closure closure) {
        assert closure, 'closure must be given'
        def handler = new RunHandler()
        callWithDelegate(closure, handler)

        log.debug("Using default settings: $CompositeSettings.With.DEFAULT")
        log.debug("Using global settings: $settings")
        log.debug("Using per-service settings: $handler.settings")

        if (handler.sessions.size() == 0) {
            null
        } else if (handler.sessions.size() == 1) {
            runInternal(new SessionTask(handler.sessions.head(), settings, handler.settings))
        } else {
            runInternal(handler.sessions.collect { session ->
                new SessionTask(session, settings, handler.settings)
            })
        }
    }

    private static <T> T runInternal(SessionTask<T> task) {
        task.call()
    }

    private static List<?> runInternal(List<SessionTask<?>> tasks) {
        log.debug("Running ${tasks.size()} sessions")
        def pool = new ForkJoinPool(10,
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            null,
            true /* use FIFO mode */)

        def futures = tasks.collect { session -> pool.submit(session) }
        pool.shutdown()
        pool.awaitTermination(1, TimeUnit.DAYS)

        if (futures*.completedNormally.every()) {
            futures*.get()
        } else {
            def failures = futures.count { task -> task.completedAbnormally }
            def rate = (failures / futures.size() * 100) as int
            def message = failures == 1 ?
                "1 of ${futures.size()} session ($rate%) failed" :
                "$failures of ${futures.size()} sessions ($rate%) failed"

            log.error(message)
            futures.eachWithIndex { task, i ->
                if (task.completedAbnormally) {
                    def e = task.exception
                    if (log.debugEnabled) {
                        log.debug("Session #${i + 1} failed", e)
                    } else {
                        log.error("Session #${i + 1} failed: $e")
                    }
                }
            }
            throw new ParallelSessionsException(message, futures)
        }
    }
}
