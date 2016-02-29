package org.hidetake.groovy.ssh.test.os

import groovy.io.FileType
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.os.Fixture.*

/**
 * Check if file transfer works with SFTP subsystem of OpenSSH.
 *
 * @author Hidetake Iwata
 */
class FileTransferSpec extends Specification {

    Service ssh

    @Rule
    TemporaryFolder temporaryFolder

    def setup() {
        ssh = Ssh.newService()
        ssh.remotes {
            localhost {
                host = hostName()
                user = userName()
                identity = privateKeyRSA()
            }
        }
    }

    def 'should put, compute and get files'() {
        given:
        def x = randomInt()
        def y = randomInt()

        def localX = temporaryFolder.newFile() << x
        def localY = temporaryFolder.newFile() << y
        def localA = temporaryFolder.newFile()
        def localB = temporaryFolder.newFile()
        def remoteX = remoteTmpPath()
        def remoteY = remoteTmpPath()
        def remoteA = remoteTmpPath()
        def remoteB = remoteTmpPath()

        when:
        ssh.run {
            session(ssh.remotes.localhost) {
                put from: localX, into: remoteX
                put from: localY, into: remoteY
                execute "expr `cat $remoteX` + `cat $remoteY` > $remoteA"
                execute "expr `cat $remoteX` - `cat $remoteY` > $remoteB"
                get from: remoteA, into: localA
                get from: remoteB, into: localB
            }
        }

        then:
        localA.text as int == (x + y)
        localB.text as int == (x - y)
    }

    def 'should put contents and compute'() {
        given:
        def x = randomInt()
        def y = randomInt()

        def remoteX = remoteTmpPath()
        def remoteY = remoteTmpPath()

        when:
        def result = ssh.run {
            session(ssh.remotes.localhost) {
                put text: x, into: remoteX
                put bytes: y.toString().bytes, into: remoteY
                [a: execute("expr `cat $remoteX` + `cat $remoteY`"),
                 b: execute("expr `cat $remoteX` - `cat $remoteY`")]
            }
        }

        then:
        result.a as int == (x + y)
        result.b as int == (x - y)
    }

    def 'should merge and overwrite a directory recursively on put'() {
        given:
        def x = randomInt()
        def y = randomInt()
        def z = randomInt()

        and: 'prepare the local folder'
        def localFolder = temporaryFolder.newFolder()
        new File(localFolder, 'Y/Z').mkdirs()
        new File(localFolder, 'xfile') << x
        new File(localFolder, 'Y/yfile') << y
        new File(localFolder, 'Y/Z/zfile') << z

        and: 'prepare the remote folder'
        def remoteFolder = remoteTmpPath()
        ssh.run {
            session(ssh.remotes.localhost) {
                execute "mkdir -vp        $remoteFolder/${localFolder.name}/Y"
                execute "echo -n dummy1 > $remoteFolder/${localFolder.name}/Y/yfile"
                execute "echo -n dummy2 > $remoteFolder/${localFolder.name}/Y/yfile2"
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.localhost) {
                put from: localFolder, into: remoteFolder
            }
        }

        and:
        def result = ssh.run {
            session(ssh.remotes.localhost) {
                [x:  get(from: "$remoteFolder/${localFolder.name}/xfile"),
                 y:  get(from: "$remoteFolder/${localFolder.name}/Y/yfile"),
                 y2: get(from: "$remoteFolder/${localFolder.name}/Y/yfile2"),
                 z:  get(from: "$remoteFolder/${localFolder.name}/Y/Z/zfile")]
            }
        }

        then:
        result.x as int == x
        result.y as int == y
        result.y2 == 'dummy2'
        result.z as int == z
    }

    def 'should merge and overwrite a directory recursively on get'() {
        given:
        def x = randomInt()
        def y = randomInt()
        def z = randomInt()

        and: 'prepare the local folder'
        def localFolder = temporaryFolder.newFolder()
        new File(localFolder, 'X/Y').mkdirs()
        new File(localFolder, 'X/Y/yfile') << 'dummy1'
        new File(localFolder, 'X/Y/yfile2') << 'dummy2'

        and: 'prepare the remote folder'
        def remoteFolder = remoteTmpPath()
        ssh.run {
            session(ssh.remotes.localhost) {
                execute "mkdir -vp $remoteFolder/X/Y/Z"
                execute "echo $x > $remoteFolder/X/xfile"
                execute "echo $y > $remoteFolder/X/Y/yfile"
                execute "echo $z > $remoteFolder/X/Y/Z/zfile"
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.localhost) {
                get from: "$remoteFolder/X", into: localFolder
            }
        }

        then:
        new File(localFolder, 'X/xfile').text as int == x
        new File(localFolder, 'X/Y/yfile').text as int == y
        new File(localFolder, 'X/Y/yfile2').text == 'dummy2'
        new File(localFolder, 'X/Y/Z/zfile').text as int == z

        when:
        def filesInLocalFolder = []
        localFolder.traverse(type: FileType.FILES) { filesInLocalFolder << it }

        then:
        filesInLocalFolder.size() == 4
    }

    def 'should get a large file and put it back'() {
        given:
        def sizeKB = 1024 * 8

        def localX = temporaryFolder.newFile()
        def remoteX = remoteTmpPath()
        def remoteY = remoteTmpPath()

        when:
        ssh.run {
            session(ssh.remotes.localhost) {
                execute "dd if=/dev/zero of=$remoteX bs=1024 count=$sizeKB"
                get from: remoteX, into: localX
            }
        }

        then:
        localX.size() == 1024 * sizeKB

        when:
        def actualSize = ssh.run {
            session(ssh.remotes.localhost) {
                put from: localX, into: remoteY
                execute("wc -c < $remoteY") as int
            }
        }

        then:
        actualSize == 1024 * sizeKB
    }

}
