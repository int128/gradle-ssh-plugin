package org.hidetake.gradle.ssh.api

/**
 * Context interface for command execution.
 *
 * @author hidetake.org
 */
interface CommandContext {
    /**
     * Declares stream interactions with the server.
     *
     * @param closure
     */
    void interaction(Closure closure)
}
