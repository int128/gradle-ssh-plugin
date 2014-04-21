package org.hidetake.gradle.ssh.api

import groovy.transform.TupleConstructor
import org.hidetake.gradle.ssh.api.ssh.ConnectionSettings

/**
 * Represents a remote host.
 *
 * @author hidetake.org
 *
 */
@TupleConstructor
class Remote {
    /**
     * Name of this instance.
     */
    final String name

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
    final roles = [] as List<String>

    @Delegate
    ConnectionSettings connectionSettings = new ConnectionSettings()

    /**
     * Add a role to this remote.
     *
     * @param role
     */
    void role(String role) {
        assert role != null, 'role should be set'
        roles.add(role)
    }

    /**
     * Returns a string representation of this remote host.
     */
    @Override
    String toString() {
        "remote '${name}'"
    }
}
