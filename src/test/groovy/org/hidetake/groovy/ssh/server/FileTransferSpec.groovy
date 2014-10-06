package org.hidetake.groovy.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.sftp.SftpSubsystem
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.Use

import static org.hidetake.groovy.ssh.Ssh.ssh

@org.junit.experimental.categories.Category(ServerIntegrationTest)
@Use(FileDivCategory)
class FileTransferSpec extends Specification {

    @Shared
    SshServer server

    @Rule
    TemporaryFolder temporaryFolder

    def setupSpec() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            _ * authenticate('someuser', 'somepassword', _) >> true
        }
        server.subsystemFactories = [new SftpSubsystem.Factory()]
        server.start()
    }

    def cleanupSpec() {
        ssh.remotes.clear()
        ssh.proxies.clear()
        ssh.settings.reset()
        server.stop(true)
    }


    def setup() {
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


    def "put a file given by path"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put(sourceFile.path, destinationFile.path)
            }
        }

        then:
        sourceFile.text == text
        destinationFile.text == text
    }

    def "put a file given by a File object"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put(sourceFile, destinationFile.path)
            }
        }

        then:
        sourceFile.text == text
        destinationFile.text == text
    }

    def "put files given by a collection of File object"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        def sourceFile1 = sourceDir / uuidgen() << uuidgen()
        def sourceFile2 = sourceDir / uuidgen() << uuidgen()
        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put([sourceFile1, sourceFile2], destinationDir.path)
            }
        }

        then:
        (destinationDir / sourceFile1.name).text == sourceFile1.text
        (destinationDir / sourceFile2.name).text == sourceFile2.text
    }

    def "put a file to a directory"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put(sourceFile, destinationDir.path)
            }
        }

        then:
        sourceFile.text == text
        (destinationDir / sourceFile.name).text == text
    }

    def "put a directory"() {
        given:
        def source1Dir = temporaryFolder.newFolder()
        def source2Dir = mkdir(source1Dir / uuidgen())
        def source3Dir = mkdir(source2Dir / uuidgen())

        def source1File = source1Dir / uuidgen() << uuidgen()
        def source2File = source2Dir / uuidgen() << uuidgen()

        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put(source1Dir, destinationDir.path)
            }
        }

        then:
        (destinationDir / source1Dir.name / source1File.name).text == source1File.text
        (destinationDir / source1Dir.name / source2Dir.name / source2File.name).text == source2File.text
        (destinationDir / source1Dir.name / source2Dir.name / source3Dir.name).list() == []
    }

    def "put an empty directory"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put(sourceDir, destinationDir.path)
            }
        }

        then:
        (destinationDir / sourceDir.name).list() == []
    }


    def "get a remote file to local given by path"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get(sourceFile.path, destinationFile.path)
            }
        }

        then:
        sourceFile.text == text
        destinationFile.text == text
    }

    def "get a remote file to local given by a File object"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get(sourceFile.path, destinationFile)
            }
        }

        then:
        sourceFile.text == text
        destinationFile.text == text
    }

    def "get a file to a directory"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get(sourceFile.path, destinationDir)
            }
        }

        then:
        sourceFile.text == text

        and:
        (destinationDir / sourceFile.name).text == text
    }

    def "get a directory"() {
        given:
        def source1Dir = temporaryFolder.newFolder()
        def source2Dir = mkdir(source1Dir / uuidgen())
        def source3Dir = mkdir(source2Dir / uuidgen())

        def source1File = source1Dir / uuidgen() << uuidgen()
        def source2File = source2Dir / uuidgen() << uuidgen()

        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get(source1Dir.path, destinationDir)
            }
        }

        then:
        (destinationDir / source1Dir.name / source1File.name).text == source1File.text
        (destinationDir / source1Dir.name / source2Dir.name / source2File.name).text == source2File.text
        (destinationDir / source1Dir.name / source2Dir.name / source3Dir.name).list() == []
    }

    def "get an empty directory"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get(sourceDir.path, destinationDir)
            }
        }

        then:
        (destinationDir / sourceDir.name).list() == []
    }

    @Category(File)
    static class FileDivCategory {
        File div(String child) {
            new File(this as File, child)
        }
    }

    static mkdir(File dir) {
        assert dir.mkdir()
        dir
    }

    static uuidgen() {
        UUID.randomUUID().toString()
    }

}
