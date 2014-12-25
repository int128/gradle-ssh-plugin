package org.hidetake.groovy.ssh.internal.connection

import org.hidetake.groovy.ssh.api.ConnectionSettings

interface ConnectionService {
    /**
     * Execute the closure with the connection manager.
     * This method blocks until all connections are closed.
     *
     * @param connectionSettings
     * @param closure
     * @return return value of the closure
     */
    def <T> T withManager(ConnectionSettings connectionSettings, Closure<T> closure)
}
