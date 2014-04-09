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
import spock.util.mop.Use

@org.junit.experimental.categories.Category(ServerIntegrationTest)
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

    @Use(FileDivCategory)
    def "put a directory"() {
        given:
        def source1Dir = temporaryFolder.newFolder()
        def source2Dir = mkdir(source1Dir / uuidgen())
        def source3Dir = mkdir(source2Dir / uuidgen())

        def source1File = source1Dir / uuidgen() << uuidgen()
        def source2File = source2Dir / uuidgen() << uuidgen()

        def destinationDir = temporaryFolder.newFolder()

        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    put(source1Dir.path, destinationDir.path)
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        (destinationDir / source1Dir.name / source1File.name).text == source1File.text
        (destinationDir / source1Dir.name / source2Dir.name / source2File.name).text == source2File.text
        (destinationDir / source1Dir.name / source2Dir.name / source3Dir.name).list() == []
    }

    def "put an empty directory"() {
        given:
        def source1Dir = temporaryFolder.newFolder()
        def destinationDir = temporaryFolder.newFolder()

        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    put(source1Dir.path, destinationDir.path)
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        new File(destinationDir, source1Dir.name).list() == []
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

    @Use(FileDivCategory)
    def "get a directory"() {
        given:
        def source1Dir = temporaryFolder.newFolder()
        def source2Dir = mkdir(source1Dir / uuidgen())
        def source3Dir = mkdir(source2Dir / uuidgen())

        def source1File = source1Dir / uuidgen() << uuidgen()
        def source2File = source2Dir / uuidgen() << uuidgen()

        def destinationDir = temporaryFolder.newFolder()

        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    get(source1Dir.path, destinationDir.path)
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        (destinationDir / source1Dir.name / source1File.name).text == source1File.text
        (destinationDir / source1Dir.name / source2Dir.name / source2File.name).text == source2File.text
        (destinationDir / source1Dir.name / source2Dir.name / source3Dir.name).list() == []
    }

    def "get an empty directory"() {
        given:
        def source1Dir = temporaryFolder.newFolder()
        def destinationDir = temporaryFolder.newFolder()

        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    get(source1Dir.path, destinationDir.path)
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        new File(destinationDir, source1Dir.name).list() == []
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
