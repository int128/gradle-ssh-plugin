package org.hidetake.groovy.ssh.core

import org.hidetake.groovy.ssh.core.settings.ConnectionSettings

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
	 * Proxy to use when establishing a connection.
	 * This may be null.
	 */
	Proxy proxy

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
        "Remote $name [$host:$port]"
    }
}
