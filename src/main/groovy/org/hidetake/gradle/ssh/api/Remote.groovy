package org.hidetake.gradle.ssh.api

import groovy.transform.TupleConstructor

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
     * Remote user.
     */
    String user

    /**
     * Port.
     */
    int port = 22

    /**
     * Remote host.
     */
    String host

    /**
     * Password.
     * Leave as null if public key authentication.
     */
    String password

    /**
     * Roles.
     */
    final roles = [] as List<String>

    /**
     * Identity key file for public-key authentication.
     */
    File identity

    /**
     * Pass-phrase for the identity key.
     * This may be null.
     */
    String passphrase

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
