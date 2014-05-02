package org.hidetake.gradle.ssh.internal.session.handler

/**
 * Handler for file transfer.
 *
 * @author hidetake.org
 */
interface FileTransfer {
    /**
     * Get a file from the remote host.
     *
     * @param remote
     * @param local
     */
    void get(String remote, String local)

    /**
     * Put a file to the remote host.
     *
     * @param local
     * @param remote
     */
    void put(String local, String remote)
}
