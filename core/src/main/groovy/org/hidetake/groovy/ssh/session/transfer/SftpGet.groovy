package org.hidetake.groovy.ssh.session.transfer

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.operation.SftpFailureException
import org.hidetake.groovy.ssh.session.SessionExtension

import static org.hidetake.groovy.ssh.util.Utility.currySelf

@Slf4j
trait SftpGet implements SessionExtension {

    void sftpGet(String remotePath, OutputStream stream) {
        assert remotePath, 'remote path must be given'
        assert stream, 'output stream must be given'
        sftp {
            getContent(remotePath, stream)
        }
    }

    void sftpGet(String remotePath, File localFile) {
        assert remotePath, 'remote path must be given'
        assert localFile, 'local file must be given'
        try {
            sftp {
                getFile(remotePath, localFile.path)
            }
            log.info("Received file from $remote.name: $remotePath -> $localFile.path")
        } catch (SftpFailureException e) {
            if (e.cause.message.startsWith('not supported to get directory')) {
                log.debug("Found directory on $remote.name: $remotePath")
                sftpGetRecursive(remotePath, localFile)
                log.info("Received directory $remote.name: $remotePath -> $localFile.path")
            } else {
                throw e
            }
        }
    }

    private void sftpGetRecursive(String baseRemoteDir, File baseLocalDir) {
        sftp {
            currySelf { Closure self, String remoteDir, File localDir ->
                def remoteDirName = remoteDir.find(~'[^/]+/?$')
                def localChildDir = new File(localDir, remoteDirName)
                localChildDir.mkdirs()

                log.debug("Entering directory on $remote.name: $remoteDir")
                cd(remoteDir)

                ls('.').each { child ->
                    if (!child.attrs.dir) {
                        getFile(child.filename, localChildDir.path)
                    } else if (child.filename in ['.', '..']) {
                        // ignore directory entries
                    } else {
                        self.call(self, child.filename, localChildDir)
                    }
                }

                log.debug("Leaving directory on $remote.name: $remoteDir")
                cd('..')
            }.call(baseRemoteDir, baseLocalDir)
        }
    }

}
