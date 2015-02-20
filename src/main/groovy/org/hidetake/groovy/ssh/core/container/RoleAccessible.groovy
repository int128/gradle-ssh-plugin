package org.hidetake.groovy.ssh.core.container

import org.hidetake.groovy.ssh.core.Remote

/**
 * Provides role access for remote hosts.
 *
 * @author Hidetake Iwata
 */
trait RoleAccessible implements Map<String, Remote> {
    /**
     * Find remote hosts associated with given roles.
     *
     * @param roles one or more roles
     * @return remote hosts associated with given roles
     */
    Collection<Remote> role(String... roles) {
        assert roles, 'At least one role must be given'
        def values = values()
        roles.collect { String role ->
            values.findAll { it.roles.contains(role) }
        }.flatten().toSet()
    }

    static class Accessor {
        private final RoleAccessible accessible

        private Accessor(RoleAccessible accessible) {
            this.accessible = accessible
        }

        /**
         * Find remote hosts associated with given role.
         *
         * @param name a role
         * @return remote hosts associated with given roles
         */
        Collection<Remote> getAt(String name) {
            accessible.role(name)
        }
    }

    final Accessor role = new Accessor(this)
}
