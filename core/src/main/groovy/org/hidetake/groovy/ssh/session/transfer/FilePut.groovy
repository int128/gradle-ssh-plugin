package org.hidetake.groovy.ssh.session.transfer

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.session.SessionExtension

/**
 * An extension class to put a file or directory.
 *
 * @author Hidetake Iwata
 */
@Slf4j
trait FilePut implements SessionExtension, SftpPut, ScpPut {

    /**
     * Put a file to the remote host.
     *
     * @param stream
     * @param remotePath
     */
    void put(InputStream stream, String remotePath) {
        assert stream, 'input stream must be given'
        assert remotePath, 'remote path must be given'
        if (mergedSettings.fileTransfer == FileTransferMethod.sftp) {
            sftpPut(stream, remotePath)
        } else if (mergedSettings.fileTransfer == FileTransferMethod.scp) {
            scpPut(stream, remotePath)
        } else {
            throw new IllegalStateException("Unknown file transfer method: ${mergedSettings.fileTransfer}")
        }
        log.info("Sent content to $remote.name: $remotePath")
    }

    /**
     * Put a file or directory to the remote host.
     *
     * @param localFile
     * @param remotePath
     */
    void put(File localFile, String remotePath) {
        assert remotePath, 'remote path must be given'
        assert localFile, 'local file must be given'
        put([localFile], remotePath)
    }

    /**
     * Put a file or directory to the remote host.
     *
     * @param localPath
     * @param remotePath
     */
    void put(String localPath, String remotePath) {
        assert remotePath, 'remote path must be given'
        assert localPath, 'local path must be given'
        put(new File(localPath), remotePath)
    }

    /**
     * Put a collection of a file or directory to the remote host.
     *
     * @param localFiles
     * @param remotePath
     */
    void put(Iterable<File> localFiles, String remotePath) {
        assert remotePath, 'remote path must be given'
        assert localFiles, 'local files must be given'
        if (mergedSettings.fileTransfer == FileTransferMethod.sftp) {
            sftpPut(localFiles, remotePath)
        } else if (mergedSettings.fileTransfer == FileTransferMethod.scp) {
            scpPut(localFiles, remotePath)
        } else {
            throw new IllegalStateException("Unknown file transfer method: ${mergedSettings.fileTransfer}")
        }
    }

    /**
     * Put file(s) or content to the remote host.
     */
    void put(HashMap map) {
        final usage = """Got $map but not following signatures:
put(from: String or File, into: String)  // put a file or directory
put(from: Iterable<File>, into: String)  // put files or directories
put(from: InputStream, into: String)     // put a stream into the remote file
put(text: String, into: String)          // put a string into the remote file
put(bytes: byte[], into: String)         // put a byte array into the remote file"""

        if (map.containsKey('from') && map.containsKey('into')) {
            try {
                //noinspection GroovyAssignabilityCheck
                put(map.from, map.into as String)
            } catch (MissingMethodException e) {
                throw new IllegalArgumentException(usage, e)
            }
        } else if (map.containsKey('text') && map.containsKey('into')) {
            def stream = new ByteArrayInputStream((map.text as String).bytes)
            put(stream, map.into as String)
        } else if (map.containsKey('bytes') && map.containsKey('into')) {
            def stream = new ByteArrayInputStream(map.bytes as byte[])
            put(stream, map.into as String)
        } else {
            throw new IllegalArgumentException(usage)
        }
    }

}