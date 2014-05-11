package org.hidetake.gradle.ssh.plugin

import groovy.transform.ToString

/**
 * Represents a remote host.
 *
 * @author hidetake.org
 *
 */
@ToString
class Remote {
    /**
     * Name of this instance.
     */
    final String name

    def Remote(String name1) {
        name = name1
        assert name
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
}
