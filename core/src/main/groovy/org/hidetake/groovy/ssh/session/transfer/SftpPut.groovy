package org.hidetake.groovy.ssh.session.transfer

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.operation.SftpFailureException
import org.hidetake.groovy.ssh.session.SessionExtension

import static org.hidetake.groovy.ssh.util.Utility.currySelf

@Slf4j
trait SftpPut implements SessionExtension {

    void sftpPut(InputStream stream, String remotePath) {
        assert stream, 'input stream must be given'
        assert remotePath, 'remote path must be given'
        sftp {
            putContent(stream, remotePath)
        }
    }

    void sftpPut(Iterable<File> baseLocalFiles, String baseRemotePath) {
        sftp {
            currySelf { Closure self, Iterable<File> localFiles, String remotePath ->
                localFiles.findAll { !it.directory }.each { localFile ->
                    putFile(localFile.path, remotePath)
                    log.info("Sent file to $remote.name: $localFile.path -> $remotePath")
                }
                localFiles.findAll { it.directory }.each { localDir ->
                    log.debug("Entering directory on $remote.name: $localDir.path")
                    def remoteDir = "$remotePath/${localDir.name}"
                    try {
                        mkdir(remoteDir)
                    } catch (SftpFailureException ignore) {
                        log.info("Remote directory already exists on $remote.name: $remoteDir")
                    }

                    self.call(self, localDir.listFiles().toList(), remoteDir)
                    log.debug("Leaving directory on $remote.name: $localDir.path")
                }
            }.call(baseLocalFiles, baseRemotePath)
        }
    }

}