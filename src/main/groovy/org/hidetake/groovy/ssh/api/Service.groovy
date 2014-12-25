package org.hidetake.groovy.ssh.api

import org.hidetake.groovy.ssh.api.util.NamedObjectMap

/**
 * An entry point of Groovy SSH.
 *
 * @author Hidetake Iwata
 */
interface Service {
    /**
     * Global settings.
     */
    CompositeSettings getSettings()

    /**
     * Remote hosts.
     */
    RemoteContainer getRemotes()

    /**
     * Configure remote hosts.
     *
     * @param closure
     */
    void remotes(Closure closure)

    /**
     * Proxy hosts.
     */
    NamedObjectMap<Proxy> getProxies()

    /**
     * Configure proxy hosts.
     *
     * @param closure
     */
    void proxies(Closure closure)

    /**
     * Configure global settings.
     *
     * @param closure
     */
    void settings(@DelegatesTo(CompositeSettings) Closure closure)

    /**
     * Run a closure.
     *
     * @param closure
     * @return returned value of the last session
     */
    def run(@DelegatesTo(RunHandler) Closure closure)
}
