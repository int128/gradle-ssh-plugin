package org.hidetake.gradle.ssh.internal.session

import org.hidetake.gradle.ssh.plugin.ConnectionSettings
import org.hidetake.gradle.ssh.plugin.OperationSettings
import org.hidetake.gradle.ssh.plugin.Remote
import org.hidetake.gradle.ssh.registry.Registry

/**
 * A list of session.
 *
 * @author hidetake.org
 */
interface Sessions {
    /**
     * A factory of {@link Sessions}.
     */
    interface Factory {
        Sessions create()
    }

    final factory = Registry.instance[Factory]

    /**
     * Add a session.
     *
     * @param remote
     * @param closure
     */
    void add(Remote remote, Closure closure)

    /**
     * Execute all sessions.
     * This method should wait until all sessions are finished.
     *
     * @param connectionSettings
     * @param operationSettings
     */
    void execute(ConnectionSettings connectionSettings, OperationSettings operationSettings)
}
