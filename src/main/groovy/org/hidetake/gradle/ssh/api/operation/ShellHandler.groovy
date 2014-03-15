package org.hidetake.gradle.ssh.api.operation

/**
 * Handler for closure of a shell operation.
 *
 * @author hidetake.org
 */
interface ShellHandler {
    /**
     * Declares stream interactions with the server.
     *
     * @param closure
     */
    void interaction(Closure closure)
}
