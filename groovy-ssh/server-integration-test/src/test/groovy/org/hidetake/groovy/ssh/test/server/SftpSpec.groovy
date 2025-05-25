package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory
import org.hidetake.groovy.ssh.operation.SftpException

import static org.hidetake.groovy.ssh.test.server.FilenameUtils.toUnixPath

class SftpSpec extends AbstractFileTransferSpec {

    def setupSpec() {
        server.subsystemFactories = [new SftpSubsystemFactory()]
        server.start()
    }


    //FIXME: should be in AbstractFileTransferSpec but put here due to bug of Apache SSHD
    def "put(dir) should throw IOException if destination does not exist"() {
        given:
        def sourceDir = temporaryFolder.newFolder('source')
        sourceDir / 'file1' << 'Source Content 1'
        def destinationDir = temporaryFolder.newFolder('destination') / 'dir1'
        assert !destinationDir.exists()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: sourceDir, into: toUnixPath(destinationDir.path)
            }
        }

        then:
        IOException e = thrown()
        e.message.contains(toUnixPath(destinationDir.path))
    }


    def "sftp.mkdir() should fail if directory already exists"() {
        given:
        def folder = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                sftp {
                    mkdir folder.path
                }
            }
        }

        then:
        SftpException e = thrown()
        e.message.contains('SFTP MKDIR')
    }

}
