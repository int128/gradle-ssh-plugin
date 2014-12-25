package org.hidetake.groovy.ssh.internal.connection

import org.hidetake.groovy.ssh.api.ConnectionSettings

class DefaultConnectionService implements ConnectionService {
    def <T> T withManager(ConnectionSettings connectionSettings, Closure<T> closure) {
        def connectionManager = new DefaultConnectionManager(connectionSettings)
        try {
            closure.call(connectionManager)
        } finally {
            connectionManager.waitAndClose()
        }
    }
}
