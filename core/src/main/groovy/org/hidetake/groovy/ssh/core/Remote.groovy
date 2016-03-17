package org.hidetake.groovy.ssh.core

import org.hidetake.groovy.ssh.connection.ConnectionSettings

/**
 * Represents a remote host.
 *
 * @author Hidetake Iwata
 *
 */
class Remote {
    /**
     * Name of this instance.
     */
    final String name

    def Remote(String name1) {
        name = name1
        assert name
    }

    def Remote(Map<String, Object> properties) {
        assert properties
        assert properties.host
        name = properties.host
        properties.each { k, v -> setProperty(k, v) }
    }

    /**
     * Port.
     */
    int port = 22

    /**
     * Remote host.
     */
    String host

    /**
     * Gateway host.
     * This may be null.
     */
    Remote gateway

    /**
     * Roles.
     */
    final List<String> roles = []

    @Delegate
    ConnectionSettings connectionSettings = new ConnectionSettings()

    void role(String role) {
        assert role != null, 'role should be set'
        roles.add(role)
    }

    String toString() {
        "$name [$host:$port]"
    }
}
