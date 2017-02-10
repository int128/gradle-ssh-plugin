package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.Use

import static org.hidetake.groovy.ssh.test.os.Fixture.createRemotes
import static org.hidetake.groovy.ssh.test.os.Fixture.remoteTmpPath
import static org.hidetake.groovy.ssh.test.os.MkdirType.DIRECTORIES
import static org.hidetake.groovy.ssh.test.os.MkdirType.DIRECTORY

/**
 * Check if file transfer works with OpenSSH.
 *
 * @author Hidetake Iwata
 */
@Use(FileDivCategory)
abstract class AbstractFileTransferSpec extends Specification {

    Service ssh

    @Rule
    TemporaryFolder temporaryFolder

    @Rule
    RemoteFixture remoteFixture

    def setup() {
        ssh = Ssh.newService()
        createRemotes(ssh)
    }

    def newRemoteTemporaryFolder() {
        def folder = remoteTmpPath()
        ssh.run {
            session(ssh.remotes.Default) {
                execute("mkdir -vp $folder")
            }
        }
        folder
    }

    def getRemoteContent(String path) {
        ssh.run {
            session(ssh.remotes.Default) {
                get from: path
            }
        }
    }


    //
    // [PUT] file transfer
    //

    def "put(file) should create a file if destination is a non-existent file in a directory"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'
        def destinationDir = remoteFixture.newFolder()
        def destinationFile = destinationDir / 'file1'

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                put from: sourceFile.path, into: destinationFile.path
            }
        }

        then:
        destinationFile.text == 'Source Content'
    }

    def "put(file) should create a file if destination is an existent directory"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'
        def destinationDir = remoteFixture.newFolder()
        destinationDir / (sourceFile.name) << 'Destination Content 1'
        destinationDir / 'file2' << 'Destination Content 2'

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                put from: sourceFile, into: destinationDir
            }
        }

        then: 'destination should be overwrote'
        (destinationDir / sourceFile.name).text == 'Source Content'

        and: 'destination should be kept as-is'
        (destinationDir / 'file2').text == 'Destination Content 2'
    }

    def "put(file) should overwrite a file if destination is an existent file"() {
        given:
        def sourceFile = temporaryFolder.newFile('file1') << 'Source Content'
        def destinationDir = remoteFixture.newFolder()
        destinationDir / 'file1' << 'Destination Content'

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                put from: sourceFile.path, into: "$destinationDir/file1"
            }
        }

        then:
        (destinationDir / 'file1').text == 'Source Content'
    }

    def "put(file) should throw IOException if destination and its parent do not exist"() {
        given:
        def sourceFile = temporaryFolder.newFile() << 'Source Content'
        def destinationFile = remoteFixture.newFolder() / 'dir1' / 'file1'

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                put from: sourceFile, into: destinationFile.path
            }
        }

        then:
        IOException e = thrown()
        e.message.contains(destinationFile.path)
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

        def destinationDir = remoteFixture.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                put from: sourceDir, into: destinationDir.path
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
        def destinationDir = remoteFixture.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                put from: sourceDir, into: destinationDir.path
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
        def destinationDir = remoteFixture.newFolder()
        destinationDir / sourceDir.name / DIRECTORY
        destinationDir / sourceDir.name / 'file1' << 'Destination Content 1'
        destinationDir / sourceDir.name / 'dir2' / DIRECTORY
        destinationDir / sourceDir.name / 'dir2' / 'file2' << 'Destination Content 2'
        destinationDir / sourceDir.name / 'dir2' / 'dir3' / DIRECTORY

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                put from: sourceDir, into: destinationDir.path
            }
        }

        then:
        (destinationDir / sourceDir.name / 'file1').text == 'Source Content 1'
        (destinationDir / sourceDir.name / 'dir2' / 'file2').text == 'Source Content 2'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3').list() == []
    }

    //FIXME: put into SftpSpec due to bug of Apache SSHD
    //def "put(dir) should throw IOException if destination does not exist"() {

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

        def destinationDir = remoteFixture.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                put from: sourceDir.path, into: destinationDir
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

        def destinationDir = remoteFixture.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                put from: sourceDir, into: destinationDir.path, filter: { it.name =~ regex }
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
    // [GET] file transfer
    //

    def "get(file) should create a file if destination is a non-existent file in a directory"() {
        given:
        def sourceFile = remoteFixture.newFile() << 'Source Content'
        def destinationFile = temporaryFolder.newFolder() / 'file1'
        assert !destinationFile.exists()

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                get from: sourceFile.path, into: destinationFile
            }
        }

        then:
        destinationFile.text == 'Source Content'
    }

    def "get(file) should create a file if destination is an existent directory"() {
        given:
        def sourceFile = remoteFixture.newFile() << 'Source Content'

        def destinationDir = temporaryFolder.newFolder()
        def destination1File = destinationDir / sourceFile.name << 'Destination Content 1'
        def destination2File = destinationDir / 'file2'         << 'Destination Content 2'

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                get from: sourceFile.path, into: destinationDir
            }
        }

        then: 'destination should be overwrote'
        destination1File.text == 'Source Content'

        and: 'destination should be kept as-is'
        destination2File.text == 'Destination Content 2'
    }

    def "get(file) should overwrite a file if destination is an existent file"() {
        given:
        def sourceFile = remoteFixture.newFile() << 'Source Content'
        def destinationFile = temporaryFolder.newFile() << 'Destination Content'

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                get from: sourceFile.path, into: destinationFile.path
            }
        }

        then:
        sourceFile.text == 'Source Content'
        destinationFile.text == 'Source Content'
    }

    def "get(file) should throw IOException if destination and its parent do not exist"() {
        given:
        def sourceFile = remoteFixture.newFile() << 'Source Content'
        def destinationFile = temporaryFolder.newFolder() / 'dir1' / 'file1'

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                get from: sourceFile.path, into: destinationFile
            }
        }

        then:
        IOException e = thrown()
        e.message.contains(destinationFile.path)
    }


    //
    // [GET] directory transfer
    //

    def "get(dir) should create a directory if destination is an existent directory"() {
        given:
        def sourceDir = remoteFixture.newFolder()
        sourceDir / 'file1' << 'Source Content 1'
        sourceDir / 'dir2' / DIRECTORY
        sourceDir / 'dir2' / 'file2' << 'Source Content 2'
        sourceDir / 'dir2' / 'dir3' / DIRECTORY

        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                get from: sourceDir.path, into: destinationDir
            }
        }

        then:
        (destinationDir / sourceDir.name / 'file1').text == 'Source Content 1'
        (destinationDir / sourceDir.name / 'dir2' / 'file2').text == 'Source Content 2'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3').list() == []
    }

    def "get(dir) should create a directory even if source is empty"() {
        given:
        def sourceDir = remoteFixture.newFolder()
        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                get from: sourceDir.path, into: destinationDir
            }
        }

        then:
        (destinationDir / sourceDir.name).list() == []
    }

    def "get(dir) should overwrite a directory if destination already exists"() {
        given:
        def sourceDir = remoteFixture.newFolder()
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
            session(ssh.remotes.Default) {
                get from: sourceDir.path, into: destinationDir
            }
        }

        then:
        (destinationDir / sourceDir.name / 'file1').text == 'Source Content 1'
        (destinationDir / sourceDir.name / 'dir2' / 'file2').text == 'Source Content 2'
        (destinationDir / sourceDir.name / 'dir2' / 'dir3').list() == []
    }

    def "get(dir) should throw IOException if destination does not exist"() {
        given:
        def sourceDir = remoteFixture.newFolder()
        def destinationDir = temporaryFolder.newFolder() / 'dir1'
        assert !destinationDir.exists()

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                get from: sourceDir.path, into: destinationDir
            }
        }

        then:
        IOException e = thrown()
        e.message.contains(destinationDir.path)
    }

    def "get(dir) should get a directory recursively"() {
        given:
        def sourceDir = remoteFixture.newFolder()
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
            session(ssh.remotes.Default) {
                get from: sourceDir.path, into: destinationDir
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
        def sourceDir = remoteFixture.newFolder()
        sourceDir / 'file1' << 'Source Content 1'
        sourceDir / 'dir2' / DIRECTORY
        sourceDir / 'dir2' / 'file2' << 'Source Content 2'
        sourceDir / 'dir2' / 'dir3' / DIRECTORY

        def destinationDir = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                get from: sourceDir.path, into: destinationDir, filter: { it.name =~ regex }
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
