package org.hidetake.groovy.ssh.operation

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.SftpATTRS
import com.jcraft.jsch.SftpException as JschSftpException
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote

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
    private final Remote remote
    private final ChannelSftp channel

    def SftpOperations(Remote remote1, ChannelSftp channel1) {
        remote = remote1
        channel = channel1
        assert remote
        assert channel
    }

    /**
     * Get a file from the remote host.
     *
     * @param remotePath
     * @param localPath
     */
    void getFile(String remotePath, String localPath) {
        log.debug("Requesting SFTP GET: $remote.name:$remotePath -> $localPath")
        try {
            channel.get(remotePath, localPath, new SftpProgressLogger({ percent ->
                log.info("Receiving $percent from $remote.name: $remotePath -> $localPath")
            }))
            log.debug("Success SFTP GET: $remote.name:$remotePath -> $localPath")
        } catch (JschSftpException e) {
            log.error("Failed SFTP GET: $remote.name:$remotePath -> $localPath")
            throw new SftpException("Failed SFTP GET: $remote.name:$remotePath -> $localPath", e)
        }
    }

    /**
     * Get a content from the remote host.
     *
     * @param remotePath
     * @param stream
     */
    void getContent(String remotePath, OutputStream stream) {
        log.debug("Requesting SFTP GET: $remote.name:$remotePath -> stream")
        try {
            channel.get(remotePath, stream, new SftpProgressLogger({ percent ->
                log.info("Receiving $percent from $remote.name: $remotePath")
            }))
            log.debug("Success SFTP GET: $remote.name:$remotePath -> stream")
        } catch (JschSftpException e) {
            log.error("Failed SFTP GET: $remote.name:$remotePath -> stream")
            throw new SftpException("Failed SFTP GET: $remote.name:$remotePath -> stream", e)
        }
    }

    /**
     * Put a file to the remote host.
     *
     * @param localPath
     * @param remotePath
     */
    void putFile(String localPath, String remotePath) {
        log.debug("Requesting SFTP PUT: $localPath -> $remote.name:$remotePath")
        try {
            channel.put(localPath, remotePath, new SftpProgressLogger({ percent ->
                log.info("Sending $percent to $remote.name: $remotePath")
            }), ChannelSftp.OVERWRITE)
            log.debug("Success SFTP PUT: $localPath -> $remote.name:$remotePath")
        } catch (JschSftpException e) {
            log.error("Failed SFTP PUT: $localPath -> $remote.name:$remotePath")
            throw new SftpException("Failed SFTP PUT: $localPath -> $remote.name:$remotePath", e)
        }
    }

    /**
     * Put a content to the remote host.
     *
     * @param stream
     * @param remotePath path
     */
    void putContent(InputStream stream, String remotePath) {
        log.debug("Requesting SFTP PUT: stream -> $remote.name:$remotePath")
        try {
            channel.put(stream, remotePath, new SftpProgressLogger({ percent ->
                log.info("Sending $percent to $remote.name: $remotePath")
            }), ChannelSftp.OVERWRITE)
            log.debug("Success SFTP PUT: stream -> $remote.name:$remotePath")
        } catch (JschSftpException e) {
            log.error("Failed SFTP PUT: stream -> $remote.name:$remotePath")
            throw new SftpException("Failed SFTP PUT: stream -> $remote.name:$remotePath", e)
        }
    }

    /**
     * Create a directory.
     *
     * @param remotePath
     */
    void mkdir(String remotePath) {
        log.debug("Requesting SFTP MKDIR: $remote.name:$remotePath")
        try {
            channel.mkdir(remotePath)
            log.debug("Success SFTP MKDIR: $remote.name:$remotePath")
        } catch (JschSftpException e) {
            log.error("Failed SFTP MKDIR: $remote.name:$remotePath")
            throw new SftpException("Failed SFTP MKDIR: $remote.name:$remotePath", e)
        }
    }

    /**
     * Removes one or several files.
     *
     * @param remotePath
     */
    void rm(String remotePath) {
        log.debug("Requesting SFTP RM: $remote.name:$remotePath")
        try {
            channel.rm(remotePath)
            log.debug("Success SFTP RM: $remote.name:$remotePath")
        } catch (JschSftpException e) {
            log.error("Failed SFTP RM: $remote.name:$remotePath")
            throw new SftpException("Failed SFTP RM: $remote.name:$remotePath", e)
        }
    }

    /**
     * Removes one or several directories.
     *
     * @param remotePath
     */
    void rmdir(String remotePath) {
        log.debug("Requesting SFTP RMDIR: $remote.name:$remotePath")
        try {
            channel.rmdir(remotePath)
            log.debug("Success SFTP RMDIR: $remote.name:$remotePath")
        } catch (JschSftpException e) {
            log.error("Failed SFTP RMDIR: $remote.name:$remotePath")
            throw new SftpException("Failed SFTP RMDIR: $remote.name:$remotePath", e)
        }
    }

    /**
     * Get a directory listing.
     *
     * @param remotePath
     * @return list of files or directories
     */
    List<ChannelSftp.LsEntry> ls(String remotePath) {
        log.debug("Requesting SFTP LS: $remote.name:$remotePath")
        try {
            def result = channel.ls(remotePath).toList()
            log.debug("Success SFTP LS: $remote.name:$remotePath")
            result
        } catch (JschSftpException e) {
            log.error("Failed SFTP LS: $remote.name:$remotePath")
            throw new SftpException("Failed SFTP LS: $remote.name:$remotePath", e)
        }
    }

    /**
     * Get a directory entry.
     *
     * @param remotePath
     * @return directory entry
     */
    SftpATTRS stat(String remotePath) {
        log.debug("Requesting SFTP STAT: $remote.name:$remotePath")
        try {
            def result = channel.stat(remotePath)
            log.debug("Success SFTP STAT: $remote.name:$remotePath")
            result
        } catch (JschSftpException e) {
            log.error("Failed SFTP STAT: $remote.name:$remotePath")
            throw new SftpException("Failed SFTP STAT: $remote.name:$remotePath", e)
        }
    }

    /**
     * Change current directory.
     *
     * @param remotePath
     */
    void cd(String remotePath) {
        log.debug("Requesting SFTP CD: $remote.name:$remotePath")
        try {
            channel.cd(remotePath)
            log.debug("Success SFTP CD: $remote.name:$remotePath")
        } catch (JschSftpException e) {
            log.error("Failed SFTP CD: $remote.name:$remotePath")
            throw new SftpException("Failed SFTP CD: $remote.name:$remotePath", e)
        }
    }
}
