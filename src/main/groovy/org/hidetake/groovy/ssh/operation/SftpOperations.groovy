package org.hidetake.groovy.ssh.operation

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.SftpException as JschSftpException
import groovy.util.logging.Slf4j

/**
 * An aggregate of file transfer operations.
 *
 * @author hidetake.org
 */
@Slf4j
class SftpOperations {
    private final ChannelSftp channel

    def SftpOperations(ChannelSftp channel1) {
        channel = channel1
        assert channel
    }

    /**
     * Get a file from the remote host.
     *
     * @param remote
     * @param local
     */
    void getFile(String remote, String local) {
        log.info("Get a remote file ($remote) to local ($local)")
        try {
            channel.get(remote, local, new FileTransferLogger())
        } catch (JschSftpException e) {
            throw new SftpException('Failed to get a file from the remote host', e)
        }
    }

    /**
     * Put a file to the remote host.
     *
     * @param local
     * @param remote
     */
    void putFile(String local, String remote) {
        log.info("Put a local file ($local) to remote ($remote)")
        try {
            channel.put(local, remote, new FileTransferLogger(), ChannelSftp.OVERWRITE)
        } catch (JschSftpException e) {
            throw new SftpException('Failed to put a file into the remote host', e)
        }
    }

    /**
     * Put a content to the remote host.
     *
     * @param content
     * @param remote path
     */
    void putContent(byte[] content, String remote) {
        log.info("Put the content to remote ($remote)")
        try {
            def stream = new ByteArrayInputStream(content)
            channel.put(stream, remote, new FileTransferLogger(), ChannelSftp.OVERWRITE)
        } catch (JschSftpException e) {
            throw new SftpException('Failed to put the content to the remote host', e)
        }
    }

    /**
     * Create a directory.
     *
     * @param path
     */
    void mkdir(String path) {
        log.info("Create a directory ($path)")
        try {
            channel.mkdir(path)
        } catch (JschSftpException e) {
            throw new SftpException('Failed to create a directory on the remote host', e)
        }
    }

    /**
     * Get a directory listing.
     *
     * @param path
     * @return list of files or directories
     */
    List<ChannelSftp.LsEntry> ls(String path) {
        log.info("Get a directory listing of ($path)")
        try {
            channel.ls(path).toList()
        } catch (JschSftpException e) {
            throw new SftpException('Failed to fetch a directory listing on the remote host', e)
        }
    }

    /**
     * Change current directory.
     *
     * @param path
     */
    void cd(String path) {
        log.info("Change current directory to ($path)")
        try {
            channel.cd(path)
        } catch (JschSftpException e) {
            throw new SftpException('Failed to change directory on the remote host', e)
        }
    }
}
