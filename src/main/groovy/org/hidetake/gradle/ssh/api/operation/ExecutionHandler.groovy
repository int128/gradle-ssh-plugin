package org.hidetake.gradle.ssh.api.operation

/**
 * Handler for closure of a command execution.
 *
 * @author hidetake.org
 */
interface ExecutionHandler {
    /**
     * Declares stream interactions with the server.
     *
     * @param closure
     */
    void interaction(Closure closure)
}
