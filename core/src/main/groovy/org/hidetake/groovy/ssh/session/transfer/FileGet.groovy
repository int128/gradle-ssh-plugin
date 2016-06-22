package org.hidetake.groovy.ssh.session.transfer

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.session.SessionExtension

/**
 * An extension class to get a file or directory.
 *
 * @author Hidetake Iwata
 */
@Slf4j
trait FileGet implements SessionExtension, SftpGet, ScpGet {

    /**
     * Get file(s) or content from the remote host.
     *
     * @param map <code>from</code> and <code>into</code>
     * @returns content as a string if <code>into</into> is not given
     */
    def get(HashMap map) {
        final usage = """Got $map but not following signatures:
get(from: String, into: String or File)  // get a file or directory recursively
get(from: String, into: OutputStream)    // get a file into the stream
get(from: String)                        // get a file and return the content"""

        if (map.containsKey('from')) {
            if (map.containsKey('into')) {
                try {
                    //noinspection GroovyAssignabilityCheck
                    getInternal(map.from as String, map.into)
                } catch (MissingMethodException e) {
                    throw new IllegalArgumentException(usage, e)
                }
            } else {
                def stream = new ByteArrayOutputStream()
                getInternal(map.from as String, stream)
                new String(stream.toByteArray())
            }
        } else {
            throw new IllegalArgumentException(usage)
        }
    }

    /**
     * Get a file or directory from the remote host.
     *
     * @param remotePath
     * @param stream
     */
    private void getInternal(String remotePath, OutputStream stream) {
        assert remotePath, 'remote path must be given'
        assert stream, 'output stream must be given'
        if (mergedSettings.fileTransfer == FileTransferMethod.sftp) {
            sftpGet(remotePath, stream)
        } else if (mergedSettings.fileTransfer == FileTransferMethod.scp) {
            scpGet(remotePath, stream)
        } else {
            throw new IllegalStateException("Unknown file transfer method: ${mergedSettings.fileTransfer}")
        }
        log.info("Received content from $remote.name: $remotePath")
    }

    /**
     * Get a file or directory from the remote host.
     *
     * @param remotePath
     * @param localPath
     */
    private void getInternal(String remotePath, String localPath) {
        assert remotePath, 'remote path must be given'
        assert localPath,  'local path must be given'
        getInternal(remotePath, new File(localPath))
    }

    /**
     * Get a file from the remote host.
     *
     * @param remotePath
     * @param localFile
     */
    private void getInternal(String remotePath, File localFile) {
        assert remotePath, 'remote path must be given'
        assert localFile,  'local file must be given'
        if (mergedSettings.fileTransfer == FileTransferMethod.sftp) {
            sftpGet(remotePath, localFile)
        } else if (mergedSettings.fileTransfer == FileTransferMethod.scp) {
            scpGet(remotePath, localFile)
        } else {
            throw new IllegalStateException("Unknown file transfer method: ${mergedSettings.fileTransfer}")
        }
    }

}