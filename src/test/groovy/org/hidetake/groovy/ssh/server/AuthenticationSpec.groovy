package org.hidetake.groovy.ssh.server

import com.jcraft.jsch.JSchException
import org.apache.sshd.SshServer
import org.apache.sshd.server.*
import spock.lang.Specification

import java.security.PublicKey

import static org.hidetake.groovy.ssh.Ssh.ssh

@org.junit.experimental.categories.Category(ServerIntegrationTest)
class AuthenticationSpec extends Specification {

    SshServer server

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
        ssh.settings {
            knownHosts = allowAnyHosts
        }
    }

    def cleanup() {
        ssh.remotes.clear()
        ssh.proxies.clear()
        ssh.settings.reset()
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

    def "password authentication"() {
        given:
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            _ * authenticate('someuser', 'somepassword', _) >> true
        }
        server.commandFactory = successCommandFactory()
        server.start()

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'somepassword'
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

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

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'wrongpassword'
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

        then:
        JSchException e = thrown()
        e.message == 'Auth fail'
    }

    def "public key authentication"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator) {
            _ * authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true
        }
        server.commandFactory = successCommandFactory()
        server.start()

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                identity = identityFile('id_rsa')
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

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

        ssh.settings {
            identity = identityFile('id_rsa')
        }

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

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

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                identity = identityFile('id_rsa')
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

        then:
        JSchException e = thrown()
        e.message == 'Auth fail'
    }

    def "public key authentication with passphrase"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator) {
            _ * authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true
        }
        server.commandFactory = successCommandFactory()
        server.start()

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                identity = identityFile('id_rsa_pass')
                passphrase = "gradle"
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

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

        ssh.settings {
            identity = identityFile('id_rsa_pass')
            passphrase = "gradle"
        }

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

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

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                identity = identityFile('id_rsa_pass')
                passphrase = "wrong"
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

        then:
        JSchException e = thrown()
        e.message == 'USERAUTH fail'
    }

    def "remote specific identity overrides global one"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator) {
            _ * authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true
        }
        server.commandFactory = Mock(CommandFactory)
        server.start()

        ssh.settings {
            identity = identityFile('id_rsa')
        }

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                identity = identityFile('id_rsa_pass')
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

        then:
        JSchException e = thrown()
        e.message == 'USERAUTH fail'
    }

    static identityFile(String name) {
        new File(AuthenticationSpec.getResource("/${name}").file)
    }

}
