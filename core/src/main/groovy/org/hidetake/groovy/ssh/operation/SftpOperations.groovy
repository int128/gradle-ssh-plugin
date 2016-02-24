package org.hidetake.groovy.ssh.operation

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.SftpATTRS
import com.jcraft.jsch.SftpException as JschSftpException
import groovy.util.logging.Slf4j

/**
 * An aggregate of file transfer operations.
 *
 * Operations should follow the logging convention, that is,
 * it should write a log as DEBUG on beginning of an operation,
 * it should write a log as INFO on success of an operation,
 * but it does not need to write an INFO log if it is an internal operation.
 *
 * @author Hidetake Iwata
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
        log.debug("Getting the remote file ($remote) as the local file ($local)")
        try {
            channel.get(remote, local, new FileTransferLogger())
            log.info("Got the remote file ($remote) as the local file ($local)")
        } catch (JschSftpException e) {
            throw new SftpException('Failed to get a file from the remote host', e)
        }
    }

    /**
     * Get a content from the remote host.
     *
     * @param remote
     * @param stream
     */
    void getContent(String remote, OutputStream stream) {
        log.debug("Getting the content of the remote file ($remote)")
        try {
            channel.get(remote, stream, new FileTransferLogger())
            log.info("Got the content of the remote file ($remote)")
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
        log.debug("Putting the local file ($local) into the remote file ($remote)")
        try {
            channel.put(local, remote, new FileTransferLogger(), ChannelSftp.OVERWRITE)
            log.info("Sent the local file ($local) into the remote file ($remote)")
        } catch (JschSftpException e) {
            throw new SftpException("Failed to put $local into $remote", e)
        }
    }

    /**
     * Put a content to the remote host.
     *
     * @param stream
     * @param remote path
     */
    void putContent(InputStream stream, String remote) {
        log.debug("Putting the content into the remote file ($remote)")
        try {
            channel.put(stream, remote, new FileTransferLogger(), ChannelSftp.OVERWRITE)
            log.info("Sent the content into the remote file ($remote)")
        } catch (JschSftpException e) {
            throw new SftpException("Failed to put the content to $remote", e)
        }
    }

    /**
     * Create a directory.
     *
     * @param path
     */
    void mkdir(String path) {
        log.debug("Creating a directory ($path)")
        try {
            channel.mkdir(path)
            log.info("Created a directory ($path)")
        } catch (JschSftpException e) {
            throw new SftpException("Failed to create a directory: $path", e)
        }
    }

    /**
     * Removes one or several files.
     *
     * @param path
     */
    void rm(String path) {
        log.debug("Removing file(s) ($path)")
        try {
            channel.rm(path)
            log.info("Removed file(s) ($path)")
        } catch (JschSftpException e) {
            throw new SftpException("Failed to remove file(s): $path", e)
        }
    }

    /**
     * Removes one or several directories.
     *
     * @param path
     */
    void rmdir(String path) {
        log.debug("Removing directory ($path)")
        try {
            channel.rmdir(path)
            log.info("Removed directory ($path)")
        } catch (JschSftpException e) {
            throw new SftpException("Failed to remove directory: $path", e)
        }
    }

    /**
     * Get a directory listing.
     *
     * @param path
     * @return list of files or directories
     */
    List<ChannelSftp.LsEntry> ls(String path) {
        log.debug("Requesting the directory list of ($path)")
        try {
            channel.ls(path).toList()
        } catch (JschSftpException e) {
            throw new SftpException("Failed to fetch the directory list of $path", e)
        }
    }

    /**
     * Get a directory entry.
     *
     * @param path
     * @return directory entry
     */
    SftpATTRS stat(String path) {
        log.debug("Requesting the directory entry of ($path)")
        try {
            channel.stat(path)
        } catch (JschSftpException e) {
            throw new SftpException("Failed to fetch the directory entry of $path", e)
        }
    }

    /**
     * Change current directory.
     *
     * @param path
     */
    void cd(String path) {
        log.debug("Changing the current directory to ($path)")
        try {
            channel.cd(path)
        } catch (JschSftpException e) {
            throw new SftpException("Failed to change the current directory to $path", e)
        }
    }
}
