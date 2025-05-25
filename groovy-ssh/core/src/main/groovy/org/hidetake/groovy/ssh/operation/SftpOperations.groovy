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
        tryCatchSftpException("SFTP GET: $remote.name:$remotePath -> $localPath") {
            channel.get(remotePath, localPath, new SftpProgress({ percent ->
                log.info("Receiving $percent from $remote.name: $remotePath -> $localPath")
            }))
        }
    }

    /**
     * Get a content from the remote host.
     *
     * @param remotePath
     * @param stream
     */
    void getContent(String remotePath, OutputStream stream) {
        tryCatchSftpException("SFTP GET: $remote.name:$remotePath -> ${stream.class.simpleName}") {
            channel.get(remotePath, stream, new SftpProgress({ percent ->
                log.info("Receiving $percent from $remote.name: $remotePath")
            }))
        }
    }

    /**
     * Put a file to the remote host.
     *
     * @param localPath
     * @param remotePath
     */
    void putFile(String localPath, String remotePath) {
        tryCatchSftpException("SFTP PUT: $localPath -> $remote.name:$remotePath") {
            channel.put(localPath, remotePath, new SftpProgress({ percent ->
                log.info("Sending $percent to $remote.name: $remotePath")
            }), ChannelSftp.OVERWRITE)
        }
    }

    /**
     * Put a content to the remote host.
     *
     * @param stream
     * @param remotePath path
     */
    void putContent(InputStream stream, String remotePath) {
        tryCatchSftpException("SFTP PUT: stream -> $remote.name:$remotePath") {
            channel.put(stream, remotePath, new SftpProgress({ percent ->
                log.info("Sending $percent to $remote.name: $remotePath")
            }), ChannelSftp.OVERWRITE)
        }
    }

    /**
     * Create a directory.
     *
     * @param remotePath
     */
    void mkdir(String remotePath) {
        tryCatchSftpException("SFTP MKDIR: $remote.name:$remotePath") {
            channel.mkdir(remotePath)
        }
    }

    /**
     * Removes one or several files.
     *
     * @param remotePath
     */
    void rm(String remotePath) {
        tryCatchSftpException("SFTP RM: $remote.name:$remotePath") {
            channel.rm(remotePath)
        }
    }

    /**
     * Removes one or several directories.
     *
     * @param remotePath
     */
    void rmdir(String remotePath) {
        tryCatchSftpException("SFTP RMDIR: $remote.name:$remotePath") {
            channel.rmdir(remotePath)
        }
    }

    /**
     * Get a directory listing.
     *
     * @param remotePath
     * @return list of files or directories
     */
    List<ChannelSftp.LsEntry> ls(String remotePath) {
        tryCatchSftpException("SFTP LS: $remote.name:$remotePath") {
            channel.ls(remotePath).toList()
        }
    }

    /**
     * Get a directory entry.
     *
     * @param remotePath
     * @return directory entry
     */
    SftpATTRS stat(String remotePath) {
        tryCatchSftpException("SFTP STAT: $remote.name:$remotePath") {
            channel.stat(remotePath)
        }
    }

    /**
     * Change current directory.
     *
     * @param remotePath
     */
    void cd(String remotePath) {
        tryCatchSftpException("SFTP CD: $remote.name:$remotePath") {
            channel.cd(remotePath)
        }
    }

    /**
     * Rename a remote item.
     *
     * @param oldPath
     * @param newPath
     */
    void rename(String oldPath, String newPath) {
        tryCatchSftpException("SFTP RENAME: $remote.name:$oldPath -> $remote.name:$newPath") {
            channel.rename(oldPath, newPath)
        }
    }        

    private static <T> T tryCatchSftpException(String operationMessage, Closure<T> closure) {
        log.debug("Requesting $operationMessage")
        try {
            def result = closure.call()
            log.debug("Success $operationMessage")
            result
        } catch (JschSftpException e) {
            log.error("Failed $operationMessage")
            throw new SftpException("Failed $operationMessage", e)
        }
    }
}
