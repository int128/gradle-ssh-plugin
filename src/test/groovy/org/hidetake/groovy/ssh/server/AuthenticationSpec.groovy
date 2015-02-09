package org.hidetake.groovy.ssh.server

import com.jcraft.jsch.JSchException
import org.apache.sshd.SshServer
import org.apache.sshd.server.*
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Specification

import java.security.PublicKey

import static org.hidetake.groovy.ssh.server.SshServerMock.commandWithExit

@org.junit.experimental.categories.Category(ServerIntegrationTest)
class AuthenticationSpec extends Specification {

    SshServer server

    Service ssh

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
        server.commandFactory = Mock(CommandFactory)

        ssh = Ssh.newService()
        ssh.settings {
            knownHosts = allowAnyHosts
        }
    }

    def cleanup() {
        server.stop(true)
    }

    def "password authentication should pass if exact one is given"() {
        given:
        server.passwordAuthenticator = Mock(PasswordAuthenticator)
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
        1 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true

        then:
        1 * server.commandFactory.createCommand('ls') >> commandWithExit(0)
    }

    def "password authentication should fail if wrong one is given"() {
        given:
        server.passwordAuthenticator = Mock(PasswordAuthenticator)
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
        (1.._) * server.passwordAuthenticator.authenticate('someuser', 'wrongpassword', _) >> false

        then:
        0 * server.commandFactory.createCommand(_)

        and:
        JSchException e = thrown()
        e.message == 'Auth fail'
    }

    def "public key authentication should pass if valid one is given"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
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
        (1.._) * server.publickeyAuthenticator.authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true

        then:
        1 * server.commandFactory.createCommand('ls') >> commandWithExit(0)
    }

    def "identity of public key authentication can be set by global settings"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
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
        (1.._) * server.publickeyAuthenticator.authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true

        then:
        1 * server.commandFactory.createCommand('ls') >> commandWithExit(0)
    }

    def "public key authentication should fail if wrong one is given"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
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
        (1.._) * server.publickeyAuthenticator.authenticate('someuser', _ as PublicKey, _) >> false

        then:
        0 * server.commandFactory.createCommand(_)

        and:
        JSchException e = thrown()
        e.message == 'Auth fail'
    }

    def "public key authentication should accept the passphrase of identity"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
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
        (1.._) * server.publickeyAuthenticator.authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true

        then:
        1 * server.commandFactory.createCommand('ls') >> commandWithExit(0)
    }

    def "identity and passphrase can be set by global settings"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
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
        (1.._) * server.publickeyAuthenticator.authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true

        then:
        1 * server.commandFactory.createCommand('ls') >> commandWithExit(0)
    }

    def "public key authentication should fail if wrong passphrase is given"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
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
        (1.._) * server.publickeyAuthenticator.authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true

        then:
        0 * server.commandFactory.createCommand(_)

        and:
        JSchException e = thrown()
        e.message == 'USERAUTH fail'
    }

    def "remote specific identity should precede one in global settings"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
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
        (1.._) * server.publickeyAuthenticator.authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true

        then:
        0 * server.commandFactory.createCommand(_)

        then:
        JSchException e = thrown()
        e.message == 'USERAUTH fail'
    }

    static identityFile(String name) {
        new File(AuthenticationSpec.getResource("/${name}").file)
    }

}
