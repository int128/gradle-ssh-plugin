package org.hidetake.groovy.ssh.core

import groovy.transform.EqualsAndHashCode
import org.hidetake.groovy.ssh.core.settings.CompositeSettings

import java.util.concurrent.atomic.AtomicInteger

/**
 * Represents a remote host.
 *
 * @author Hidetake Iwata
 */
@EqualsAndHashCode(includes = 'name')
class Remote implements CompositeSettings {
    /**
     * Name of this instance.
     */
    final String name

    def Remote(String name1) {
        name = name1
        assert name
    }

    def Remote(Map<String, Object> settingsMap) {
        name = settingsMap.name ?: "Remote${sequenceForAutoNaming.incrementAndGet()}"
        settingsMap.findAll { key, value ->
            key != 'name'
        }.each { key, value ->
            setProperty(key, value)
        }
    }

    private static final sequenceForAutoNaming = new AtomicInteger()

    /**
     * Remote host.
     */
    String host

    /**
     * Port.
     */
    int port = 22

    /**
     * Roles.
     */
    final Set<String> roles = []

    /**
     * Add the role.
     * @param role
     */
    void role(String role) {
        assert role != null, 'role should be set'
        roles.add(role)
    }

    String toString() {
        "$name[$host:$port]"
    }
}
