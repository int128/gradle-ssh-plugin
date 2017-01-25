package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.sftp.SftpSubsystem
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import spock.util.mop.Use

import static org.hidetake.groovy.ssh.test.server.FileDivCategory.DirectoryType.DIRECTORIES
import static org.hidetake.groovy.ssh.test.server.FileDivCategory.DirectoryType.DIRECTORY
import static org.hidetake.groovy.ssh.test.server.FilenameUtils.toUnixPath

@Use(FileDivCategory)
class SftpRemoveSpec extends Specification {

    @Shared
    SshServer server

    @Shared @ClassRule
    TemporaryFolder temporaryFolder

    Service ssh

    def setupSpec() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            (1.._) * authenticate('someuser', 'somepassword', _) >> true
        }
        server.subsystemFactories = [new SftpSubsystem.Factory()]
        server.start()
    }

    def cleanupSpec() {
        new PollingConditions().eventually {
            assert server.activeSessions.empty
        }
        server.stop()
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


    def "remove() should accept a path string"() {
        given:
        def file = temporaryFolder.newFile() << 'content'

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

    def "remove() should accept a list of paths"() {
        given:
        def dir = temporaryFolder.newFolder()
        def dir1 = dir / 'dir1' / DIRECTORY
        dir / 'dir1' / 'file1' << 'content1'
        def dir2 = dir / 'dir2' / DIRECTORY
        def file2 = dir / 'dir2' / 'file2' << 'content2'

        when:
        def result = ssh.run {
            session(ssh.remotes.testServer) {
                remove(toUnixPath(dir1.path), toUnixPath(file2.path))
            }
        }

        then:
        result == true
        !dir1.exists()
        dir2.exists()
        !file2.exists()
    }

    def "remove() should delete a directory recursively"() {
        given:
        def dir = temporaryFolder.newFolder()
        dir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'dir8' / 'dir9' / DIRECTORIES

        dir / 'file1' << 'Content 1'
        dir / 'dir2' / 'file2' << 'Content 2'
        dir / 'dir2' / 'dir3' / 'file3' << 'Content 3'
        dir / 'dir2' / 'dir3' / 'dir4' / 'file4' << 'Content 4'
        dir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'file5' << 'Content 5'
        dir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'file6' << 'Content 6'
        dir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'file7' << 'Content 7'
        dir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'dir8' / 'file8' << 'Content 8'
        dir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'dir8' / 'dir9' / 'file9' << 'Content 9'

        when:
        def result = ssh.run {
            session(ssh.remotes.testServer) {
                remove(toUnixPath(dir.path))
            }
        }

        then:
        result == true
        !dir.exists()
    }

    def "remove() should return false if target does not exist"() {
        given:
        def dir = temporaryFolder.newFolder()
        def file1 = dir / 'file1'
        def file2 = dir / 'file2'

        when:
        def result = ssh.run {
            session(ssh.remotes.testServer) {
                remove(toUnixPath(file1.path), toUnixPath(file2.path))
            }
        }

        then:
        result == false
    }

    def "remove() should return true if at least one is removed"() {
        given:
        def dir = temporaryFolder.newFolder()
        def file1 = dir / 'file1'
        def file2 = dir / 'file2' << 'content2'

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

}
