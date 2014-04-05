package org.hidetake.gradle.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.sftp.SftpSubsystem
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.plugin.SshTask
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
                knownHosts = allowAnyHosts
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
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationFile = temporaryFolder.newFile()

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
        sourceFile.text == text
        destinationFile.text == text
    }

    def "put a file to a directory"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationDir = temporaryFolder.newFolder()

        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    put(sourceFile.path, destinationDir.path)
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        sourceFile.text == text

        and:
        def destinationFile = new File(destinationDir, sourceFile.name)
        destinationFile.text == text
    }

    def "get a file"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationFile = temporaryFolder.newFile()

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
        sourceFile.text == text
        destinationFile.text == text
    }

    def "get a file to a directory"() {
        given:
        def text = uuidgen()
        def sourceFile = temporaryFolder.newFile() << text
        def destinationDir = temporaryFolder.newFolder()

        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    get(sourceFile.path, destinationDir.path)
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        sourceFile.text == text

        and:
        def destinationFile = new File(destinationDir, sourceFile.name)
        destinationFile.text == text
    }


    static uuidgen() {
        UUID.randomUUID().toString()
    }

}
