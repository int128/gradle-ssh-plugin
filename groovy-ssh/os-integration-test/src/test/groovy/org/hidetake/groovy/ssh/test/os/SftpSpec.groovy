package org.hidetake.groovy.ssh.test.os

import static org.hidetake.groovy.ssh.test.os.Fixture.remoteTmpPath

/**
 * Check if file transfer works with SFTP subsystem of OpenSSH.
 *
 * @author Hidetake Iwata
 */
class SftpSpec extends AbstractFileTransferSpec {

    def 'remove() should delete a directory recursively'() {
        given:
        def remoteDir = remoteTmpPath()

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                execute "mkdir -vp $remoteDir/foo/bar"
                execute "date > $remoteDir/foo/bar/baz"
                remove remoteDir
                execute "test ! -d $remoteDir"
            }
        }

        then:
        noExceptionThrown()
    }

}
