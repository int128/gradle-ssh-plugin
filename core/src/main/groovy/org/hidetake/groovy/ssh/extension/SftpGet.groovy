package org.hidetake.groovy.ssh.extension

import com.jcraft.jsch.ChannelSftp.LsEntry
import groovy.transform.ToString
import org.hidetake.groovy.ssh.operation.SftpException
import org.hidetake.groovy.ssh.session.SessionExtension
import org.hidetake.groovy.ssh.util.Utility
import org.slf4j.LoggerFactory

import static org.hidetake.groovy.ssh.util.Utility.currySelf

/**
 * An extension class to get a file or directory via SFTP.
 *
 * @author Hidetake Iwata
 */
trait SftpGet implements SessionExtension {
    private static final log = LoggerFactory.getLogger(SftpGet)

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

    /**
     * Get a file from the remote host.
     *
     * @param remote
     * @param stream
     */
    void get(String remote, OutputStream stream) {
        assert remote, 'remote path must be given'
        assert stream,  'output stream must be given'
        sftp {
            getContent(remote, stream)
        }
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
        get(remote, new File(local))
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
        try {
            sftp {
                getFile(remote, local.path)
            }
        } catch (SftpException e) {
            if (e.cause.message.startsWith('not supported to get directory')) {
                log.debug(e.localizedMessage)
                getRecursive(remote, local)
            } else {
                throw new RuntimeException(e)
            }
        }
    }

    private void getRecursive(String baseRemoteDir, File baseLocalDir) {
        sftp {
            currySelf { Closure self, String remoteDir, File localDir ->
                def remoteDirName = remoteDir.find(~'[^/]+/?$')
                def localChildDir = new File(localDir, remoteDirName)
                localChildDir.mkdirs()

                log.debug("Entering directory $remoteDir")
                cd(remoteDir)

                ls('.').each { child ->
                    if (!child.attrs.dir) {
                        getFile(child.filename, localChildDir.path)
                    } else if (child.filename in ['.', '..']) {
                        log.debug("Ignored directory entry: ${child.longname}")
                    } else {
                        self.call(self, child.filename, localChildDir)
                    }
                }

                log.debug("Leaving directory $remoteDir")
                cd('..')
            }.call(baseRemoteDir, baseLocalDir)
        }
    }
}
