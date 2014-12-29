package org.hidetake.groovy.ssh.api

import org.hidetake.groovy.ssh.util.NamedObjectMap

/**
 * A container of {@link Remote}s.
 *
 * @author Hidetake Iwata
 */
interface RemoteContainer {
    interface RoleAccessor {
        /**
         * Find remote hosts associated with given role.
         *
         * @param name a role
         * @return remote hosts associated with given roles
         */
        Collection<Remote> getAt(String name)
    }

    RoleAccessor getRole()

    /**
     * Find remote hosts associated with given roles.
     *
     * @param roles one or more roles
     * @return remote hosts associated with given roles
     */
    Collection<Remote> role(String... roles)
}
