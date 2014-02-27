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
     * Gateway host.
     * This may be null.
     */
    Remote gateway

    /**
     * Password.
     * Leave as null if public key authentication.
     */
    String password

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
     * Use agent flag.
     * If <code>true</code>, Putty Agent or ssh-agent will be used to authenticate.
     */
    boolean agent

    /**
     * Roles.
     */
    final roles = [] as List<String>

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
