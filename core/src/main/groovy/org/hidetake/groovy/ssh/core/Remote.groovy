package org.hidetake.groovy.ssh.core

import groovy.transform.EqualsAndHashCode
import org.hidetake.groovy.ssh.connection.ConnectionSettings
import org.hidetake.groovy.ssh.extension.settings.SudoSettings

import java.util.concurrent.atomic.AtomicInteger

/**
 * Represents a remote host.
 *
 * @author Hidetake Iwata
 *
 */
@EqualsAndHashCode(includes = 'name')
class Remote {
    /**
     * Name of this instance.
     */
    final String name

    def Remote(String name1) {
        name = name1
        assert name
    }

    def Remote(Map<String, Object> settings) {
        name = settings.name ?: "Remote${sequenceForAutoNaming.incrementAndGet()}"
        settings.findAll { key, value ->
            key != 'name'
        }.each { key, value ->
            setProperty(key, value)
        }
    }

    private static final AtomicInteger sequenceForAutoNaming = new AtomicInteger()

    /**
     * Port.
     */
    int port = 22

    /**
     * Remote host.
     */
    String host

    /**
     * Roles.
     */
    final List<String> roles = []

    // Excludes traits to avoid side-effect
    @Delegate(interfaces = false)
    ConnectionSettings connectionSettings = new ConnectionSettings()

    // Excludes traits to avoid side-effect
    @Delegate(interfaces = false)
    SudoSettings sudoSettings = new SudoSettings()

    void role(String role) {
        assert role != null, 'role should be set'
        roles.add(role)
    }

    String toString() {
        "$name [$host:$port]"
    }
}
