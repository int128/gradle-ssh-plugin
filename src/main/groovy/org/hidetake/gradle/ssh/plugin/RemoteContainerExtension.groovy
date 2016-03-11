package org.hidetake.gradle.ssh.plugin

import org.hidetake.groovy.ssh.core.Remote

/**
 * An extension class of the remote container.
 *
 * @author hidetake.org
 */
@Category(Collection)
class RemoteContainerExtension {
    /**
     * Find remote hosts associated with given roles.
     *
     * @param remotes mixin object
     * @param roles one or more roles
     * @return remote hosts associated with given roles
     */
    static Collection<Remote> role(Collection<Remote> remotes, String... roles) {
        assert remotes != null
        assert roles, 'At least one role must be given'
        roles.collect { String role ->
            remotes.findAll { it.roles.contains(role) }
        }.flatten().toSet()
    }

    /**
     * Find remote hosts associated with all given roles.
     *
     * @param remotes mixin object
     * @param roles one or more roles
     * @return remote hosts associated with all given roles
     */
    static Collection<Remote> allRoles(Collection<Remote> remotes, String... roles) {
        assert remotes != null
        assert roles, 'At least one role must be given'
        remotes.findAll { it.roles.containsAll(roles) }.flatten().toSet()
    }
}
