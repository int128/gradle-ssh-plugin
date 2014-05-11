package org.hidetake.gradle.ssh.internal.connection

import org.hidetake.gradle.ssh.plugin.Remote

/**
 * A manager of {@link Connection}s.
 *
 * @author hidetake.org
 */
interface ConnectionManager {
    /**
     * Establish a connection.
     *
     * @param remote the remote host
     * @return a connection
     */
    Connection connect(Remote remote)

    /**
     * Wait for pending connections and close all.
     *
     * @throws org.hidetake.gradle.ssh.plugin.session.BackgroundCommandException if any error occurs
     */
    void waitAndClose()
}
