package org.hidetake.gradle.ssh.internal.connection

import org.hidetake.gradle.ssh.plugin.ConnectionSettings

@Singleton(lazy = true)
class ConnectionService {
    /**
     * Execute the closure with the connection manager.
     *
     * @param connectionSettings
     * @param closure
     * @return return value of the closure
     */
    def <T> T withManager(ConnectionSettings connectionSettings, Closure<T> closure) {
        def connectionManager = new DefaultConnectionManager(connectionSettings)
        try {
            closure.call(connectionManager)
        } finally {
            connectionManager.waitAndClose()
        }
    }
}
