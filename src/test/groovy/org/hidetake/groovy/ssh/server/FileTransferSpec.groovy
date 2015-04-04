package org.hidetake.groovy.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.sftp.SftpSubsystem
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.operation.SftpException
import org.hidetake.groovy.ssh.util.FilenameUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.Use

@org.junit.experimental.categories.Category(ServerIntegrationTest)
@Use(FileDivCategory)
class FileTransferSpec extends Specification {

    @Shared
    SshServer server

    Service ssh

    @Rule
    TemporaryFolder temporaryFolder=new TemporaryFolder(new File("build"))

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


    def "put() should accept a path string as source"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put(sourceFile.path, FilenameUtils.toUnixSeparator(destinationFile.path))
            }
        }

        then:
        sourceFile.text == text
        destinationFile.text == text
    }

    def "put() should accept a File object as source"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put(sourceFile, FilenameUtils.toUnixSeparator(destinationFile.path))
            }
        }

        then:
        sourceFile.text == text
        destinationFile.text == text
    }

    def "put() should accept a collection of file as source"() {
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

    def "put() should accept an input stream as source"() {
        given:
        def text = uuidgen()
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                def stream = new ByteArrayInputStream(text.bytes)
                put stream, destinationFile.path
            }
        }

        then:
        destinationFile.text == text
    }

    def "put() should accept a path string via map"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: sourceFile.path, into: destinationFile.path
            }
        }

        then:
        sourceFile.text == text
        destinationFile.text == text
    }

    def "put() should accept a File object via map"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: sourceFile, into: destinationFile.path
            }
        }

        then:
        sourceFile.text == text
        destinationFile.text == text
    }

    def "put() should accept a collection of file via map"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        def sourceFile1 = sourceDir / uuidgen() << uuidgen()
        def sourceFile2 = sourceDir / uuidgen() << uuidgen()
        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: [sourceFile1, sourceFile2], into: destinationDir.path
            }
        }

        then:
        (destinationDir / sourceFile1.name).text == sourceFile1.text
        (destinationDir / sourceFile2.name).text == sourceFile2.text
    }

    def "put() should accept a string content via map"() {
        given:
        def text = uuidgen()
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put text: text, into: destinationFile.path
            }
        }

        then:
        destinationFile.text == text
    }

    def "put() should accept a byte array content via map"() {
        given:
        def text = uuidgen()
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put bytes: text.bytes, into: destinationFile.path
            }
        }

        then:
        destinationFile.text == text
    }

    def "put() should accept an input stream via map"() {
        given:
        def text = uuidgen()
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                def stream = new ByteArrayInputStream(text.bytes)
                put from: stream, into: destinationFile.path
            }
        }

        then:
        destinationFile.text == text
    }

    def "put() should overwrite a file if destination already exists"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationFile = temporaryFolder.newFile() << uuidgen()

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

    def "put() should save a file of same name if destination is a directory"() {
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

    def "put() should merge and overwrite a file to a directory if it is not empty"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text

        def destinationDir = temporaryFolder.newFolder()
        def destination1File = destinationDir / sourceFile.name << uuidgen()
        def destination2File = destinationDir / uuidgen() << uuidgen()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put(sourceFile, destinationDir.path)
            }
        }

        then:
        sourceFile.text == text
        destination1File.text == text
        destination2File.exists()
    }

    def "put() should put a whole directory if both are directories"() {
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

    def "put() should merge and overwrite a directory to a directory if it is not empty"() {
        given:
        def source1Dir = temporaryFolder.newFolder()
        def source2Dir = mkdir(source1Dir / uuidgen())
        def source3Dir = mkdir(source2Dir / uuidgen())

        def source1File = source1Dir / uuidgen() << uuidgen()
        def source2File = source2Dir / uuidgen() << uuidgen()

        def destination0Dir = temporaryFolder.newFolder()
        def destination1Dir = mkdir(destination0Dir / source1Dir.name)
        def destination2Dir = mkdir(destination1Dir / source2Dir.name)
        def destination3Dir = mkdir(destination2Dir / source3Dir.name)

        def destination1File = destination1Dir / source1File.name << uuidgen()
        def destination2File = destination2Dir / source2File.name << uuidgen()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put(source1Dir, destination0Dir.path)
            }
        }

        then:
        (destination0Dir / source1Dir.name / source1File.name).text == source1File.text
        (destination0Dir / source1Dir.name / source2Dir.name / source2File.name).text == source2File.text
        (destination0Dir / source1Dir.name / source2Dir.name / source3Dir.name).list() == []

        and:
        destination1File.text == source1File.text
        destination2File.text == source2File.text
        destination3Dir.exists()
    }

    def "put() should put a whole directory even if empty"() {
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

    def "put() should throw an error if source is null"() {
        given:
        def file = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put(null, file.path)
            }
        }

        then:
        AssertionError e = thrown()
        e.localizedMessage.contains 'local'
    }

    def "put() should throw an error if destination is null"() {
        given:
        def file = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put(file.path, null)
            }
        }

        then:
        AssertionError e = thrown()
        e.localizedMessage.contains 'remote'
    }

    @Unroll
    def "put(#key) should throw an error if into is not given"() {
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


    def "get() should accept a path string as destination"() {
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

    def "get() should accept a file object as destination"() {
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

    def "get() should accept an output stream as destination"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def outputStream = new ByteArrayOutputStream()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get sourceFile.path, outputStream
            }
        }

        then:
        sourceFile.text == text
        outputStream.toByteArray() == text.bytes
    }

    def "get() should accept a path string via map"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: sourceFile.path, into: destinationFile.path
            }
        }

        then:
        sourceFile.text == text
        destinationFile.text == text
    }

    def "get() should accept a file object via map"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationFile = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: sourceFile.path, into: destinationFile
            }
        }

        then:
        sourceFile.text == text
        destinationFile.text == text
    }

    def "get() should accept an output stream via map"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def outputStream = new ByteArrayOutputStream()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: sourceFile.path, into: outputStream
            }
        }

        then:
        sourceFile.text == text
        outputStream.toByteArray() == text.bytes
    }

    def "get() should return content if into is not given"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text

        when:
        def content = ssh.run {
            session(ssh.remotes.testServer) {
                get from: sourceFile.path
            }
        }

        then:
        sourceFile.text == text
        content == text
    }

    def "get() should overwrite a file if destination already exists"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationFile = temporaryFolder.newFile() << uuidgen()

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

    def "get() should save a file of same name if destination is a directory"() {
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

    def "get() should merge and overwrite a file to a directory if it is not empty"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationDir = temporaryFolder.newFolder()
        def destination1File = destinationDir / sourceFile.name << text
        def destination2File = destinationDir / uuidgen() << text

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get(sourceFile.path, destinationDir)
            }
        }

        then:
        sourceFile.text == text
        destination1File.text == text
        destination2File.exists()
    }

    def "get() should get a whole directory if source is a directory"() {
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

    def "get() should merge and overwrite a directory to a directory if it is not empty"() {
        given:
        def source1Dir = temporaryFolder.newFolder()
        def source2Dir = mkdir(source1Dir / uuidgen())
        def source3Dir = mkdir(source2Dir / uuidgen())

        def source1File = source1Dir / uuidgen() << uuidgen()
        def source2File = source2Dir / uuidgen() << uuidgen()

        def destination0Dir = temporaryFolder.newFolder()
        def destination1Dir = mkdir(destination0Dir / source1Dir.name)
        def destination2Dir = mkdir(destination1Dir / source2Dir.name)
        def destination3Dir = mkdir(destination2Dir / source3Dir.name)

        def destination1File = destination1Dir / source1File.name << uuidgen()
        def destination2File = destination2Dir / source2File.name << uuidgen()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get(source1Dir.path, destination0Dir)
            }
        }

        then:
        (destination0Dir / source1Dir.name / source1File.name).text == source1File.text
        (destination0Dir / source1Dir.name / source2Dir.name / source2File.name).text == source2File.text
        (destination0Dir / source1Dir.name / source2Dir.name / source3Dir.name).list() == []

        and:
        destination1File.text == source1File.text
        destination2File.text == source2File.text
        destination3Dir.exists()
    }

    def "get() should get a whole directory even if empty"() {
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

    def "get() should throw an error if source is null"() {
        given:
        def file = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get(null, file.path)
            }
        }

        then:
        AssertionError e = thrown()
        e.localizedMessage.contains 'remote'
    }

    def "get() should throw an error if destination is null string"() {
        given:
        def file = temporaryFolder.newFile()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get(file.path, '')
            }
        }

        then:
        AssertionError e = thrown()
        e.localizedMessage.contains 'local'
    }

    def "get() should throw an error if from is not given"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get(into: 'somefile')
            }
        }

        then:
        thrown(IllegalArgumentException)
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
        e.message.contains('create a directory')
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
