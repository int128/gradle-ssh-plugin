package org.hidetake.groovy.ssh.core.container

import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.util.NamedObjectMap

/**
 * A container of {@link org.hidetake.groovy.ssh.core.Remote}s.
 *
 * @author Hidetake Iwata
 */
class RemoteContainer extends NamedObjectMap<Remote> {
    class RoleAccessor {
        /**
         * Find remote hosts associated with given role.
         *
         * @param name a role
         * @return remote hosts associated with given roles
         */
        Collection<Remote> getAt(String name) {
            role(name)
        }
    }

    final RoleAccessor role = new RoleAccessor()

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
}
