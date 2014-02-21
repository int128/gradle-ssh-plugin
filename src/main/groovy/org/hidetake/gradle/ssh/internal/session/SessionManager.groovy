package org.hidetake.gradle.ssh.internal.session

import com.jcraft.jsch.Session

/**
 * Session lifecycle manager.
 *
 * @author hidetake.org
 */
class SessionManager {
    final sessions = [] as List<Session>

    void add(Session session) {
        sessions << session
    }

    /**
     * Disconnect all channels.
     */
    void disconnect() {
        sessions.each { it.disconnect() }
    }
}
