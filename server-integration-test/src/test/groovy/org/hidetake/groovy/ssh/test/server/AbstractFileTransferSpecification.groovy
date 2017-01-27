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

import static org.hidetake.groovy.ssh.test.server.FileDivCategory.DirectoryType.DIRECTORIES
import static org.hidetake.groovy.ssh.test.server.FileDivCategory.DirectoryType.DIRECTORY
import static org.hidetake.groovy.ssh.test.server.FilenameUtils.toUnixPath

@Use(FileDivCategory)
abstract class AbstractFileTransferSpecification extends Specification {

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

    def "put() should throw an error if source does not exist"() {
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
        //FIXME
        thrown()
    }



    //
    // [PUT] file transfer
    //

    def "put(file) should create a file if destination is a non-existent file in a directory"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'
        def destinationFile = temporaryFolder.newFolder() / 'file1'
        assert !destinationFile.exists()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: sourceFile.path, into: toUnixPath(destinationFile.path)
            }
        }

        then:
        destinationFile.text == 'Source Content'
    }

    def "put(file) should create a file if destination is an existent directory"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'

        def destinationDir = temporaryFolder.newFolder()
        def destination1File = destinationDir / sourceFile.name << 'Destination Content 1'
        def destination2File = destinationDir / 'file2'         << 'Destination Content 2'

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: sourceFile, into: toUnixPath(destinationDir.path)
            }
        }

        then: 'destination should be overwrote'
        destination1File.text == 'Source Content'

        and: 'destination should be kept as-is'
        destination2File.text == 'Destination Content 2'
    }

    def "put(file) should overwrite a file if destination is an existent file"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'
        def destinationFile = temporaryFolder.newFile() << 'Destination Content'

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: sourceFile.path, into: toUnixPath(destinationFile.path)
            }
        }

        then:
        destinationFile.text == 'Source Content'
    }

    def "put(file) should throw an error if destination and its parent do not exist"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'
        def destinationFile = temporaryFolder.newFolder() / 'dir1' / 'file1'

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: sourceFile, into: toUnixPath(destinationFile.path)
            }
        }

        then:
        //FIXME
        thrown()
    }



    //
    // [PUT] directory transfer
    //

    def "put(dir) should create a directory if destination is an existent directory"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        sourceDir / 'file1' << 'Source Content 1'
        sourceDir / 'dir2' / DIRECTORY
        sourceDir / 'dir2' / 'file2' << 'Source Content 2'
        sourceDir / 'dir2' / 'dir3' / DIRECTORY

        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: sourceDir, into: toUnixPath(destinationDir.path)
            }
        }

        then:
        (destinationDir / sourceDir.name / 'file1').text == 'Source Content 1'
        (destinationDir / sourceDir.name / 'dir2' / 'file2').text == 'Source Content 2'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3').list() == []
    }

    def "put(dir) should create a directory even if source is empty"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: sourceDir, into: toUnixPath(destinationDir.path)
            }
        }

        then:
        (destinationDir / sourceDir.name).list() == []
    }

    def "put(dir) should overwrite a directory if destination already exists"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        sourceDir / 'file1' << 'Source Content 1'
        sourceDir / 'dir2' / DIRECTORY
        sourceDir / 'dir2' / 'file2' << 'Source Content 2'
        sourceDir / 'dir2' / 'dir3' / DIRECTORY

        and:
        def destinationDir = temporaryFolder.newFolder()
        destinationDir / sourceDir.name / DIRECTORY
        destinationDir / sourceDir.name / 'file1' << 'Destination Content 1'
        destinationDir / sourceDir.name / 'dir2' / DIRECTORY
        destinationDir / sourceDir.name / 'dir2' / 'file2' << 'Destination Content 2'
        destinationDir / sourceDir.name / 'dir2' / 'dir3' / DIRECTORY

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: sourceDir, into: toUnixPath(destinationDir.path)
            }
        }

        then:
        (destinationDir / sourceDir.name / 'file1').text == 'Source Content 1'
        (destinationDir / sourceDir.name / 'dir2' / 'file2').text == 'Source Content 2'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3').list() == []
    }

    def "put(dir) should throw an error if destination does not exist"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        def destinationDir = temporaryFolder.newFolder() / 'dir1'
        assert !destinationDir.exists()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: sourceDir, into: toUnixPath(destinationDir.path)
            }
        }

        then:
        //FIXME
        thrown()
    }

    def "put(dir) should put a directory recursively"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        sourceDir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'dir8' / 'dir9' / DIRECTORIES

        sourceDir / 'file1' << 'Source Content 1'
        sourceDir / 'dir2' / 'file2' << 'Source Content 2'
        sourceDir / 'dir2' / 'dir3' / 'file3' << 'Source Content 3'
        sourceDir / 'dir2' / 'dir3' / 'dir4' / 'file4' << 'Source Content 4'
        sourceDir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'file5' << 'Source Content 5'
        sourceDir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'file6' << 'Source Content 6'
        sourceDir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'file7' << 'Source Content 7'
        sourceDir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'dir8' / 'file8' << 'Source Content 8'
        sourceDir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'dir8' / 'dir9' / 'file9' << 'Source Content 9'

        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: toUnixPath(sourceDir.path), into: destinationDir
            }
        }

        then:
        (destinationDir / sourceDir.name / 'file1').text == 'Source Content 1'
        (destinationDir / sourceDir.name / 'dir2' / 'file2').text == 'Source Content 2'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3' / 'file3').text == 'Source Content 3'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3' / 'dir4' / 'file4').text == 'Source Content 4'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'file5').text == 'Source Content 5'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'file6').text == 'Source Content 6'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'file7').text == 'Source Content 7'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'dir8' / 'file8').text == 'Source Content 8'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'dir8' / 'dir9' / 'file9').text == 'Source Content 9'
    }

    @Unroll
    def "put(dir) should put filtered files with regex #regex"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        sourceDir / 'file1' << 'Source Content 1'
        sourceDir / 'dir2' / DIRECTORY
        sourceDir / 'dir2' / 'file2' << 'Source Content 2'
        sourceDir / 'dir2' / 'dir3' / DIRECTORY

        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                put from: sourceDir, into: toUnixPath(destinationDir.path), filter: { it.name =~ regex }
            }
        }

        then:
        (destinationDir / sourceDir.name).exists() == d1
        (destinationDir / sourceDir.name / 'file1').exists() == f1
        (destinationDir / sourceDir.name / 'dir2').exists() == d2
        (destinationDir / sourceDir.name / 'dir2' / 'file2').exists() == f2

        and: 'empty directory should not be put'
        !(destinationDir / sourceDir.name / 'dir2' / 'dir3').exists()

        where:
        regex | d1    | f1     | d2    | f2
        /0$/  | false | false | false | false
        /1$/  | true  | true  | false | false
        /2$/  | true  | false | true  | true
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

    def "get() should throw an error if source does not exist"() {
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
        //FIXME
        thrown()
    }



    //
    // [GET] file transfer
    //

    def "get(file) should create a file if destination is a non-existent file in a directory"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'
        def destinationFile = temporaryFolder.newFolder() / 'file1'
        assert !destinationFile.exists()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: toUnixPath(sourceFile.path), into: destinationFile
            }
        }

        then:
        destinationFile.text == 'Source Content'
    }

    def "get(file) should create a file if destination is an existent directory"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'

        def destinationDir = temporaryFolder.newFolder()
        def destination1File = destinationDir / sourceFile.name << 'Destination Content 1'
        def destination2File = destinationDir / 'file2'         << 'Destination Content 2'

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: toUnixPath(sourceFile.path), into: destinationDir
            }
        }

        then: 'destination should be overwrote'
        destination1File.text == 'Source Content'

        and: 'destination should be kept as-is'
        destination2File.text == 'Destination Content 2'
    }

    def "get(file) should overwrite a file if destination is an existent file"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'
        def destinationFile = temporaryFolder.newFile() << 'Destination Content'

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

    def "get(file) should throw an error if destination and its parent do not exist"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'
        def destinationFile = temporaryFolder.newFolder() / 'dir1' / 'file1'

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: toUnixPath(sourceFile.path), into: destinationFile
            }
        }

        then:
        //FIXME
        thrown()
    }



    //
    // [GET] directory transfer
    //

    def "get(dir) should create a directory if destination is an existent directory"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        sourceDir / 'file1' << 'Source Content 1'
        sourceDir / 'dir2' / DIRECTORY
        sourceDir / 'dir2' / 'file2' << 'Source Content 2'
        sourceDir / 'dir2' / 'dir3' / DIRECTORY

        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: toUnixPath(sourceDir.path), into: destinationDir
            }
        }

        then:
        (destinationDir / sourceDir.name / 'file1').text == 'Source Content 1'
        (destinationDir / sourceDir.name / 'dir2' / 'file2').text == 'Source Content 2'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3').list() == []
    }

    def "get(dir) should create a directory even if source is empty"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: toUnixPath(sourceDir.path), into: destinationDir
            }
        }

        then:
        (destinationDir / sourceDir.name).list() == []
    }

    def "get(dir) should overwrite a directory if destination already exists"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        sourceDir / 'file1' << 'Source Content 1'
        sourceDir / 'dir2' / DIRECTORY
        sourceDir / 'dir2' / 'file2' << 'Source Content 2'
        sourceDir / 'dir2' / 'dir3' / DIRECTORY

        and:
        def destinationDir = temporaryFolder.newFolder()
        destinationDir / sourceDir.name / DIRECTORY
        destinationDir / sourceDir.name / 'file1' << 'Destination Content 1'
        destinationDir / sourceDir.name / 'dir2' / DIRECTORY
        destinationDir / sourceDir.name / 'dir2' / 'file2' << 'Destination Content 2'
        destinationDir / sourceDir.name / 'dir2' / 'dir3' / DIRECTORY

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: toUnixPath(sourceDir.path), into: destinationDir
            }
        }

        then:
        (destinationDir / sourceDir.name / 'file1').text == 'Source Content 1'
        (destinationDir / sourceDir.name / 'dir2' / 'file2').text == 'Source Content 2'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3').list() == []
    }

    def "get(dir) should throw an error if destination does not exist"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        def destinationDir = temporaryFolder.newFolder() / 'dir1'
        assert !destinationDir.exists()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: toUnixPath(sourceDir.path), into: destinationDir
            }
        }

        then:
        //FIXME
        thrown()
    }

    def "get(dir) should get a directory recursively"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        sourceDir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'dir8' / 'dir9' / DIRECTORIES

        sourceDir / 'file1' << 'Source Content 1'
        sourceDir / 'dir2' / 'file2' << 'Source Content 2'
        sourceDir / 'dir2' / 'dir3' / 'file3' << 'Source Content 3'
        sourceDir / 'dir2' / 'dir3' / 'dir4' / 'file4' << 'Source Content 4'
        sourceDir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'file5' << 'Source Content 5'
        sourceDir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'file6' << 'Source Content 6'
        sourceDir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'file7' << 'Source Content 7'
        sourceDir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'dir8' / 'file8' << 'Source Content 8'
        sourceDir / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'dir8' / 'dir9' / 'file9' << 'Source Content 9'

        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: toUnixPath(sourceDir.path), into: destinationDir
            }
        }

        then:
        (destinationDir / sourceDir.name / 'file1').text == 'Source Content 1'
        (destinationDir / sourceDir.name / 'dir2' / 'file2').text == 'Source Content 2'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3' / 'file3').text == 'Source Content 3'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3' / 'dir4' / 'file4').text == 'Source Content 4'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'file5').text == 'Source Content 5'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'file6').text == 'Source Content 6'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'file7').text == 'Source Content 7'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'dir8' / 'file8').text == 'Source Content 8'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3' / 'dir4' / 'dir5' / 'dir6' / 'dir7' / 'dir8' / 'dir9' / 'file9').text == 'Source Content 9'
    }

    @Unroll
    def "get(dir) should get filtered files with regex #regex"() {
        given:
        def sourceDir = temporaryFolder.newFolder()
        sourceDir / 'file1' << 'Source Content 1'
        sourceDir / 'dir2' / DIRECTORY
        sourceDir / 'dir2' / 'file2' << 'Source Content 2'
        sourceDir / 'dir2' / 'dir3' / DIRECTORY

        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                get from: toUnixPath(sourceDir.path), into: destinationDir, filter: { it.name =~ regex }
            }
        }

        then:
        (destinationDir / sourceDir.name).exists() == d1
        (destinationDir / sourceDir.name / 'file1').exists() == f1
        (destinationDir / sourceDir.name / 'dir2').exists() == d2
        (destinationDir / sourceDir.name / 'dir2' / 'file2').exists() == f2

        and: 'empty directory should not be get'
        !(destinationDir / sourceDir.name / 'dir2' / 'dir3').exists()

        where:
        regex | d1    | f1    | d2    | f2
        /0$/  | false | false | false | false
        /1$/  | true  | true  | false | false
        /2$/  | true  | false | true  | true
    }

}
