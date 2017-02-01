package org.hidetake.groovy.ssh.session.transfer

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.session.SessionExtension
import org.hidetake.groovy.ssh.session.transfer.put.Instructions
import org.hidetake.groovy.ssh.session.transfer.put.Provider
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
put(from: String, into: String)             // put a file or directory
put(from: File, into: String)               // put a file or directory
put(from: File, into: String, filter: {})   // put files by file filter
put(from: Iterable<File>, into: String)     // put files
put(from: InputStream, into: String)        // put a stream into the remote file
put(text: String, into: String)             // put a string into the remote file
put(bytes: byte[], into: String)            // put a byte array into the remote file"""

        if (map.containsKey('from') && map.containsKey('into')) {
            assert map.from != null, 'from must not be null'
            assert map.into != null, 'into must not be null'
            try {
                if (map.containsKey('filter')) {
                    assert map.filter != null, 'filter must not be null'
                    //noinspection GroovyAssignabilityCheck
                    putInternal(map.from, map.into as String, map.filter as Closure)
                } else {
                    //noinspection GroovyAssignabilityCheck
                    putInternal(map.from, map.into as String)
                }
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
        createPutProvider().put(instructions)
    }

    /**
     * Put a file or directory to the remote host.
     *
     * @param localFile
     * @param remotePath
     */
    private void putInternal(File localFile, String remotePath) {
        if (!localFile.exists()) {
            throw new FileNotFoundException(localFile.path)
        }
        def instructions = Instructions.forFile(localFile, remotePath)
        createPutProvider().put(instructions)
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
        localFiles.each { localFile ->
            if (!localFile.exists()) {
                throw new FileNotFoundException(localFile.path)
            }
        }
        def instructions = Instructions.forFiles(localFiles, remotePath)
        createPutProvider().put(instructions)
    }

    /**
     * Put filtered files to the remote host.
     *
     * @param localFile
     * @param remotePath
     */
    private void putInternal(File localFile, String remotePath, Closure<Boolean> filter) {
        if (!localFile.exists()) {
            throw new FileNotFoundException(localFile.path)
        }
        def instructions = Instructions.forFileWithFilter(localFile, remotePath, filter)
        createPutProvider().put(instructions)
    }

    /**
     * Put filtered files to the remote host.
     *
     * @param localPath
     * @param remotePath
     */
    private void putInternal(String localPath, String remotePath, Closure<Boolean> filter) {
        putInternal(new File(localPath), remotePath, filter)
    }

    private Provider createPutProvider() {
        if (mergedSettings.fileTransfer == FileTransferMethod.sftp) {
            new Sftp(operations, mergedSettings)
        } else if (mergedSettings.fileTransfer == FileTransferMethod.scp) {
            new Scp(operations, mergedSettings)
        } else {
            throw new IllegalStateException("Unknown file transfer method: ${mergedSettings.fileTransfer}")
        }
    }

}