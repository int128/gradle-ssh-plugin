package org.hidetake.groovy.ssh.internal

import org.hidetake.groovy.ssh.api.Remote
import org.hidetake.groovy.ssh.api.RemoteContainer
import org.hidetake.groovy.ssh.internal.util.DefaultNamedObjectMap

/**
 * A default implementation of {@link RemoteContainer}.
 *
 * @author Hidetake Iwata
 */
class DefaultRemoteContainer extends DefaultNamedObjectMap<Remote> implements RemoteContainer {
    class DefaultRoleAccessor implements RemoteContainer.RoleAccessor {
        Collection<Remote> getAt(String name) {
            role(name)
        }
    }

    final RemoteContainer.RoleAccessor role = new DefaultRoleAccessor()

    Collection<Remote> role(String... roles) {
        assert roles, 'At least one role must be given'
        def values = values()
        roles.collect { String role ->
            values.findAll { it.roles.contains(role) }
        }.flatten().toSet()
    }
}
