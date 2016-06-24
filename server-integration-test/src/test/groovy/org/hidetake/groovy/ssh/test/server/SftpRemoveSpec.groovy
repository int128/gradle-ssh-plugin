package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.sftp.SftpSubsystem
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.Use

import static org.hidetake.groovy.ssh.test.server.FileDivCategory.DirectoryType.DIRECTORY
import static org.hidetake.groovy.ssh.test.server.FilenameUtils.toUnixPath
import static org.hidetake.groovy.ssh.test.server.Helper.uuidgen

@Use(FileDivCategory)
class SftpRemoveSpec extends Specification {

    @Shared
    SshServer server

    Service ssh

    @Rule
    TemporaryFolder temporaryFolder

    def setupSpec() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            (1.._) * authenticate('someuser', 'somepassword', _) >> true
        }
        server.subsystemFactories = [new SftpSubsystem.Factory()]
        server.start()
    }

    def cleanupSpec() {
        server.stop(true)
    }


    def setup() {
        ssh = Ssh.newService()
        ssh.settings {
            knownHosts = allowAnyHosts
        }
        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'somepassword'
            }
        }
    }


    def "remove() should delete a file"() {
        given:
        def file = temporaryFolder.newFile() << 'file'
        assert file.exists()

        when:
        def result = ssh.run {
            session(ssh.remotes.testServer) {
                remove(toUnixPath(file.path))
            }
        }

        then:
        result == true
        !file.exists()
    }

    def "remove() should delete files"() {
        given:
        def file1 = temporaryFolder.newFile() << 'file'
        def file2 = temporaryFolder.newFile() << 'file'
        assert file1.exists()
        assert file2.exists()

        when:
        def result = ssh.run {
            session(ssh.remotes.testServer) {
                remove(toUnixPath(file1.path), toUnixPath(file2.path))
            }
        }

        then:
        result == true
        !file1.exists()
        !file2.exists()
    }

    def "remove() should delete a directory"() {
        given:
        def folder1 = temporaryFolder.newFolder()
        def file1 = folder1 / uuidgen() << 'file1'
        def folder2 = folder1 / uuidgen() / DIRECTORY
        def file2 = folder2 / uuidgen() << 'file2'

        assert file2.exists()
        assert folder2.exists()
        assert file1.exists()
        assert folder1.exists()

        when:
        def result = ssh.run {
            session(ssh.remotes.testServer) {
                remove(toUnixPath(folder1.path))
            }
        }

        then:
        result == true
        !file2.exists()
        !folder2.exists()
        !file1.exists()
        !folder1.exists()
    }

    def "remove() should delete a directory and file"() {
        given:
        def folder1 = temporaryFolder.newFolder()
        def file1 = folder1 / uuidgen() << 'file1'
        def folder2 = temporaryFolder.newFolder()
        def file2 = folder2 / uuidgen() << 'file2'

        assert file2.exists()
        assert folder2.exists()
        assert file1.exists()
        assert folder1.exists()

        when:
        def result = ssh.run {
            session(ssh.remotes.testServer) {
                remove(toUnixPath(folder1.path), toUnixPath(file2.path))
            }
        }

        then:
        result == true
        !file2.exists()
        folder2.exists()  // not deleted
        !file1.exists()
        !folder1.exists()
    }

    def "remove() should return false if target does not exist"() {
        given:
        def folder1 = temporaryFolder.newFolder()
        def file1 = folder1 / uuidgen() << 'file1'
        def file2 = folder1 / uuidgen()

        assert file1.exists()
        assert !file2.exists()
        assert folder1.exists()

        when:
        def result = ssh.run {
            session(ssh.remotes.testServer) {
                remove(toUnixPath(file2.path))
            }
        }

        then:
        result == false
        file1.exists()
        !file2.exists()
    }

    def "remove() should return true if at least one is removed"() {
        given:
        def folder1 = temporaryFolder.newFolder()
        def file1 = folder1 / uuidgen() << 'file1'
        def file2 = folder1 / uuidgen()

        assert file1.exists()
        assert !file2.exists()
        assert folder1.exists()

        when:
        def result = ssh.run {
            session(ssh.remotes.testServer) {
                remove(toUnixPath(file1.path), toUnixPath(file2.path))
            }
        }

        then:
        result == true
        !file1.exists()
        !file2.exists()
    }

    def "remove() should return false if anything is not removed"() {
        given:
        def folder1 = temporaryFolder.newFolder()
        def file1 = folder1 / uuidgen()
        def file2 = folder1 / uuidgen()

        assert !file1.exists()
        assert !file2.exists()
        assert folder1.exists()

        when:
        def result = ssh.run {
            session(ssh.remotes.testServer) {
                remove(toUnixPath(file1.path), toUnixPath(file2.path))
            }
        }

        then:
        result == false
        !file1.exists()
        !file2.exists()
    }

}
