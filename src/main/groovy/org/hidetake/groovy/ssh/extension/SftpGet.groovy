package org.hidetake.groovy.ssh.extension

import com.jcraft.jsch.ChannelSftp.LsEntry
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.operation.SftpException
import org.hidetake.groovy.ssh.session.SessionHandler

/**
 * An extension class to get a file or directory via SFTP.
 *
 * @author hidetake.org
 */
@Category(SessionHandler)
@Slf4j
class SftpGet {
    @ToString
    private static class GetOptions {
        def from
        def into

        static usage = '''get() accepts following signatures:
get(from: String, into: String or File) // get a file or directory recursively
get(from: String, into: OutputStream)   // get a file into the stream
get(from: String)                       // get a file and return the content'''

        static create(HashMap map) {
            try {
                assert map.from, 'from must be given'
                new GetOptions(map)
            } catch (MissingPropertyException e) {
                throw new IllegalArgumentException(usage, e)
            } catch (AssertionError e) {
                throw new IllegalArgumentException(usage, e)
            }
        }
    }

    /**
     * Get file(s) or content from the remote host.
     *
     * @param map {@link GetOptions}
     * @returns content as a string if <code>into</into> is not given
     */
    String get(HashMap map) {
        def options = GetOptions.create(map)
        if (options.into) {
            get(options.from, options.into)
        } else {
            def stream = new ByteArrayOutputStream()
            get(options.from, stream)
            new String(stream.toByteArray())
        }
    }

    private static final sftpGetContent = { String remoteFile, OutputStream stream ->
        getContent(remoteFile, stream)
    }

    /**
     * Get a file from the remote host.
     *
     * @param remote
     * @param stream
     */
    void get(String remote, OutputStream stream) {
        assert remote, 'remote path must be given'
        assert stream,  'output stream must be given'
        sftp(sftpGetContent.curry(remote, stream))
    }

    /**
     * Get a file or directory from the remote host.
     *
     * @param remote
     * @param local
     */
    void get(String remote, File local) {
        assert remote, 'remote path must be given'
        assert local,  'local file must be given'
        sftp(sftpGetRecursive.curry(remote, local))
    }

    /**
     * Get a file or directory from the remote host.
     *
     * @param remote
     * @param local
     */
    void get(String remote, String local) {
        assert remote, 'remote path must be given'
        assert local,  'local path must be given'
        sftp(sftpGetRecursive.curry(remote, new File(local)))
    }

    private static final sftpGetRecursive = { String remoteFile, File localFile ->
        final Closure getDirectory
        getDirectory = { String remoteDir, File localDir ->
            def remoteDirName = remoteDir.find(~'[^/]+/?$')
            def localChildDir = new File(localDir, remoteDirName)
            localChildDir.mkdirs()

            cd(remoteDir)
            List<LsEntry> children = ls('.')
            children.findAll { !it.attrs.dir }.each {
                getFile(it.filename, localChildDir.path)
            }
            children.findAll { it.attrs.dir }.each {
                switch (it.filename) {
                    case '.':
                    case '..':
                        log.debug("Ignored a directory entry: ${it.longname}")
                        break
                    default:
                        getDirectory(it.filename, localChildDir)
                }
            }
            cd('..')
        }

        try {
            getFile(remoteFile, localFile.path)
        } catch (SftpException e) {
            if (e.cause.message.startsWith('not supported to get directory')) {
                log.debug(e.localizedMessage)
                log.debug('Starting to get a directory recursively')
                getDirectory(remoteFile, localFile)
            } else {
                throw new RuntimeException(e)
            }
        }
    }
}
