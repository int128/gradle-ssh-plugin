package org.hidetake.gradle.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.sftp.SftpSubsystem
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.SshTask
import org.hidetake.gradle.ssh.test.SshServerMock
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

class FileTransferSpec extends Specification {

    @Shared
    SshServer server

    Project project

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

    def teardownSpec() {
        server.stop(true)
    }


    def setup() {
        project = ProjectBuilder.builder().build()
        project.with {
            apply plugin: 'ssh'
            ssh {
                config(StrictHostKeyChecking: 'no')
            }
            remotes {
                testServer {
                    host = server.host
                    port = server.port
                    user = 'someuser'
                    password = 'somepassword'
                }
            }
        }
    }


    def "put a file"() {
        given:
        def sourceFile = temporaryFolder.newFile()
        def destinationFile = temporaryFolder.newFile() << uuidgen()

        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    put(sourceFile.path, destinationFile.path)
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        destinationFile.exists()
        destinationFile.text == sourceFile.text
    }

    def "get a file"() {
        given:
        def sourceFile = temporaryFolder.newFile()
        def destinationFile = temporaryFolder.newFile() << uuidgen()

        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    get(sourceFile.path, destinationFile.path)
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        destinationFile.exists()
        destinationFile.text == sourceFile.text
    }


    static uuidgen() {
        UUID.randomUUID().toString()
    }

}
