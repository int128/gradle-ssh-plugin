package org.hidetake.gradle.ssh.server

import com.jcraft.jsch.JSchException
import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.plugin.SshTask
import org.hidetake.gradle.ssh.test.SshServerMock
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.hidetake.gradle.ssh.test.SshServerMock.commandWithExit

class HostKeyCheckingSpec extends Specification {

    SshServer server
    Project project

    @Rule
    TemporaryFolder temporaryFolder

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator)
        server.commandFactory = Mock(CommandFactory)
        server.start()

        project = ProjectBuilder.builder().build()
        project.with {
            apply plugin: 'ssh'
            remotes {
                testServer {
                    host = server.host
                    port = server.port
                    user = 'someuser'
                    password = 'somepassword'
                }
            }
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    execute 'somecommand'
                }
            }
        }
    }

    def teardown() {
        server.stop(true)
    }


    def "turn off strict host key checking"() {
        given:
        project.with {
            ssh {
                knownHosts = allowAnyHosts
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        1 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true
        1 * server.commandFactory.createCommand('somecommand') >> commandWithExit(0)
    }

    def "strict host key checking with a valid known-hosts"() {
        given:
        def hostKey = HostKeyCheckingSpec.getResourceAsStream('/hostkey.pub').text
        def knownHostsFile = temporaryFolder.newFile() << "[localhost]:${server.port} ${hostKey}"

        project.with {
            ssh {
                knownHosts = knownHostsFile
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        1 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true
        1 * server.commandFactory.createCommand('somecommand') >> commandWithExit(0)
    }

    def "strict host key checking with an empty known-hosts"() {
        given:
        def knownHostsFile = temporaryFolder.newFile()

        project.with {
            ssh {
                knownHosts = knownHostsFile
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        0 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _)
        0 * server.commandFactory.createCommand('somecommand')

        then:
        TaskExecutionException e = thrown()
        e.cause.cause instanceof JSchException
        e.cause.cause.message.contains 'reject HostKey'
    }

}
