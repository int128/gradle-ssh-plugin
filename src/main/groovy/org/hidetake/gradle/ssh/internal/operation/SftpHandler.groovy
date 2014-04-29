package org.hidetake.gradle.ssh.internal.operation

import com.jcraft.jsch.ChannelSftp

/**
 * Handler for closure of file transfer.
 *
 * @author hidetake.org
 */
interface SftpHandler {
    /**
     * Get a file from the remote host.
     *
     * @param remote
     * @param local
     */
    void getFile(String remote, String local)

    /**
     * Put a file to the remote host.
     *
     * @param local
     * @param remote
     */
    void putFile(String local, String remote)

    /**
     * Create a directory.
     *
     * @param path
     */
    void mkdir(String path)

    /**
     * Get a directory listing.
     *
     * @param path
     * @return list of files or directories
     */
    List<ChannelSftp.LsEntry> ls(String path)

    /**
     * Change current directory.
     *
     * @param path
     */
    void cd(String path)
}
