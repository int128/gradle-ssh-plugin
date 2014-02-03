package org.hidetake.gradle.ssh.server

import com.jcraft.jsch.JSchException
import org.apache.sshd.SshServer
import org.apache.sshd.server.*
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.SshTask
import org.hidetake.gradle.ssh.test.SshServerMock
import spock.lang.Specification

import java.security.PublicKey

/**
 * Authentication tests with the SSH server.
 *
 * @author hidetake.org
 *
 */
class AuthenticationSpec extends Specification {

    SshServer server
    Project project

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
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
                }
            }
        }
    }

    def teardown() {
        server.stop(true)
    }

    def successCommandFactory() {
        Mock(CommandFactory) {
            1 * createCommand('ls') >> Mock(Command) {
                setExitCallback(_) >> { ExitCallback callback ->
                    callback.onExit(0)
                }
            }
        }
    }

    def defineTestTask() {
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    execute 'ls'
                }
            }
        }
    }

    def "password authentication"() {
        given:
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            _ * authenticate('someuser', 'somepassword', _) >> true
        }
        server.commandFactory = successCommandFactory()
        server.start()

        project.remotes.testServer.password = 'somepassword'
        defineTestTask()

        when:
        project.tasks.testTask.execute()

        then:
        noExceptionThrown()
    }

    def "password authentication with wrong password"() {
        given:
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            _ * authenticate('someuser', 'wrongpassword', _) >> false
        }
        server.commandFactory = Mock(CommandFactory) {
            0 * createCommand(_)
        }
        server.start()

        project.remotes.testServer.password = 'wrongpassword'
        defineTestTask()

        when:
        project.tasks.testTask.execute()

        then:
        TaskExecutionException e = thrown()
        e.cause.cause instanceof JSchException
        e.cause.cause.message == 'Auth fail'
    }

    def "public key authentication"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator) {
            _ * authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true
        }
        server.commandFactory = successCommandFactory()
        server.start()

        project.remotes.testServer.identity = identityFile('id_rsa')
        defineTestTask()

        when:
        project.tasks.testTask.execute()

        then:
        noExceptionThrown()
    }

    def "public key authentication with global identity"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator) {
            _ * authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true
        }
        server.commandFactory = successCommandFactory()
        server.start()

        project.with {
            ssh {
                identity = identityFile('id_rsa')
            }
        }
        defineTestTask()

        when:
        project.tasks.testTask.execute()

        then:
        noExceptionThrown()
    }

    def "public key authentication but denied"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator) {
            _ * authenticate('someuser', _ as PublicKey, _) >> false
        }
        server.commandFactory = Mock(CommandFactory) {
            0 * createCommand(_)
        }
        server.start()

        project.remotes.testServer.identity = identityFile('id_rsa')
        defineTestTask()

        when:
        project.tasks.testTask.execute()

        then:
        TaskExecutionException e = thrown()
        e.cause.cause instanceof JSchException
        e.cause.cause.message == 'Auth fail'
    }

    def "public key authentication with passphrase"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator) {
            _ * authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true
        }
        server.commandFactory = successCommandFactory()
        server.start()

        project.remotes.testServer.identity = identityFile('id_rsa_pass')
        project.remotes.testServer.passphrase = "gradle"
        defineTestTask()

        when:
        project.tasks.testTask.execute()

        then:
        noExceptionThrown()
    }

    def "public key authentication with global identity and passphrase"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator) {
            _ * authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true
        }
        server.commandFactory = successCommandFactory()
        server.start()

        project.with {
            ssh {
                identity = identityFile('id_rsa_pass')
                passphrase = "gradle"
            }
        }
        defineTestTask()

        when:
        project.tasks.testTask.execute()

        then:
        noExceptionThrown()
    }

    def "public key authentication with wrong passphrase"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator) {
            _ * authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true
        }
        server.commandFactory = Mock(CommandFactory) {
            0 * createCommand(_)
        }
        server.start()

        project.remotes.testServer.identity = identityFile('id_rsa_pass')
        project.remotes.testServer.passphrase = "wrong"
        defineTestTask()

        when:
        project.tasks.testTask.execute()

        then:
        TaskExecutionException e = thrown()
        e.cause.cause instanceof JSchException
        e.cause.cause.message == 'Auth fail'
    }

    def "remote specific identity overrides global one"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator) {
            _ * authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true
        }
        server.commandFactory = Mock(CommandFactory)
        server.start()

        project.with {
            ssh {
                identity = identityFile('id_rsa')
            }
            // may be failed because it needs pass-phrase
            remotes.testServer.identity = identityFile('id_rsa_pass')
        }
        defineTestTask()

        when:
        project.tasks.testTask.execute()

        then:
        0 * server.commandFactory.createCommand(_)

        then:
        TaskExecutionException e = thrown()
        e.cause.cause instanceof JSchException
        e.cause.cause.message == 'Auth fail'
    }

    static identityFile(String name) {
        new File(AuthenticationSpec.getResource("/${name}").file)
    }

}
