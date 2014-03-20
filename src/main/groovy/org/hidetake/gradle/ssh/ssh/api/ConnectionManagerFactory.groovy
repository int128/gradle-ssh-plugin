package org.hidetake.gradle.ssh.ssh.api

import org.hidetake.gradle.ssh.api.SshSettings

/**
 * An interface to SSH infrastructure.
 */
interface ConnectionManagerFactory {
    ConnectionManager create(SshSettings sshSettings)
}
