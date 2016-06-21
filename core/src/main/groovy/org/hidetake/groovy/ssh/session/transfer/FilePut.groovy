package org.hidetake.groovy.ssh.session.transfer

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.session.SessionExtension
import org.hidetake.groovy.ssh.session.transfer.put.Instructions
import org.hidetake.groovy.ssh.session.transfer.put.Scp
import org.hidetake.groovy.ssh.session.transfer.put.Sftp

/**
 * An extension class to put a file or directory.
 *
 * @author Hidetake Iwata
 */
@Slf4j
trait FilePut implements SessionExtension {

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
            assert map.from != null, 'from must not be null'
            assert map.into != null, 'into must not be null'
            try {
                //noinspection GroovyAssignabilityCheck
                putInternal(map.from, map.into as String)
            } catch (MissingMethodException e) {
                throw new IllegalArgumentException(usage, e)
            }
        } else if (map.containsKey('text') && map.containsKey('into')) {
            assert map.text != null, 'text must not be null'
            assert map.into != null, 'into must not be null'
            def stream = new ByteArrayInputStream((map.text as String).bytes)
            putInternal(stream, map.into as String)
        } else if (map.containsKey('bytes') && map.containsKey('into')) {
            assert map.bytes != null, 'bytes must not be null'
            assert map.into != null, 'into must not be null'
            def stream = new ByteArrayInputStream(map.bytes as byte[])
            putInternal(stream, map.into as String)
        } else {
            throw new IllegalArgumentException(usage)
        }
    }

    /**
     * Put a file to the remote host.
     *
     * @param stream
     * @param remotePath
     */
    private void putInternal(InputStream stream, String remotePath) {
        def instructions = Instructions.forStreamContent(stream, remotePath)
        putInternal(instructions)
    }

    /**
     * Put a file or directory to the remote host.
     *
     * @param localFile
     * @param remotePath
     */
    private void putInternal(File localFile, String remotePath) {
        def instructions = Instructions.forFile(localFile, remotePath)
        putInternal(instructions)
    }

    /**
     * Put a file or directory to the remote host.
     *
     * @param localPath
     * @param remotePath
     */
    private void putInternal(String localPath, String remotePath) {
        putInternal(new File(localPath), remotePath)
    }

    /**
     * Put a collection of files or directories to the remote host.
     *
     * @param localFiles
     * @param remotePath
     */
    private void putInternal(Iterable<File> localFiles, String remotePath) {
        def instructions = Instructions.forFiles(localFiles, remotePath)
        putInternal(instructions)
    }

    private void putInternal(Instructions instructions) {
        if (mergedSettings.fileTransfer == FileTransferMethod.sftp) {
            new Sftp(operations).put(instructions)
        } else if (mergedSettings.fileTransfer == FileTransferMethod.scp) {
            new Scp(operations, mergedSettings).put(instructions)
        } else {
            throw new IllegalStateException("Unknown file transfer method: ${mergedSettings.fileTransfer}")
        }
    }

}