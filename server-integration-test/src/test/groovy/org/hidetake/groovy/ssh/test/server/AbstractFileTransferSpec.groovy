package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions
import spock.util.mop.Use

import static org.hidetake.groovy.ssh.test.server.FilenameUtils.toUnixPath

@Use(FileDivCategory)
abstract class AbstractFileTransferSpec extends Specification {

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



    //
    // [PUT] arguments
    //

    def "put() should accept a path string"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: sourceFile.path, into: toUnixPath(destinationFile.path)
            }
        }

        then:
        sourceFile.text == 'Source Content'
        destinationFile.text == 'Source Content'
    }

    def "put() should accept a File object"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: sourceFile, into: toUnixPath(destinationFile.path)
            }
        }

        then:
        sourceFile.text == 'Source Content'
        destinationFile.text == 'Source Content'
    }

    def "put() should accept a collection of file"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        def sourceFile1 = sourceDir / 'file1' << 'Source Content 1'
        def sourceFile2 = sourceDir / 'file2' << 'Source Content 2'

        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: [sourceFile1, sourceFile2], into: toUnixPath(destinationDir.path)
            }
        }

        then:
        (destinationDir / 'file1').text == 'Source Content 1'
        (destinationDir / 'file2').text == 'Source Content 2'
    }

    def "put() should accept a string content"() {
        given:
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put text: 'Text Content', into: toUnixPath(destinationFile.path)
            }
        }

        then:
        destinationFile.text == 'Text Content'
    }

    def "put() should accept a byte array content"() {
        given:
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put bytes: [0xff, 0xfe] as byte[], into: toUnixPath(destinationFile.path)
            }
        }

        then:
        destinationFile.bytes == [0xff, 0xfe] as byte[]
    }

    def "put() should accept an input stream"() {
        given:
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                def stream = new ByteArrayInputStream([0xff, 0xfe, 0xfd] as byte[])
                put from: stream, into: toUnixPath(destinationFile.path)
            }
        }

        then:
        destinationFile.bytes == [0xff, 0xfe, 0xfd] as byte[]
    }

    def "put() should throw an error if source is null"() {
        given:
        def file = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: null, into: file.path
            }
        }

        then:
        AssertionError e = thrown()
        e.localizedMessage.contains 'from'
    }

    def "put() should throw an error if destination is null"() {
        given:
        def file = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: file.path, into: null
            }
        }

        then:
        AssertionError e = thrown()
        e.localizedMessage.contains 'into'
    }

    @Unroll
    def "put() should throw an error if into is not given (#key)"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put(argument)
            }
        }

        then:
        thrown(IllegalArgumentException)

        where:
        key      | argument
        'file'   | [from: 'somefile.txt']
        'files'  | [from: ['somefile.txt']]
        'stream' | [from: new ByteArrayInputStream([0xff, 0xff] as byte[])]
        'text'   | [text: 'something']
        'bytes'  | [bytes: [0xff, 0xff]]
    }

    def "put() should throw an error if from is not given"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put into: 'file'
            }
        }

        then:
        thrown(IllegalArgumentException)
    }

    def "put() should throw FileNotFoundException if source does not exist"() {
        given:
        def sourceFile = temporaryFolder.newFolder() / 'file1'
        def destinationFile = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: sourceFile.path, into: toUnixPath(destinationFile.path)
            }
        }

        then:
        FileNotFoundException e = thrown()
        e.message.contains(toUnixPath(sourceFile.path))
    }



    //
    // [GET] arguments
    //

    def "get() should accept a path string"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: toUnixPath(sourceFile.path), into: destinationFile.path
            }
        }

        then:
        sourceFile.text == 'Source Content'
        destinationFile.text == 'Source Content'
    }

    def "get() should accept a file object"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: toUnixPath(sourceFile.path), into: destinationFile
            }
        }

        then:
        sourceFile.text == 'Source Content'
        destinationFile.text == 'Source Content'
    }

    def "get() should accept an output stream"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'
        def outputStream = new ByteArrayOutputStream()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: toUnixPath(sourceFile.path), into: outputStream
            }
        }

        then:
        sourceFile.text == 'Source Content'
        outputStream.toByteArray() == 'Source Content'.bytes
    }

    def "get() should return content if into is not given"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'

        when:
        def content = ssh.run {
            session(ssh.remotes.testServer) {
                get from: toUnixPath(sourceFile.path)
            }
        }

        then:
        sourceFile.text == 'Source Content'
        content == 'Source Content'
    }

    @Unroll
    def "get() should handle a binary file with #size bytes"() {
        given:
        def content = new byte[size]
        new Random().nextBytes(content)

        and:
        def sourceFile = temporaryFolder.newFile() << content
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: toUnixPath(sourceFile.path), into: destinationFile.path
            }
        }

        then:
        sourceFile.bytes == content
        destinationFile.bytes == content

        where:
        size << [0, 1, 1024, 12345]
    }

    def "get() should throw an error if source is null"() {
        given:
        def file = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: null, into: file.path
            }
        }

        then:
        AssertionError e = thrown()
        e.localizedMessage.contains 'from'
    }

    def "get() should throw an error if destination is null"() {
        given:
        def file = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: file.path, into: null
            }
        }

        then:
        AssertionError e = thrown()
        e.localizedMessage.contains 'into'
    }

    def "get() should throw an error if from is not given"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get into: 'somefile'
            }
        }

        then:
        thrown(IllegalArgumentException)
    }

    def "get() should throw IOException if source does not exist"() {
        given:
        def sourceFile = temporaryFolder.newFolder() / 'file1'
        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: toUnixPath(sourceFile.path), into: destinationDir
            }
        }

        then:
        IOException e = thrown()
        e.message.contains(toUnixPath(sourceFile.path))
    }

}
